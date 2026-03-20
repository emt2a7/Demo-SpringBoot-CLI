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
     * 與 AI 進行對話
     * @param userPrompt
     * @return
     */
    public AiResponseWrapper<String> chat(String userPrompt) {
        return chat(null, userPrompt);
    }

    /**
     * 與 AI 進行對話，並將回傳的純文字資料與 Metadata 包裝在 AiResponseWrapper 中回傳
     * @param systemPrompt
     * @param userPrompt
     * @return
     */
    public AiResponseWrapper<String> chat(String systemPrompt, String userPrompt) {
        log.info("📄 開始與 AI 對話...");

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();

        // 系統提示詞
        if (StringUtils.isNotBlank(systemPrompt)) {
            requestSpec.system(StringUtils.trimToEmpty(systemPrompt));
        }

        // 使用者提示詞
        requestSpec.user(StringUtils.trimToEmpty(userPrompt));

        // 執行呼叫 AI 並取得回傳結果
        ChatResponse response = requestSpec.call().chatResponse();

        // 萃取回傳結果的 Metadata
        String model = response.getMetadata().getModel();
        Integer tokens = response.getMetadata().getUsage().getTotalTokens();

        // 解析回傳結果的純文字
        String data = response.getResult().getOutput().getText();

        log.info("✅ 與 AI 對話完成，回覆內容：{}", data);

        // 將資料與 Metadata 包裝在一起回傳
        return new AiResponseWrapper<>(data, model, tokens);
    }
}
