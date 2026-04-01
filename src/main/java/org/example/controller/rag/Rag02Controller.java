package org.example.controller.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.rag.Rag02Service;
import org.example.framework.util.DateUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class Rag02Controller {
    private final Rag02Service service;

    public void run() {
        log.info("run()#START");

        OffsetDateTime 執行起始時間 = null;
        var inputDir = "D:/idea-worksapce/Demo-SpringBoot/doc/測試資料/";

        try {
            執行起始時間 = OffsetDateTime.now();

            // 上傳文件至 HR 向量資料庫 (Vector Store)
            var inputFile1 = "測試資料_HR_人資法規.pdf";
            var resource = new FileSystemResource(inputDir + inputFile1);
            service.updateHrDocument(resource);

            // 上傳文件至 IT 向量資料庫 (Vector Store)
            var inputFile2 = "測試資料_智慧工廠4.0大師.txt";
            resource = new FileSystemResource(inputDir + inputFile2);
            service.updateItDocument(resource);

            // 提問 AI (包含搜尋工具組)
            var question = "公司要資遣勞工時，需要多久的預告時間？";
            service.askTools(question);

            // 提問 AI (包含搜尋工具組)
            question = "倍福(Beckhoff)產品主要負責工廠哪個部分？";
            service.askTools(question);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　範例程式　執行結束");
            log.info("-------------------------------------------------------------");
            log.info("【功能】");
            log.info(" - 上傳文件到指定的 RAG 向量資料庫 (Vector Store)");
            log.info(" - 使用者開始提問");
            log.info(" - AI 根據使用者的問題，判斷要呼叫哪個工具 (Tool)，並將使用者的問題傳入工具中。");
            log.info(" - AI 根據工具回傳的結果，進行生成 (Generation)，並回覆給使用者。");
            log.info("【相關程式】");
            log.info(" - Rag02Controller.run()");
            log.info(" - Rag02Service.updateHrDocument()");
            log.info(" - Rag02Service.updateItDocument()");
            log.info(" - Rag02Service.askTools()");
            log.info(" - AiConfig.chatClient()");
            log.info(" - AiConfig.hrVectorStore()");
            log.info(" - AiConfig.itVectorStore()");
            log.info(" - RagTools.searchHrDatabase()");
            log.info(" - RagTools.searchItDatabase()");
            log.info(" - application.yml");
            log.info("【相關 Table】");
            log.info(" - vector_main");
            log.info(" - hr_vector_store");
            log.info(" - it_vector_store");
            log.info("【注意事項】");
            log.info(" - 允許上傳 pdf/word/excel/txt 文件。");
            log.info(" - 允許重複上傳，後蓋前");
            log.info(" - 無法解析圖片、影片等二進位檔案。");
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}
