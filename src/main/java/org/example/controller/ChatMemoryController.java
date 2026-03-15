package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.ChatMemoryService;
import org.example.util.DateUtil;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

/**
 * 範例 Controller，示範如何使用 Constructor Injection 與 Lombok。
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMemoryController {

    private final ChatMemoryService service;

    public void run() {
        log.info("run#START");

        OffsetDateTime 執行起始時間 = null;

        try {
            執行起始時間 = OffsetDateTime.now();

            log.info("========== 開始【有記憶】的對話測試 ==========");

            // 模擬兩位不同的使用者
            String userA_Id = "SESSION_01";
            String userB_Id = "SESSION_02";

            // ------------------------------------------------
            // 🙎‍♂️ User A 的對話情境
            // ------------------------------------------------
            service.chat(userA_Id, "你好，我叫做 Justin，我是一位資深軟體架構師。");

            // 這裡 AI 應該要能叫出名字和職業！
            service.chat(userA_Id, "你還記得我叫什麼名字？我的職業是什麼嗎？");

            // ------------------------------------------------
            // 🙎‍♀️ User B 的對話情境 (測試記憶隔離)
            // ------------------------------------------------
            service.chat(userB_Id, "哈囉，我是 Mary，我最喜歡吃蘋果。");

            // 這裡 AI 不能把 Mary 跟 Justin 搞混！
            service.chat(userB_Id, "請問我是誰？我剛剛說我喜歡吃什麼？");

            // ------------------------------------------------
            // 🙎‍♂️ User A 繼續對話
            // ------------------------------------------------
            service.chat(userA_Id, "那我呢？你還記得我嗎？");

            // ------------------------------------------------
            // 🧹 測試清除記憶
            // ------------------------------------------------
            service.clearMemory(userA_Id);
            service.chat(userA_Id, "你還記得我是誰嗎？"); // 清除後，AI 會忘記 Justin
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　範例程式　執行結束");
            log.info("-------------------------------------------------------------");
            log.info("【功能】");
            log.info(" - 有記憶功能的對話");
            log.info(" - 模擬兩位不同使用者的對話情境，並測試記憶隔離與清除記憶的功能。");
            log.info("【相關程式】");
            log.info(" - ChatMemoryController.run()");
            log.info(" - ChatMemoryService.chat()");
            log.info(" - AiConfig.chatMemory()");
            log.info("【注意事項】");
            log.info(" - 保留 N 筆記憶功能，設定在 AiConfig.chatMemory()");
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}

