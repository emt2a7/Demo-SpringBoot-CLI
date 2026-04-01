# 1. 元件定位與類別設計

- 類別名稱：`DateUtil`
- 所屬模組：底層共用工具 (Utility)
- 定位：提供純記憶體內之日期運算、格式轉換、逾時判斷等靜態工具方法，無狀態、無外部依賴。

# 2. 方法簽章設計

```java
/**
 * 計算兩個 OffsetDateTime 之間的毫秒差距。
 * @param date1 起始時間，必須早於 date2，否則拋出 IllegalArgumentException
 * @param date2 結束時間
 * @return 毫秒差距
 * @throws IllegalArgumentException 任一為 null 或 date1 > date2
 */
public static long calculateMillisDiff(OffsetDateTime date1, OffsetDateTime date2);

/**
 * 以中文描述兩個 OffsetDateTime 之間的差距。
 * @param date1 起始時間
 * @param date2 結束時間
 * @return 差距中文描述
 * @throws IllegalArgumentException 任一為 null
 */
public static String describeDateDiff(OffsetDateTime date1, OffsetDateTime date2);

/**
 * 判斷指定日期是否逾時 (以 Asia/Taipei 為預設時區)。
 * @param date 欲判斷之時間
 * @param timeoutThreshold 逾時門檻 (毫秒)
 * @return true=逾時，false=未逾時
 * @throws IllegalArgumentException date 為 null 或 timeoutThreshold 為負
 */
public static boolean isTimeout(OffsetDateTime date, int timeoutThreshold);

/**
 * 將 epoch millis 轉為 OffsetDateTime。
 * @param timestamp 毫秒級時間戳
 * @param zoneId 時區 ID
 * @return OffsetDateTime 物件
 * @throws IllegalArgumentException timestamp 非法或 zoneId 非法
 */
public static OffsetDateTime epochMillisToOffsetDateTime(long timestamp, String zoneId);

/**
 * 將 OffsetDateTime 依指定時區轉為 yyyy-MM-dd HH:mm:ss 格式字串。
 * @param date 欲格式化之時間
 * @param zoneId 時區 ID
 * @return 格式化字串
 * @throws IllegalArgumentException date 為 null 或 zoneId 非法
 */
public static String formatOffsetDateTime(OffsetDateTime date, String zoneId);
```

# 3. 技術與架構約束

- 僅允許 public static 方法，嚴禁任何實例方法或狀態。
- 嚴禁全域可變變數，必須完全無狀態 (Stateless) 並保證執行緒安全。
- 嚴禁依賴 Spring、第三方框架或外部資源，僅可使用 Java 21 標準函式庫。
- 所有方法皆須主動檢查輸入參數，遇到無效輸入時拋出 IllegalArgumentException。
- 若需正則表達式，必須宣告為 static final 並預先編譯。
- 僅允許 OffsetDateTime、String、long、int 等標準型別作為參數與回傳值。
- 嚴禁過度設計，僅聚焦於日期運算與格式轉換核心功能。
