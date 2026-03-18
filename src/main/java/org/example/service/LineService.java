package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.dto.AiResponseWrapper;
import org.example.dto.line.LineMessage;
import org.example.dto.line.LinePushRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class LineService {

    private final String token;
    private final String apiUrl;
    private final Integer messageLimit;
    private final RestClient restClient;
    private final ChatClient chatClient;

    public LineService(
            @Value("${line.bot.token:}") String token,
            @Value("${line.bot.api-url:}") String apiUrl,
            @Value("${line.bot.message-limit:}") Integer messageLimit,
            RestClient.Builder restClient,
            ChatClient chatClient) {
        this.token = token;
        this.apiUrl = apiUrl;
        this.messageLimit = messageLimit;
        this.restClient = restClient.build();
        this.chatClient = chatClient;
    }

    public AiResponseWrapper<String> chatWrapper(String userPrompt) {
        log.info("📄 開始與 AI 對話 (LINE 通道)...");

        String systemPrompt = """
        你是一個專業的 AI 助理。
        【重要規則】
        請注意你的回答內容請務必精簡，絕對不可超過 %d 個字元，以免超出系統的傳送限制。
        """.formatted(messageLimit);

        ChatResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .chatResponse();

        String model = response.getMetadata().getModel();
        Integer tokens = response.getMetadata().getUsage().getTotalTokens();
        String data = response.getResult().getOutput().getText();

        log.info("✅ 與 AI 對話完成，回覆內容：{}", data);
        return new AiResponseWrapper<>(data, model, tokens);
    }

    public void sendToLine(String chatId, String text) {
        try {
            // LINE 單則純文字的極限是 5000 字，為了安全我們一樣套用防呆截斷
            String safeText = StringUtils.abbreviate(text, messageLimit.intValue());

            // 組裝 LINE 專屬的 Request Body
            LinePushRequest requestBody = LinePushRequest.builder()
                    .to(chatId)
                    .messages(List.of(
                            LineMessage.builder()
                                    .type("text")
                                    .text(safeText)
                                    .build()
                    ))
                    .build();

            // 使用 RestClient 發送請求給 LINE
            String response = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + token) // ✨ 差異點：Token 放這裡！
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("✅ 成功傳送至 LINE！");
        } catch (Exception e) {
            log.error("❌ 傳送至 LINE 失敗！", e);
        }
    }
}