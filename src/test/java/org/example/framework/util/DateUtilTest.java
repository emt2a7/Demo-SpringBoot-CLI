package org.example.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {
    @Test
    @DisplayName("品質驗證：不可實例化 DateUtil")
    void testConstructorIsPrivate() throws Exception {
        java.lang.reflect.Constructor<DateUtil> constructor = DateUtil.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()), "建構子必須是 private");
        constructor.setAccessible(true);
        java.lang.reflect.InvocationTargetException exception = assertThrows(
                java.lang.reflect.InvocationTargetException.class,
                constructor::newInstance
        );
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    @DisplayName("calculateMillisDiff：傳入 null 參數應拋出 IllegalArgumentException")
    void testCalculateMillisDiff_NullInput() {
        OffsetDateTime now = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> DateUtil.calculateMillisDiff(null, now), "date1 為 null 應拋出異常");
        assertThrows(IllegalArgumentException.class, () -> DateUtil.calculateMillisDiff(now, null), "date2 為 null 應拋出異常");
    }

    @Test
    @DisplayName("calculateMillisDiff：date1 晚於 date2 應拋出 IllegalArgumentException")
    void testCalculateMillisDiff_Date1AfterDate2() {
        OffsetDateTime date1 = OffsetDateTime.of(2024, 3, 30, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime date2 = OffsetDateTime.of(2024, 3, 29, 12, 0, 0, 0, ZoneOffset.UTC);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.calculateMillisDiff(date1, date2), "date1 晚於 date2 應拋出異常");
    }

    @Test
    @DisplayName("計算相差多少時間：傳入 null 參數應拋出 IllegalArgumentException")
    void test計算相差多少時間_NullInput() {
        OffsetDateTime now = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> DateUtil.計算相差多少時間(null, now), "date1 為 null 應拋出異常");
        assertThrows(IllegalArgumentException.class, () -> DateUtil.計算相差多少時間(now, null), "date2 為 null 應拋出異常");
    }

    @Test
    @DisplayName("計算相差多少時間：date1 晚於 date2 應拋出 IllegalArgumentException")
    void test計算相差多少時間_Date1AfterDate2() {
        OffsetDateTime date1 = OffsetDateTime.of(2024, 3, 30, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime date2 = OffsetDateTime.of(2024, 3, 29, 12, 0, 0, 0, ZoneOffset.UTC);
        assertThrows(IllegalArgumentException.class, () -> DateUtil.計算相差多少時間(date1, date2), "date1 晚於 date2 應拋出異常");
    }

    @Test
    @DisplayName("計算相差多少時間：正常情境")
    void test計算相差多少時間_valid() {
        OffsetDateTime d1 = OffsetDateTime.parse("2026-03-30T00:00:00+08:00");
        OffsetDateTime d2 = OffsetDateTime.parse("2026-03-31T00:00:00+08:00");
        assertEquals(86_400_000L, DateUtil.計算相差多少時間(d1, d2), "應正確計算毫秒差");
    }

    @Test
    @DisplayName("describeDateDiff：傳入 null 參數應拋出 IllegalArgumentException")
    void testDescribeDateDiff_NullInput() {
        OffsetDateTime now = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> DateUtil.describeDateDiff(null, now), "date1 為 null 應拋出異常");
        assertThrows(IllegalArgumentException.class, () -> DateUtil.describeDateDiff(now, null), "date2 為 null 應拋出異常");
    }

    @Test
    @DisplayName("describeDateDiff：date1 晚於 date2 仍能正確描述差距")
    void testDescribeDateDiff_Date1AfterDate2() {
        OffsetDateTime date1 = OffsetDateTime.of(2024, 3, 30, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime date2 = OffsetDateTime.of(2024, 3, 29, 12, 0, 0, 0, ZoneOffset.UTC);
        String result = DateUtil.describeDateDiff(date1, date2);
        assertTrue(result.contains("1天"), "應正確描述 1 天差距");
    }

    @Test
    @DisplayName("describeDateDiff：時間差距為 0 秒 (測試 sb.isEmpty 分支)")
    void testDescribeDateDiff_Zero() {
        OffsetDateTime now = OffsetDateTime.now();
        // 傳入兩個相同的時間
        String desc = DateUtil.describeDateDiff(now, now);
        assertEquals("0秒", desc, "時間相同時應回傳 0秒");
    }

    @Test
    @DisplayName("isTimeout：date 為 null 應拋出 IllegalArgumentException")
    void testIsTimeout_DateNull() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.isTimeout(null, 1000), "date 為 null 應拋出異常");
    }

    @Test
    @DisplayName("isTimeout：timeoutThreshold 為負數應拋出 IllegalArgumentException")
    void testIsTimeout_ThresholdNegative() {
        OffsetDateTime now = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> DateUtil.isTimeout(now, -1), "timeoutThreshold 為負數應拋出異常");
    }

    @Test
    @DisplayName("epochMillisToOffsetDateTime：timestamp 為負數應拋出 IllegalArgumentException")
    void testEpochMillisToOffsetDateTime_TimestampNegative() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.epochMillisToOffsetDateTime(-1L, "Asia/Taipei"), "timestamp 為負數應拋出異常");
    }

    @Test
    @DisplayName("epochMillisToOffsetDateTime：zoneId 非法應拋出 IllegalArgumentException")
    void testEpochMillisToOffsetDateTime_ZoneIdInvalid() {
        long now = System.currentTimeMillis();
        assertThrows(IllegalArgumentException.class, () -> DateUtil.epochMillisToOffsetDateTime(now, null), "zoneId 為 null 應拋出異常");
        assertThrows(IllegalArgumentException.class, () -> DateUtil.epochMillisToOffsetDateTime(now, ""), "zoneId 為空字串應拋出異常");
        assertThrows(IllegalArgumentException.class, () -> DateUtil.epochMillisToOffsetDateTime(now, "INVALID_ZONE"), "zoneId 非法應拋出異常");
    }

    @Test
    @DisplayName("formatOffsetDateTime：date 為 null 應拋出 IllegalArgumentException")
    void testFormatOffsetDateTime_DateNull() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatOffsetDateTime(null, "Asia/Taipei"), "date 為 null 應拋出異常");
    }

    @Test
    @DisplayName("formatOffsetDateTime：zoneId 非法應拋出 IllegalArgumentException")
    void testFormatOffsetDateTime_ZoneIdInvalid() {
        OffsetDateTime now = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatOffsetDateTime(now, null), "zoneId 為 null 應拋出異常");
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatOffsetDateTime(now, ""), "zoneId 為空字串應拋出異常");
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatOffsetDateTime(now, "INVALID_ZONE"), "zoneId 非法應拋出異常");
    }

    // T003: calculateMillisDiff 失敗案例測試
    @Test
    void testCalculateMillisDiff_invalidArgs() {
        // null 參數
        assertThrows(IllegalArgumentException.class, () -> DateUtil.calculateMillisDiff(null, OffsetDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.calculateMillisDiff(OffsetDateTime.now(), null));
        // date1 > date2
        OffsetDateTime d1 = OffsetDateTime.now().plusDays(1);
        OffsetDateTime d2 = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> DateUtil.calculateMillisDiff(d1, d2));
    }

    // T004: calculateMillisDiff 正常案例
    @Test
    void testCalculateMillisDiff_valid() {
        OffsetDateTime d1 = OffsetDateTime.parse("2026-03-30T00:00:00+08:00");
        OffsetDateTime d2 = OffsetDateTime.parse("2026-03-31T00:00:00+08:00");
        assertEquals(86_400_000L, DateUtil.calculateMillisDiff(d1, d2));
    }

    // T006: describeDateDiff 失敗案例
    @Test
    void testDescribeDateDiff_invalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.describeDateDiff(null, OffsetDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.describeDateDiff(OffsetDateTime.now(), null));
    }

    // T007: describeDateDiff 正常案例（自動調整順序）
    @Test
    void testDescribeDateDiff_valid() {
        OffsetDateTime d1 = OffsetDateTime.parse("2026-03-30T00:00:00+08:00");
        OffsetDateTime d2 = OffsetDateTime.parse("2026-03-31T00:01:00+08:00");
        String desc = DateUtil.describeDateDiff(d2, d1); // 測試自動調整順序
        System.out.println("describeDateDiff 輸出: " + desc);
        assertTrue(desc.contains("1天"), "應包含 1天");

        d1 = OffsetDateTime.parse("2026-03-30T00:00:00+08:00");
        d2 = OffsetDateTime.parse("2026-03-30T01:02:03+08:00");
        desc = DateUtil.describeDateDiff(d1, d2); // 測試自動調整順序
        System.out.println("describeDateDiff 輸出: " + desc);
        assertTrue(desc.contains("1小時"), "應包含 1小時");

        d1 = OffsetDateTime.parse("2026-03-30T00:00:00+08:00");
        d2 = OffsetDateTime.parse("2026-03-30T00:02:02+08:00");
        desc = DateUtil.describeDateDiff(d1, d2); // 測試自動調整順序
        System.out.println("describeDateDiff 輸出: " + desc);
        assertTrue(desc.contains("2分"), "應包含 2分");

        d1 = OffsetDateTime.parse("2026-03-30T00:00:00+08:00");
        d2 = OffsetDateTime.parse("2026-03-30T00:00:03+08:00");
        desc = DateUtil.describeDateDiff(d2, d1); // 測試自動調整順序
        System.out.println("describeDateDiff 輸出: " + desc);
        assertTrue(desc.contains("3秒"), "應包含 3秒");
    }

    // T009: isTimeout 失敗案例
    @Test
    void testIsTimeout_invalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.isTimeout(null, 10));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.isTimeout(OffsetDateTime.now(), -1));
    }

    // T010: isTimeout 正常案例
    @Test
    void testIsTimeout_valid() {
        OffsetDateTime now = OffsetDateTime.now();
        assertFalse(DateUtil.isTimeout(now.plusSeconds(10), 5));
        assertTrue(DateUtil.isTimeout(now.minusSeconds(10), 5));
    }

    // T012: epochMillisToOffsetDateTime 失敗案例
    @Test
    void testEpochMillisToOffsetDateTime_invalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.epochMillisToOffsetDateTime(0L, null));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.epochMillisToOffsetDateTime(0L, ""));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.epochMillisToOffsetDateTime(-1L, "Asia/Taipei"));
    }

    // T013: epochMillisToOffsetDateTime 正常案例
    @Test
    void testEpochMillisToOffsetDateTime_valid() {
        long ts = 1777777777000L;
        OffsetDateTime odt = DateUtil.epochMillisToOffsetDateTime(ts, "Asia/Taipei");
        assertEquals(ts, odt.toInstant().toEpochMilli());
    }

    // T015: formatOffsetDateTime 失敗案例
    @Test
    void testFormatOffsetDateTime_invalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatOffsetDateTime(null, "Asia/Taipei"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatOffsetDateTime(OffsetDateTime.now(), null));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.formatOffsetDateTime(OffsetDateTime.now(), ""));
    }

    // T016: formatOffsetDateTime 正常案例
    @Test
    void testFormatOffsetDateTime_valid() {
        OffsetDateTime odt = OffsetDateTime.parse("2026-03-30T12:34:56+08:00");
        String str = DateUtil.formatOffsetDateTime(odt, "Asia/Taipei");
        assertTrue(str.contains("2026") && str.contains("12:34:56"));
    }
}

