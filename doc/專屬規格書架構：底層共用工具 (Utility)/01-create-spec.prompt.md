/my-specify-util
# 我需要一個日期工具類別，能夠提供以下功能：
    - 計算兩個指定日期之間的差距
    - 計算指定日期是否逾時(timeout)
    - 時間戳轉換

# 引用輸入路徑
    - 憲法規章：`.specify/memory/constitution.md`

# 指定輸出路徑
    - 【最高級別警告】：請絕對、嚴格遵守以下路徑！**禁止**參考任何終端機的輸出日誌、環境變數或目前所在的 Git 分支名稱。
    - 必須將產出的 `spec.md` 建立在：`specs/001-core-util-date/` 目錄下。

# 功能需求規格
  ## method 1: 計算兩個指定日期之間的差距(1)
    - 輸入
      - 日期 (Date1)：OffsetDateTime型態
      - 日期 (Date2)：OffsetDateTime型態
    - 輸出：long型態，計算 (Date1, Date2) 差距的豪秒數
    - 業務邏輯
      - 一律回傳毫秒數
    - 防呆機制
      - 若輸入任一者為 null，則拋出 IllegalArgumentException
      - 若輸入日期格式不合法，則拋出 IllegalArgumentException
      - 若輸入日期順序錯誤 (Date1 在 Date2 之後)，則拋出 IllegalArgumentException

  ## method 2: 計算兩個指定日期之間的差距(2)
    - 輸入
      - 日期 (Date1)：OffsetDateTime型態
      - 日期 (Date2)：OffsetDateTime型態
    - 輸出：表示兩個日期 (Date1, Date2) 之間差距的描述
      - String型態
    - 業務邏輯
      - 呼叫「計算兩個指定日期之間的差距(1)」取得毫秒數
        - 輸入：兩個日期型態 (Date1, Date2)
      - 若小於1秒，則回傳「X毫秒」
      - 若小於1分鐘，則回傳「X秒」
      - 若小於1小時，則回傳「X分Y秒」
      - 若小於1天，則回傳「X時Y分Z秒」
      - 若大於等於1天，則回傳「X天Y時Z分W秒」
      - 其中 X、Y、Z、W 為對應的數字，且當某個單位為 0 時，則不顯示該單位（例如：若差距為 90秒，則回傳「1分30秒」，而非「0時1分30秒」）
    - 防呆機制
      - 若輸入任一者為 null，則拋出 IllegalArgumentException
      - 若輸入日期格式不合法，則拋出 IllegalArgumentException
      - 若輸入日期順序 (Date1 在 Date2 之後) 錯誤，也要能夠正確計算差距，並回傳正確的文字描述
    - 測試與品質約束 (Testing & QA Constraints)
      - 必須明文規定測試案例涵蓋「單一單位 (僅有秒)」、「跨單位 (有天、有分，但無小時)」、「剛好進位 (剛好60秒、剛好24小時)」等所有 `if` 邏輯分支。

  ## method 3: 計算指定日期是否逾時(timeout)
    - 輸入
      - 日期 (Date)：OffsetDateTime型態
      - 逾時閾值 (Timeout Threshold)：int型態，單位為毫秒
    - 輸出：表示是否逾時
      - Boolean型態 (true/false)；true 表示逾時，false 表示未逾時
    - 業務邏輯
      - 計算公式：當下時間 (Now) - 日期 (Date) > 逾時閾值 (Timeout Threshold)
      - 呼叫「計算兩個指定日期之間的差距(1)」取得差距的毫秒數
        - 輸入：日期 (Date)、當下時間 (Now)
        - 其中「當下時間 (Now)」為系統當前的日期時間，且必須明確綁定為台灣時區 (Asia/Taipei)
      - 若毫秒數大於逾時閾值，則回傳 true；否則回傳 false
    - 防呆機制
      - 若輸入日期為 null，則拋出 IllegalArgumentException
      - 若輸入逾時閾值為負數，則拋出 IllegalArgumentException
      - 若輸入日期格式不合法，則拋出 IllegalArgumentException

  ## method 4: 時間戳轉換 (Epoch Millis to OffsetDateTime)
    - 輸入
      - 時間戳 (timestamp)：long型態，範例：1774834908295
      - 時區 (ZoneId)：String型態，常見的有台灣時區跟 UTC，範例："Asia/Taipei"、"UTC"
    - 輸出
      - 格式化日期 (Formatted OffsetDateTime)：OffsetDateTime型態，範例：2026-03-30T09:41:48.295475900+08:00
    - 業務邏輯
        - 將時區 (ZoneId) 的時間戳 (timestamp) 轉換成「格式化日期 (Formatted OffsetDateTime)」
    - 防呆機制
      - 若輸入時間戳為 null 或空字串，則拋出 IllegalArgumentException
      - 若輸入時間戳格式不合法（非數字字串），則拋出 IllegalArgumentException
      - 若輸入時區格式不合法，則拋出 IllegalArgumentException

  ## method 5: 日期格式化 (OffsetDateTime to Formatted String)
    - 輸入
      - 日期 (Date)：OffsetDateTime型態，範例：2026-03-30T09:41:48.295475900+08:00
      - 時區 (ZoneId)：String型態，常見的有台灣時區跟 UTC，範例："Asia/Taipei"、"UTC"
    - 輸出
      - 格式化日期字串 (Formatted OffsetDateTime String)：String型態
      - 格式：yyyy-MM-dd HH:mm:ss，例如："2026-03-30 09:41:48"
    - 業務邏輯
        - 將時區 (ZoneId) 的日期 (Date) 轉換為「格式化日期字串 (Formatted OffsetDateTime String)」
    - 防呆機制
      - 若輸入日期為 null，則拋出 IllegalArgumentException
      - 若輸入時區格式不合法，則拋出 IllegalArgumentException

# 功能設計規格
    - package：org.example.framework.util
    - class：DateUtil

# 測試與品質約束 (Testing & QA Constraints)
    為了確保 JaCoCo 達到 100% 分支涵蓋率 (Branch Coverage)，請在產出規格與任務清單時，嚴格遵守以下測試約束：
    1. **異常捕捉可達性**：所有要求拋出 `IllegalArgumentException` 的防呆機制，都必須對應一個明確的 `assertThrows` 測試情境。若防呆條件在 Java 強型別機制下無法被觸發（例如已經是有效物件），請由工具類別自行處理字串解析 (Parse) 以確保異常可被捕捉。

# 補充事項
  - 無



