package org.example.dto;

/**
 * 結構化輸出：AI 回傳的加密貨幣估值報告
 * 說明: 採用 JDK 25 Record，Spring AI 會自動將其轉換為 JSON Schema。
 */
public record CryptoPriceResponse(
        String coinName,      // 幣種名稱 (例如: Bitcoin)
        double currentPrice,  // 單顆目前價格
        double totalValue,    // 使用者持有的總價值
        String currency,      // 計價幣別 (例如: USD)
        String aiAdvice       // AI 給予的一句簡短投資建議
) {}
