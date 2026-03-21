package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.dto.AiResponseWrapper;
import org.example.dto.telegram.TelegramRequest;
import org.example.framework.prop.TelegramProp;
import org.example.service.tool.GitHubToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramProp telegramProp;
    private final RestClient.Builder restClient;
    private final AiChatService aiChatService;
    private final GitHubToolService gitHubToolService;

    /**
     * 與 AI 進行對話，並取得回應
     * @param userPrompt
     * @return
     */
    public AiResponseWrapper<String> chat(String userPrompt) {
        // 取得台灣當下的時間字串
        String currentTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Taipei"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 系統提示詞
        String systemPrompt = """
        你是一個專業的 AI 助理。
        
        【系統資訊】
        現在的系統時間是 (台灣時區)：%s
        
        【重要規則】
        1. 你的回答內容請務必精簡，絕對不可超過 %d 個字元，以免超出系統的傳送限制。
        2. 你的回覆將傳送至通訊軟體，請一律使用「純文字 (Plain Text)」回答。
        3. 絕對禁止在回答中使用任何 Markdown 排版符號（嚴禁出現 **粗體**、*斜體*、`標記` 等符號）。
        """.formatted(currentTime, telegramProp.messageLimit().intValue());

        // 動態組裝 AI 工具清單 (List)
        List<Object> tools = new ArrayList<>();
        tools.add(gitHubToolService);

        return aiChatService.chat(systemPrompt, userPrompt, tools.toArray());
    }

    public void sendToTelegram(String telegramChatId, String text) {
        try {
            String url = telegramProp.apiUrl() + telegramProp.token() + telegramProp.sendUrl();

            // 超過 4000 字時，自動在結尾補上 "..."，且總長度保證 <= 4000
            String safeText = StringUtils.abbreviate(text, telegramProp.messageLimit().intValue());

            TelegramRequest requestBody = TelegramRequest.builder()
                    .chatId(telegramChatId)
                    .text(safeText)
                    .build();
            log.info("url:{}", url);

            // 使用 RestClient 發送請求
            String reesponse = restClient.build().post()
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
