---
description: 【底層共用工具(Util)】生成「任務規格書 (tasks.md)」
---

# 任務目標 (Mission)
請讀取工作區中的技術規格書 (`plan.md`) 與需求規格書 (`spec.md`)，制定一份以 TDD (測試驅動開發) 為核心的任務清單 (`tasks.md`)。

# 角色與紀律 (Role & Constraints)
- 角色：你是一位極度自律的「TDD 結對工程師 (Pair Programmer)」。
- 任務定位：為 Java 靜態工具類別規劃實作與單元測試步驟。
- 【警告】絕對禁止在任務清單中加入任何關於「修改 pom.xml」、「設定 Spring Boot」、「建立 Controller」等無關工具類別的任務。
- 【警告】嚴禁「先寫完全部實作，再寫測試」的瀑布流開發模式。必須強制套用微循環。

# 產出格式約束 (Checklist Constraints)
- 每項任務必須使用 `- [ ] T###` 開頭（例如 `- [ ] T001`）。
- 任務描述必須精確，並附上預計操作的實體檔案名稱（如 `DateUtil.java` 或 `DateUtilTest.java`）。

# 產出章節結構 (Strict TDD Workflow)
請嚴格依照以下階段來規劃任務清單：

## Phase 1: 基礎骨架建立 (Skeleton)
- [任務] 建立工具類別 (如 `XxxUtil.java`)，宣告為 `public final`，並實作 `private` 建構子拋出 `UnsupportedOperationException` (防止反射實例化)。
- [任務] 建立對應的 JUnit 5 測試類別 (如 `XxxUtilTest.java`)。

## Phase 2: 核心方法實作 (TDD Micro-Iterations)
（請針對 `plan.md` 中定義的每一個 public 方法，**逐一展開**以下 3 步驟的循環任務：）
1. **[測試先行 (Red)]**：在 `XxxUtilTest.java` 撰寫該方法的單元測試。必須包含「正常路徑 (Happy Path)」、「邊界條件 (Edge Cases，如 null、空值、0)」、以及「異常拋出驗證 (assertThrows)」。此時測試執行必為失敗。
2. **[業務實作 (Green)]**：在 `XxxUtil.java` 中實作該方法的最簡邏輯，使其剛好能通過測試。必須遵守無狀態與型別限制。
3. **[驗證與重構 (Refactor)]**：執行單元測試確保綠燈通過，並檢查程式碼是否符合 Clean Code 規範（消除重複、變數命名優化）。

## Phase 3: 品質與覆蓋率驗證 (Quality Assurance)
- [任務] 執行全類別代碼審查，確認絕對的「無狀態 (Stateless)」、無 Console 輸出日誌。
- [任務] 執行測試覆蓋率檢查 (Coverage Report)，確保該 Utility 類別達到 100% 的行覆蓋率與分支覆蓋率。

# **【最高級別警告】**：請絕對、嚴格遵守上述規定的「只有 3 個章節」限制！
