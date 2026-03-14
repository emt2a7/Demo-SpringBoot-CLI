package org.example.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor // 強制使用建構子注入
public class Rag01Service {

    // 注入 Spring AI 的 ChatClient，負責跟 Gemini 互動
    private final ChatClient chatClient;

    // 注入 Spring AI 的 VectorStore，負責管理向量(Vector)資料庫
    private final VectorStore vectorStore;

    /**
     * 上傳 PDF 文件至預設的向量資料庫 (Vector Store)
     * @param pdfResource PDF 文件資源
     */
    public void uploadPdfDocument(Resource pdfResource) {
        log.info("上傳 pdf 文件至向量資料庫 (Vector Store): {}", pdfResource.getFilename());

        try {
            // 讀取 PDF 文件
            var pdfReader = new PagePdfDocumentReader(pdfResource);
            var documents = pdfReader.get();

            // 切割 PDF 文件：將厚厚的文件切成一小塊一小塊 (Chunking)，確保不會超過 Token 限制
            var textSplitter = new TokenTextSplitter();
            var splitDocuments = textSplitter.apply(documents);

            // 向量化與儲存：將文字轉成向量並存入 Vector Database
            vectorStore.add(splitDocuments);

            log.info("✅ 文件上傳完成！共切分為 {} 個知識區塊。", splitDocuments.size());
        } catch (Exception e) {
            log.error("❌ 文件解析失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 提問 AI (包含搜尋向量資料庫)
     * @param question 使用者的問題
     */
    public String askVectorStore(String question) {
        log.info("使用者提問並搜尋向量資料庫 (Vector Store): {}", question);

        // 建構 QA Advisor (QA顧問)
        //   topK(3)：只取最相關的前 3 筆結果
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(3).build())
                .build();

        // 使用者在提問 AI 之前，先去搜尋向量資料庫 (Vector Store)，並塞進 Prompt 中
        var answer = chatClient.prompt()
                .user(question)         // 使用者提問 AI
                .advisors(qaAdvisor)    // 搜尋向量資料庫 (Vector Store)
                .call()                 // 將「提問」和「搜尋資料」，一同拿去給 AI
                .content();             // 取得 AI 回答的內容

        log.info("AI回覆內容: {}", answer);
        return answer;
    }
}
