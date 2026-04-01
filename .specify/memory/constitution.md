<!--
Sync Impact Report

- Version change: 0.1.0 -> 1.0.0 (MAJOR：專案脈絡全面重新定義為 Java 企業級後端；所有原則重寫；
  新增強制 API 合約語意 BaseResponse 與 Stateless 架構約束。)
- 修改原則（舊名稱 → 新名稱）:
  - 高品質且可測試為先（Testable Quality）→ 絕對的 MVP 與 YAGNI，嚴禁 Overdesign
  - 最小可行產品優先（MVP First）→ RESTful API 設計與統一回應包裝器
  - 簡潔可維護（Simplicity & Maintainability）→ 無狀態系統（Stateless）
  - 可觀察性（Observability）→ 高可測試性
  - 版本管理與破壞性變更（Versioning & Breaking Changes）→ 高可觀察性（Log 追蹤）
- 新增章節：技術棧規範
- 移除章節：無
- 範本檢閱：
  - .specify/templates/plan-template.md: ✅ 語意對齊（Constitution Check gate 欄位保留動態配置）
  - .specify/templates/spec-template.md: ✅ 語意對齊（強制獨立可測試使用情境不變）
  - .specify/templates/tasks-template.md: ✅ 語意對齊（MVP-first 與測試先行結構不變）
  - .specify/templates/checklist-template.md: ✅ 不需修改
  - .specify/templates/agent-file-template.md: ✅ 不需修改
- 延遲事項：無
-->

# Java 企業級後端系統憲章

## 技術棧規範

| 項目 | 規格                                      |
|------|-----------------------------------------|
| 語言 | Java 21+                                |
| 框架 | Spring Boot 3.4.3+<br/>Spring AI 1.1.0+ |
| 建置工具 | Maven                                   |
| 封裝格式 | War                                     |
| API 風格 | RESTful HTTP API                        |
| 測試框架 | JUnit 5 + Mockito                       |

## 核心原則

### 1. 絕對的 MVP 與 YAGNI，嚴禁 Overdesign（NON-NEGOTIABLE）

每個功能實作僅包含當前需求所必要的最小程式碼。MUST NOT 為未來假設需求預先設計介面、抽象層或擴充點。
每次 PR 的複雜度增加必須能直接對應到具體的使用者故事；若無法對應，一律拒絕合併。
判斷標準：若移除某段程式碼不影響任何現有測試，該程式碼即為 Overdesign 且 MUST 被刪除。

### 2. RESTful API 設計與統一回應包裝器

所有 HTTP API MUST 遵循 RESTful 設計風格：以資源為導向的 URI、使用標準 HTTP 動詞（GET / POST / PUT / PATCH / DELETE）、狀態碼語意正確。
所有 API 回應（成功與失敗）MUST 使用統一的 `BaseResponse<T>` 包裝器結構，至少包含 `success`（boolean）、`code`（業務代碼）、`message`（可讀描述）、`data`（酬載）四個欄位。
例外：健康檢查端點（如 `/actuator/health`）可豁免包裝器。

### 3. 無狀態系統（Stateless）

應用程式實例 MUST NOT 在本地記憶體中儲存任何跨請求的業務狀態（包含 HttpSession 中的使用者資料）。
需要共享狀態時 MUST 透過外部儲存（資料庫、Redis 等）存取。
此原則為橫向擴充（Scale Out）的必要前提：任何實例 MUST 能在不依賴其他實例狀態的情況下處理任意請求。

### 4. 高可測試性

所有業務邏輯 MUST 封裝於可被單元測試的 Service 層，與 Controller 及基礎設施層解耦。
外部相依（資料庫、第三方 API）MUST 透過介面注入（依賴反轉），以便測試時可以 Mock 替換。
合併門檻：新增程式碼的單元測試覆蓋率 MUST ≥ 80%（以行覆蓋率計）。
TDD 為建議實踐（SHOULD）：先寫測試確認失敗，再實作使其通過。

### 5. 高可觀察性（Log 追蹤）

每個 API 請求 MUST 在進入與離開時記錄結構化 Log，包含：請求 ID（Trace ID）、HTTP 方法、URI、回應狀態碼、處理耗時（ms）。
所有例外（Exception）MUST 在統一例外處理器（`@ControllerAdvice`）中記錄完整的堆疊追蹤（stack trace）與請求上下文。
Log 層級使用規範：ERROR（系統異常）、WARN（業務異常或降級）、INFO（正常流程關鍵節點）、DEBUG（開發除錯，生產環境 MUST NOT 啟用 DEBUG 層級於熱路徑）。

## 技術約束

1. 嚴禁引入未在 `plan.md` 中列出且未經審查的第三方函式庫。
2. Controller 層 MUST NOT 包含業務邏輯；僅負責參數接收、呼叫 Service、以 `BaseResponse` 包裝回傳。
3. 資料庫欄位變更 MUST 透過版本化遷移腳本（如 Flyway / Liquibase）管理，嚴禁直接修改 Schema。
4. 每個功能的 `spec.md` MUST 列出獨立可測試的使用情境（User Scenarios），`tasks.md` MUST 包含對應的測試任務。

## 開發工作流程

1. 所有變更透過 Pull Request 提交，PR 描述 MUST 包含：對應的使用者故事、測試覆蓋說明、相關文件更新。
2. 合併條件：至少一位維護者核准 + CI 綠燈（包含單元測試、Checkstyle、SpotBugs 靜態分析）。
3. 破壞性變更（API 合約異動、資料庫 Schema 變更）MUST 在 PR 中附上影響範圍說明與遷移計畫。

## 治理

1. 本憲章為專案的最高治理文件；任何與憲章衝突的實踐均視為違規，MUST 透過修訂流程解決。
2. 修訂程序：提出修訂 PR → 說明變更理由與影響 → 至少一位維護者核准 → 合併並更新版本與修訂日期。
3. 憲章合規檢查：每次 Sprint Review 或重大里程碑時執行一次，確認所有正在進行的功能符合各原則。

**Version**: 1.0.0 | **Ratified**: 2026-03-23 | **Last Amended**: 2026-03-23
