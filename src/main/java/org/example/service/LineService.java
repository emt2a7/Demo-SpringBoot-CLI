package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.dto.AiResponseWrapper;
import org.example.dto.line.LineMessage;
import org.example.dto.line.LinePushRequest;
import org.example.framework.prop.LineProp;
import org.example.framework.prop.TelegramProp;
import org.example.service.tool.GitHubToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineService {

    private final LineProp lineProp;
    private final RestClient.Builder restClient;
    private final AiChatService aiChatService;
    private final GitHubToolService gitHubToolService;

    public AiResponseWrapper<String> chat(String userPrompt) {
        // 系統提示詞
        String systemPrompt = """
        你是一個專業的 AI 助理。
        【重要規則】
        請注意你的回答內容請務必精簡，絕對不可超過 %d 個字元，以免超出系統的傳送限制。
        """.formatted(lineProp.messageLimit().intValue());

        // 動態組裝 AI 工具清單 (List)
        List<Object> tools = new ArrayList<>();
        tools.add(gitHubToolService);

        return aiChatService.chat(systemPrompt, userPrompt.toString(), tools.toArray());
    }

    public void sendToLine(String chatId, String text) {
        try {
            // LINE 單則純文字的極限是 5000 字，為了安全我們一樣套用防呆截斷
            String safeText = StringUtils.abbreviate(text, lineProp.messageLimit().intValue());

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
            String response = restClient.build().post()
                    .uri(lineProp.apiUrl())
                    .header("Authorization", "Bearer " + lineProp.token()) // ✨ 差異點：Token 放這裡！
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