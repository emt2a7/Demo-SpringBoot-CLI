package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tool.CryptoPriceTool;
import org.example.framework.util.DateUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Spring AI 1.1.0 版本，若使用 gemini-3.1-pro-preview 模型呼叫 function calling 時，會發生 bug (thought_signature 遭限制)，若切換模型為 gemini-2.5-flash 則正常，等待 Spring AI 升級改版
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoPriceService {

    // 依據專案規範：宣告為 final 並由 Lombok @RequiredArgsConstructor 進行建構子注入
    private final ChatClient chatClient;

    // 關鍵修正：透過建構子注入「真正的 Tool 實例」，而不是用字串！
    private final CryptoPriceTool cryptoPriceTool;

    // 注入我們在 AiConfig 註冊的 ChatMemory Bean
    private final ChatMemory chatMemory;

    /**
     * 發送訊息給 Gemini 並取得回應（使用 Function Calling）
     *
     * @param userMessage 使用者的輸入訊息
     * @return Gemini 的文字回應
     */
    public void askGemini(String userMessage) {
        log.info("準備發送訊息至 Gemini，使用者輸入: {}", userMessage);

        var modelName = "未知模型";
        var content = "系統目前無法連線至 AI 引擎，請稍後再試。";
        OffsetDateTime 執行起始時間 = null;
        ChatResponse response = null;

        // 依據專案規範：使用 JDK 現代化文字區塊 (Text Blocks) 定義 Prompt
        var systemPrompt = """
            你是一位具備 20 年經驗的資深 Java 架構師，同時也精通區塊鏈技術。
            如果使用者詢問加密貨幣價格，請務必呼叫提供的工具來獲取最新資訊。
            請使用繁體中文回答。
            """;

        try {
            執行起始時間 = OffsetDateTime.now();

            // 依據專案規範：精準使用 var，並採用 Spring AI 1.1.0 推薦的 Fluent API
            response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .tools(cryptoPriceTool) // 註冊 Function Calling
                    .call()
                    .chatResponse();

            // 2. 從 Metadata 中抽出模型名稱
            //modelName = response.getMetadata().getModel();
            content = response.getResult().getOutput().getText();

            log.debug("Gemini 原始回應內容:");
            log.debug("{}", content);
        } catch (Exception e) {
            // 依據專案規範：捕捉連線超時或異常，並將 Exception 作為最後一個參數傳入 log
            log.error("與 Gemini API 進行通訊時發生異常，請檢查網路狀態或 API Key，訊息: {}", e.getMessage(), e);
        } finally {
            log.info("========================================");
            log.info("　　　　　　　　執行結果");
            log.info("========================================");
            //log.info("使用模型：{}", modelName);
            log.info("花費時間：{}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("========================================");
        }
    }
}