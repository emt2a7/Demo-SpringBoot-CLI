package org.example.framework.hints;

import org.example.Main;
import org.example.dto.*;
import org.example.dto.github.GithubRepoDto;
import org.example.dto.github.GithubWorkflowDto;
import org.example.dto.github.GithubWorkflowResponse;
import org.example.dto.line.LineMessage;
import org.example.dto.line.LinePushRequest;
import org.example.dto.telegram.TelegramRequest;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;

/**
 * 🛡️ GraalVM Native Image 專用「保命白名單」
 *
 * 【為什麼需要這隻程式？】
 * 本專案會運行在 GitHub 雲端上，為了縮短運行時間、節省資源，
 * 選擇把 Java 程式透過 GraalVM 編譯成 Linux 執行檔 (.exe / binary)。
 *
 * GraalVM 把 Java 編譯成 Linux 執行檔 (.exe / binary) 時非常極端，
 * 它會把「表面上看起來沒直接呼叫」的程式碼當成垃圾刪掉以節省空間。
 * 但有些東西是「程式跑起來後，才偷偷動態建立的」(例如 Jackson 轉 JSON)，
 * 這支程式的作用就是明確告訴編譯器：「這些類別和檔案我執行時絕對會用到，不准刪掉！」
 * 否則上雲端執行時，會瞬間報錯崩潰 (ClassNotFound 或 FileNotFound)。
 */
@Configuration
public class NativeAllowlistHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        /* * ===================================================================
         * 📦 類別白名單 (Reflection Hints)
         *
         * 【什麼情況才需要把 Class 加進來？】
         * 你要轉成 JSON 的物件 (DTO、Record)。
         * 例如：用來接 OpenAI 或 Telegram API 回傳結果的 Response 物件，
         * 或是你要傳送出去的 Request 物件。因為 Jackson 是用「反射」偷偷塞資料的。
         *
         * 【什麼情況不需要加進來？】
         * 使用 @Service, @Controller, @ConfigurationProperties 等類別。 (Spring 會處理好，不用我們煩惱)
         * 使用 Lombok 提供的 @RequiredArgsConstructor 類別。 (Lombok 在編譯期就處理完了，不用擔心)
         * ===================================================================
         */
        Class<?>[] registeredTypes = new Class<?>[] {
                // 主類別（避免被當成無用程式碼刪除）
                Main.class,

                // 各種需要與 JSON 互轉的 DTO / Record
                AiResponseWrapper.class,
                CryptoPriceResponse.class,
                HelloResponse.class,
                StructuredRecord.class,
                TelegramRequest.class,
                LineMessage.class,
                LinePushRequest.class,

                GithubRepoDto.class,
                GithubWorkflowDto.class,
                GithubWorkflowResponse.class
        };

        for (Class<?> type : registeredTypes) {
            // 允許操作公開的「建構子、方法、屬性欄位」
            hints.reflection().registerType(type,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.DECLARED_FIELDS);
        }

        /* * ===================================================================
         * 📂 實體檔案白名單 (Resource Hints)
         *
         * 【什麼情況才需要把 檔案/資料夾 加進來？】
         * 你自己建立的文字檔、憑證、或是自訂目錄 (例如 doc/ 底下的文件)。
         * GraalVM 預設不會把這些實體檔案「烤死」進執行檔裡面。
         *
         * 【什麼情況不需要加進來？】
         * application.yml, application-*.yml, logback-spring.xml。 (Spring 會處理好，不用我們煩惱)
         * ===================================================================
         */
        hints.resources().registerPattern("doc/**");

    }
}