# Implementation Plan: 履歷解析 API (resume-parse-api)

## 概要
根據 `spec.md` 與專案憲章（無狀態、BaseResponse、可測試性、可觀察性），建立一個最小可行的 RESTful 服務，提供 `POST /api/v1/resume/parse`，使用 Spring AI 的 `ChatClient` 呼叫 LLM 解析輸入文字，並回傳 `BaseResponse<CandidateProfile>`。

遵守嚴格技術約束（禁止自建 Regex 或其他 NLP 函式庫；使用 `record` 定義 `CandidateProfile`；使用 `BeanOutputConverter<CandidateProfile>` 做回應轉換；實作全域例外處理與結構化日誌並帶 Trace ID）。避免 Overdesign，僅包含 MVP 必要元件。

## 交付物
- `plan.md`（本檔）
- `research.md`（Phase 0，解決若有 NEEDS CLARIFICATION）
- `data-model.md`（定義實體）
- `/contracts/`（若需要，API contract 範本）
- 原始碼：簡潔的 Spring Boot 專案（最小模組）包含：
  - `CandidateProfile` record
  - `BaseResponse<T>` 泛型封裝
  - `ResumeParseService`（使用 `ChatClient`）
  - `ChatBeanOutputConverter` 實作 `BeanOutputConverter<CandidateProfile>`
  - `ResumeController` (`@RestController` + `/api/v1/resume/parse`)
  - `GlobalExceptionHandler` (`@ControllerAdvice`) 與 logging (MDC)
  - Unit tests（mock ChatClient, 驗證錯誤處理與回傳格式）

## 技術背景與重要決策
- LLM 呼叫：僅使用 Spring AI 的 `ChatClient`，由 `ResumeParseService` 封裝呼叫邏輯（注入 `ChatClient`）。
- 回應解析：使用 `BeanOutputConverter<CandidateProfile>` 由 Spring AI 轉換 LLM 原始回應為 `CandidateProfile` record。
- Data model：`CandidateProfile` 使用 Java `record`。`BaseResponse<T>` 為簡潔 POJO/record（視需要）以統一 API 回應。
- 日誌：使用 SLF4J + MDC 實作 Trace ID（在過濾器/Interceptor 設置 MDC, 清理）。
- 測試：Unit tests mock `ChatClient` 與 `BeanOutputConverter`；避免在測試時呼叫真實 LLM。

## Phase 0 — Outline & Research
1. 檢視 `spec.md` 與 `constitution.md`，確認無衝突（已完成）。
2. 釐清「LLM 回傳格式範本」：需一個簡短的 LLM prompt 與預期 JSON schema，讓 `BeanOutputConverter` 可轉換為 `CandidateProfile`。（輸出於 `research.md`。）
3. 決定 ChatClient 設定（模型、超時、重試）與安全（API key 由 environment 或 Spring property 管理）。

Output: `research.md`（包含 prompt template 與示例 LLM 回應 JSON schema）。

## Phase 1 — Design & Contracts
1. `data-model.md`：列出 `CandidateProfile` 欄位與驗證規則（name:string, education:string, yearsExperience:number, skills:string[]）。
2. `/contracts/resume-parse-openapi.yaml`：最小 OpenAPI 描述（endpoint、request schema、BaseResponse）。
3. `quickstart.md`：如何在本機運行（環境變數、啟動命令、簡單 curl 範例）。

## Phase 2 — Implementation（最小可行實作）
每一步盡可能保持簡潔、可測試、且符合憲章。

Tasks:
- Project skeleton: Spring Boot minimal app（Maven），只加入必要依賴：Spring Web, Spring Boot Starter Logging, Spring AI（指定版本）。
- Config: `application.yml` 節點用於 Spring AI 設定（model, api-key via env）。
- Data contracts:
  - `record CandidateProfile(String name, String education, Double yearsExperience, List<String> skills)`
  - `class BaseResponse<T> { boolean success; String code; String message; T data; static factory methods }`（或 minimal record 形式）
- Chat conversion:
  - `class CandidateProfileConverter implements BeanOutputConverter<CandidateProfile>`：將 LLM 回應（預期 JSON）轉為 `CandidateProfile`。
- Service:
  - `interface ResumeParseService { CandidateProfile parse(String text); }`
  - `class ChatResumeParseService implements ResumeParseService`：注入 `ChatClient`，組裝 prompt（使用 `research.md` 的模板），呼叫 ChatClient，同步取得並透過 `CandidateProfileConverter` 轉換回 `CandidateProfile`。
- Controller:
  - `@RestController` `ResumeController`，`@PostMapping("/api/v1/resume/parse")`，接收 `Map<String,String>` 或 DTO `{ text }`，驗證非空（若空，拋出自訂 InvalidInputException），呼叫 Service，回傳 `BaseResponse.success=true`。
- Exception handling & Logging:
  - `@ControllerAdvice GlobalExceptionHandler`：捕捉 `InvalidInputException` → HTTP 400 + `INVALID_INPUT`；捕捉其他 Exception → HTTP 500 + `INTERNAL_ERROR`。
  - 在 GlobalExceptionHandler 記錄完整 stack trace 與 Trace ID。
  - 實作 Filter 或 HandlerInterceptor 在 request 進入時：產生/取用 Trace ID（若無則產生 UUID），寫入 MDC；在 response 完成後清除 MDC；同時記錄進入/離開的結構化 log（method, uri, status, duration, traceId）。
- Tests:
  - Unit: mock `ChatClient` 回傳預定 JSON 字串，驗證 `ChatResumeParseService` 與 `CandidateProfileConverter` 正常運作；驗證 `ResumeController` 對空輸入回傳 400；驗證 `GlobalExceptionHandler` 對未預期例外回傳 500 並記錄。
  - Contract test（簡單 curl-based）：示範 200/400/500 情境。

## Minimal File Layout
- src/main/java/.../controller/ResumeController.java
- src/main/java/.../service/ResumeParseService.java
- src/main/java/.../service/ChatResumeParseService.java
- src/main/java/.../model/CandidateProfile.java (record)
- src/main/java/.../model/BaseResponse.java
- src/main/java/.../convert/CandidateProfileConverter.java
- src/main/java/.../config/TraceFilter.java (或 Interceptor)
- src/main/java/.../exception/GlobalExceptionHandler.java
- src/test/... unit tests

## Acceptance Criteria 映射（驗證點）
- Endpoint: `POST /api/v1/resume/parse` 接受 `{ "text": "..." }`，成功回傳 HTTP 200 與 `BaseResponse.data` 包含四個欄位。
- 空輸入 → HTTP 400 + `INVALID_INPUT`。
- 非預期錯誤 → HTTP 500 + `INTERNAL_ERROR`，Log 含 Trace ID 與 stack trace。
- 所有回應皆以 `BaseResponse<T>` 包裝。

## 風險與緩解
- 風險：LLM 回應格式不穩定，導致 `BeanOutputConverter` 轉換失敗。
  - 緩解：在 `research.md` 定義嚴格 prompt 與 JSON schema 範例；在 service 層加入防禦性檢查與明確錯誤處理（回傳 PARTIAL_PARSE 或 INTERNAL_ERROR）。
- 風險：依賴 Spring AI 版號或 API 變動。
  - 緩解：在 `pom.xml` 指定可控版本並加入簡單抽象（ResumeParseService interface）以便未來替換。

## 時程建議（極簡、單人，工作天）
- Day 0: Phase 0 research（prompt template 與 JSON schema）
- Day 1: 設計 `data-model.md`、contracts、quickstart
- Day 2: 建置專案骨架、config 與 `CandidateProfile`、`BaseResponse`
- Day 3: 實作 `ChatResumeParseService` 與 `CandidateProfileConverter`
- Day 4: 實作 Controller、Exception Handler、Trace MDC
- Day 5: 撰寫單元測試與 contract 測試，修正

## 下一步（短期行動項）
1. 產出 `research.md`（prompt 與 LLM 回應範例）。
2. 建立最小 Spring Boot 專案骨架並實作 `CandidateProfile`、`BaseResponse`。

---
*此檔為 MVP-first 的實作計畫，遵守 `.specify/memory/constitution.md` 並避免任何不必要的抽象或函式庫引入。*
