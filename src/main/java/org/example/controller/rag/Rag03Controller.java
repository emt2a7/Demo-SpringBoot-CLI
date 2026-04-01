package org.example.controller.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.rag.Rag03Service;
import org.example.framework.util.DateUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class Rag03Controller {
    private final Rag03Service service;

    public void run() {
        log.info("run()#START");

        OffsetDateTime 執行起始時間 = null;
        var inputDir = "D:/idea-worksapce/Demo-SpringBoot/doc/測試資料/";

        try {
            執行起始時間 = OffsetDateTime.now();

            // 上傳 pdf 文件至 Baby 向量資料庫 (Vector Store)
            var inputFile = "測試資料_台灣總人口連26個月負成長.pdf"; // 含圖片的 PDF 文件
            var resource = new FileSystemResource(inputDir + inputFile);
            service.updateBabyDocument(resource);

            // 提問 AI 關於圖片內容的問題(包含搜尋工具組)
            var question = "台灣的出生死亡人數，從哪一年開始形成死亡交叉？";
            service.askTools(question);

            // 提問 AI 關於圖片內容的問題(包含搜尋工具組)
            question = "台灣在2021年全球生育率排名第幾？";
            service.askTools(question);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　範例程式　執行結束");
            log.info("-------------------------------------------------------------");
            log.info("【功能】");
            log.info(" - 解析上傳文件中的純文字及圖片，並將圖片提問AI轉成文字描述，再保存到指定的 RAG 向量資料庫 (Vector Store)");
            log.info(" - 使用者開始提問");
            log.info(" - AI 根據使用者的問題，判斷要呼叫哪個工具 (Tool)，並將使用者的問題傳入工具中。");
            log.info(" - AI 根據工具回傳的結果，進行生成 (Generation)，並回覆給使用者。");
            log.info("【相關程式】");
            log.info(" - Rag03Controller.run()");
            log.info(" - Rag03Service.updateBabyDocument()");
            log.info(" - Rag03Service.askPdfImageToDescription()");
            log.info(" - Rag03Service.askTools()");
            log.info(" - AiConfig.chatClient()");
            log.info(" - AiConfig.babyVectorStore()");
            log.info(" - RagTools.searchBabyDatabase()");
            log.info(" - application.yml");
            log.info("【相關 Table】");
            log.info(" - vector_main");
            log.info(" - baby_vector_store");
            log.info("【注意事項】");
            log.info(" - 允許上傳 pdf 文件。");
            log.info(" - 允許重複上傳，後蓋前");
            log.info(" - 允許解析圖片轉文字描述。");
            log.info("【致命缺點】");
            log.info(" - 若使用 Google Gemini 3+ 模型並搭配 Tools 工具組時，會發生 Function call is missing a thought_signature。");
            log.info(" - 意思是，會遺失 thought_signature，導致無法正確回傳工具的結果。");
            log.info(" - 不確定未來 Spring AI 或 Gemini 是否會修復，以後再說吧。");
            log.info(" - Rag04Controller 有提供另一種解決方案：「Controller-Agent (控制者代理)」");
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}
