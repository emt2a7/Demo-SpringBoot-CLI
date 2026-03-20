package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.dto.AiResponseWrapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient chatClient;

    /**
     * 與 AI 進行對話 (無系統提示詞，無工具)
     */
    public AiResponseWrapper<String> chat(String userPrompt) {
        return chat(null, userPrompt, (Object[]) null);
    }

    /**
     * 與 AI 進行對話 (有系統提示詞，無工具)
     */
    public AiResponseWrapper<String> chat(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, userPrompt, (Object[]) null);
    }

    /**
     * ✨ 與 AI 進行對話 (支援系統提示詞與動態掛載工具)
     * @param systemPrompt 系統提示詞
     * @param userPrompt   使用者提示詞
     * @param tools        要掛載給 AI 使用的工具陣列 (例如：GitHubToolService)
     */
    public AiResponseWrapper<String> chat(String systemPrompt, String userPrompt, Object... tools) {
        log.info("📄 開始與 AI 對話...");

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();

        // 1. 設定系統提示詞
        if (StringUtils.isNotBlank(systemPrompt)) {
            requestSpec.system(StringUtils.trimToEmpty(systemPrompt));
        }

        // 2. 設定使用者提示詞
        requestSpec.user(StringUtils.trimToEmpty(userPrompt));

        // 3. ✨ 掛載工具 (Tools)
        if (tools != null && tools.length > 0) {
            requestSpec.tools(tools);
            log.info("🔧 已成功掛載 {} 個 AI 工具包", tools.length);
        }

        // 4. 執行呼叫 AI 並取得回傳結果
        ChatResponse response = requestSpec.call().chatResponse();

        // 5. 萃取回傳結果的 Metadata
        String model = response.getMetadata().getModel();
        Integer tokens = response.getMetadata().getUsage().getTotalTokens();

        // 6. 解析回傳結果的純文字
        String data = response.getResult().getOutput().getText();

        log.info("✅ 與 AI 回覆：{}", data);

        // 將資料與 Metadata 包裝在一起回傳
        return new AiResponseWrapper<>(data, model, tokens);
    }
}