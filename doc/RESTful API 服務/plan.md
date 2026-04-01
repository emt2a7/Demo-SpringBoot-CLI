# 產出章節結構 (Structure Constraints)
本專案為 Java Spring Boot 架構，請嚴格且僅能使用以下五個章節來組織 `plan.md`，禁止自行新增其他章節：

1. **架構定位與依賴變更 (Architecture & Dependencies)**
    - 說明此功能屬於哪個模組，以及是否需要修改 `pom.xml` 引入新的依賴套件。
2. **資料庫與實體設計 (Database & Entity Layer)**
    - 定義 JPA Entity 類別名稱、資料表 Schema 變更 (Table/Columns)、索引 (Indexes) 以及關聯映射 (如 `@OneToMany`)。
3. **介面層設計 (Controller & DTO Layer)**
    - 規劃 Spring `@RestController` 類別名稱與方法簽章。
    - 明確定義 Request/Response DTO 類別，並標註需要的驗證註解 (Validation Annotations，如 `@NotBlank`, `@NotNull`)。
4. **業務邏輯層設計 (Service Layer)**
    - 規劃 Service Interface 與 Impl 類別。
    - 標註交易邊界 (Transaction Boundaries，何處應加上 `@Transactional`)，並簡述核心演算法與呼叫外部服務的邏輯。
5. **技術與架構約束 (Technical Constraints)**
    - （請在此帶入專案的全域技術規範，例如：一律使用建構子注入、統一例外處理機制、禁用 System.out 等）。