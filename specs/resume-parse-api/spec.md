# Feature Specification: 履歷解析 API

**Feature Branch**: `resume-parse-api`  
**Created**: 2026-03-23  
**Status**: Draft  
**Input**: 使用者描述："建立一支 RESTful API，接收非結構化之求職者自傳（純文字），解析並回傳結構化 JSON；萃取欄位包含姓名、最高學歷、工作年資、專業技能；MVP 不需寫入資料庫；請符合憲章要求（RESTful, BaseResponse, Stateless, 高可測試性, 高可觀察性）。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 即時解析非結構化自傳 (Priority: P1)

一名系統使用者（前端或第三方系統）上傳一段非結構化之求職者自傳文字，系統解析後即時回傳結構化 JSON，包含 `name`、`education`、`yearsExperience`、`skills`。

**Why this priority**: 直接產生可用的結構化欄位，為上游應用（配對、搜尋、顯示）提供最小可行價值，且符合 MVP 原則。

**Independent Test**: 使用 curl 對 API 發出 POST 請求，帶入範例純文字，自動比對回傳 JSON 欄位是否存在且格式正確。

**Acceptance Scenarios**:
1. **Given** 非結構化自傳文字（含姓名、學歷、年資、技能）， **When** 呼叫 `POST /api/v1/resume/parse` 並以 `application/json` 傳入 `{"text": "..."}`， **Then** 回應 HTTP 200 且 `BaseResponse.data` 為下列結構：
   - `name`: 字串或空字串
   - `education`: 字串（最高學歷描述）
   - `yearsExperience`: 浮點數或整數（工作年資）
   - `skills`: 字串陣列（至少空陣列）

2. **Given** 無效或空白的輸入， **When** 呼叫同一端點， **Then** 回應 HTTP 400，`BaseResponse.success=false` 且 `code` 為 `INVALID_INPUT`。

3. **Given** 系統發生非預期錯誤， **When** 呼叫端點， **Then** 回應 HTTP 500，`BaseResponse.success=false` 且 `code` 為 `INTERNAL_ERROR`，且 Log 記錄完整堆疊與 Trace ID。

---

### Edge Cases

- 輸入文字中缺少某些欄位（例：無姓名或無技能）：系統應以空字串或空陣列回傳該欄位，不視為 500 錯誤。  
- 多人履歷合併於同一段文字：MVP 回傳最先匹配到的單一個人資訊，並在 `message` 提醒可能不完整或有多位候選人。  
- 非中文或混合語言自傳：系統應接受 UTF-8 編碼文字，並盡力解析；若語言無法解析則回傳 `PARTIAL_PARSE` code。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系統 MUST 提供 `POST /api/v1/resume/parse` 端點，接受 JSON `{ "text": "..." }` 並回傳 `BaseResponse<CandidateProfile>`。
- **FR-002**: `CandidateProfile` MUST 包含欄位 `name`、`education`、`yearsExperience`、`skills`。
- **FR-003**: 所有 API 回應 MUST 使用 `BaseResponse<T>` 包裝（包含 `success`, `code`, `message`, `data`）。
- **FR-004**: 系統 MUST 為無狀態（Stateless），不得在本地記憶體中保存跨請求業務狀態。MVP 不持久化資料。
- **FR-005**: 輸入驗證：若 `text` 欄位缺失或為空，API MUST 回傳 HTTP 400 與 `INVALID_INPUT`。
- **FR-006**: 異常處理：任何未預期的例外 MUST 由統一例外處理器捕捉，回傳 HTTP 500 與 `INTERNAL_ERROR`，並在 Log 中記錄完整堆疊與 Trace ID。
- **FR-007**: API MUST 記錄結構化日誌（含 Trace ID、URI、方法、狀態碼、耗時）。
- **FR-008**: 輸入/輸出 MUST 使用 UTF-8 編碼並支援中文（繁體/簡體）與英文。

### Key Entities

- **CandidateProfile**: 代表解析後的履歷資料
  - `name`: string
  - `education`: string
  - `yearsExperience`: number
  - `skills`: string[]

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 在典型輸入（≤ 2000 字）下，API 平均回應時間 ≤ 2 秒（P95 ≤ 3 秒）。
- **SC-002**: 在提供的 50 範例測試集中，主要欄位（姓名、最高學歷、工作年資、技能）之欄位正確萃取率 ≥ 80%（MVP 基準，可逐步提高）。
- **SC-003**: API 回應符合憲章：所有回應使用 `BaseResponse`，且系統為無狀態（可透過啟動兩個實例並均能處理請求驗證）。
- **SC-004**: 日誌能藉由 Trace ID 對應到單一請求的所有處理記錄；在發生 500 錯誤時可追溯 stack trace。

## Assumptions

- 輸入文本以自然語言書寫，含姓名/經歷資訊；格式多樣且不保證標準化。  
- MVP 階段不需存入資料庫或建立使用者帳號。  
- 若需提高解析精準度，未來可引入外部 NLP 服務或機器學習模型，但初期以規則式與簡單詞庫為主。  

## Acceptance Test Examples (簡要)

1. 範例成功解析測試：提供含完整欄位的自傳，驗證 HTTP 200，`BaseResponse.success=true`，`data` 包含非空 `name`、`education`、數值 `yearsExperience` 與 `skills` 陣列。  
2. 無輸入測試：`{"text":""}` → HTTP 400，`code=INVALID_INPUT`。  
3. 部分欄位缺失測試：缺少技能 → HTTP 200，`data.skills=[]`。

---

**Notes / Next Steps**: 建議同時建立簡單的 contract test（contract/tests/）與 50 筆測試範例資料集以衡量解析準確度。
