package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AiResponseWrapper;
import org.example.service.TelegramService;
import org.example.util.DateUtil;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

/**
 * 範例 Controller，示範如何使用 Constructor Injection 與 Lombok。
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService service;

    public void run(String chatId, String userPrompt) {
        log.info("run#START");
        OffsetDateTime 執行起始時間 = null;
        AiResponseWrapper<String> responseWrapper = null;

        try {
            執行起始時間 = OffsetDateTime.now();

            responseWrapper = service.chat(userPrompt);
            service.sendToTelegram(chatId, responseWrapper.data());
        } catch (Exception e) {
            var msg = """
            ⚠️error⚠️ %s
            """.formatted(e.getMessage());
            log.error("{}", msg, e);
            service.sendToTelegram(chatId, msg);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　Telegram Bot 執行結果");
            log.info("-------------------------------------------------------------");
            log.info("【傳入參數-聊天室ID】 {}", chatId);
            log.info("【傳入參數-提示詞】 {}", userPrompt);
            log.info("【使用模型】 {}", responseWrapper.model());
            log.info("【消耗Token】 {}", responseWrapper.tokens());
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}

