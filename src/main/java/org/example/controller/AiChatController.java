package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AiResponseWrapper;
import org.example.service.AiChatService;
import org.example.service.TelegramService;
import org.example.util.DateUtil;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 範例 Controller，示範如何使用 Constructor Injection 與 Lombok。
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService service;

    public void run() {
        log.info("run#START");
        String prompt = "";
        AiResponseWrapper<String> responseWrapper = null;
        OffsetDateTime 執行起始時間 = null;
        try {
            執行起始時間 = OffsetDateTime.now();
            prompt = "你好，今天天氣如何？";
            responseWrapper = service.chatWrapper(prompt);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　測試與 AI 對話，執行結果");
            log.info("-------------------------------------------------------------");
            log.info("【提示詞】 {}", prompt);
            log.info("【回覆內容】 {}", responseWrapper.data());
            log.info("【使用模型】 {}", responseWrapper.model());
            log.info("【消耗Token】 {}", responseWrapper.tokens());
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}