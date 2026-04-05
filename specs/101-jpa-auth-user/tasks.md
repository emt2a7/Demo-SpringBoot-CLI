# Skeleton

- [ ] T001 建立 Entity 類別骨架於 `src/main/java/org/example/framework/entity/AuthUser.java`
- [ ] T002 建立 Repository 介面骨架於 `src/main/java/org/example/framework/repository/AuthUserRepository.java`
- [ ] T003 建立 Service 類別骨架於 `src/main/java/org/example/framework/service/AuthUserService.java`
- [ ] T004 [P] 建立 Repository 測試類別骨架於 `src/test/java/org/example/framework/repository/AuthUserRepositoryTest.java`
- [ ] T005 [P] 建立 Service 測試類別骨架於 `src/test/java/org/example/framework/service/AuthUserServiceTest.java`

# Entity Layer Implementation

- [ ] T006 為 `AuthUser` 實體類別添加所有欄位映射（id, name, age, createTime, createUser, updateTime, updateUser）
- [ ] T007 為 `AuthUser` 配置所有 JPA 標註（@Entity, @Table, @Id, @GeneratedValue, @Column, @CreationTimestamp, @UpdateTimestamp）
- [ ] T008 為 `AuthUser` 配置所有 Lombok 標註（@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder）
- [ ] T009 驗證 `AuthUser` 的審計欄位約束（createTime/createUser 設定 updatable=false，updateTime/updateUser 設定 insertable=false）

# Repository Layer Implementation

- [ ] T010 為 `AuthUserRepository` 介面繼承 `BaseRepository<AuthUser, UUID>`
- [ ] T011 為 `AuthUserRepository` 添加 @Repository 標註
- [ ] T012 驗證 `AuthUserRepository` 無自訂方法（僅繼承標準 CRUD）

# Service Layer Implementation - Query Methods

## exists(AuthUser entity)

- [ ] T013 [P] 為 `exists(AuthUser entity)` 撰寫單元測試（實體存在、實體不存在）於 `AuthUserServiceTest.java`
- [ ] T014 於 `AuthUserService.java` 實作 `exists(AuthUser entity)` 方法，標註 @Transactional(readOnly = true)
- [ ] T015 [P] 驗證 `exists(AuthUser entity)` 通過所有測試

## existsById(UUID id)

- [ ] T016 [P] 為 `existsById(UUID id)` 撰寫單元測試（ID 存在、ID 不存在）於 `AuthUserServiceTest.java`
- [ ] T017 於 `AuthUserService.java` 實作 `existsById(UUID id)` 方法，標註 @Transactional(readOnly = true)
- [ ] T018 [P] 驗證 `existsById(UUID id)` 通過所有測試

## findById(UUID id)

- [ ] T019 [P] 為 `findById(UUID id)` 撰寫單元測試（ID 存在回傳 Optional.of(), ID 不存在回傳 Optional.empty()）於 `AuthUserServiceTest.java`
- [ ] T020 於 `AuthUserService.java` 實作 `findById(UUID id)` 方法，標註 @Transactional(readOnly = true)
- [ ] T021 [P] 驗證 `findById(UUID id)` 通過所有測試

## findOne(AuthUser entity)

- [ ] T022 [P] 為 `findOne(AuthUser entity)` 撰寫單元測試（單筆符合、無符合、多筆符合拋出例外）於 `AuthUserServiceTest.java`
- [ ] T023 於 `AuthUserService.java` 實作 `findOne(AuthUser entity)` 方法，標註 @Transactional(readOnly = true)
- [ ] T024 [P] 驗證 `findOne(AuthUser entity)` 通過所有測試

## findAll()

- [ ] T025 [P] 為 `findAll()` 撰寫單元測試（有資料回傳 List、無資料回傳空 List）於 `AuthUserServiceTest.java`
- [ ] T026 於 `AuthUserService.java` 實作 `findAll()` 方法，標註 @Transactional(readOnly = true)
- [ ] T027 [P] 驗證 `findAll()` 通過所有測試

## findAll(AuthUser entity)

- [ ] T028 [P] 為 `findAll(AuthUser entity)` 撰寫單元測試（Example 符合多筆、符合零筆）於 `AuthUserServiceTest.java`
- [ ] T029 於 `AuthUserService.java` 實作 `findAll(AuthUser entity)` 方法，標註 @Transactional(readOnly = true)
- [ ] T030 [P] 驗證 `findAll(AuthUser entity)` 通過所有測試

# Service Layer Implementation - Command Methods

## save(AuthUser entity)

- [ ] T031 [P] 為 `save(AuthUser entity)` 撰寫單元測試（新增成功、更新成功）於 `AuthUserServiceTest.java`
- [ ] T032 於 `AuthUserService.java` 實作 `save(AuthUser entity)` 方法，標註 @Transactional
- [ ] T033 [P] 驗證 `save(AuthUser entity)` 通過所有測試

## saveAndFlush(AuthUser entity)

- [ ] T034 [P] 為 `saveAndFlush(AuthUser entity)` 撰寫單元測試（新增並立即同步、更新並立即同步）於 `AuthUserServiceTest.java`
- [ ] T035 於 `AuthUserService.java` 實作 `saveAndFlush(AuthUser entity)` 方法，標註 @Transactional
- [ ] T036 [P] 驗證 `saveAndFlush(AuthUser entity)` 通過所有測試

## saveAll(List<AuthUser> entities)

- [ ] T037 [P] 為 `saveAll(List<AuthUser> entities)` 撰寫單元測試（批次新增、空 List）於 `AuthUserServiceTest.java`
- [ ] T038 於 `AuthUserService.java` 實作 `saveAll(List<AuthUser> entities)` 方法，標註 @Transactional
- [ ] T039 [P] 驗證 `saveAll(List<AuthUser> entities)` 通過所有測試

## saveAllAndFlush(List<AuthUser> entities)

- [ ] T040 [P] 為 `saveAllAndFlush(List<AuthUser> entities)` 撰寫單元測試（批次新增並立即同步）於 `AuthUserServiceTest.java`
- [ ] T041 於 `AuthUserService.java` 實作 `saveAllAndFlush(List<AuthUser> entities)` 方法，標註 @Transactional
- [ ] T042 [P] 驗證 `saveAllAndFlush(List<AuthUser> entities)` 通過所有測試

## deleteById(UUID id)

- [ ] T043 [P] 為 `deleteById(UUID id)` 撰寫單元測試（刪除存在的 ID、刪除不存在的 ID 拋出例外）於 `AuthUserServiceTest.java`
- [ ] T044 於 `AuthUserService.java` 實作 `deleteById(UUID id)` 方法，標註 @Transactional
- [ ] T045 [P] 驗證 `deleteById(UUID id)` 通過所有測試

# Integration Testing (Repository Layer)

- [ ] T046 [P] 為 `AuthUserRepository` 撰寫整合測試：測試 save 與 findById（使用 @DataJpaTest + Testcontainers）
- [ ] T047 [P] 為 `AuthUserRepository` 撰寫整合測試：測試 name 唯一性約束（重複 name 拋出 DataIntegrityViolationException）
- [ ] T048 [P] 為 `AuthUserRepository` 撰寫整合測試：測試 createTime 自動生成（@CreationTimestamp 驗證）
- [ ] T049 [P] 為 `AuthUserRepository` 撰寫整合測試：測試 updateTime 自動更新（@UpdateTimestamp 驗證）
- [ ] T050 [P] 為 `AuthUserRepository` 撰寫整合測試：測試審計欄位防呆機制（createTime 不可更新、updateTime 不可在新增時設定）

# Logging & Observability

- [ ] T051 於 `AuthUserService.save()` 添加 INFO 層級 Log（記錄實體 ID 與 name）
- [ ] T052 於 `AuthUserService.saveAndFlush()` 添加 INFO 層級 Log（記錄實體 ID 與 name）
- [ ] T053 於 `AuthUserService.saveAll()` 添加 INFO 層級 Log（記錄批次數量）
- [ ] T054 於 `AuthUserService.saveAllAndFlush()` 添加 INFO 層級 Log（記錄批次數量）
- [ ] T055 於 `AuthUserService.deleteById()` 添加 INFO 層級 Log（記錄刪除的 ID）
- [ ] T056 於 `AuthUserService` 關鍵方法添加 ERROR 層級 Log（捕捉 DataIntegrityViolationException 並記錄完整堆疊）

# Quality Assurance

- [ ] T057 檢查 `AuthUser.java` 所有欄位的 @Column name 屬性與資料庫完全一致（區分大小寫）
- [ ] T058 檢查 `AuthUser.java` 的 @Table(name = "auth_user") 精確對應資料表名稱
- [ ] T059 檢查 `AuthUser.java` 的主鍵 id 使用 @GeneratedValue(strategy = GenerationType.UUID)
- [ ] T060 檢查 `AuthUser.java` 的審計欄位標註正確（@CreationTimestamp + updatable=false, @UpdateTimestamp + insertable=false）
- [ ] T061 檢查 `AuthUserRepository.java` 繼承 BaseRepository<AuthUser, UUID> 且標註 @Repository
- [ ] T062 檢查 `AuthUserService.java` 的 Repository 依賴使用 private final 宣告並透過 @RequiredArgsConstructor 注入
- [ ] T063 檢查 `AuthUserService.java` 所有讀取方法標註 @Transactional(readOnly = true)
- [ ] T064 檢查 `AuthUserService.java` 所有寫入/刪除方法標註 @Transactional
- [ ] T065 檢查 `AuthUserService.java` 所有方法回傳值符合規格（Optional 包裝、List 非 NULL）
- [ ] T066 檢查 `AuthUserServiceTest.java` 所有 public 方法皆有單元測試覆蓋（使用 Mockito Mock Repository）
- [ ] T067 檢查 `AuthUserRepositoryTest.java` 所有整合測試使用 Testcontainers 或 H2
- [ ] T068 檢查測試涵蓋正常情境與異常情境（唯一性約束違反、查無資料、NULL 參數）
- [ ] T069 檢查 `AuthUserService.java` 無實例欄位儲存業務狀態（Stateless 驗證）
- [ ] T070 檢查所有 import 無重複且精簡（移除未使用的 import）
- [ ] T071 檢查所有實作程式碼嚴禁包含註解說明（除非為必要的複雜邏輯說明）
- [ ] T072 最終程式碼格式化與靜態分析於 `AuthUser.java`、`AuthUserRepository.java`、`AuthUserService.java`
- [ ] T073 執行所有測試並確保 100% 通過（單元測試 + 整合測試）
- [ ] T074 驗證憲章遵循性（MVP、YAGNI、Stateless、高可測試性、高可觀察性）

