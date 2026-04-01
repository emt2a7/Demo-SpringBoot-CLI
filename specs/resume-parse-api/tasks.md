# tasks.md — 履歷解析 API (resume-parse-api)

此檔案依據 `constitution.md`、`spec.md` 與 `plan.md` 編製，嚴格遵循要求的相依性順序與 TDD 流程。每項實作任務後緊接著一個執行對應單元測試的任務（包含 `mvn test -Dtest=...` 指令提示）。

## Feature

- Feature: 履歷解析 API
- Story: US1 (P1) — 即時解析非結構化自傳

## Phase 1 — Setup

- [x] T001 建立 Maven 專案骨架與 `pom.xml`（檔案：pom.xml）
- [ ] T002 新增必要依賴於 `pom.xml`（Spring Boot, Spring Web, Spring AI, JUnit5, Mockito）（檔案：pom.xml）
- [ ] T003 建立 `application.yml` 用於 Spring AI 與基本 config（檔案：src/main/resources/application.yml）

## Phase 2 — Foundational (資料結構、例外、Trace)

- [x] T004 實作 `CandidateProfile` record（檔案：src/main/java/com/example/resumeparser/model/CandidateProfile.java）
- [x] T005 執行單元測試 `CandidateProfileTest` 並確認通過：`mvn test -Dtest=CandidateProfileTest`（測試檔：src/test/java/.../model/CandidateProfileTest.java）
- [x] T006 實作 `BaseResponse<T>`（檔案：src/main/java/com/example/resumeparser/model/BaseResponse.java）
- [x] T007 執行單元測試 `BaseResponseTest` 並確認通過：`mvn test -Dtest=BaseResponseTest`（測試檔：src/test/java/.../model/BaseResponseTest.java）
- [x] T008 實作 `GlobalExceptionHandler`（檔案：src/main/java/com/example/resumeparser/exception/GlobalExceptionHandler.java）
- [x] T009 執行單元測試 `GlobalExceptionHandlerTest` 並確認通過：`mvn test -Dtest=GlobalExceptionHandlerTest`（測試檔：src/test/java/.../exception/GlobalExceptionHandlerTest.java）
- [x] T010 實作 Trace ID MDC 的 Filter 或 Interceptor（`TraceFilter`）（檔案：src/main/java/com/example/resumeparser/config/TraceFilter.java）
- [x] T011 執行單元測試 `TraceFilterTest` 並確認通過：`mvn test -Dtest=TraceFilterTest`（測試檔：src/test/java/.../config/TraceFilterTest.java）

## Phase 3 — User Story Phases

### US1 (P1) — 解析非結構化自傳（嚴格相依於 Phase 2 完成）

註：依規範「在實作 ChatResumeParseService 之前，先撰寫 mock 測試」，下列任務亦遵循 TDD：先測試、再實作、再跑測試。

- [ ] T012 [US1] 撰寫 `ChatResumeParseService` 的 Mock 單元測試（使用 Mock 的 `ChatClient`）：`src/test/java/com/example/resumeparser/service/ChatResumeParseServiceTest.java`
- [ ] T013 [US1] 執行該 Mock 測試並觀察失敗/綠燈：`mvn test -Dtest=ChatResumeParseServiceTest`
- [ ] T014 [US1] 實作 `ResumeParseService` 介面（檔案：src/main/java/com/example/resumeparser/service/ResumeParseService.java）
- [ ] T015 [US1] 執行 `ResumeParseService` 相關測試：`mvn test -Dtest=ResumeParseServiceTest`（測試檔：src/test/java/.../service/ResumeParseServiceTest.java）
- [ ] T016 [US1] 實作 `ChatResumeParseService`（注入 `ChatClient`、組裝 prompt、呼叫並使用 `BeanOutputConverter`）（檔案：src/main/java/com/example/resumeparser/service/ChatResumeParseService.java）
- [ ] T017 [US1] 執行 `ChatResumeParseService` 的單元測試：`mvn test -Dtest=ChatResumeParseServiceImplTest`（測試檔：src/test/java/.../service/ChatResumeParseServiceImplTest.java）
- [ ] T018 [US1] 實作 `CandidateProfileConverter` （實作 `BeanOutputConverter<CandidateProfile>`）（檔案：src/main/java/com/example/resumeparser/convert/CandidateProfileConverter.java）
- [ ] T019 [US1] 執行 `CandidateProfileConverterTest`：`mvn test -Dtest=CandidateProfileConverterTest`（測試檔：src/test/java/.../convert/CandidateProfileConverterTest.java）
- [ ] T020 [US1] 實作 `ParseRequest` DTO（含非空驗證）（檔案：src/main/java/com/example/resumeparser/dto/ParseRequest.java）
- [ ] T021 [US1] 執行 `ParseRequestTest`（驗證輸入為空時會拋 InvalidInput）：`mvn test -Dtest=ParseRequestTest`（測試檔：src/test/java/.../dto/ParseRequestTest.java）
- [ ] T022 [US1] 實作 `ResumeController` 並暴露 `POST /api/v1/resume/parse`（檔案：src/main/java/com/example/resumeparser/controller/ResumeController.java）
- [ ] T023 [US1] 執行 `ResumeController` 的單元測試：`mvn test -Dtest=ResumeControllerTest`（測試檔：src/test/java/.../controller/ResumeControllerTest.java）
- [ ] T024 [US1] 執行整合測試（Contract test / Integration）：`mvn test -Dtest=ResumeControllerIntegrationTest`（測試檔：src/test/java/.../integration/ResumeControllerIntegrationTest.java）

## Final Phase — Polish & Cross-Cutting

- [ ] T025 新增 `quickstart.md`（檔案：specs/resume-parse-api/quickstart.md）並包含 `mvn spring-boot:run` 與範例 curl
- [ ] T026 新增 OpenAPI contract：`contracts/resume-parse-openapi.yaml`（檔案：specs/resume-parse-api/contracts/resume-parse-openapi.yaml）
- [ ] T027 執行最終品質檢查：確認所有任務均依 checklist 格式、每個實作後有對應的測試指令，並更新此 `tasks.md`（檔案：specs/resume-parse-api/tasks.md）

## 依賴關係摘要（嚴格順序）

1. Phase 1 (Setup)
2. Phase 2 (Foundational): `CandidateProfile` → `BaseResponse` → `GlobalExceptionHandler` → `TraceFilter`
3. Phase 3 (US1): (先寫 `ChatResumeParseService` 的 Mock 測試) → `ResumeParseService` interface → `ChatResumeParseService` impl → `CandidateProfileConverter` → `ParseRequest` DTO → `ResumeController` → Integration
4. Final Phase: 文件與 contract

## 平行執行建議

- 由於需求強制的相依性順序，大多數核心任務不可平行。可行平行工作：
  - 文件與 contract 的撰寫（T025、T026）可在 Phase 2 的測試階段平行進行。
  - 不同子系統的獨立測試（若已存在）可由 CI 並行執行，但本地開發仍建議按序執行。

## 任務計數與摘要

- 總任務數：27
- US1 任務數：13（T012 ~ T024）
- 平行機會：文件/contract（T025/T026）

## 每個使用情境的獨立測試準則（Independent Test Criteria）

- US1: 提供 `curl` 發送 `POST /api/v1/resume/parse` 並驗證回傳 `BaseResponse.data` 含 `name`, `education`, `yearsExperience`, `skills`。範例測試位置／指令見 `quickstart.md`（T025）。

## 建議 MVP 範圍

- 建議 MVP 首次交付僅覆蓋 US1（T012 ~ T024），並保證上述所有單元測試與整合測試綠燈。

## 格式驗證

- 本清單每一項均以 `- [ ] T###` 開頭並包含檔案路徑或測試指令，符合指定的 checklist 格式要求。
