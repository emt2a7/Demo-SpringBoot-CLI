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
}
