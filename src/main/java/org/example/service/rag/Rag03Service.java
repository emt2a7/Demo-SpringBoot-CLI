package org.example.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.framework.component.PdfImageExtractor;
import org.example.tool.RagTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
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
public class Rag03Service {

    // 注入 自訂的 PdfImageExtractor，負責從 PDF 中挖圖並取得圖片實體路徑
    private final PdfImageExtractor pdfImageExtractor;

    // 注入 Spring AI 的 ChatClient，負責跟 Gemini 互動
    private final ChatClient chatClient;

    // 注入 Spring AI 的 VectorStore，負責管理向量(Vector)資料庫
    private final VectorStore babyVectorStore;

    // 注入 JdbcTemplate 來執行精準的 PostgreSQL 指令
    private final JdbcTemplate jdbcTemplate;

    // 注入 工具箱 (Tools)
    private final RagTools ragTools;

    /**
     * 上傳 pdf 文件至 Baby 向量資料庫 (Vector Store)
     * @param pdfResource
     */
    @Transactional
    public void updateBabyDocument(Resource pdfResource) {
        var fileName = pdfResource.getFilename();
        log.info("上傳 pdf/word/excel/txt 文件至 Baby 向量資料庫 (Vector Store): {}", fileName);

        var category = "Baby";
        var outputImageDIr = "D:/idea-worksapce/temp/image";
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
            var fileBytes = pdfResource.getContentAsByteArray();
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

            jdbcTemplate.update(insertDocSql, category, fileName, fileBytes, fileSize);
            log.info("上傳「內文表」完成，文件大小: {} bytes", fileSize);

            // ==========================================
            // 上傳「向量表」 for 純文字
            // ==========================================
            // 使用 Tika 萬能解析器，自動辨識 pdf/word/excel/txt 並抽出純文字 (無法抽出圖片)
            var tikaReader = new TikaDocumentReader(pdfResource);
            var rawDocuments = tikaReader.get();

            // 將抽出來的文字切塊 (Chunking)
            var chunkedDocuments = new TokenTextSplitter().apply(rawDocuments);

            // 綁定檔名到 Metadata 建立關聯
            chunkedDocuments.forEach(doc -> doc.getMetadata().put("file_name", fileName));

            // ==========================================
            // 上傳「向量表」 for 圖片轉純文字
            // ==========================================
            // 解析 pdf 文件圖片轉成文字描述
            imageDescriptionList = askPdfImageToDescription(pdfResource);

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
     * 解析 pdf 文件圖片轉成文字描述
     * @param pdfResource
     */
    private ArrayList<String> askPdfImageToDescription(Resource pdfResource) {
        var fileName = pdfResource.getFilename();
        log.info("解析 pdf 文件圖片轉成文字描述: {}", fileName);

        String prompt = "";
        Media imageMedia = null;
        UserMessage userMessage = null;
        Resource imageResource = null;
        var imageDescriptionList = new ArrayList<String>();

        try {
            // 保存圖片的實體目錄
            var outputImageDIr = "D:/idea-worksapce/temp/image";

            // 讀取 PDF 檔案中所有圖片的實體路徑，並且按照「頁碼」對應到圖片清單。
            var pageImagesMap = pdfImageExtractor.extractImagesAndMapToPages(pdfResource, outputImageDIr);

            for (Integer pageNum : pageImagesMap.keySet()) {
                var images = pageImagesMap.get(pageNum);
                log.info("第 {} 頁的圖片清單: {}", pageNum, images);

                // 將圖片路徑提問 AI，讓 AI 判讀圖片內容並回傳文字描述
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
            }
        } catch (Exception e) {
            log.error("❌ 處理檔案 {} 時發生未預期錯誤: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("檔案上傳與解析失敗", e);
        } finally {
            return imageDescriptionList;
        }
    }

    /**
     * 提問 AI (包含搜尋工具組)
     * @param question 使用者的問題
     */
    public String askTools(String question) {
        log.info("使用者提問並搜尋工具組 (Tools): {}", question);

        // 使用者在提問 AI 之前，先去搜尋工具組 (Tools)，並塞進 Prompt 中
        var answer = chatClient.prompt()
                .user(question)   // 使用者提問 AI
                .tools(ragTools)  // 搜尋工具組 (Tools)
                .call()           // 將「提問」和「搜尋資料」，一同拿去給 AI
                .content();       // 取得 AI 回答的內容

        log.info("AI回覆內容: {}", answer);
        return answer;
    }
}
