package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

/**
 * Spring AI 1.1.0 版本，若使用 gemini-3.1-pro-preview 模型呼叫 function calling 時，會發生 bug (thought_signature 遭限制)，若切換模型為 gemini-2.5-flash 則正常，等待 Spring AI 升級改版
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    // 依據專案規範：宣告為 final 並由 Lombok @RequiredArgsConstructor 進行建構子注入
    private final ChatClient chatClient;

    // 注入我們在 AiConfig 註冊的 ChatMemory Bean
    private final ChatMemory chatMemory;

    /**
     * 具有長期記憶的對話服務，能夠記住同一對話 ID 下的歷史訊息，讓 AI 在回覆時能夠參考過去的對話內容。
     *
     * @param sessionId 對話/使用者的唯一識別碼 (例如: "user_a_chat", "room_123")
     * @param userMessage    使用者本次輸入的訊息
     * @return AI 的回覆
     */
    public String chat(String sessionId, String userMessage) {
        log.info("👤 [對話 ID: {}] 使用者說: {}", sessionId, userMessage);

        // 建立具有記憶功能的顧問，並綁定對話 ID
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(sessionId)
                .build();

        var answer = chatClient.prompt()
                .user(userMessage)
                .advisors(memoryAdvisor) // 乾淨俐落，完全沒有 Lambda！
                .call()
                .content();

        log.info("🤖 [對話 ID: {}] AI 回覆: {}", sessionId, answer);
        return answer;
    }

    /**
     * 清除特定對話的記憶 (例如：使用者點擊了「開啟新對話」按鈕)
     */
    public void clearMemory(String sessionId) {
        log.info("🧹 清除對話 ID [{}] 的歷史記憶...", sessionId);
        chatMemory.clear(sessionId);
    }
}