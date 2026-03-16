package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AiResponseWrapper;
import org.example.dto.StructuredRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding(StructuredRecord.class) // 告訴 GraalVM：請為 StructuredRecord 保留反射能力，讓 Jackson 可以順利反序列化！
public class StructuredService {

    private final ChatClient chatClient;

    /**
     * 解析非結構化的自傳，萃取為標準的 Record 物件
     *
     * @param prompt 亂七八糟的自傳文字
     * @return 結構化的 Java 物件
     */
    public StructuredRecord chat(String prompt) {
        log.info("📄 開始解析非結構化履歷...");

        // 您完全不需要手動寫 JSON Schema 的提示詞，框架會全自動處理！
        StructuredRecord profile = chatClient.prompt()
                .user(u -> u.text("請從以下非結構化的履歷文本中，精準萃取出求職者的資訊：\n\n{resume}")
                        .param("resume", prompt))
                .call()
                // 🌟 神奇魔法：直接告訴它你要轉成什麼 Class！
                // 底層會自動實例化 BeanOutputConverter，塞入 JSON Schema，並攔截回傳值進行轉換
                .entity(StructuredRecord.class);
        log.info("✅ 履歷解析完成！");
        return profile;
    }

    public AiResponseWrapper<StructuredRecord> chatWrapper(String prompt) {
        log.info("📄 開始解析非結構化履歷...");

        // 建立結構化轉換器 (產生 JSON Schema 規則)
        var converter = new BeanOutputConverter<>(StructuredRecord.class);

        ChatResponse response = chatClient.prompt()
                .user(u -> u.text("請從以下非結構化的履歷文本中，精準萃取出求職者的資訊：\n\n{resume}\n\n{format}")
                        .param("resume", prompt)
                        .param("format", converter.getFormat())) // 告訴 AI 回覆內容請按照這個格式來
                .call()
                .chatResponse();

        // 萃取系統 Metadata
        String model = response.getMetadata().getModel();
        Integer tokens = response.getMetadata().getUsage().getTotalTokens();


        // 解析 AI 回傳的純文字 JSON，轉換成 Java 實體物件
        String rawJson = response.getResult().getOutput().getText();
        StructuredRecord data = converter.convert(rawJson);

        log.info("✅ 履歷解析完成！");

        // 將資料與 Metadata 包裝在一起回傳
        return new AiResponseWrapper<>(data, model, tokens);
    }
}
