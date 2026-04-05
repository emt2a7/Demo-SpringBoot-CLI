# 技術規劃書：AuthUser JPA 領域模型實作

## 專案概述
本技術規劃書定義 `auth_user` 資料表的 JPA 架構實作細節，包含實體層（Entity）、資料存取層（Repository）與業務邏輯層（Service）的完整 Java 程式碼骨架。嚴格遵循 Java 21 + Spring Boot 3.4.3 技術棧，確保無狀態、高可測試性與交易邊界管理。

---

## 1. Entity 類別設計 (Entity Class Design)

### 1.1 完整類別骨架

```java
package org.example.framework.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", unique = true, length = 100)
    private String name;

    @Column(name = "age")
    private Integer age;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "create_user", length = 100, updatable = false)
    private String createUser;

    @UpdateTimestamp
    @Column(name = "update_time", insertable = false)
    private OffsetDateTime updateTime;

    @Column(name = "update_user", length = 100, insertable = false)
    private String updateUser;
}
```

### 1.2 技術約束說明

| 欄位          | JPA 標註組合                                                                 | 強制約束                                |
|-------------|--------------------------------------------------------------------------|-------------------------------------|
| id          | `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)`                | 主鍵自動生成，開發者嚴禁手動設定                  |
| name        | `@Column(unique = true, length = 100)`                                   | 資料庫層級唯一性約束                        |
| age         | `@Column`                                                                | 可為 NULL                            |
| createTime  | `@CreationTimestamp`, `@Column(updatable = false)`                       | JPA 自動維護，更新時不可變                   |
| createUser  | `@Column(updatable = false)`                                             | 更新時不可變                            |
| updateTime  | `@UpdateTimestamp`, `@Column(insertable = false)`                        | JPA 自動維護，新增時不可設定                  |
| updateUser  | `@Column(insertable = false)`                                            | 新增時不可設定                           |

### 1.3 Lombok 標註職責

| 標註                  | 職責說明                      |
|---------------------|---------------------------|
| `@Getter`           | 自動生成所有欄位的 Getter 方法       |
| `@Setter`           | 自動生成所有欄位的 Setter 方法       |
| `@NoArgsConstructor` | 自動生成無參數建構子（JPA 必要）       |
| `@AllArgsConstructor` | 自動生成全參數建構子（Builder 搭配使用） |
| `@Builder`          | 啟用建造者模式（Builder Pattern）  |

---

## 2. Repository 介面設計 (Repository Interface Design)

### 2.1 完整介面骨架

```java
package org.example.framework.repository;

import org.example.framework.entity.AuthUser;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthUserRepository extends BaseRepository<AuthUser, UUID> {
}
```

### 2.2 技術約束說明

| 項目         | 規格                                                                                     |
|------------|----------------------------------------------------------------------------------------|
| 繼承介面     | `BaseRepository<AuthUser, UUID>`                                                       |
| 內建方法     | 繼承自 `SimpleJpaRepository`，包含 `save()`, `findById()`, `delete()`, `exists()` 等標準 CRUD 方法 |
| 自訂查詢     | 目前無需自訂方法，若未來需要請遵循 Spring Data JPA 命名規範或使用 `@Query` 標註                                |
| 交易邊界     | Repository 層不定義交易邊界，所有交易由 Service 層管理                                                 |

---

## 3. Service 類別設計 (Service Class Design)

### 3.1 完整類別骨架

```java
package org.example.framework.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final AuthUserRepository authUserRepository;

    @Transactional(readOnly = true)
    public boolean exists(AuthUser entity) {
        // 實作：呼叫 authUserRepository.exists(Example.of(entity))
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        // 實作：呼叫 authUserRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> findById(UUID id) {
        // 實作：呼叫 authUserRepository.findById(id)
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> findOne(AuthUser entity) {
        // 實作：呼叫 authUserRepository.findOne(Example.of(entity))
    }

    @Transactional(readOnly = true)
    public List<AuthUser> findAll() {
        // 實作：呼叫 authUserRepository.findAll()
    }

    @Transactional(readOnly = true)
    public List<AuthUser> findAll(AuthUser entity) {
        // 實作：呼叫 authUserRepository.findAll(Example.of(entity))
    }

    @Transactional
    public AuthUser save(AuthUser entity) {
        // 實作：呼叫 authUserRepository.save(entity)
    }

    @Transactional
    public AuthUser saveAndFlush(AuthUser entity) {
        // 實作：呼叫 authUserRepository.saveAndFlush(entity)
    }

    @Transactional
    public List<AuthUser> saveAll(List<AuthUser> entities) {
        // 實作：呼叫 authUserRepository.saveAll(entities)
    }

    @Transactional
    public List<AuthUser> saveAllAndFlush(List<AuthUser> entities) {
        // 實作：呼叫 authUserRepository.saveAllAndFlush(entities)
    }

    @Transactional
    public void deleteById(UUID id) {
        // 實作：呼叫 authUserRepository.deleteById(id)
    }
}
```

### 3.2 方法簽章規範表

| 方法名稱                  | 輸入參數                      | 回傳型態                 | 交易標註                            | 核心邏輯                                      |
|-----------------------|---------------------------|----------------------|----------------------------------|-------------------------------------------|
| exists                | `AuthUser entity`         | `boolean`            | `@Transactional(readOnly = true)` | `authUserRepository.exists(Example.of(entity))` |
| existsById            | `UUID id`                 | `boolean`            | `@Transactional(readOnly = true)` | `authUserRepository.existsById(id)`       |
| findById              | `UUID id`                 | `Optional<AuthUser>` | `@Transactional(readOnly = true)` | `authUserRepository.findById(id)`         |
| findOne               | `AuthUser entity`         | `Optional<AuthUser>` | `@Transactional(readOnly = true)` | `authUserRepository.findOne(Example.of(entity))` |
| findAll               | 無                         | `List<AuthUser>`     | `@Transactional(readOnly = true)` | `authUserRepository.findAll()`            |
| findAll               | `AuthUser entity`         | `List<AuthUser>`     | `@Transactional(readOnly = true)` | `authUserRepository.findAll(Example.of(entity))` |
| save                  | `AuthUser entity`         | `AuthUser`           | `@Transactional`                  | `authUserRepository.save(entity)`         |
| saveAndFlush          | `AuthUser entity`         | `AuthUser`           | `@Transactional`                  | `authUserRepository.saveAndFlush(entity)` |
| saveAll               | `List<AuthUser> entities` | `List<AuthUser>`     | `@Transactional`                  | `authUserRepository.saveAll(entities)`    |
| saveAllAndFlush       | `List<AuthUser> entities` | `List<AuthUser>`     | `@Transactional`                  | `authUserRepository.saveAllAndFlush(entities)` |
| deleteById            | `UUID id`                 | `void`               | `@Transactional`                  | `authUserRepository.deleteById(id)`       |

### 3.3 交易管理約束

#### 讀取操作 (Query Methods)
- **強制標註**：`@Transactional(readOnly = true)`
- **效能優化**：關閉 Dirty Checking 機制，避免不必要的快照比對
- **適用方法**：`exists`, `existsById`, `findById`, `findOne`, `findAll`

#### 寫入/刪除操作 (Command Methods)
- **強制標註**：`@Transactional`
- **ACID 保證**：確保原子性、一致性、隔離性、持久性
- **適用方法**：`save`, `saveAndFlush`, `saveAll`, `saveAllAndFlush`, `deleteById`

### 3.4 審計欄位自動維護

| 欄位          | 觸發時機      | 維護機制                 | 開發者職責                      |
|-------------|-----------|----------------------|-----------------------------|
| createTime  | 新增時       | `@CreationTimestamp` | **嚴禁**手動設定，由 JPA 自動填入       |
| createUser  | 新增時       | 手動設定                 | 必須在呼叫 `save()` 前設定（未來可由 AOP 統一處理） |
| updateTime  | 更新時       | `@UpdateTimestamp`   | **嚴禁**手動設定，由 JPA 自動更新       |
| updateUser  | 更新時       | 手動設定                 | 必須在呼叫 `save()` 前設定（未來可由 AOP 統一處理） |

### 3.5 例外處理策略

| 例外類型                                | 觸發情境                     | Service 層處理建議              |
|-------------------------------------|--------------------------|----------------------------|
| `DataIntegrityViolationException`   | name 唯一性約束違反            | 捕捉後轉換為業務異常並回傳語義化錯誤訊息     |
| `EmptyResultDataAccessException`    | deleteById 刪除不存在的 ID     | 視業務需求決定是否忽略或向上拋出         |
| `IncorrectResultSizeDataAccessException` | findOne 查詢結果超過一筆        | 向上拋出，由 Controller 層轉換為 400 錯誤 |

---

## 4. 依賴套件管理

### 4.1 Maven 依賴 (pom.xml)

```xml
<!-- Spring Boot Starter Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Jakarta Persistence API (已包含於 spring-boot-starter-data-jpa) -->
<!-- PostgreSQL Driver (若使用 PostgreSQL) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 4.2 必要的 application.yml 配置

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 生產環境必須使用 validate，禁止使用 update/create
    show-sql: false       # 生產環境必須關閉
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC  # 統一使用 UTC 時區
  datasource:
    url: jdbc:postgresql://localhost:5432/demo
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

## 5. 測試策略

### 5.1 Repository 層測試 (整合測試)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AuthUserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private AuthUserRepository authUserRepository;

    @Test
    void testSaveAndFindById() {
        // 測試：儲存實體後可透過 ID 查詢
    }

    @Test
    void testNameUniqueConstraint() {
        // 測試：重複 name 會拋出 DataIntegrityViolationException
    }

    @Test
    void testCreationTimestampAutoGenerated() {
        // 測試：createTime 自動填入且不為 NULL
    }
}
```

### 5.2 Service 層測試 (單元測試)

```java
@ExtendWith(MockitoExtension.class)
class AuthUserServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @InjectMocks
    private AuthUserService authUserService;

    @Test
    void testFindById_EntityExists_ReturnsOptionalWithEntity() {
        // 測試：查詢存在的 ID 回傳非空 Optional
    }

    @Test
    void testFindById_EntityNotExists_ReturnsEmptyOptional() {
        // 測試：查詢不存在的 ID 回傳 Optional.empty()
    }

    @Test
    void testSave_ValidEntity_ReturnsPersistedEntity() {
        // 測試：儲存合法實體成功並回傳持久化物件
    }

    @Test
    void testDeleteById_ExistingId_NoException() {
        // 測試：刪除存在的 ID 不拋出例外
    }
}
```

---

## 6. 實作檢查清單 (Implementation Checklist)

### 6.1 Entity 層檢查

- [ ] 所有欄位對應的 `@Column` name 屬性與資料庫完全一致（區分大小寫）
- [ ] `@Table(name = "auth_user")` 精確對應資料表名稱
- [ ] 主鍵 `id` 使用 `@GeneratedValue(strategy = GenerationType.UUID)`
- [ ] `createTime` 標註 `@CreationTimestamp` 與 `updatable = false`
- [ ] `updateTime` 標註 `@UpdateTimestamp` 與 `insertable = false`
- [ ] 所有 Lombok 標註完整 (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`)

### 6.2 Repository 層檢查

- [ ] Interface 繼承 `BaseRepository<AuthUser, UUID>`
- [ ] 標註 `@Repository`
- [ ] 無自訂方法（若有需求必須更新規格書）

### 6.3 Service 層檢查

- [ ] 所有讀取方法標註 `@Transactional(readOnly = true)`
- [ ] 所有寫入/刪除方法標註 `@Transactional`
- [ ] Repository 依賴使用 `private final` 宣告並透過 `@RequiredArgsConstructor` 注入
- [ ] 所有方法回傳值符合規格（`Optional` 包裝、`List` 非 NULL）
- [ ] 關鍵操作記錄 Log（`log.info`, `log.error`）

### 6.4 測試覆蓋檢查

- [ ] Repository 整合測試使用 Testcontainers 或 H2
- [ ] Service 單元測試使用 Mockito Mock Repository
- [ ] 測試涵蓋正常情境與異常情境（如唯一性約束違反、查無資料）

---

## 7. 憲章遵循性確認

| 憲章原則                          | 遵循狀態 | 檢查項目                                   |
|--------------------------------|--------|------------------------------------------|
| 絕對的 MVP 與 YAGNI，嚴禁 Overdesign | ✅      | 僅實作必要的 CRUD 方法，無多餘抽象層          |
| 無狀態系統（Stateless）             | ✅      | Service 層無實例欄位儲存業務狀態             |
| 高可測試性                         | ✅      | Repository 透過建構子注入，可 Mock 測試       |
| 高可觀察性（Log 追蹤）               | ✅      | Service 層強制記錄關鍵操作與例外 Log          |
| 使用 Java 21 + Spring Boot 3.4.3 | ✅      | 使用 Jakarta EE 9+ 與現代 Java 特性         |

---

**規劃書版本**：1.0.0  
**制定日期**：2026-04-05  
**對應規格書**：`specs/101-jpa-auth-user/spec.md` v1.0.0  
**技術審查狀態**：待實作驗證

