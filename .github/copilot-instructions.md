# GitHub Copilot Instructions
1. 【語言】：一律使用台灣 IT 業界習慣之正體中文。
2. 【技術棧】：本專案為 Java 21 + Spring Boot 3.4.3，嚴禁使用過時的 Java EE 寫法。
3. 【API 規範】：所有 RESTful API 的回傳值必須使用 `BaseResponse<T>` 統一包裝。
4. 【架構規範】：保持系統無狀態 (Stateless)，嚴禁過度設計 (Overdesign)，遵守 MVP 原則。
5. 【測試驅動】：產出業務邏輯時，請優先考慮高可測試性 (DI 依賴注入)。


