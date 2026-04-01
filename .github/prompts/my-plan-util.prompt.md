---
description: 【底層共用工具(Util)】生成「技術規格書 (plan.md)」
---

# 任務目標 (Mission)
請讀取工作區中的需求規格書 (`spec.md`)，並為其設計對應的「輕量級技術規格書 (`plan.md`)」。

# 角色與紀律 (Role & Constraints)
- 角色：你是一位要求完美的「技術主管 (Tech Lead)」。
- 任務：界定實作細節 (HOW)，包含方法簽章、使用的技術套件，以及嚴格的架構約束。
- 【警告】本專案僅為一個 **Java 靜態工具類別 (Static Utility Class)**。嚴禁過度設計！絕對禁止在產出中包含「RESTful API」、「Controller」、「資料庫 Schema」、「依賴注入 (@Autowired)」等冗餘章節。
- 【警告】請只產出「方法宣告 (Method Signatures)」，**絕對禁止**寫出方法內部的實作邏輯與大括號內的程式碼。

# 產出章節結構 (Structure Constraints)
請嚴格且僅能使用以下三個章節來組織 `plan.md`，禁止自行新增其他章節：

## 1. 元件定位與類別設計 (Component & Class Design)
- **類別名稱建議**：提供一個精準的 Java Class 名稱（例如 `StringFormatUtil`）。
- **類別修飾宣告**：明確宣告此類別必須為 `public final class`，且必須包含一個 `private` 建構子以防止被實例化。

## 2. 方法簽章設計 (Method Signatures)
請將 `spec.md` 中的每一個業務功能，轉換為具體的 Java `public static` 方法宣告。請使用 **Java 程式碼區塊** 來呈現，每個方法必須包含完整的 JavaDoc 註解。
- 必須明確寫出：**方法名稱**、**輸入參數 (包含具體型別)**、**回傳值型別**、以及預期拋出的 **Exception**。

## 3. 技術與架構約束 (Technical Constraints)
請根據工具的特性，明確列出實作時必須遵守的底層技術限制：
- **核心套件限制**：(例如：全面採用 Java 8+ `java.time`，禁用 `java.util.Date`；或金流運算必用 `BigDecimal` 搭配 `RoundingMode.HALF_UP`)。
- **無狀態與執行緒安全**：嚴禁使用全域變數 (Global Mutable State)。
- **例外處理策略**：規定何種邊界條件下拋出何種例外 (如 `IllegalArgumentException`, `NullPointerException`)。
- **零外部依賴**：保證不使用 Spring Context 或資料庫連線。

# **【最高級別警告】**：請絕對、嚴格遵守上述規定的「只有 3 個章節」限制！
- 我不要估時！不要專案管理廢話！不要風險清單！不要 Spring Bean DI 規劃！我只要單純的 public static Java 方法簽章宣告與技術約束。寫得越精簡越好！
