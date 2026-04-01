package org.example.controller.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.rag.Rag06Service;
import org.example.framework.util.DateUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class Rag06Controller {
    private final Rag06Service service;

    public void run() {
        log.info("run()#START");

        OffsetDateTime 執行起始時間 = null;
        var inputDir = "D:/idea-worksapce/Demo-SpringBoot/doc/測試資料/";

        try {
            執行起始時間 = OffsetDateTime.now();

            // 上傳 excel 文件至 Baby 向量資料庫 (Vector Store)
            var inputFile = "測試資料_出生數及粗出生率(按登記及發生)(36).xlsx"; // 含圖片的 excel 文件
            var resource = new FileSystemResource(inputDir + inputFile);
            service.updateBabyDocument(resource);

            // 提問 AI 關於圖片內容的問題(包含搜尋工具組)
            var question = "台灣的出生死亡人數，從哪一年開始形成死亡交叉？";
            service.askTools(question);

            // 提問 AI 關於圖片內容的問題(包含搜尋工具組)
            question = "台灣在2021年全球生育率排名第幾？";
            service.askTools(question);

            // 提問 AI 關於圖片內容的問題(包含搜尋工具組)
            question = "在民國113年的全國登記當中，男女生的出生人口有多少個？";
            service.askTools(question);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　範例程式　執行結束");
            log.info("-------------------------------------------------------------");
            log.info("【解決 thought_signature 問題】");
            log.info(" - 關鍵程式碼在 Rag04Service.askTools()");
            log.info(" - 原先讓 AI 主動決定要呼叫哪個工具 (Tool)，現改成由我們主動問 AI");
            log.info(" - 讓 AI 被動回答要呼叫哪個工具 (Tool) 後，再由我們根據 AI 的回答來呼叫工具 (Tool)，這樣就能解決 thought_signature 的問題。");
            log.info(" - 假設以後 Spring AI 或 Gemini 能解決 thought_signature 問題的話，到時候再把程式寫法改回去。");
            log.info("【功能】");
            log.info(" - 解析上傳文件中的純文字及圖片，並將圖片提問AI轉成文字描述，再保存到指定的 RAG 向量資料庫 (Vector Store)");
            log.info(" - 使用者開始提問");
            log.info(" - 我們主動問 AI，讓 AI 被動回答要呼叫哪個工具 (Tool)");
            log.info(" - 再由我們根據 AI 的回答來呼叫工具 (Tool)，並將使用者的問題傳入工具中。");
            log.info(" - AI 根據工具回傳的結果，進行生成 (Generation)，並回覆給使用者。");
            log.info("【相關程式】");
            log.info(" - Rag06Controller.run()");
            log.info(" - Rag06Service.updateBabyDocument()");
            log.info(" - Rag06Service.askWordImageToDescription()");
            log.info(" - Rag06Service.askTools()");
            log.info(" - AiConfig.chatClient()");
            log.info(" - AiConfig.babyVectorStore()");
            log.info(" - RagTools.searchBabyDatabase()");
            log.info(" - application.yml");
            log.info("【相關 Table】");
            log.info(" - vector_main");
            log.info(" - baby_vector_store");
            log.info("【注意事項】");
            log.info(" - 允許上傳 excel 文件，副檔名必須是 *.xlsx，且文件內可以包含圖片。");
            log.info(" - 允許重複上傳，後蓋前");
            log.info(" - 允許解析圖片轉文字描述。");
            log.info(" - 能夠解決 thought_signature 問題");
            log.info("【缺點】");
            log.info(" - 在 Rag06Service.askTools() 中，必須要寫死 routePrompt 的內容，以後有需要再來優化");
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}
