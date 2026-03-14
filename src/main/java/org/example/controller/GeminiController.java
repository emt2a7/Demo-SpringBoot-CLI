package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.GeminiChatService;
import org.springframework.stereotype.Controller;

/**
 * 範例 Controller，示範如何使用 Constructor Injection 與 Lombok。
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiChatService service;

    public void run() {
        log.info("GeminiController#START");
        service.askGemini("請簡短說明一下什麼是 Spring AI？");
        log.info("GeminiController#END");
    }
}

