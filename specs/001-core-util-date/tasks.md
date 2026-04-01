# Skeleton

- [ ] T001 建立 `DateUtil` 類別骨架於 `src/main/java/org/example/util/DateUtil.java`
- [ ] T002 [P] 建立測試類別骨架於 `src/test/java/org/example/util/DateUtilTest.java`

# TDD Micro-Iterations

## calculateMillisDiff

- [ ] T003 [P] 為 `calculateMillisDiff` 撰寫失敗案例測試（null、date1 > date2）於 `src/test/java/org/example/util/DateUtilTest.java`
- [ ] T004 [P] 於 `DateUtil.java` 實作 `calculateMillisDiff` 方法（含參數檢查與例外處理）
- [ ] T005 [P] 驗證並重構 `calculateMillisDiff`，確保通過所有測試

## describeDateDiff

- [ ] T006 [P] 為 `describeDateDiff` 撰寫失敗案例測試（null、格式不合法）於 `src/test/java/org/example/util/DateUtilTest.java`
- [ ] T007 [P] 於 `DateUtil.java` 實作 `describeDateDiff` 方法（自動單位切換、順序自動調整）
- [ ] T008 [P] 驗證並重構 `describeDateDiff`，確保通過所有測試

## isTimeout

- [ ] T009 [P] 為 `isTimeout` 撰寫失敗案例測試（date 為 null、timeoutThreshold 為負）於 `src/test/java/org/example/util/DateUtilTest.java`
- [ ] T010 [P] 於 `DateUtil.java` 實作 `isTimeout` 方法（Asia/Taipei 預設時區）
- [ ] T011 [P] 驗證並重構 `isTimeout`，確保通過所有測試

## epochMillisToOffsetDateTime

- [ ] T012 [P] 為 `epochMillisToOffsetDateTime` 撰寫失敗案例測試（timestamp 非法、zoneId 非法）於 `src/test/java/org/example/util/DateUtilTest.java`
- [ ] T013 [P] 於 `DateUtil.java` 實作 `epochMillisToOffsetDateTime` 方法
- [ ] T014 [P] 驗證並重構 `epochMillisToOffsetDateTime`，確保通過所有測試

## formatOffsetDateTime

- [ ] T015 [P] 為 `formatOffsetDateTime` 撰寫失敗案例測試（date 為 null、zoneId 非法）於 `src/test/java/org/example/util/DateUtilTest.java`
- [ ] T016 [P] 於 `DateUtil.java` 實作 `formatOffsetDateTime` 方法
- [ ] T017 [P] 驗證並重構 `formatOffsetDateTime`，確保通過所有測試

# Quality Assurance

- [ ] T018 檢查 `DateUtil.java` 是否完全無狀態、無全域可變數，並移除多餘實作
- [ ] T019 檢查所有 public static 方法皆有完整單元測試覆蓋
- [ ] T020 檢查所有異常處理與邊界條件皆符合規格書
- [ ] T021 檢查無任何外部依賴（Spring/第三方框架）
- [ ] T022 檢查所有正則表達式（如有）皆宣告為 static final 並預先編譯
- [ ] T023 最終程式碼格式化與靜態分析於 `src/main/java/org/example/util/DateUtil.java`、`src/test/java/org/example/util/DateUtilTest.java`

