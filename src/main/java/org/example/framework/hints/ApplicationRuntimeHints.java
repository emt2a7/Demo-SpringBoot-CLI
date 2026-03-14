package org.example.framework.hints;

import org.example.Main;
import org.example.dto.CryptoPriceResponse;
import org.example.dto.HelloResponse;
import org.example.framework.BitfinexProperties;
import org.example.framework.KioskProperties;
import org.example.framework.component.ExcelImageExtractor;
import org.example.framework.component.PdfImageExtractor;
import org.example.framework.component.WordImageExtractor;
import org.example.service.ChatMemoryService;
import org.example.service.CryptoPriceService;
import org.example.service.GeminiChatService;
import org.example.service.StructuredService;
import org.example.service.rag.Rag02Service;
import org.example.service.rag.Rag03Service;
import org.example.service.rag.Rag04Service;
import org.example.service.rag.Rag05Service;
import org.example.service.rag.Rag06Service;
import org.example.tool.CryptoPriceTool;
import org.example.tool.RagTools;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;

/**
 * GraalVM Native Image 友善提示 (RuntimeHints)
 *
 * 說明：
 * 1. 本類別用於統一註冊應用程式在執行期可能以反射或序列化被存取的類別與資源。
 * 2. 由於 Spring AOT / GraalVM 在靜態分析時容易遺漏動態載入或反射存取，本檔提供一份保守的註冊範例。
 * 3. 本檔案之建議條目應與 native-image-agent 輸出合併審核後再正式採用；agent 產物能補足測試跑不到的 code-path。
 *
 * 注意：請依專案實際情況精簡或擴充 registeredTypes 與 resourcesPatterns。
 */
@Configuration
public class ApplicationRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // ---------------------------------------------------------------------
        // 1) 要求對應應用類別提供必要的反射存取權限
        //    - MemberCategory 可依需求放寬或收窄；此處採保守方式註冊：
        //      註冊 public constructors + public methods + declared methods
        // ---------------------------------------------------------------------
        Class<?>[] registeredTypes = new Class<?>[] {
                // DTOs / Records
                HelloResponse.class,
                CryptoPriceResponse.class,

                // Config Properties (record)
                BitfinexProperties.class,
                KioskProperties.class,

                // Framework Components (會存取檔案、圖片、第三方 lib)
                PdfImageExtractor.class,
                WordImageExtractor.class,
                ExcelImageExtractor.class,

                // Tools / Services
                RagTools.class,
                CryptoPriceTool.class,

                Rag02Service.class,
                Rag03Service.class,
                Rag04Service.class,
                Rag05Service.class,
                Rag06Service.class,

                ChatMemoryService.class,
                CryptoPriceService.class,
                GeminiChatService.class,
                StructuredService.class,

                // 主類別（若以 java -jar 啟動時可能被框架反射檢查）
                Main.class
        };

        for (Class<?> type : registeredTypes) {
            // 註冊：可呼叫 public 建構子以及 public 方法
            hints.reflection().registerType(type,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            // 備註：若需要 Jackson 的序列化相關提示，可在合併 native-image-agent 的輸出後，加上更精準的提示。
        }

        // ---------------------------------------------------------------------
        // 2) 資源註冊：logback 與 application yml 是必須的動態資源
        //    (logback-spring.xml 由 Spring 在啟動期間載入並解析，因此顯式註冊)
        // ---------------------------------------------------------------------
        hints.resources().registerPattern("logback-spring.xml");
        hints.resources().registerPattern("application.yml");
        hints.resources().registerPattern("application-*.yml");

        // 建議註冊 doc 與測試資料檔案（若你在 runtime 會以 classpath 方式讀取）
        hints.resources().registerPattern("doc/**");

        // ---------------------------------------------------------------------
        // 3) 針對可能由框架建立的動態代理（proxy）進行示例註冊。
        //    若你在執行 native build 時觀察到缺少的代理介面，請在此加入。
        //    範例: hints.proxies().registerJdkProxy(InterfaceA.class, InterfaceB.class);
        // ---------------------------------------------------------------------
        // 留白以便後續由 agent 檔案補齊

        // ---------------------------------------------------------------------
        // 4) 結語說明
        //    - 建議的工作流程：先採用 static hints + native-image-agent 產出配置
        //      再將 agent 的 reflect-config.json 與本類別合併，最後在 CI 上驗證
        // ---------------------------------------------------------------------
    }
}
