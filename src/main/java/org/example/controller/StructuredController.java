package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.StructuredRecord;
import org.example.service.StructuredService;
import org.example.util.DateUtil;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StructuredController {

    private final StructuredService service;

    public void run() {
        log.info("run()#START");

        OffsetDateTime 執行起始時間 = null;
        var inputDir = "D:/idea-worksapce/Demo-SpringBoot/doc/測試資料/";

        try {
            執行起始時間 = OffsetDateTime.now();

            log.info("========== 開始【結構化輸出】測試 ==========");

            // 這是一段毫無結構、充滿口語廢話的自傳
            String prompt = """
                    哈囉面試官您好，我叫王小明，大家都叫我明哥。
                    我大概是從 2019 年 3 月開始出來工作的，算一算到現在大概快五年了。
                    我大學是讀國立台灣科技大學資訊工程系畢業的。
                    平常上班都在寫 Java，最近這兩年狂寫 Spring Boot，
                    然後資料庫是用 PostgreSQL，偶爾也會碰到一點點 Docker 跟 Kubernetes (K8s) 的部署。
                    我的缺點是有點容易緊張，但我學習能力滿強的，希望能加入貴公司！
                    """;

            // 呼叫我們的 Service
            StructuredRecord profile = service.chat(prompt);

            // 印出轉化後的 Java 實體物件
            log.info("🎯 成功取得 Java 物件！");
            log.info("姓名: {}", profile.fullName());
            log.info("學歷: {}", profile.highestSchool());
            log.info("年資: {} 年", profile.yearsOfExperience());
            log.info("技能: {}", profile.technicalSkills());
            log.info("HR評價: {}", profile.hrRemark());
            log.info("========== 結構化輸出測試結束 ==========");
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　範例程式　執行結束");
            log.info("-------------------------------------------------------------");
            log.info("【功能】");
            log.info(" - 使用者向 AI 提問，並將 AI 的回答直接轉換成結構化的 Java 物件，方便後續程式使用。");
            log.info("【相關程式】");
            log.info(" - StructuredController.run()");
            log.info(" - StructuredController.chat()");
            log.info(" - StructuredRecord.java");
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}
