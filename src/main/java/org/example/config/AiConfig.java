package org.example.config;

import org.example.tool.CryptoPriceTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Spring AI 核心配置類別
 *
 * 說明: 負責將 ChatClient.Builder 實例化為全局可共用的 ChatClient Bean。
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // 在這裡可以設定全局的 Default System Prompt，若無則直接 build()
        return builder.build();
    }

    /**
      * 手動建立 ChatMemory Bean，並指定使用 MessageWindowChatMemory 策略與 InMemoryChatMemoryRepository 儲存庫
      *
      * 說明: Spring AI 1.1.0 引入了職責分離的記憶體管理架構，讓開發者可以靈活選擇不同的記憶體管理策略與儲存庫實作。
      * 在這裡，我們選擇了 MessageWindowChatMemory 策略來管理對話歷史，並使用 InMemoryChatMemoryRepository 作為底層儲存庫。
      * 這樣的配置非常適合開發階段或小型專案，若未來需要支援企業級的巨量資料庫（如 Redis, PostgreSQL, Neo4j），只需替換儲存庫實作即可。
      */
    @Bean
    public ChatMemory chatMemory() {
        // 到了 Spring AI 1.1.0，為了支援企業級的巨量資料庫（如 Redis, PostgreSQL, Neo4j），官方實施了嚴格的**「職責分離 (Separation of Concerns)」**：
        // 底層儲存庫 (Storage)：抽離成了 ChatMemoryRepository 介面。
        // 記憶體管理策略 (Strategy)：負責管理「要保留最近幾則訊息（滑動窗口）」，這個職責交給了 MessageWindowChatMemory，而它實作了 ChatMemory 介面。
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository()) // 指定底層儲存庫
                .maxMessages(100)                                         // 指定保留的最大歷史訊息數
                .build();
    }

    /**
     * 💡 架構師魔法：手動建立 ToolCallbackProvider Bean，並直接注入 CryptoPriceFunction 工具物件
     *
     * 說明: 由於 Spring 掃描器在處理 @Tool 註解的生命週期上存在一些限制，導致無法直接將 CryptoPriceFunction 作為 Bean 注入到 ChatClient 中。
     * 為了解決這個問題，我們直接使用 MethodToolCallbackProvider 的 Builder 模式來建立 ToolCallbackProvider Bean，並將 CryptoPriceFunction 物件傳入其中。
     * 這樣一來，Spring 就不會干涉 @Tool 註解的解析過程，AI 就能順利地呼叫我們定義的工具方法了！
     */
    @Bean
    public ToolCallbackProvider cryptoMcpTools(CryptoPriceTool cryptoPriceTool) {
        // 使用底層的 Provider 直接解析 @Tool 物件，完美避開 Spring 掃描器的生命週期 Bug
        return MethodToolCallbackProvider.builder().toolObjects(cryptoPriceTool).build();
    }

//    // 💡 註冊向量資料庫 (簡易版)，實驗性功能，僅供概念驗證 (POC) 使用
//    @Bean
//    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
//        // 使用記憶體級別的 VectorStore 進行概念驗證 (POC)。
//        return SimpleVectorStore.builder(embeddingModel).build();
//    }

    /**
     * 註冊向量資料庫 (Vector Store)並注入至 vectorStore 物件。
     * @param jdbcTemplate
     * @param embeddingModel
     * @return
     */
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        // 建立 PgVectorStore
        //   未指定 vectorTableName() 時，預設會使用 "vector_store" 作為向量表名稱。
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(768)                 // 維度為 768 (對應 Gemini 模型)，防止維度不匹配報錯 (若更換其他模型時，請務必修改這裡的維度設定！)
                .initializeSchema(false)         // false：不讓 Spring 去初始化 Table Schema
                .build();
    }

    /**
     * 註冊向量資料庫 (Vector Store)並注入至 hrVectorStore 物件。
     * @param jdbcTemplate
     * @param embeddingModel
     * @return
     */
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public VectorStore hrVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        // 建立 PgVectorStore
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(768)                    // 維度為 768 (對應 Gemini 模型)，防止維度不匹配報錯 (若更換其他模型時，請務必修改這裡的維度設定！)
                .vectorTableName("hr_vector_store") // 指定向量表名稱，對應資料庫中的任一向量表格名稱
                .initializeSchema(false)            // false：不讓 Spring 去初始化 Table Schema
                .build();
    }

    /**
     * 註冊向量資料庫 (Vector Store)並注入至 hrVectorStore 物件。
     * @param jdbcTemplate
     * @param embeddingModel
     * @return
     */
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public VectorStore itVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        // 建立 PgVectorStore
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(768)                     // 維度為 768 (對應 Gemini 模型)，防止維度不匹配報錯 (若更換其他模型時，請務必修改這裡的維度設定！)
                .vectorTableName("it_vector_store") // 指定向量表名稱，對應資料庫中的任一向量表格名稱
                .initializeSchema(false)             // false：不讓 Spring 去初始化 Table Schema
                .build();
    }

    /**
     * 註冊向量資料庫 (Vector Store)並注入至 hrVectorStore 物件。
     * @param jdbcTemplate
     * @param embeddingModel
     * @return
     */
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public VectorStore babyVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        // 建立 PgVectorStore
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(768)                      // 維度為 768 (對應 Gemini 模型)，防止維度不匹配報錯 (若更換其他模型時，請務必修改這裡的維度設定！)
                .vectorTableName("baby_vector_store") // 指定向量表名稱，對應資料庫中的任一向量表格名稱
                .initializeSchema(false)              // false：不讓 Spring 去初始化 Table Schema
                .build();
    }
}
