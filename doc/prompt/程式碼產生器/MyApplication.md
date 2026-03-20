你是一位資深 Java 程式設計專家。

# 目標
- 新增「程式碼產生器」程式，其主要功能為：
  - 自動化配置類別生成。
  - 負責讀取 `*.yml` 檔案並自動生成 Java 類別。
- 「程式碼產生器」
  - 僅能生成一支程式，不可以多支程式。 
  - 存放在 `src/test/java/codegen/GenMyApplication.java`。
  - 要有程式進入點 `public static void main(String[] args)`。
  - 動態生成對應的 Java `record` 類別，並生成多個實體檔案。

# 自動化配置類別生成 (Dynamic Properties Class Generation) 規範
1. **跨檔案讀取與智慧分群 (Grouping)**：
- 同時掃描 `src/main/resources/*.yml` 等所有環境設定檔，並將相同 key 的值進行去重與聯集。
- 略過以 `#` 開頭的註解與空行。
- 嚴禁轉換 Spring Boot 內建的標準屬性（例如 `spring.*`, `logging.*`, `server.*`），這些請交由框架自動裝配。(納入維護清單)
- 根據自定義屬性的「第一層根節點 (Top-level Key)」進行分群（例如以 `bitfinex.*` 為一群、`kiosk.*` 為另一群）。
2. **多檔案拆分與動態命名 (Dynamic Naming)**：
- 針對每一個獨立的根節點，必須分別生成一個獨立的 Java 檔案。
- **命名規則**：將根節點名稱首字母大寫，並強制加上 `Prop` 後綴(納入維護清單)。例如：根節點為 `bitfinex`，則產出 `BitfinexProp.java`；根節點為 `kiosk`，則產出 `KioskProp.java`。
3. **套件與原生 Record 宣告 (Package & Record)**：
- **Package 位置**：第一行必須宣告 `package org.example.framework.prop;`。(納入維護清單)
- **不可變設計**：必須使用 JDK 21 原生的 `record` 語法宣告，嚴禁使用舊版的 `@Value`、`@Getter` 或傳統 `class`。
- 在 record 上方加上 `@ConfigurationProperties(prefix = "該群組的小寫根節點名稱")`。
4. **欄位生成與型別推斷 (Field Mapping & Type Inference)**：
- **屬性對應**：利用 Spring Boot 的寬鬆綁定 (Relaxed Binding)，移除根節點前綴後，將剩餘的 key 轉換為標準的**駝峰式命名 (camelCase)**。
- **型別推斷**：
    - 若值為數值型態（如 timeout: 10000、rate: 2.15），請宣告為 `BigDecimal`。
    - 若值為 `yes/no`、`true/false`、`on/off`、`0/1`、`Y/N`，請宣告為 `Boolean`。
    - 否則一律宣告為 `String`。
- **隱藏陷阱迴避 (Defensive Design)**：
  - 在解析 Properties 集合時，嚴禁使用 properties.stringPropertyNames() 進行迭代，因為這會導致 SnakeYAML 解析出來的整數 (Integer) 或布林 (Boolean) 屬性被略過。
  - 請一律使用 properties.entrySet() 進行迭代，並利用 String.valueOf() 將 Key 與 Value 強制轉回字串，再進行後續的型別推斷。
5. **排版要求**：
- 每個欄位之間保留一個空行，以維持程式碼的極致易讀性。
- 若 yml 中有對應的註解，請轉換為 Javadoc `/** ... */` 放置於該變數上方。
6. **生成檔案位置**：所有生成的 Java 類別檔案必須放置於 `src/main/java/org/example/framework/prop/` 目錄下。(納入維護清單)
7. **執行結果**：當執行 `GenMyApplication.main()` 方法後，應該能在 `src/main/java/org/example/framework/prop/` 目錄下看到對應的 Java `record` 類別檔案，且內容符合上述規範。
8. **錯誤處理**：在讀取 yml 檔案或生成 Java 類別的過程中，若發生任何異常，必須印出清晰的錯誤訊息並終止程式執行。
9. **程式碼品質**：生成的 Java 類別必須具備高可讀性與防禦性設計 (Defensive Programming)，並且所有的 JavaDoc、重要業務邏輯註解、以及 Git Commit Message，請一律使用**繁體中文 (Traditional Chinese)** 撰寫。
10. **依賴管理**：程式碼產生器應該使用 SpringBoot 3.4.3 幫我來管理解析 yml 檔案，同時搭配自建的輕量級掃描器來精準擷取 YAML 中的註解並轉換為 JavaDoc。
11. 上述有提到「納入維護清單」的規範，必須設計成採用 物件導向 (OOP) 設計 將維護清單置於頂部，並且在程式碼中明確標註哪些部分是「維護清單」，以便未來進行規範調整與優化。
