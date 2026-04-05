/my-specify-jpa
# 我需要根據以下的 Table Schema，設計標準的 Entity, Repository 與 Service 框架：
CREATE TABLE auth_user (
id UUID     DEFAULT uuid_generate_v4() PRIMARY KEY, -- 使用者 UUID 作為主鍵，確保全球唯一性
name        VARCHAR(100) UNIQUE,                    -- 姓名 (加上 UNIQUE 確保名稱不重複)
age         integer,                                -- 年齡
create_time TIMESTAMP,                              -- 建立時間
create_user VARCHAR(100),                           -- 建立人員
update_time TIMESTAMP,                              -- 異動時間
update_user VARCHAR(100)                            -- 異動人員
);

# 引用輸入路徑
  - 憲法規章：`.specify/memory/constitution.md`

# 指定輸出路徑
  - 【最高級別警告】：請絕對、嚴格遵守以下路徑！**禁止**參考任何終端機的輸出日誌、環境變數或目前所在的 Git 分支名稱。
  - 必須將產出的 `spec.md` 建立在：`specs/101-jpa-[將_table_name_替換為實際的表名]/` 目錄下 (例如：`specs/101-jpa-auth-user/`)。

# 功能需求規格
## 1. 實體層 (Entity) 設計
  - Package 名稱：`org.example.framework.entity`
  - Class 名稱
    - 必須為 `[實體類別名稱]`：依據 Table Schema name 自動轉換為 Java 的 PascalCase (大駝峰) 命名
    - 例如：table 為 auth_user，則 Class 名稱為 `AuthUser`
    - 必須對應 Table Schema，精準映射欄位與型別
    - 必要的引入：
      ```java
      import jakarta.persistence.*;
      import lombok.*;
      ```
    - 若 Table Schema 的 PRIMARY KEY 為 UUID，則必須引入：
      ```java
      import java.util.UUID;
      ```
    - 若 Table Schema 的欄位為 `create_time TIMESTAMP`，則必須引入：
      ```java
      import org.hibernate.annotations.CreationTimestamp;
      import java.time.OffsetDateTime;
      ```
    - 若 Table Schema 的欄位為 `update_time TIMESTAMP`，則必須引入：
      ```java
      import org.hibernate.annotations.UpdateTimestamp;
      import java.time.OffsetDateTime;
      ```
    - 若 Table Schema 的欄位符合任一數字型態，則必須引入：
      ```java
      import java.math.BigDecimal;
      ```
    - 引入的 import 有重複時，請務必去除重複的 import，保持程式碼整潔。
    - 其他必要的 import，請依照實際需求添加，但必須保持精簡，避免不必要的依賴。
  - Class Annotation
    - 必須使用下列 Lombok 標註：
      ```java
      @Entity
      @Table
      @Getter
      @Setter
      @NoArgsConstructor
      @AllArgsConstructor
      @Builder
      ```
    - @Table 的 name 屬性必須精確對應 Table Schema 的名稱，且區分大小寫，例如：(name = "auth_user")
    - **主鍵策略**
      - `id` 欄位必須設定為 UUID 自動生成策略 (`GenerationType.UUID`)。
    - **審計欄位**
      - `create_time`
        - 必須使用 JPA 的 `@CreationTimestamp` 標註，交由底層自動維護時間。
        - `@Column` 標註的屬性必須設定為 `updatable = false`，確保建立時間不會被修改。
      - `update_time`
        - 必須使用 JPA 的 `@UpdateTimestamp` 標註，交由底層自動維護時間。
        - `@Column` 標註的屬性嚴禁設定為 `insertable = false`。
      - `create_user`
        - 必須使用 JPA 的 `@Column` 標註，並設定 `updatable = false`，確保建立人員不會被修改。
      - `update_user`
        - `@Column` 標註的屬性嚴禁設定為 `insertable = false`。

## 2. 資料存取層 (Repository) 設計
  - Package 名稱：`org.example.framework.repository`
  - Interface 名稱
    - 命名規則：`[實體類別名稱]Repository`
    - 必須繼承專案既有的 `BaseRepository<[實體類別名稱], [主鍵型態]>`。
      - [主鍵型態] 必須遵循「實體層 (Entity)」的主鍵型態定義，例如：UUID。
  - 必要的引入：
    ```java
    import org.springframework.stereotype.Repository;
    ```
    - 若 Table Schema 的 PRIMARY KEY 為 UUID，則必須引入：
      ```java
      import java.util.UUID;
      ```
    - 必須引入 `[實體類別名稱]`
    - 引入的 import 有重複時，請務必去除重複的 import，保持程式碼整潔。
    - 其他必要的 import，請依照實際需求添加，但必須保持精簡，避免不必要的依賴。
  - Class Annotation
    - 必須使用下列 annotation：
      ```java
      @Repository
      ```

## 3. 業務邏輯層 (Service) 設計
  - Package 名稱：`org.example.framework.service`
  - Class 名稱：`[實體類別名稱]Service`
  - 必要的引入：
    ```java
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.Example;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import java.util.List;
    import java.util.Optional;
    ```
    - 若 Table Schema 的 PRIMARY KEY 為 UUID，則必須引入：
      ```java
      import java.util.UUID;
      ```
    - 必須引入 `[實體類別名稱]`
    - 必須引入 `[實體類別名稱]Repository`
    - 引入的 import 有重複時，請務必去除重複的 import，保持程式碼整潔。
    - 其他必要的 import，請依照實際需求添加，但必須保持精簡，避免不必要的依賴。
  - Class Annotation
    - 必須使用下列標註：
      ```java
      @Slf4j
      @Service
      @RequiredArgsConstructor
      ```
  - 宣告 Repository 依賴
    - 必須使用 `private final [實體類別名稱]Repository [實體類別名稱首字母小寫]Repository;` 的格式宣告 Repository 依賴，並交由 Lombok 的 `@RequiredArgsConstructor` 自動生成建構子。
    - 接下來定義的方法可以直接使用該 Repository 依賴進行資料存取操作。

### method 1: exists([實體類別名稱] entity)
  - 輸入
    - entity 類別：`[實體類別名稱]`
  - 輸出：
    - boolean型態，表示該實體是否存在於資料庫中
  - Annotation
    - `@Transactional(readOnly = true)` （關閉 Dirty Checking）
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `exists(Example)` 方法，以檢查資料庫中是否存在符合該實體類別屬性值的紀錄。
    - 例如：`[實體類別名稱首字母小寫]Repository.exists(Example.of(entity))`

### method 2: existsById(UUID id)
  - 輸入
    - UUID型態的 id，表示要檢查的實體主鍵值
  - 輸出：
    - boolean型態，表示該實體是否存在於資料庫中
  - Annotation
    - `@Transactional(readOnly = true)` （關閉 Dirty Checking）
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `existsById(UUID)` 方法，以檢查資料庫中是否存在符合該實體類別屬性值的紀錄。
    - 例如：`[實體類別名稱首字母小寫]Repository.existsById(id)`

### method 3: findById(UUID id)
  - 輸入
    - UUID型態的 id，表示要檢查的實體主鍵值
  - 輸出：
    - Optional<[實體類別名稱]> 型態，表示該實體是否存在於資料庫中，若存在則包含該實體物件，否則為 Optional.empty()
  - Annotation
    - `@Transactional(readOnly = true)` （關閉 Dirty Checking）
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `findById(UUID)` 方法，以檢查資料庫中是否存在符合該實體類別屬性值的紀錄。
    - 例如：`[實體類別名稱首字母小寫]Repository.findById(id)`

### method 4: findOne([實體類別名稱] entity)
  - 輸入
    - entity 類別：`[實體類別名稱]`
  - 輸出：
    - Optional<[實體類別名稱]> 型態，表示該實體是否存在於資料庫中，若存在則包含該實體物件，否則為 Optional.empty()
  - Annotation
    - `@Transactional(readOnly = true)` （關閉 Dirty Checking）
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `findOne(Example)` 方法，以檢查資料庫中是否存在符合該實體類別屬性值的紀錄。
    - 例如：`[實體類別名稱首字母小寫]Repository.findOne(Example.of(entity))`

### method 5: findAll()
  - 輸入
    - 無輸入參數
  - 輸出：
    - List<[實體類別名稱]> 型態，表示資料庫中所有該實體類別的紀錄列表
  - Annotation
    - `@Transactional(readOnly = true)` （關閉 Dirty Checking）
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `findAll()` 方法，以取得資料庫中所有符合該實體類別屬性值的紀錄列表。
    - 例如：`[實體類別名稱首字母小寫]Repository.findAll()`

### method 6: findAll([實體類別名稱] entity)
  - 輸入
    - entity 類別：`[實體類別名稱]`
  - 輸出：
    - List<[實體類別名稱]> 型態，表示資料庫中所有該實體類別的紀錄列表
  - Annotation
    - `@Transactional(readOnly = true)` （關閉 Dirty Checking）
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `findAll(Example)` 方法，以取得資料庫中所有符合該實體類別屬性值的紀錄列表。
    - 例如：`[實體類別名稱首字母小寫]Repository.findAll(Example.of(entity))`

### method 7: save([實體類別名稱] entity)
  - 輸入
    - entity 類別：`[實體類別名稱]`
  - 輸出：
    - `[實體類別名稱]` 型態，表示儲存後的實體物件
  - Annotation
    - `@Transactional`
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `save([實體類別名稱])` 方法，以儲存該實體物件到資料庫中，並回傳儲存後的實體物件。
    - 例如：`[實體類別名稱首字母小寫]Repository.save(entity)`

### method 8: saveAndFlush([實體類別名稱] entity)
  - 輸入
    - entity 類別：`[實體類別名稱]`
  - 輸出：
    - `[實體類別名稱]` 型態，表示儲存後的實體物件
  - Annotation
    - `@Transactional`
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `saveAndFlush([實體類別名稱])` 方法，以儲存該實體物件到資料庫中，並立即將變更同步到資料庫，最後回傳儲存後的實體物件。
    - 例如：`[實體類別名稱首字母小寫]Repository.saveAndFlush(entity)`

### method 9: saveAll(List<[實體類別名稱]> entities)
  - 輸入
    - List<[實體類別名稱]> 型態
  - 輸出：
    - List<[實體類別名稱]> 型態，表示儲存後的實體物件列表
  - Annotation
    - `@Transactional`
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `saveAll(List<[實體類別名稱]>)` 方法，以儲存該實體物件列表到資料庫中，並回傳儲存後的實體物件列表。
    - 例如：`[實體類別名稱首字母小寫]Repository.saveAll(entities)`

### method 10: saveAllAndFlush(List<[實體類別名稱]> entities)
  - 輸入
    - List<[實體類別名稱]> 型態
  - 輸出：
    - List<[實體類別名稱]> 型態，表示儲存後的實體物件列表
  - Annotation
    - `@Transactional`
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `saveAllAndFlush(List<[實體類別名稱]>)` 方法，以儲存該實體物件列表到資料庫中，並立即將變更同步到資料庫，最後回傳儲存後的實體物件列表。
    - 例如：`[實體類別名稱首字母小寫]Repository.saveAllAndFlush(entities)`

### method 11: deleteById(UUID id)
  - 輸入
    - UUID型態的 id，表示要刪除的實體主鍵值
  - 輸出：
    - void型態，表示刪除操作完成
  - Annotation
    - `@Transactional`
  - 業務邏輯
    - 呼叫 `[實體類別名稱首字母小寫]Repository` 的 `deleteById(UUID)` 方法，以刪除資料庫中符合該實體類別主鍵值的紀錄。
    - 例如：`[實體類別名稱首字母小寫]Repository.deleteById(id)`

## 4. 測試層 (Testing) 設計約束
  - 無論是 Repository 的切片測試 (`@DataJpaTest`) 還是 Service 的整合測試 (`@SpringBootTest`)，在設計測試類別時，**必須強制**加上環境描述檔標註。
  - 必要的引入：
    ```java
    import org.springframework.test.context.ActiveProfiles;
    ```
  - Class Annotation 限制：
    - 必須在測試類別的最上方加上 `@ActiveProfiles("test")`。
    - **架構目的**：明確宣告測試環境，確保與正式環境 (dev/prod) 隔離，避免測試啟動時誤觸發 Main class 中的 `ApplicationRunner` 或不相關的 Bean。

# 補充事項
  - 產出的規格書只需定義介面與行為準則，請勿寫出具體實作程式碼。
  - 實作程式碼，嚴禁寫出註解說明，嚴禁任何 logging 或 debug 相關的程式碼。
