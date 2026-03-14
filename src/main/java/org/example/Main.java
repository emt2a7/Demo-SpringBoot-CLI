package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.controller.ChatMemoryController;
import org.example.controller.StructuredController;
import org.example.controller.rag.*;
import org.example.util.DateUtil;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.time.OffsetDateTime;

/**
 * 主啟動類別。
 *
 * 此類別為 Spring Boot 應用的進入點，負責啟動整個 Spring 容器。
 * 所有示範 Controller / Service 將由 Spring 容器管理。
 */
@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class Main {

    /**
     * 應用程式主方法。
     *
     * @param args 啟動參數
     */
    public static void main(String[] args) {
        OffsetDateTime 執行起始時間 = null;
        ApplicationContext context = null;
        try {
            執行起始時間 = OffsetDateTime.now();

            // 啟動並取得 Spring 容器 (ApplicationContext)
            SpringApplication app = new SpringApplication(Main.class);
            app.setWebApplicationType(WebApplicationType.NONE); // 強制設定為非 Web 應用程式，進一步加快啟動速度與降低記憶體消耗
            //app.setLazyInitialization(true); // 開啟全局懶加載 (Lazy Initialization)，只有在你真正 getBean() 時才去建立物件與連線。
            context = app.run(args); // 將 args 原封不動地交給了 Spring 引擎

            //範例_有記憶功能的聊天對話(context);

            //範例_將AI回覆對話轉換成結構化輸出(context);

            //範例_上傳PDF到RAG向量資料庫(context);

            //範例_上傳pdf_word_excel_txt到指定的RAG向量資料庫並啟用Tools_解析文字(context);

            //範例_上傳pdf_word_excel_txt到指定的RAG向量資料庫並啟用Tools_解析文字及圖片(context);

            //範例_上傳pdf到指定的RAG向量資料庫並啟用Tools_解析文字及圖片_解決thought_signature問題(context);

            //範例_上傳word到指定的RAG向量資料庫並啟用Tools_解析文字及圖片_解決thought_signature問題(context);

            //範例_上傳excel到指定的RAG向量資料庫並啟用Tools_解析文字及圖片_解決thought_signature問題(context);


        } catch (Exception e) {
            // 🔴 架構師的 AOT 防禦：絕對不能吞掉 Spring Boot AOT 內部的中斷例外
            if (e.getClass().getName().equals("org.springframework.boot.SpringApplication$AbandonedRunException")) {
                throw (RuntimeException) e; // 直接往上拋，交還控制權給 AOT 引擎
            }
            log.error("執行發生錯誤: {}", e.getMessage(), e);
        } finally {
            log.info("【CLI 總花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            exit(context);
        }
    }

    @Bean
    public ApplicationRunner runSmartTask() {
        return (ApplicationArguments args) -> {
            // 自動解析類似 --mode=agent 的參數
            if (args.containsOption("mode")) {
                log.info("啟動模式: {}", args.getOptionValues("mode"));
            }
            // 取得沒有加上 -- 的純文字參數
            log.info("一般參數: {}", args.getNonOptionArgs());
        };
    }

    public static void 範例_有記憶功能的聊天對話(ApplicationContext context) {
        var controller = context.getBean(ChatMemoryController.class);
        controller.run();
    }

    public static void 範例_將AI回覆對話轉換成結構化輸出(ApplicationContext context) {
        var controller = context.getBean(StructuredController.class);
        controller.run();
    }

    public static void 範例_上傳PDF到RAG向量資料庫(ApplicationContext context) {
        var controller = context.getBean(Rag01Controller.class);
        controller.run();
    }

    public static void 範例_上傳pdf_word_excel_txt到指定的RAG向量資料庫並啟用Tools_解析文字(ApplicationContext context) {
        var controller = context.getBean(Rag02Controller.class);
        controller.run();
    }

    public static void 範例_上傳pdf_word_excel_txt到指定的RAG向量資料庫並啟用Tools_解析文字及圖片(ApplicationContext context) {
        var controller = context.getBean(Rag03Controller.class);
        controller.run();
    }

    public static void 範例_上傳pdf到指定的RAG向量資料庫並啟用Tools_解析文字及圖片_解決thought_signature問題(ApplicationContext context) {
        var controller = context.getBean(Rag04Controller.class);
        controller.run();
    }

    public static void 範例_上傳word到指定的RAG向量資料庫並啟用Tools_解析文字及圖片_解決thought_signature問題(ApplicationContext context) {
        var controller = context.getBean(Rag05Controller.class);
        controller.run();
    }

    public static void 範例_上傳excel到指定的RAG向量資料庫並啟用Tools_解析文字及圖片_解決thought_signature問題(ApplicationContext context) {
        var controller = context.getBean(Rag06Controller.class);
        controller.run();
    }

    /**
     * 優雅關閉 Spring Boot 應用程式。
     * @param context Spring 的應用程式上下文，包含了所有被 Spring 管理的 Bean 與資源。
     */
    public static void exit(ApplicationContext context) {
        if (context != null) {
            // 任務執行完畢後，通知 Spring 容器優雅關閉 (這會關閉 MCP Server 與 DB 連線池)
            int exitCode = SpringApplication.exit(context, () -> 0);

            // 強制退出 JVM
            System.exit(exitCode);
        }
    }
}
