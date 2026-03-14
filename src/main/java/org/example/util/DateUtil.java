package org.example.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

/**
 * 日期共用元件
 * 
 * @author achi
 *
 */
public class DateUtil {

	// 定義輸出日期時間格式
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // 定義時區：由於您沒有指定時區，我們通常假設為 UTC 或系統預設時區。
    // 在金融應用中，使用 UTC (協調世界時) 是最安全的做法。
    public static final ZoneId ZONE = ZoneId.of("Asia/Taipei"); // 假設您位於台灣時區 (CST, UTC+8)
    
    // 使用一個 AtomicLong 來儲存上次使用的 Nonce，確保在多執行緒環境下仍能嚴格遞增
    private static final AtomicLong lastNonce = new AtomicLong(0);
    
    public static void main(String[] args) throws Exception {
    	//long 查詢時間戳_起 = DateUtil.找出Ｎ天前４小時時區的時間(7);
    	//long 查詢時間戳_迄 = DateUtil.找出最近４小時時區的時間();
    	
    	//System.out.println("查詢時間戳_起：" + 查詢時間戳_起);
    	//System.out.println("查詢時間戳_迄：" + 查詢時間戳_迄);
    	
    	BigDecimal a = new BigDecimal(0.25);
    	System.out.println("百分比格式化：" + DateUtil.百分比格式化(a));
    }
    
    public static long 找出最近４小時時區的時間() {
    	int 幾天前 = 0;
    	int 多少時區 = 4;
    	return findLastCompletedTimestamp(LocalTime.now(), 幾天前, 多少時區, ZONE);
    }
    
    public static long 找出Ｎ天前４小時時區的時間(int 幾天前) {
    	int 多少時區 = 4;
    	return findLastCompletedTimestamp(LocalTime.now(), 幾天前, 多少時區, ZONE);
    }
    
    public static int 計算Ｎ天４小時的周期數(int day) {
    	int result = 0;
    	
    	for (int i = 1 ; i <= day ; i++) {
    		for (int j = 1 ; j <= 6 ; j++) {
    			result = result + 4;
    		}
    	}
    	return result;
    }
    
    /**
     * 計算相差多少時間 (輸出格式：N天N小時N分N秒N毫秒)
     * 
     * 0.5 秒 → 500毫秒
     * 0.001 秒 → 1毫秒
     * 0 秒 → 0毫秒
     * 1 秒 → 1秒
     * 1.5 秒 → 1秒
     * 1 天 2 小時 3 分 4.9 秒 → 1天2小時3分4秒
     * 5 分鐘 → 5分
     * 
     * @param startDateTime
     * @param endDateTime
     * @return
     */
    public static String 計算相差多少時間(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        
        // 1. 計算兩個時間點之間的總毫秒數差異 (確保結果為正)
        Duration duration = Duration.between(startDateTime, endDateTime).abs();
        long totalMilliseconds = duration.toMillis();
        
        final long MILLIS_PER_SECOND = 1000L;

        // 2. 處理邊界情況：總時長為 0
        if (totalMilliseconds == 0) {
            return "0毫秒"; // 總時長為 0，精確輸出
        }

        // 3. 【修正邏輯】判斷是否小於 1 秒
        if (totalMilliseconds < MILLIS_PER_SECOND) {
            // **0秒以下時，才顯示毫秒**
            return totalMilliseconds + "毫秒";
        }

        // 4. 處理 1 秒以上的情況：只計算到秒，並忽略不足 1 秒的毫秒部分
        // 0秒以上時，不顯示毫秒
        long totalSeconds = totalMilliseconds / MILLIS_PER_SECOND; // 相當於向下取整到秒

        // 5. 轉換為 天/小時/分/秒 (基於 totalSeconds)
        
        // 常數定義
        final long SECONDS_PER_MINUTE = 60L;
        final long SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
        final long SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

        long remainingSeconds = totalSeconds;

        // 計算天數
        long days = remainingSeconds / SECONDS_PER_DAY;
        remainingSeconds %= SECONDS_PER_DAY;

        // 計算小時數
        long hours = remainingSeconds / SECONDS_PER_HOUR;
        remainingSeconds %= SECONDS_PER_HOUR;

        // 計算分鐘數
        long minutes = remainingSeconds / SECONDS_PER_MINUTE;
        
        // 剩下的即為秒數
        long seconds = remainingSeconds % SECONDS_PER_MINUTE;

        // 6. 根據「非零」規則格式化字串 (只到秒)
        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小時");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分");
        }
        if (seconds > 0) {
            sb.append(seconds).append("秒");
        }
        
        // 由於我們已經在步驟 3 處理了 < 1 秒的情況，且在步驟 4 確保 totalSeconds >= 1，
        // 因此 sb.length() 在這裡一定大於 0。

        return sb.toString();
    }
    
    public static long 計算相差秒數(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        Duration duration = Duration.between(startDateTime, endDateTime).abs();
        return duration.getSeconds();
    }
    
    public static boolean 是否超時(OffsetDateTime offsetDateTime, int 超時秒數) throws Exception {
		boolean 是否超時 = false;
		
	    if (DateUtil.計算相差秒數(offsetDateTime, OffsetDateTime.now()) > 超時秒數) {
	    	是否超時 = true;
	    }
		return 是否超時;
	}
    
    /**
     * 將 String 格式的毫秒時間戳 (如 Bitfinex 的 MTS_CREATED) 轉換為 OffsetDateTime。
     * * @param timestampString String 格式的毫秒時間戳 (例如: "1761293879000")
     * @return 轉換後的 OffsetDateTime 物件 (包含 DEFAULT_ZONE 時區資訊)
     * @throws NumberFormatException 如果傳入的字串無法解析為 Long
     */
    public static OffsetDateTime 時間戳轉換(String timestampString) throws NumberFormatException, NullPointerException {
        
        if (timestampString == null || timestampString.trim().isEmpty()) {
            // 處理 null 或空字串，可以選擇回傳 null 或拋出 IllegalArgumentException
            throw new NullPointerException("時間戳字串不能為空值或空字串。");
        }
        
        // 1. 將 String 轉換為 Long (毫秒數)
        long timestampMillis = Long.parseLong(timestampString.trim());
        
        // 2. 使用 Long 毫秒數創建 Instant (UTC 時刻)
        Instant instant = Instant.ofEpochMilli(timestampMillis);
        
        // 3. 結合 Instant 和 ZoneId 轉換為 OffsetDateTime
        // 這一步將 UTC 時刻應用到指定的時區，得到該時區下的 OffsetDateTime
        return OffsetDateTime.ofInstant(instant, ZONE);
    }
    
    /**
     * 將毫秒級時間戳轉換為指定格式的日期時間字串。
     * * @param timestampMs 毫秒級時間戳 (例如: 1761525646000)
     * @return 格式化的日期時間字串 (yyyy/MM/dd HH:mm:ss)
     */
    public static String 時間戳轉日期格式(long timestampMs) {
        
        // 1. 將毫秒級時間戳轉換為 Instant (時間線上的瞬間)
        Instant instant = Instant.ofEpochMilli(timestampMs);
        
        // 2. 將 Instant 轉換為帶有目標時區的 LocalDateTime
        // (這一步會自動處理時區轉換)
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZONE);
        
        // 3. 使用格式化器輸出字串 (這會忽略毫秒/奈秒部分)
        return localDateTime.format(FORMATTER);
    }
    
    public static String 時間戳轉日期毫秒格式(long timestampMs) {
        
        // 1. 將毫秒級時間戳轉換為 Instant (時間線上的瞬間)
        Instant instant = Instant.ofEpochMilli(timestampMs);
        
        // 2. 將 Instant 轉換為帶有目標時區的 LocalDateTime
        // (這一步會自動處理時區轉換)
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZONE);
        
        // 3. 使用格式化器輸出字串 (這會忽略毫秒/奈秒部分)
        return localDateTime.format(FORMATTER2);
    }
    
    public static String 千分位格式化(BigDecimal value) {
    	return 千分位格式化(value, 2, 8);
    }
    
    public static String 百分比格式化(BigDecimal value) {
    	return 百分比格式化(value, 0);
    }
    
    public static String 百分比格式化(BigDecimal value, int scale) {
    	return 百分比格式化(value, scale, RoundingMode.HALF_UP); // HALF_UP: 四捨五入
    }
    
    public static String 百分比格式化(BigDecimal value, int scale, RoundingMode roundingMode) {
    	if (value == null) {
    		return " 0%";
    	}
    	String val = (value.multiply(new BigDecimal(100))).setScale(scale, roundingMode).toPlainString();
    	if (val.length() == 1) {
    		val = " " + val;
    	}
    	return val + "%";
    }

    /**
     * 將 BigDecimal 格式化為包含千分位分隔符的字串。
     * @param value 要格式化的 BigDecimal 數值。
     * @param minDecimalPlaces 最小小數位數 (例如 2)。
     * @param maxDecimalPlaces 最大小數位數 (例如 8)。
     * @return 包含千分位的格式化字串。
     */
    public static String 千分位格式化(BigDecimal value, int minDecimalPlaces, int maxDecimalPlaces) {
        
    	if (value == null) {
    		return "0";
    	}
    	
        // 1. 定義格式模式
        // "#,##0" 表示整數部分使用千分位，且至少顯示一位數字 (0)
        // "." 後的 "#" 表示小數點可選，會根據實際數值顯示
        String pattern = "#,##0." + "#".repeat(maxDecimalPlaces);
        
        // 2. 創建 DecimalFormatSymbols 以確保使用正確的地區設定符號
        // 這裡使用台灣/美國常用的符號：點(.)作為小數點，逗號(,)作為千分位
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); // 使用 US locale 的符號
        
        // 3. 創建 DecimalFormat
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        
        // 4. 設定小數位數的範圍，確保精度保持
        decimalFormat.setMinimumFractionDigits(minDecimalPlaces);
        decimalFormat.setMaximumFractionDigits(maxDecimalPlaces);
        
        // 5. 使用 format() 方法進行格式化
        return decimalFormat.format(value);
    }
    
	/**
     * 【最終修正】生成 Bitfinex API 所需的單調遞增 Nonce
     * 將毫秒時間戳提升至微秒數量級 (16 位數) 以滿足長度要求。
     * @return 遞增的 Nonce 字串 (至少 16 位數)
     */
    public static String generateNonce() {
        long newNonce;
        
        while (true) {
            // 1. 獲取當前毫秒時間戳 (13 位)
            long currentMillis = System.currentTimeMillis(); 
            
            // 2. 提升至微秒級別基礎值 (乘以 1000)，結果約為 16 位數
            long microsecondBase = currentMillis * 1000; 
            
            long last = lastNonce.get();

            // 3. 核心邏輯：新的 Nonce 必須是：
            //    a) 不小於微秒級別基礎值 (保持大數和時間同步)
            //    b) 至少比上一個 Nonce 大 1 (保證單調遞增，解決同毫秒/微秒問題)
            // 
            // 備註：如果上次的 Nonce 已經很大 (例如 15 或 16 位數)，
            // 且本次仍在同一毫秒內，它會執行 last + 1。
            newNonce = Math.max(microsecondBase, last + 1);

            // 4. 使用 CAS (Compare-and-Swap) 原子性更新 Nonce
            if (lastNonce.compareAndSet(last, newNonce)) {
                // 更新成功，跳出迴圈
                break; 
            }
            // 失敗時，迴圈將重試，重新讀取新的 lastNonc e值
        }
        
        // 5. 返回 String 格式的 Nonce
        return String.valueOf(newNonce);
    }

	/**
	 * 計算距離當前時間 (往前回溯 N 天) 最近的、已結束的完整區間起始時間，
	 * 並將其轉換為 Unix 時間戳 (毫秒)。
	 *
	 * @param currentTime    作為計算基準的當前時間 (LocalTime)
	 * @param daysAgo        需要回溯的天數 (例如 1 代表昨天)
	 * @param periodHours    區間長度 (例如 4 小時)
	 * @param zoneId         要使用的時區 (ZoneId)
	 * @return 完整區間的起始時間的 Unix 時間戳 (毫秒)
	 */
	private static long findLastCompletedTimestamp(
	        LocalTime currentTime, 
	        int daysAgo, 
	        int periodHours, 
	        ZoneId zoneId) {
	    
	    // 1. 確定計算的日期基準點
	    // 取得 "當前日期"，並回溯指定的 daysAgo 天數。
	    LocalDate targetDate = LocalDate.now(zoneId).minusDays(daysAgo);
	
	    // 2. 結合日期和時間，創建一個完整的時間基準點 (ZonedDateTime)
	    // 例如：如果當前日期是 2025/10/21，daysAgo=1，則基準日期是 2025/10/20。
	    // 基準時間點 ZonedDateTime = 2025/10/20 17:26:00
	    ZonedDateTime currentZonedTime = ZonedDateTime.of(targetDate, currentTime, zoneId);
	    
	    // 3. 計算 ZonedDateTime 對應的 Unix 毫秒數
	    long currentTimestamp = currentZonedTime.toInstant().toEpochMilli();
	    
	    // 4. 計算區間長度的毫秒數
	    long periodMillis = (long) periodHours * 60 * 60 * 1000;
	
	    // 5. 計算自 Epoch Time (1970-01-01) 以來，當前時間點的溢出時間
	    // remainderMillis = 當前時間戳 % 區間毫秒數
	    // 這是找出當前時間在 4 小時區間中已經過了多少時間
	    long remainderMillis = currentTimestamp % periodMillis;
	    
	    // 6. 計算最近一次完整區間的結束時間 (即下一個區間的起始時間)
	    // 由於我們想要的是【已結束】的完整區間，我們必須減去這個溢出的部分
	    // 例如：當前時間戳是 16:26:00，區間是 4 小時，溢出是 26 分鐘
	    // 下一個區間的起始點（即當前區間的結束點）= 16:00:00 (如果以 4 小時區間劃分)
	    long nextStartTimeTimestamp = currentTimestamp - remainderMillis;
	    
	    // 7. 找出最近一次【完整結束】的區間的開始時間 (即上一個區間的起始點)
	    // 最近一次完整結束的區間的開始時間 = 當前區間起始時間 - 區間長度 (4小時)
	    long lastStartTimeTimestamp = nextStartTimeTimestamp - periodMillis;
	    
	    return lastStartTimeTimestamp;
	}
}
