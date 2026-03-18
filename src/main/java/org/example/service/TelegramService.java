package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
//@RegisterReflectionForBinding(TelegramRequest.class) // 告訴 GraalVM：請為 StructuredRecord 保留反射能力，讓 Jackson 可以順利反序列化！
public class TelegramService {

    private final String botToken;
    private final String botUrlRoot;
    private final String botUrlSend;
    private final Integer messageLimit;
    private final RestClient restClient;
    private final ChatClient chatClient;

    public TelegramService(
            @Value("${telegram.bot-token:}") String botToken,
            @Value("${telegram.bot-url-root:}") String botUrlRoot,
            @Value("${telegram.bot-url-send:}") String botUrlSend,
            @Value("${telegram.message-limit:}") Integer messageLimit,
            RestClient.Builder restClient,
            ChatClient chatClient) {
        this.botToken = botToken;
        this.botUrlRoot = botUrlRoot;
        this.botUrlSend = botUrlSend;
        this.messageLimit = messageLimit;
        this.restClient = restClient.build();
        this.chatClient = chatClient;
    }

    public AiResponseWrapper<String> chatWrapper(String prompt) {
        log.info("📄 開始與 AI 對話...");

        // 加入系統提示詞 (System Prompt)，從源頭限制 AI 輸出長度
        String systemPrompt = """
        你是一個專業的 AI 助理。
        【重要規則】
        請注意你的回答內容請務必精簡，絕對不可超過 %d 個字元，以免超出系統的傳送限制。
        """.formatted(messageLimit);

        ChatResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
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

            // 超過 4000 字時，自動在結尾補上 "..."，且總長度保證 <= 4000
            String safeText = StringUtils.abbreviate(text, messageLimit.intValue());

            TelegramRequest requestBody = TelegramRequest.builder()
                    .chatId(telegramChatId)
                    .text(safeText)
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
