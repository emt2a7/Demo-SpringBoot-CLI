package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AiResponseWrapper;
import org.example.dto.StructuredRecord;
import org.example.dto.TelegramRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RegisterReflectionForBinding(TelegramRequest.class) // 告訴 GraalVM：請為 StructuredRecord 保留反射能力，讓 Jackson 可以順利反序列化！
public class TelegramService {

    private final String botToken;
    private final String botUrlRoot;
    private final String botUrlSend;
    private final RestClient restClient;
    private final ChatClient chatClient;

    public TelegramService(
            @Value("${telegram.bot-token:}") String botToken,
            @Value("${telegram.bot-url-root:}") String botUrlRoot,
            @Value("${telegram.bot-url-send:}") String botUrlSend,
            RestClient.Builder restClient,
            ChatClient chatClient) {
        this.botToken = botToken;
        this.botUrlRoot = botUrlRoot;
        this.botUrlSend = botUrlSend;
        this.restClient = restClient.build();
        this.chatClient = chatClient;
    }

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

    public void sendToTelegram(String telegramChatId, String text) {
        try {
            String url = botUrlRoot + botToken + botUrlSend;

            TelegramRequest requestBody = TelegramRequest.builder()
                    .chatId(telegramChatId)
                    .text(text)
                    .build();

            log.info("url:{}", url);

            // 使用 RestClient 發送請求
            String reesponse = restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    // 注意：這裡我用最簡單的字串拼接示範。建議實務上可改用 DTO 物件 (如上一篇提到的 TelegramMessageRequest)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("✅ 成功傳送至 Telegram！");
        } catch (Exception e) {
            log.error("❌ 傳送至 Telegram 失敗！", e);
        }
    }
}
