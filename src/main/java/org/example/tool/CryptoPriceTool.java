package org.example.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 供給 AI 使用的工具：加密貨幣報價服務
 * * 說明: 實作 Function 介面，並以 @Service 註冊為 Spring Bean。
 */
@Slf4j
@Service("cryptoPriceFunction") // 👈 定義 Bean 名稱
public class CryptoPriceTool {

    // 依據專案規範：使用 JDK 25 record 嚴格定義傳入與傳出的資料結構
    public record Request(String coinName) {}
    public record Response(String coinName, double price, String currency) {}

    /**
     * 💡 架構師魔法：使用 @Tool 註解來標記這個方法為 AI 可呼叫的工具方法，並提供詳細的描述
     *
     * 說明: 這個方法會被 AI 呼叫來查詢指定加密貨幣的最新美金價格。AI 會根據 Request record 中的 coinName 欄位來決定要查詢哪種加密貨幣，然後回傳一個 Response record 包含幣種名稱、價格和貨幣單位。
     */
    @Tool(description = "查詢指定加密貨幣（如 Bitcoin, Ethereum）的最新美金價格")
    public Response getCryptoPrice(Request request) {
        log.info("🛠️ [Function Calling] AI 正在呼叫本地端工具，查詢 {} 的價格...", request.coinName());

        // 依據專案規範：使用 JDK 25 現代化 Switch 表達式
        var price = switch (request.coinName().toLowerCase()) {
            case "bitcoin", "btc" -> 95000.0;
            case "ethereum", "eth" -> 3200.0;
            case "solana", "sol" -> 150.0;
            default -> -1.0; // 找不到幣種
        };

        log.info("🛠️ [Function Calling] 查詢完成，{} 的價格為 {} USD", request.coinName(), price);
        return new Response(request.coinName(), price, "USD");
    }
}
