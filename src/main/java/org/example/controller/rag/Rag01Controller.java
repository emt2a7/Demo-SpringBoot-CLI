package org.example.controller.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.rag.Rag01Service;
import org.example.util.DateUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class Rag01Controller {
    private final Rag01Service service;

    public void run() {
        log.info("run()#START");

        OffsetDateTime 執行起始時間 = null;

        try {
            執行起始時間 = OffsetDateTime.now();

            // 上傳 PDF 文件至向量資料庫 (Vector Store)
            var inputDir = "D:/idea-worksapce/Demo-SpringBoot/doc/測試資料/";
            var inputFile1 = "測試資料_2025製造業薪資報告.pdf";
            var resource = new FileSystemResource(inputDir + inputFile1);
            service.uploadPdfDocument(resource);

            // 提問 AI (包含搜尋向量資料庫)
            var question = "十大熱門的職務有哪些？";
            var answer = service.askVectorStore(question);

            // 提問 AI (包含搜尋向量資料庫)
            question = "鋼鐵工業哪一家薪水排行最高？";
            answer = service.askVectorStore(question);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　範例程式　執行結束");
            log.info("-------------------------------------------------------------");
            log.info("【功能】上傳 PDF 文件到 RAG 向量資料庫 (Vector Store)，並進行 AI 問答。");
            log.info(" - 上傳文件到預設的 RAG 向量資料庫 (Vector Store)");
            log.info(" - 使用者開始提問");
            log.info(" - AI 根據使用者的問題，搜尋向量資料庫 (Vector Store)。");
            log.info(" - AI 根據搜尋結果，進行生成 (Generation)，並回覆給使用者。");
            log.info("【相關程式】");
            log.info(" - Rag01Controller.run()");
            log.info(" - Rag01Service.uploadPdfDocument()");
            log.info(" - Rag01Service.askVectorStore()");
            log.info(" - AiConfig.chatClient()");
            log.info(" - AiConfig.vectorStore()");
            log.info(" - application.yml");
            log.info("【相關 Table】");
            log.info(" - vector_store");
            log.info("【注意事項】");
            log.info(" - 僅能上傳 pdf 檔案。");
            log.info(" - 僅能上傳一次，重複上傳資料會一直增加，需手動刪除資料庫內的資料。");
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}
