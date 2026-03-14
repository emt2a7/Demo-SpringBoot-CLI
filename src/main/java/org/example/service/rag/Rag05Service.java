package org.example.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.framework.component.WordImageExtractor;
import org.example.tool.RagTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // 強制使用建構子注入
public class Rag05Service {

    // 注入 自訂的 WordImageExtractor，負責從 word 中挖圖並取得圖片實體路徑
    private final WordImageExtractor wordImageExtractor;

    // 注入 Spring AI 的 ChatClient，負責跟 Gemini 互動
    private final ChatClient chatClient;

    // 注入 Spring AI 的 VectorStore，負責管理向量(Vector)資料庫
    private final VectorStore babyVectorStore;

    // 注入 JdbcTemplate 來執行精準的 PostgreSQL 指令
    private final JdbcTemplate jdbcTemplate;

    // 注入 工具箱 (Tools)
    private final RagTools ragTools;

    /**
     * 上傳 Word 文件至 Baby 向量資料庫 (Vector Store)
     * @param wordResource
     */
    @Transactional
    public void updateBabyDocument(Resource wordResource) {
        var fileName = wordResource.getFilename();
        log.info("上傳 word 文件至 Baby 向量資料庫 (Vector Store): {}", fileName);

        var category = "Baby";
        var imageDescriptionList = new ArrayList<String>();

        try {
            // ==========================================
            // 刪除舊資料
            // ==========================================
            // 刪除舊的「向量表」
            String deleteVectorsSql = "DELETE FROM baby_vector_store WHERE metadata->>'file_name' = ?";
            jdbcTemplate.update(deleteVectorsSql, fileName);

            // 刪除舊的「內文表」
            String deleteDocSql = "DELETE FROM vector_main WHERE file_name = ?";
            jdbcTemplate.update(deleteDocSql, fileName);

            // ==========================================
            // 上傳「內文表」
            // ==========================================
            // 將檔案轉換為 byte 陣列 (二進位資料)
            var fileBytes = wordResource.getContentAsByteArray();
            long fileSize = fileBytes.length;

            // 寫入「內文表」資料庫
            var insertDocSql = """
                INSERT INTO vector_main (category, file_name, file_data, file_size) 
                VALUES (?, ?, ?, ?) 
                ON CONFLICT (file_name) 
                DO UPDATE SET category = EXCLUDED.category, file_data = EXCLUDED.file_data, file_size = EXCLUDED.file_size, upload_time = NOW()
            """;
            jdbcTemplate.update(insertDocSql, category, fileName, fileBytes, fileSize);
            log.info("上傳「內文表」完成，文件大小: {} bytes", fileSize);

            // ==========================================
            // 上傳「向量表」 for 純文字
            // ==========================================
            // 使用 Tika 萬能解析器，自動辨識 pdf/word/excel/txt 並抽出純文字 (無法抽出圖片)
            var tikaReader = new TikaDocumentReader(wordResource);
            var rawDocuments = tikaReader.get();

            // 將抽出來的文字切塊 (Chunking)
            var chunkedDocuments = new TokenTextSplitter().apply(rawDocuments);

            // 綁定檔名到 Metadata 建立關聯
            chunkedDocuments.forEach(doc -> doc.getMetadata().put("file_name", fileName));

            // ==========================================
            // 上傳「向量表」 for 圖片轉純文字
            // ==========================================
            // 解析 word 文件圖片轉成文字描述
            imageDescriptionList = askWordImageToDescription(wordResource);

            if (imageDescriptionList != null && !imageDescriptionList.isEmpty()) {
                log.info("🖼️ 準備將 {} 筆圖片 AI 描述合併至知識庫中...", imageDescriptionList.size());

                List<Document> imageDocuments = new ArrayList<>();
                for (int i = 0; i < imageDescriptionList.size(); i++) {
                    String description = imageDescriptionList.get(i);

                    // 建立新的 Document，並在文字前面加上提示詞，讓未來的 AI 知道這是圖片內容
                    Document imgDoc = new Document("【文件附圖解析】\n" + description);

                    // 綁定 Metadata，讓這個區塊跟原本的檔案名稱有關聯
                    imgDoc.getMetadata().put("file_name", fileName);
                    imgDoc.getMetadata().put("chunk_type", "image_description"); // 加上一個特殊標籤，未來您在維護 DB 時，就能一眼看出這筆是圖片產生的！
                    imgDoc.getMetadata().put("image_index", i + 1);
                    imageDocuments.add(imgDoc);
                }

                // 將這些「圖片描述區塊」加入到原本的「純文字區塊」清單中
                chunkedDocuments.addAll(imageDocuments);
            }

            // 寫入「向量表」資料庫
            babyVectorStore.add(chunkedDocuments);
        } catch (IOException e) {
            log.error("❌ 讀取實體檔案失敗: {}", e.getMessage(), e);
            throw new RuntimeException("檔案讀取失敗", e);
        } catch (Exception e) {
            log.error("❌ 處理檔案 {} 時發生未預期錯誤: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("檔案上傳與解析失敗", e);
        }
    }

    /**
     * 解析 word 文件圖片轉成文字描述
     * @param wordResource
     */
    private ArrayList<String> askWordImageToDescription(Resource wordResource) {
        var fileName = wordResource.getFilename();
        log.info("解析 word 文件圖片轉成文字描述: {}", fileName);

        String prompt = "";
        Media imageMedia = null;
        UserMessage userMessage = null;
        Resource imageResource = null;
        var imageDescriptionList = new ArrayList<String>();

        try {
            // 保存圖片的實體目錄
            var outputImageDIr = "D:/idea-worksapce/temp/image";

            // 讀取 word 檔案中所有圖片的實體路徑，並且按照「頁碼」對應到圖片清單。
            List<String> images = wordImageExtractor.extractImages(wordResource, outputImageDIr);

            for (String imagePath : images) {
                // 讀取實體圖片
                imageResource = new FileSystemResource(imagePath);

                // 建立 Spring AI 的 Media 物件 (告訴 AI 這是一張 PNG)
                imageMedia = new Media(MimeTypeUtils.IMAGE_PNG, imageResource);

                // 提問 Prompt
                prompt = "請描述這張圖片裡面的數據或內容，盡可能詳細。";

                final String finalPrompt = prompt;
                final Media finalImageMedia = imageMedia;
                var response = chatClient.prompt()
                        .user(u -> u.text(finalPrompt).media(finalImageMedia))
                        .call()
                        .content();
                imageDescriptionList.add(response);
                log.info("圖片 {} 的 AI 描述: {}", imagePath, response);
            }
        } catch (Exception e) {
            log.error("❌ 處理檔案 {} 時發生未預期錯誤: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("檔案上傳與解析失敗", e);
        } finally {
            return imageDescriptionList;
        }
    }

    /**
     * 提問 AI (使用手動語意路由，完美避開 thought_signature Bug)
     * @param question 使用者的問題
     */
    public String askTools(String question) {
        log.info("🔍 使用者提問 (Agentic 語意路由模式): {}", question);

        // ==========================================
        // 步驟 1：使用 LLM 進行「意圖識別 (Intent Classification)」
        // 讓模型當「總機」，判斷這個問題該由哪個部門(資料庫)處理
        // ==========================================
        String routePrompt = """
                請分析以下使用者的問題，判斷應該去哪一個企業資料庫查詢資料。
                你只能回答以下四個選項的其中一個「英文單字」，絕對不要輸出任何標點符號或其他文字：
                
                - BABY (適用於：人口、出生率、死亡率、生育率等台灣人口統計問題)
                - HR   (適用於：請假、薪資、出差、員工福利等公司人資規章問題)
                - EMP  (適用於：勞工、工作規則、職業災害等員工守則問題)
                - NONE (如果不屬於以上任何範圍)

                使用者問題：%s
                """.formatted(question);

        // 呼叫 AI 進行判斷 (注意：這裡不加 .tools()，所以不會觸發 Bug)
        String intent = chatClient.prompt()
                .user(routePrompt)
                .call()
                .content()
                .trim()
                .toUpperCase();

        log.info("🧭 LLM 總機判定該問題屬於: [{}] 分類", intent);

        // ==========================================
        // 步驟 2：Java 手動呼叫工具撈取資料 (取代 Spring AI 的自動攔截)
        // ==========================================
        String contextData = "";

        // 將原先自動呼叫的 RagTools，改為我們手動依照意圖來呼叫
        if (intent.contains("BABY")) {
            contextData = ragTools.searchBabyDatabase(new RagTools.Request(question)).result();
        } else if (intent.contains("HR")) {
            contextData = ragTools.searchHrDatabase(new RagTools.Request(question)).result();
        } else if (intent.contains("EMP")) {
            contextData = ragTools.searchItDatabase(new RagTools.Request(question)).result();
        }

        // ==========================================
        // 步驟 3：將撈出來的知識庫資料，連同原問題交給 AI 進行最終總結
        // ==========================================
        if (contextData.isEmpty()) {
            log.info("⚠️ 工具箱無回傳資料，交由 AI 憑藉自身常識回答。");
            return chatClient.prompt().user(question).call().content();
        }

        String finalPrompt = """
                請根據以下提供的【企業內部參考資料】來精準回答使用者的問題。
                如果參考資料中沒有相關答案，請誠實回答「根據現有資料無法得知」，切勿自行捏造。

                【企業內部參考資料】
                %s

                【使用者問題】
                %s
                """.formatted(contextData, question);

        // 進行最後的解答生成
        var answer = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();

        log.info("🤖 AI 最終回答: {}", answer);
        return answer;
    }
}
