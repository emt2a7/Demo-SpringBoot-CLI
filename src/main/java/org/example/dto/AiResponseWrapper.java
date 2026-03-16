package org.example.dto;

/**
 * 企業級 AI 回覆包裝器
 * 用來同時裝載「結構化資料」與「系統元數據 (Metadata)」
 *
 * @param <T> 結構化資料的型別 (例如 StructuredRecord)
 */
public record AiResponseWrapper<T>(
        T data,          // AI 解析出來的真實物件
        String model,    // 真正執行回答的模型名稱
        Integer tokens   // 這次請求總共消耗了多少 Token (計費關鍵！)
) {
}
