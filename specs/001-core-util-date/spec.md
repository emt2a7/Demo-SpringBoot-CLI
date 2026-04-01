---
description: 日期工具類別 DateUtil 之需求規格書 (spec.md)
---

# 1. 模組定位與職責邊界 (Module Positioning & Boundaries)
- **定位**：本模組為「底層共用工具 (Utility)」中的日期處理核心，專責於各式日期差距計算、逾時判斷、時間戳轉換與格式化等純記憶體內運算。
- **職責內 (In-Scope)**：
  - 計算兩個 OffsetDateTime 之間的毫秒差距
  - 以文字描述兩個 OffsetDateTime 之間的差距
  - 判斷指定日期是否逾時 (timeout)
  - 時間戳 (epoch millis) 轉 OffsetDateTime
  - OffsetDateTime 轉格式化字串 (yyyy-MM-dd HH:mm:ss)
- **職責外 (Out-of-Scope)**：
  - 本模組不處理資料庫讀寫、不處理外部 API 呼叫，僅提供純粹的記憶體內資料處理與運算。

# 2. 核心處理邏輯與業務規則 (Core Logic & Business Rules)
- 所有方法皆須主動檢查輸入參數，遇到無效輸入時拋出 IllegalArgumentException。
- 差距計算：
  - 方法1：回傳兩個 OffsetDateTime 之間的毫秒差距，Date1 必須早於 Date2，否則拋出例外。
  - 方法2：回傳兩個 OffsetDateTime 差距的中文描述，單位依據毫秒數自動切換，且不顯示為 0 的單位。
    - 若 Date1 晚於 Date2，仍能正確計算並回傳正確描述。
- 逾時判斷：
  - 以 Asia/Taipei 為預設時區，計算 Now - Date > TimeoutThreshold (ms) 判斷是否逾時。
- 時間戳轉換：
  - 將 long 型態的 epoch millis 依指定時區轉為 OffsetDateTime。
- 日期格式化：
  - 將 OffsetDateTime 依指定時區轉為 yyyy-MM-dd HH:mm:ss 格式字串。

# 3. 方法介面合約設計 (Method Interface Contracts)
| 方法名稱 | 功能描述 | 輸入 | 輸出 | 異常 |
|---|---|---|---|---|
| calculateMillisDiff | 計算兩個日期之間的毫秒差距 | Date1: OffsetDateTime<br>Date2: OffsetDateTime | long (毫秒數) | 任一為 null、格式不合法、Date1 > Date2 時拋 IllegalArgumentException |
| describeDateDiff | 以中文描述兩個日期之間的差距 | Date1: OffsetDateTime<br>Date2: OffsetDateTime | String (差距描述) | 任一為 null、格式不合法時拋 IllegalArgumentException |
| isTimeout | 判斷指定日期是否逾時 | Date: OffsetDateTime<br>TimeoutThreshold: int (ms) | boolean (true=逾時) | Date 為 null、TimeoutThreshold 為負、格式不合法時拋 IllegalArgumentException |
| epochMillisToOffsetDateTime | 時間戳轉 OffsetDateTime | timestamp: long<br>zoneId: String | OffsetDateTime | timestamp 為 null/空字串/格式不合法、zoneId 格式不合法時拋 IllegalArgumentException |
| formatOffsetDateTime | OffsetDateTime 轉格式化字串 | Date: OffsetDateTime<br>zoneId: String | String (yyyy-MM-dd HH:mm:ss) | Date 為 null、zoneId 格式不合法時拋 IllegalArgumentException |

# 4. 邊界條件與防呆機制 (Edge Cases & Defensive Programming)
- 任一 OffsetDateTime 輸入為 null，拋 IllegalArgumentException
- 任一 String 型態 zoneId 為 null 或非法，拋 IllegalArgumentException
- timestamp 為 null、空字串或非數字，拋 IllegalArgumentException
- TimeoutThreshold 為負數，拋 IllegalArgumentException
- Date1 > Date2 時，calculateMillisDiff 拋 IllegalArgumentException；describeDateDiff 則自動調整順序正確計算
- 格式化時，若 zoneId 不合法，拋 IllegalArgumentException

# 5. 非功能性需求與技術約束 (NFRs & Technical Constraints)
1. **無狀態與執行緒安全 (Stateless & Thread-Safety)**：嚴禁使用全域可變變數，保證在高併發環境下的絕對安全。
2. **零外部依賴 (Zero External Dependencies)**：保證測試可獨立運行，不依賴 Spring 容器或任何第三方重量級框架。
3. **高效能 (High Performance)**：若有正則表達式，必須宣告為 static final 預先編譯；避免不必要的物件實例化。

