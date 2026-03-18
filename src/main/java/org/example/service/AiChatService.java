package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AiResponseWrapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient chatClient;

    public AiResponseWrapper<String> chatWrapper(String prompt) {
        log.info("📄 開始與 AI 對話...");

        ChatResponse response = chatClient.prompt()
                .user(u -> u.text("{prompt}")
                        .param("prompt", prompt))
                .call()
                .chatResponse();

        // 萃取系統 Metadata
        String model = response.getMetadata().getModel();
        Integer tokens = response.getMetadata().getUsage().getTotalTokens();

        // 解析 AI 回傳的純文字
        String data = response.getResult().getOutput().getText();

        log.info("✅ 與 AI 對話完成，回覆內容：{}", data);

        // 將資料與 Metadata 包裝在一起回傳
        return new AiResponseWrapper<>(data, model, tokens);
    }
}
