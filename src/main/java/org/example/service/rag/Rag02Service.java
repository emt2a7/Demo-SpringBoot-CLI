package org.example.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tool.RagTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor // 強制使用建構子注入
public class Rag02Service {

    // 注入 Spring AI 的 ChatClient，負責跟 Gemini 互動
    private final ChatClient chatClient;

    // 注入 Spring AI 的 VectorStore，負責管理向量(Vector)資料庫
    private final VectorStore hrVectorStore;

    // 注入 Spring AI 的 VectorStore，負責管理向量(Vector)資料庫
    private final VectorStore itVectorStore;

    // 注入 JdbcTemplate 來執行精準的 PostgreSQL 指令
    private final JdbcTemplate jdbcTemplate;

    // 注入 工具箱 (Tools)
    private final RagTools ragTools;

    /**
     * 上傳 pdf/word/excel/txt 文件至 HR 向量資料庫 (Vector Store)
     * @param fileResource
     */
    @Transactional
    public void updateHrDocument(Resource fileResource) {
        var fileName = fileResource.getFilename();
        log.info("上傳 pdf/word/excel/txt 文件至 HR 向量資料庫 (Vector Store): {}", fileName);

        var category = "HR";

        try {
            // ==========================================
            // 刪除舊資料
            // ==========================================
            // 刪除舊的「向量表」
            String deleteVectorsSql = "DELETE FROM hr_vector_store WHERE metadata->>'file_name' = ?";
            jdbcTemplate.update(deleteVectorsSql, fileName);

            // 刪除舊的「內文表」
            String deleteDocSql = "DELETE FROM vector_main WHERE file_name = ?";
            jdbcTemplate.update(deleteDocSql, fileName);

            // ==========================================
            // 上傳「內文表」
            // ==========================================
            // 將檔案轉換為 byte 陣列 (二進位資料)
            var fileBytes = fileResource.getContentAsByteArray();
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
            // 上傳「向量表」
            // ==========================================
            // 使用 Tika 萬能解析器，自動辨識 pdf/word/excel/txt 並抽出純文字 (無法抽出圖片)
            var tikaReader = new TikaDocumentReader(fileResource);
            var rawDocuments = tikaReader.get();

            // 將抽出來的文字切塊 (Chunking)
            var chunkedDocuments = new TokenTextSplitter().apply(rawDocuments);

            // 綁定檔名到 Metadata 建立關聯
            chunkedDocuments.forEach(doc -> doc.getMetadata().put("file_name", fileName));

            // 寫入「向量表」資料庫
            hrVectorStore.add(chunkedDocuments);

            log.info("上傳「內文表」完成，共切為 {} 個知識區塊。", chunkedDocuments.size());
        } catch (IOException e) {
            log.error("❌ 讀取實體檔案失敗: {}", e.getMessage(), e);
            throw new RuntimeException("檔案讀取失敗", e);
        } catch (Exception e) {
            log.error("❌ 處理檔案 {} 時發生未預期錯誤: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("檔案上傳與解析失敗", e);
        }
    }

    /**
     * 上傳 pdf/word/excel/txt 文件至 IT 向量資料庫 (Vector Store)
     * @param fileResource
     */
    @Transactional
    public void updateItDocument(Resource fileResource) {
        var fileName = fileResource.getFilename();
        log.info("上傳 pdf/word/excel/txt 文件至 IT 向量資料庫 (Vector Store): {}", fileName);

        var category = "IT";

        try {
            // ==========================================
            // 刪除舊資料
            // ==========================================
            // 刪除舊的「向量表」
            String deleteVectorsSql = "DELETE FROM it_vector_store WHERE metadata->>'file_name' = ?";
            jdbcTemplate.update(deleteVectorsSql, fileName);

            // 刪除舊的「內文表」
            String deleteDocSql = "DELETE FROM vector_main WHERE file_name = ?";
            jdbcTemplate.update(deleteDocSql, fileName);

            // ==========================================
            // 上傳「內文表」
            // ==========================================
            // 將檔案轉換為 byte 陣列 (二進位資料)
            var fileBytes = fileResource.getContentAsByteArray();
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
            // 上傳「向量表」
            // ==========================================
            // 使用 Tika 萬能解析器，自動辨識 pdf/word/excel/txt 並抽出純文字 (無法抽出圖片)
            var tikaReader = new TikaDocumentReader(fileResource);
            var rawDocuments = tikaReader.get();

            // 將抽出來的文字切塊 (Chunking)
            var chunkedDocuments = new TokenTextSplitter().apply(rawDocuments);

            // 綁定檔名到 Metadata 建立關聯
            chunkedDocuments.forEach(doc -> doc.getMetadata().put("file_name", fileName));

            // 寫入「向量表」資料庫
            itVectorStore.add(chunkedDocuments);

            log.info("上傳「內文表」完成，共切為 {} 個知識區塊。", chunkedDocuments.size());
        } catch (IOException e) {
            log.error("❌ 讀取實體檔案失敗: {}", e.getMessage(), e);
            throw new RuntimeException("檔案讀取失敗", e);
        } catch (Exception e) {
            log.error("❌ 處理檔案 {} 時發生未預期錯誤: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("檔案上傳與解析失敗", e);
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
