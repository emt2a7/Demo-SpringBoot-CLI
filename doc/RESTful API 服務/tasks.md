# 產出格式約束 (Checklist Constraints)
- 每項任務必須使用 `- [ ] T###` 開頭（例如 `- [ ] T001`）。
- 任務描述必須精確，並附上預計操作的檔案路徑。
- 【警告】嚴禁「先寫完全部實作，再寫測試」的瀑布流開發模式。

# 產出章節結構 (TDD 施工順序原則)
請嚴格依照 Spring Boot 的分層架構，由底層到外層規劃任務：

## Phase 1: 基礎骨架建立 (Skeleton & Domain)
- [任務] 建立 DTOs、例外類別 (Exceptions) 與 JPA Entity 類別。
- [任務] 建立 Spring Data JPA Repository 介面。

## Phase 2: 資料層實作與測試 (Repository Layer)
- [任務] 建立 Repository 測試類別 (使用 `@DataJpaTest`)。
- [任務] 實作自訂的 JPQL 或 Query Method 並確保測試通過。

## Phase 3: 業務層實作與測試 (Service Layer)
（請針對每一個 Service 方法，嚴格展開以下微循環）
- [任務] 建立 Service 測試類別 (使用 `@ExtendWith(MockitoExtension.class)` 進行 Mock 測試)。
- [任務] 針對特定方法撰寫失敗的單元測試 (Red)。
- [任務] 實作該 Service 方法的業務邏輯 (Green)。
- [任務] 執行單元測試指令驗證，確保綠燈。

## Phase 4: 介面層實作與測試 (Controller Layer)
- [任務] 建立 Controller 測試類別 (使用 `@WebMvcTest` 進行 API 路由與驗證測試)。
- [任務] 撰寫失敗的 MockMvc 測試 (Red)。
- [任務] 實作 Controller API 與輸入參數驗證 (Green)。
- [任務] 執行測試指令驗證，確保 HTTP 狀態碼與 JSON 回傳格式正確。

## Phase 5: 整合與程式碼品質 (Integration & Quality)
- [任務] 執行全專案的 Maven Test 確保無任何破壞性變更。
- [任務] 檢查程式碼風格 (無狀態、依賴注入正確、註解齊全)。