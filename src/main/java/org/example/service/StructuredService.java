package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.StructuredRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
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

        // ==========================================
        // 💡 寫法一：Spring AI 1.1.0 的終極流暢 API (最推薦！)
        // ==========================================
        // 您完全不需要手動寫 JSON Schema 的提示詞，框架會全自動處理！
        StructuredRecord profile = chatClient.prompt()
                .user(u -> u.text("請從以下非結構化的履歷文本中，精準萃取出求職者的資訊：\n\n{resume}")
                        .param("resume", prompt))
                .call()
                // 🌟 神奇魔法：直接告訴它你要轉成什麼 Class！
                // 底層會自動實例化 BeanOutputConverter，塞入 JSON Schema，並攔截回傳值進行轉換
                .entity(StructuredRecord.class);

        /* // ==========================================
        // 💡 寫法二：底層原理版 (如果您想看透底層運作原理)
        // ==========================================
        var converter = new BeanOutputConverter<>(CandidateProfile.class);
        // 這行會產出一大串 JSON Schema 規定，例如 "請回傳 JSON，包含 fullName 欄位..."
        String formatInstructions = converter.getFormat();

        String rawJsonResponse = chatClient.prompt()
                .user(rawResumeText + "\n\n" + formatInstructions)
                .call()
                .content();

        CandidateProfile profile = converter.convert(rawJsonResponse);
        */

        log.info("✅ 履歷解析完成！");
        return profile;
    }
}
