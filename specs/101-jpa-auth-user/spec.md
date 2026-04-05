# 功能需求規格書：AuthUser JPA 領域模型

## 專案脈絡
本規格書定義 `auth_user` 資料表的完整 JPA 領域驅動設計架構，包含實體層（Entity）、資料存取層（Repository）與業務邏輯層（Service）。嚴格遵循 Java 21 + Spring Boot 3.4.3 技術棧，確保無狀態、高可測試性與 MVP 原則。

## Table Schema 對應

```sql
CREATE TABLE auth_user (
    id          UUID     DEFAULT uuid_generate_v4() PRIMARY KEY,
    name        VARCHAR(100) UNIQUE,
    age         integer,
    create_time TIMESTAMP,
    create_user VARCHAR(100),
    update_time TIMESTAMP DEFAULT NOW(),
    update_user VARCHAR(100)
);
```

---

## 1. 實體層 (Entity) 設計規格

### 1.1 基本資訊

| 項目           | 規格                             |
|--------------|--------------------------------|
| Package      | `org.example.framework.entity` |
| Class Name   | `AuthUser`                     |
| Table Name   | `auth_user`                    |
| 主鍵型態       | `UUID`                         |
| 主鍵欄位       | `id`                           |

### 1.2 必要引入 (Import)

```java
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;
```

### 1.3 Class Annotation 規範

```java
@Entity
@Table(name = "auth_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

**強制約束**：
- `@Table` 的 `name` 屬性必須精確對應 Table Schema 名稱（區分大小寫）。
- 必須使用 Lombok 標註簡化程式碼，避免手動撰寫 Getter/Setter/Constructor。

### 1.4 欄位映射規範

#### 1.4.1 主鍵欄位：id

| 屬性         | 規格                                      |
|------------|------------------------------------------|
| Java 型態   | `UUID`                                   |
| Column Name | `id`                                     |
| 約束條件     | Primary Key, NOT NULL                    |
| 生成策略     | `@GeneratedValue(strategy = GenerationType.UUID)` |

**強制約束**：
- 主鍵必須由 JPA 底層自動生成，嚴禁開發者手動設定 UUID 值。
- 必須使用 `@Id` 與 `@GeneratedValue` 標註。

#### 1.4.2 業務欄位：name

| 屬性         | 規格                                      |
|------------|------------------------------------------|
| Java 型態   | `String`                                 |
| Column Name | `name`                                   |
| 約束條件     | UNIQUE, 最大長度 100                       |
| JPA 標註    | `@Column(unique = true, length = 100)`   |

**業務語意**：使用者姓名，必須全域唯一。

#### 1.4.3 業務欄位：age

| 屬性         | 規格                                      |
|------------|------------------------------------------|
| Java 型態   | `Integer`                                |
| Column Name | `age`                                    |
| 約束條件     | 可為 NULL                                 |

**業務語意**：使用者年齡，選填欄位。

#### 1.4.4 審計欄位：create_time

| 屬性         | 規格                                      |
|------------|------------------------------------------|
| Java 型態   | `OffsetDateTime`                         |
| Column Name | `create_time`                            |
| 約束條件     | 自動維護，建立時寫入                        |
| JPA 標註    | `@CreationTimestamp`<br/>`@Column(updatable = false)` |

**時間審計防呆**：
- 必須使用 JPA 的 `@CreationTimestamp` 標註，由底層自動維護時間戳記。
- 必須設定 `updatable = false`，確保建立時間不會被異動操作修改。
- **嚴禁**開發者在程式碼中手動設定此欄位值。

#### 1.4.5 審計欄位：create_user

| 屬性         | 規格                                      |
|------------|------------------------------------------|
| Java 型態   | `String`                                 |
| Column Name | `create_user`                            |
| 約束條件     | 建立時寫入，最大長度 100                    |
| JPA 標註    | `@Column(length = 100, updatable = false)` |

**防呆約束**：
- 必須設定 `updatable = false`，確保建立人員不會被修改。

#### 1.4.6 審計欄位：update_time

| 屬性         | 規格                                      |
|------------|------------------------------------------|
| Java 型態   | `OffsetDateTime`                         |
| Column Name | `update_time`                            |
| 約束條件     | 自動維護，異動時更新                        |
| JPA 標註    | `@UpdateTimestamp`<br/>`@Column(insertable = false)` |

**時間審計防呆**：
- 必須使用 JPA 的 `@UpdateTimestamp` 標註，由底層自動維護時間戳記。
- 必須設定 `insertable = false`，確保異動時間不會在建立時被設定，僅在更新時自動維護。
- **嚴禁**開發者在程式碼中手動設定此欄位值。

#### 1.4.7 審計欄位：update_user

| 屬性         | 規格                                      |
|------------|------------------------------------------|
| Java 型態   | `String`                                 |
| Column Name | `update_user`                            |
| 約束條件     | 異動時更新，最大長度 100                    |
| JPA 標註    | `@Column(length = 100, insertable = false)` |

**防呆約束**：
- 必須設定 `insertable = false`，確保異動人員不會在建立時被設定，僅在更新時自動維護。

### 1.5 Entity 架構約束

- **無狀態原則**：Entity 僅作為資料載體（Data Carrier），不得包含業務邏輯方法。
- **不可變性建議**：建立後的審計欄位（`create_time`、`create_user`）應透過 JPA 層級約束確保不可變。
- **型別安全**：所有日期時間欄位必須使用 `OffsetDateTime`（帶時區），確保跨時區一致性。

---

## 2. 資料存取層 (Repository) 設計規格

### 2.1 基本資訊

| 項目           | 規格                                        |
|--------------|-------------------------------------------|
| Package      | `org.example.framework.repository`        |
| Interface Name | `AuthUserRepository`                     |
| 繼承介面       | `BaseRepository<AuthUser, UUID>`          |

### 2.2 必要引入 (Import)

```java
import org.springframework.stereotype.Repository;
import org.example.framework.entity.AuthUser;
import java.util.UUID;
```

### 2.3 Interface Annotation 規範

```java
@Repository
```

### 2.4 方法宣告約束

**依賴 SimpleJpaRepository 的內建巨集**：
- 由於本 Repository 無特殊跨表查詢需求，**嚴禁**額外宣告基礎 CRUD 方法（如 `save()`, `findById()`, `delete()` 等）。
- 所有標準操作必須直接繼承自 `BaseRepository` 與 `SimpleJpaRepository`。
- 若未來需要自訂查詢方法（如 `findByName`），必須遵循 Spring Data JPA 的命名規範或使用 `@Query` 標註。

### 2.5 Repository 架構約束

- **無狀態原則**：Repository 僅負責資料存取，不得包含業務邏輯或快取機制。
- **交易邊界**：Repository 層不應定義交易邊界，交易管理必須委派給 Service 層。
- **測試要求**：Repository 的整合測試必須使用內嵌資料庫（如 H2）或 Testcontainers，確保可獨立執行。

---

## 3. 業務邏輯層 (Service) 設計規格

### 3.1 基本資訊

| 項目           | 規格                                      |
|--------------|------------------------------------------|
| Package      | `org.example.framework.service`          |
| Class Name   | `AuthUserService`                        |
| 依賴注入       | `AuthUserRepository`（透過 `@RequiredArgsConstructor`） |

### 3.2 必要引入 (Import)

```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.framework.entity.AuthUser;
import org.example.framework.repository.AuthUserRepository;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
```

### 3.3 Class Annotation 規範

```java
@Slf4j
@Service
@RequiredArgsConstructor
```

### 3.4 Repository 依賴宣告

```java
private final AuthUserRepository authUserRepository;
```

**依賴注入約束**：
- 必須使用 `private final` 宣告，交由 Lombok 的 `@RequiredArgsConstructor` 自動生成建構子。
- 嚴禁使用 `@Autowired` 欄位注入，確保測試時可透過建構子注入 Mock 物件。

---

### 3.5 方法規格定義

#### 3.5.1 exists(AuthUser entity)

**業務語意**：檢查符合指定屬性條件的實體是否存在於資料庫中。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `AuthUser entity`                            |
| 輸出       | `boolean`                                    |
| 交易標註   | `@Transactional(readOnly = true)`            |
| 核心邏輯   | 呼叫 `authUserRepository.exists(Example.of(entity))` |

**交易約束**：關閉 Dirty Checking，提升查詢性能。

---

#### 3.5.2 existsById(UUID id)

**業務語意**：檢查指定 ID 的實體是否存在於資料庫中。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `UUID id`                                    |
| 輸出       | `boolean`                                    |
| 交易標註   | `@Transactional(readOnly = true)`            |
| 核心邏輯   | 呼叫 `authUserRepository.existsById(id)`     |

**交易約束**：關閉 Dirty Checking，提升查詢性能。

---

#### 3.5.3 findById(UUID id)

**業務語意**：透過主鍵 ID 查詢單筆實體。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `UUID id`                                    |
| 輸出       | `Optional<AuthUser>`                         |
| 交易標註   | `@Transactional(readOnly = true)`            |
| 核心邏輯   | 呼叫 `authUserRepository.findById(id)`       |

**回傳約束**：
- 若查無資料，回傳 `Optional.empty()`，**嚴禁**拋出例外。
- 呼叫端必須透過 `Optional` 的 API 處理空值情境（如 `orElseThrow()`, `ifPresent()`）。

---

#### 3.5.4 findOne(AuthUser entity)

**業務語意**：透過 Query By Example 查詢符合條件的單筆實體。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `AuthUser entity`                            |
| 輸出       | `Optional<AuthUser>`                         |
| 交易標註   | `@Transactional(readOnly = true)`            |
| 核心邏輯   | 呼叫 `authUserRepository.findOne(Example.of(entity))` |

**查詢約束**：
- Example 僅匹配非 NULL 欄位。
- 若查詢結果超過一筆，將拋出 `IncorrectResultSizeDataAccessException`。

---

#### 3.5.5 findAll()

**業務語意**：查詢資料庫中所有 AuthUser 實體。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | 無                                           |
| 輸出       | `List<AuthUser>`                             |
| 交易標註   | `@Transactional(readOnly = true)`            |
| 核心邏輯   | 呼叫 `authUserRepository.findAll()`          |

**性能警告**：
- 此方法會載入所有紀錄，若資料量大可能導致 OOM（Out of Memory）。
- 生產環境建議使用分頁查詢（Pageable）或限定條件查詢。

---

#### 3.5.6 findAll(AuthUser entity)

**業務語意**：透過 Query By Example 查詢符合條件的所有實體。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `AuthUser entity`                            |
| 輸出       | `List<AuthUser>`                             |
| 交易標註   | `@Transactional(readOnly = true)`            |
| 核心邏輯   | 呼叫 `authUserRepository.findAll(Example.of(entity))` |

**查詢約束**：
- Example 僅匹配非 NULL 欄位。
- 若查無資料，回傳空 List（`Collections.emptyList()`），**嚴禁**回傳 NULL。

---

#### 3.5.7 save(AuthUser entity)

**業務語意**：儲存或更新單筆實體。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `AuthUser entity`                            |
| 輸出       | `AuthUser`（儲存後的實體）                     |
| 交易標註   | `@Transactional`                             |
| 核心邏輯   | 呼叫 `authUserRepository.save(entity)`       |

**審計欄位防呆**：
- JPA 底層會自動處理 `create_time`（新增時）與 `update_time`（更新時）。
- 開發者**嚴禁**在呼叫 `save()` 前手動設定這些欄位。

---

#### 3.5.8 saveAndFlush(AuthUser entity)

**業務語意**：儲存或更新單筆實體，並立即同步到資料庫。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `AuthUser entity`                            |
| 輸出       | `AuthUser`（儲存後的實體）                     |
| 交易標註   | `@Transactional`                             |
| 核心邏輯   | 呼叫 `authUserRepository.saveAndFlush(entity)` |

**使用情境**：
- 需要立即取得資料庫自動生成的值（如主鍵、審計欄位）。
- 需要確保資料立即寫入，避免交易結束前的延遲寫入。

---

#### 3.5.9 saveAll(List<AuthUser> entities)

**業務語意**：批次儲存或更新多筆實體。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `List<AuthUser> entities`                    |
| 輸出       | `List<AuthUser>`（儲存後的實體列表）            |
| 交易標註   | `@Transactional`                             |
| 核心邏輯   | 呼叫 `authUserRepository.saveAll(entities)`  |

**性能建議**：
- 批次操作會在單一交易中執行，若資料量過大建議分批處理。

---

#### 3.5.10 saveAllAndFlush(List<AuthUser> entities)

**業務語意**：批次儲存或更新多筆實體，並立即同步到資料庫。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `List<AuthUser> entities`                    |
| 輸出       | `List<AuthUser>`（儲存後的實體列表）            |
| 交易標註   | `@Transactional`                             |
| 核心邏輯   | 呼叫 `authUserRepository.saveAllAndFlush(entities)` |

**使用情境**：
- 需要立即取得批次資料的自動生成值。
- 需要確保批次資料立即寫入資料庫。

---

#### 3.5.11 deleteById(UUID id)

**業務語意**：透過主鍵 ID 刪除實體。

| 項目       | 規格                                          |
|----------|---------------------------------------------|
| 輸入       | `UUID id`                                    |
| 輸出       | `void`                                       |
| 交易標註   | `@Transactional`                             |
| 核心邏輯   | 呼叫 `authUserRepository.deleteById(id)`     |

**例外處理**：
- 若指定 ID 不存在，JPA 會拋出 `EmptyResultDataAccessException`。
- Service 層應自行決定是否捕捉例外或向上拋出。

---

### 3.6 Service 架構約束

#### 3.6.1 交易邊界管理

- **讀取操作**：必須標註 `@Transactional(readOnly = true)`，關閉 Dirty Checking 提升性能。
- **寫入/刪除操作**：必須標註 `@Transactional`，確保原子性與一致性。
- **嚴禁**在 Service 方法內手動控制交易（如 `TransactionTemplate`），除非有明確的複雜交易場景。

#### 3.6.2 高可測試性約束

- 所有業務邏輯必須封裝於 Service 層，與 Controller 及基礎設施層解耦。
- Repository 依賴必須透過建構子注入，確保單元測試時可注入 Mock 物件。
- 單元測試必須覆蓋所有公開方法，並驗證正常情境與異常情境（如查無資料、重複 name）。

#### 3.6.3 無狀態原則

- Service 層**嚴禁**在實例欄位中儲存任何業務狀態（如快取、計數器）。
- 所有資料必須透過參數傳入、從 Repository 查詢或從外部儲存（Redis、資料庫）載入。

#### 3.6.4 高可觀察性 (Log 追蹤)

- 關鍵業務操作（如 `save`, `delete`）必須記錄 INFO 層級 Log，包含操作類型、實體 ID、執行結果。
- 例外情況必須記錄 ERROR 層級 Log，包含完整堆疊追蹤與請求上下文。
- Log 格式範例：
  ```
  log.info("AuthUser saved successfully, id={}, name={}", entity.getId(), entity.getName());
  log.error("Failed to delete AuthUser, id={}", id, exception);
  ```

---

## 4. Constitution Check

### 4.1 憲章遵循性檢查

| 憲章原則                          | 遵循狀態 | 檢查項目                                   |
|--------------------------------|--------|------------------------------------------|
| 絕對的 MVP 與 YAGNI，嚴禁 Overdesign | ✅      | 僅實作必要的 CRUD 方法，無多餘抽象層          |
| RESTful API 設計與統一回應包裝器      | N/A    | 本規格書僅定義 Service 層，不涉及 API 層      |
| 無狀態系統（Stateless）             | ✅      | Entity/Repository/Service 均無本地狀態儲存   |
| 高可測試性                         | ✅      | Repository 透過建構子注入，可 Mock 測試       |
| 高可觀察性（Log 追蹤）               | ✅      | Service 層強制記錄關鍵操作與例外 Log          |

### 4.2 技術棧合規性

| 技術要求               | 遵循狀態 | 說明                                       |
|----------------------|--------|------------------------------------------|
| Java 21+             | ✅      | 使用 `java.util.UUID` 與 `OffsetDateTime`  |
| Spring Boot 3.4.3+   | ✅      | 使用 Jakarta EE 9+ 的 `jakarta.persistence.*` |
| Maven 建置工具         | ✅      | 無外部建置工具依賴                          |
| JUnit 5 + Mockito    | ✅      | Service 層可透過 Mockito Mock Repository   |

---

## 5. 獨立可測試的使用情境 (User Scenarios)

### 5.1 情境一：新增使用者

**前置條件**：資料庫中不存在 name = "張三" 的使用者。

**操作流程**：
1. 建立 `AuthUser` 實體，設定 `name = "張三"`, `age = 30`。
2. 呼叫 `authUserService.save(entity)`。
3. 驗證回傳的實體 `id` 不為 NULL。
4. 驗證 `create_time` 自動填入且不為 NULL。
5. 驗證資料庫中存在該筆紀錄。

**驗收標準**：
- 實體成功儲存，主鍵自動生成。
- 審計欄位 `create_time` 自動維護，無需手動設定。

---

### 5.2 情境二：透過 ID 查詢使用者

**前置條件**：資料庫中存在一筆 id = `550e8400-e29b-41d4-a716-446655440000` 的使用者。

**操作流程**：
1. 呼叫 `authUserService.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))`。
2. 驗證回傳的 `Optional` 非空。
3. 驗證實體的 `name` 與 `age` 與資料庫一致。

**驗收標準**：
- 查詢成功回傳實體。
- 若 ID 不存在，回傳 `Optional.empty()` 而非拋出例外。

---

### 5.3 情境三：透過 name 查詢使用者（Query By Example）

**前置條件**：資料庫中存在一筆 name = "李四" 的使用者。

**操作流程**：
1. 建立 `AuthUser` 實體，僅設定 `name = "李四"`。
2. 呼叫 `authUserService.findOne(entity)`。
3. 驗證回傳的 `Optional` 非空。
4. 驗證實體的 `age` 與資料庫一致。

**驗收標準**：
- 查詢成功回傳符合條件的單筆實體。
- 若查無資料，回傳 `Optional.empty()`。

---

### 5.4 情境四：更新使用者年齡

**前置條件**：資料庫中存在一筆 id = `550e8400-e29b-41d4-a716-446655440000`, age = 30 的使用者。

**操作流程**：
1. 透過 `findById` 查詢該實體。
2. 修改 `age = 31`。
3. 呼叫 `authUserService.save(entity)`。
4. 驗證 `update_time` 自動更新且晚於 `create_time`。

**驗收標準**：
- 實體成功更新。
- 審計欄位 `update_time` 自動維護，`create_time` 保持不變。

---

### 5.5 情境五：批次新增使用者

**前置條件**：資料庫為空。

**操作流程**：
1. 建立 3 筆 `AuthUser` 實體（name = "王五", "趙六", "孫七"）。
2. 呼叫 `authUserService.saveAll(entities)`。
3. 驗證回傳 List 大小為 3。
4. 驗證每筆實體的 `id` 均不為 NULL。

**驗收標準**：
- 批次儲存成功。
- 每筆實體的主鍵與審計欄位均自動生成。

---

### 5.6 情境六：刪除使用者

**前置條件**：資料庫中存在一筆 id = `550e8400-e29b-41d4-a716-446655440000` 的使用者。

**操作流程**：
1. 呼叫 `authUserService.deleteById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))`。
2. 驗證資料庫中該筆紀錄已被刪除。
3. 再次呼叫 `findById` 驗證回傳 `Optional.empty()`。

**驗收標準**：
- 實體成功刪除。
- 刪除不存在的 ID 時，拋出 `EmptyResultDataAccessException`（或 Service 層自行處理）。

---

### 5.7 情境七：檢查 name 唯一性約束

**前置條件**：資料庫中已存在 name = "張三" 的使用者。

**操作流程**：
1. 建立新 `AuthUser` 實體，設定 `name = "張三"`。
2. 呼叫 `authUserService.save(entity)`。
3. 驗證拋出 `DataIntegrityViolationException`（唯一性約束違反）。

**驗收標準**：
- 資料庫正確執行 UNIQUE 約束。
- Service 層或上層（如 Controller）必須捕捉此例外並轉換為業務友善的錯誤訊息。

---

## 6. 補充事項

### 6.1 實作約束

- 本規格書僅定義介面與行為準則，**嚴禁**在規格書中撰寫完整的實作程式碼。
- 實作程式碼必須於 `plan.md` 與 `tasks.md` 階段產出，並遵循本規格書的所有約束。
- 所有實作程式碼**嚴禁**包含註解說明（除非為 JavaDoc 或必要的複雜邏輯說明）。

### 6.2 延伸需求

若未來需要以下功能，應更新本規格書並重新執行規劃流程：
- 自訂查詢方法（如 `findByNameContaining`, `findByAgeGreaterThan`）。
- 分頁與排序查詢（Pageable）。
- 軟刪除機制（Soft Delete）。
- 實體關聯映射（OneToMany, ManyToOne 等）。

---

**規格書版本**：1.0.0  
**制定日期**：2026-04-05  
**最後修訂**：2026-04-05  
**憲章合規性**：已通過 Constitution Check

