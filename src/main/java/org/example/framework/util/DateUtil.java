package org.example.framework.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具類，提供靜態方法進行日期運算。
 * 僅允許 public static 方法，無任何狀態。
 */
public class DateUtil {
//    public static void main(String[] args) {
//        OffsetDateTime d1 = OffsetDateTime.parse("2026-03-30T00:00:00+08:00");
//        OffsetDateTime d2 = OffsetDateTime.parse("2026-03-31T00:01:00+08:00");
//        String desc = DateUtil.describeDateDiff(d2, d1); // 測試自動調整順序
//        System.out.println("describeDateDiff 輸出: " + desc);
//    }
    // 加入私有建構子，防止實例化
    private DateUtil() {
        throw new UnsupportedOperationException("工具類不可實例化");
    }

    // T003~T005: 計算毫秒差
    /**
     * 計算兩個 OffsetDateTime 之間的毫秒差
     * @param date1 起始時間
     * @param date2 結束時間
     * @return 毫秒差
     * @throws IllegalArgumentException 參數為 null 或 date1 > date2
     */
    public static long calculateMillisDiff(OffsetDateTime date1, OffsetDateTime date2) {
        if (date1 == null || date2 == null) throw new IllegalArgumentException("OffsetDateTime 不能為 null");
        if (date1.isAfter(date2)) throw new IllegalArgumentException("起始時間不能晚於結束時間");
        return Duration.between(date1, date2).toMillis();
    }

    /**
     * 【繁體中文別名】計算兩個 OffsetDateTime 之間的毫秒差
     * @param date1 起始時間
     * @param date2 結束時間
     * @return 毫秒差
     * @throws IllegalArgumentException 參數為 null 或 date1 > date2
     */
    public static long 計算相差多少時間(OffsetDateTime date1, OffsetDateTime date2) {
        return calculateMillisDiff(date1, date2);
    }

    // T006~T008: 產生人類可讀的時間差描述
    /**
     * 產生人類可讀的時間差描述
     * @param date1 起始時間
     * @param date2 結束時間
     * @return 例如 "1天2小時3分4秒"
     * @throws IllegalArgumentException 參數為 null
     */
    public static String describeDateDiff(OffsetDateTime date1, OffsetDateTime date2) {
        if (date1 == null || date2 == null) throw new IllegalArgumentException("OffsetDateTime 不能為 null");
        if (date1.isAfter(date2)) { OffsetDateTime tmp = date1; date1 = date2; date2 = tmp; }
        Duration d = Duration.between(date1, date2);
        long days = d.toDays();
        long hours = d.minusDays(days).toHours();
        long minutes = d.minusDays(days).minusHours(hours).toMinutes();
        long seconds = d.minusDays(days).minusHours(hours).minusMinutes(minutes).getSeconds();
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小時");
        if (minutes > 0) sb.append(minutes).append("分");
        if (seconds > 0) sb.append(seconds).append("秒");
        if (sb.isEmpty()) sb.append("0秒");
        return sb.toString();
    }

    // T009~T011: 判斷是否逾時
    /**
     * 判斷指定時間是否已逾時
     * @param date 欲檢查的時間
     * @param timeoutThreshold 逾時秒數
     * @return true: 已逾時
     * @throws IllegalArgumentException 參數為 null 或 timeoutThreshold < 0
     */
    public static boolean isTimeout(OffsetDateTime date, int timeoutThreshold) {
        if (date == null) throw new IllegalArgumentException("OffsetDateTime 不能為 null");
        if (timeoutThreshold < 0) throw new IllegalArgumentException("timeoutThreshold 不能為負");
        return date.isBefore(OffsetDateTime.now().minusSeconds(timeoutThreshold));
    }

    // T012~T014: epochMillis 轉 OffsetDateTime
    /**
     * 將 epoch 毫秒轉為 OffsetDateTime
     * @param timestamp 毫秒
     * @param zoneId 時區
     * @return OffsetDateTime
     * @throws IllegalArgumentException zoneId 為 null/空/非法，timestamp < 0
     */
    public static OffsetDateTime epochMillisToOffsetDateTime(long timestamp, String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) throw new IllegalArgumentException("zoneId 不能為空");
        if (timestamp < 0) throw new IllegalArgumentException("timestamp 不能為負");
        try {
            ZoneId zid = ZoneId.of(zoneId);
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zid);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("zoneId 非法", e);
        }
    }

    // T015~T017: OffsetDateTime 格式化
    /**
     * 將 OffsetDateTime 依指定時區格式化為字串
     * @param date 欲格式化的時間
     * @param zoneId 時區
     * @return 格式化字串
     * @throws IllegalArgumentException 參數為 null/空/非法
     */
    public static String formatOffsetDateTime(OffsetDateTime date, String zoneId) {
        if (date == null) throw new IllegalArgumentException("OffsetDateTime 不能為 null");
        if (zoneId == null || zoneId.isEmpty()) throw new IllegalArgumentException("zoneId 不能為空");
        try {
            ZoneId zid = ZoneId.of(zoneId);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zid);
            return fmt.format(date);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("zoneId 非法", e);
        }
    }
}
