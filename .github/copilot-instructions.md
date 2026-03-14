# 專案上下文與技術棧 (Project Context & Tech Stack)
當我要求你「更新pom.xml」或在「生成任何程式碼或修改架構前」，請務必嚴格遵循以下技術組合與版本邊界：
這是一個基於 JDK 25 與 Spring Boot 4.0.3 的前沿企業級微服務專案。
- **核心框架**：Spring Boot 4.0.3
- **AI 整合組件**：
  - Spring AI BOM 1.1.0+ (由 BOM 管理 Spring AI 模組版本)
  - org.springframework.ai:spring-ai-starter-model-google-genai (提供 Google GenAI 的整合，版本由 BOM 管理)
  - org.springframework.ai:spring-ai-starter-model-chat-memory (提供 Chat Memory 的整合，版本由 BOM 管理)
  - org.springframework.ai:spring-ai-starter-mcp-server (提供 MCP Server 的整合，版本由 BOM 管理)
- **工具與輔助**：
  - org.apache.commons:commons-lang3 3.19.0+ (用於字串處理與其他常用工具方法)
  - org.apache.commons:commons-compress 1.28.0+ (用於處理壓縮檔案)
  - org.apache.commons:commons-email 1.6.0+ (用於電子郵件處理)
  - commons-io:commons-io 2.19.0+ (用於檔案處理)
  - commons-cli:commons-cli 1.11.0+ (用於解析命令列參數)
  - org.projectlombok:lombok 1.18.42+ (必須確保與 JDK 25 相容)
  - Jackson (JSON 序列化與反序列化)(由 Spring Boot 管理版本)
- **測試框架**：Spring Boot Test 4.0.3 (已包含 JUnit 5，嚴禁引入 JUnit 4/Vintage)
- **建置工具**：Maven 3.9.13+ (Java 25 編譯環境，強烈建議使用 Maven Wrapper 確保版本一致性)

# 🤖 Copilot 架構師級生成規範 (Architectural & Code Generation Guidelines)

## 1. Maven 依賴與版本管理 (Strict Maven Management)
- **單一真理來源**：當被要求新增套件時，若該套件包含版本號，必須將版本號定義在 `pom.xml` 的 `<properties>` 區塊中，並在 `<dependency>` 中使用 `${變數名稱}` 參照，嚴禁在 `<dependency>` 中硬編碼寫死版本號。
- **BOM 優先原則**：專案已在 `<dependencyManagement>` 載入 `spring-ai-bom` (1.1.0)。未來新增任何 Spring AI 模組時，絕對不要加上 `<version>` 標籤，全權交由 BOM 管理。
- **依賴純淨度**：引入 `spring-boot-starter-test` 時，必須維持排除 `junit-vintage-engine` 的設定。
- **動態生成強制下載指令**：請根據缺少的套件，動態組合出 Maven 的強制下載指令並執行。
  - **指令格式樣板**：`mvn dependency:get -Dartifact={groupId}:{artifactId}:{version}`
  - **動態替換規則**：請精準地將 `{groupId}`, `{artifactId}`, 與 `{version}` 替換為該套件的真實座標。
  - **範例**：若缺少 `commons-cli` 1.9.0，請直接輸出代碼區塊 `mvn dependency:get -Dartifact=commons-cli:commons-cli:1.9.0`。
- **座標驗證機制**：在提供指令前，請先利用你的知識庫驗證該 `groupId` 與 `artifactId` 的拼寫是否正確（例如確認是 `commons-io:commons-io` 而不是 `org.apache.commons:commons-io`）。
- **IDE 同步提醒**：在指令下方溫馨提醒我：「執行完畢後，請在 IDE 中點擊 Reload All Maven Projects 以重整快取」。
- **建置驗證機制 (Build Verification)**：每次完成 `pom.xml` 的修改或更新後，你必須執行以下指令，以驗證建置狀態並強制更新快取：
  `mvn clean package -DskipTests -U > build-error.log 2>&1`
  （同時提醒我：若建置失敗，請將 `build-error.log` 的內容提供給你以進行排錯。）

## 2. JDK 25 編譯器與打包規範 (Compiler & Build Pipeline)
- **編譯參數鎖定**：修改 `maven-compiler-plugin` 時，必須永遠保留 `<compilerArgs>` 中的 `-parameters` 以及 `-proc:full` 參數。這是確保 JDK 25 與 Lombok 能正常協作的底線。
- **Fat Jar 打包**：`spring-boot-maven-plugin` 必須明確包含 `<goal>repackage</goal>`，確保產出可獨立執行的 Fat Jar。
- **環境設定檔實體隔離 (Resource Filtering)**：基於資安與環境純淨度考量，`pom.xml` 中必須配置 `<profiles>` 進行資源過濾。當打包特定環境時，嚴禁將其他環境的機密設定檔（如內網密碼）打包進 JAR 檔中。請確保 `pom.xml` 永遠維持以下標準的資源過濾配置：
  ```xml
  <profiles>
      <profile>
          <id>dev</id>
          <activation>
              <activeByDefault>true</activeByDefault>
          </activation>
          <build>
              <resources>
                  <resource>
                      <directory>src/main/resources</directory>
                      <excludes>
                          <exclude>application-prod.yml</exclude>
                      </excludes>
                  </resource>
              </resources>
          </build>
      </profile>

      <profile>
          <id>prod</id>
          <build>
              <resources>
                  <resource>
                      <directory>src/main/resources</directory>
                      <excludes>
                          <exclude>application-dev.yml</exclude>
                      </excludes>
                  </resource>
              </resources>
          </build>
      </profile>
  </profiles>
    ```

## 3. Spring Boot 實作與多環境設定檔標準 (Spring Boot & Multi-Environment Config Standards)
- **依賴注入 (DI)**：嚴禁使用 `@Autowired` 進行欄位注入。一律採用「建構子注入 (Constructor Injection)」，並搭配 Lombok 的 `@RequiredArgsConstructor`。
- **Controller 規範**：API 節點必須使用 `@RestController`，並適當使用 `@GetMapping` 等明確標註。
- **多環境配置 (Multi-Environment Profiles)**：專案採用嚴謹的環境隔離設計。
  - 共用的基礎設定（如 `spring.application.name`）必須放在 `application.yml`。
  - 開發環境專屬設定（如本機測試資料庫、詳細日誌層級）必須放在 `application-dev.yml`。
  - 正式環境專屬設定（如生產環境金鑰、生產資料庫連線）必須放在 `application-prod.yml`。
  - 所有的設定檔皆必須放置於 `src/main/resources/` 目錄下。

## 4. 企業級日誌與環境連動規範 (Logging & Profile Synergy)
- **日誌記錄 (Logging)**：
  - 在需要印出 log 的 Service/Controller 類別層級加上 `@Slf4j` 註解，嚴禁使用 `System.out.println` 或 `e.printStackTrace()`。
  - 捕捉到 Exception 時，必須將 Exception 物件作為日誌的最後一個參數傳入：`log.error("訊息: {}", 變數, e);`
- **YAML 與 XML 變數連動**：修改或生成 `logback-spring.xml` 時，嚴禁硬編碼日誌路徑與專案名稱。必須使用 `<springProperty>` 從 `application.yml` 動態讀取以下變數：
  - `APP_NAME` (對應 `spring.application.name`)
  - `LOG_PATH` (對應 `logging.file.path`)
  - `LOG_FILE_NAME` (對應 `logging.file.name`)
- **環境分流管理**：`logback-spring.xml` 必須依照 `<springProfile>` 來區分 Appender（例如 `dev` 輸出 Console，`prod` 輸出 Rolling File）。日誌輸出層級 (Level) 的動態調整，應優先透過各環境的 `application-{profile}.yml` (如 `application-dev.yml`) 進行覆寫管控。

## 5. Spring AI 實作指南 (Spring AI Best Practices)
- 實作 AI 聊天、提示詞生成或 RAG 邏輯時，請優先使用 Spring AI 1.1.0 最新推薦的 `ChatClient` Fluent API 進行開發，不要使用舊版 Deprecated 的 `ChatClient` 介面。
- 呼叫 LLM API 時必須妥善處理網路與超時異常。

## 6. 註解與語氣 (Documentation & Tone)
- 所有的 JavaDoc、重要業務邏輯註解、以及 Git Commit Message，請一律使用**繁體中文 (Traditional Chinese)** 撰寫。
- 生成的程式碼必須具備高可讀性與防禦性設計 (Defensive Programming)。

## 7. 自動化配置類別生成 (Dynamic Properties Class Generation)
當我要求你「更新 Properties」或「根據 yml 產生配置檔」時，請嚴格執行以下多檔案拆分與轉換邏輯：
1. **跨檔案讀取與智慧分群 (Grouping)**：
- 同時掃描 `application.yml`、`application-dev.yml`、`application-prod.yml` 等所有環境設定檔，並將相同 key 的值進行去重與聯集。
- 略過以 `#` 開頭的註解與空行。
- 嚴禁轉換 Spring Boot 內建的標準屬性（例如 `spring.*`, `logging.*`, `server.*`），這些請交由框架自動裝配。
- 根據自定義屬性的「第一層根節點 (Top-level Key)」進行分群（例如以 `bitfinex.*` 為一群、`kiosk.*` 為另一群）。
2. **多檔案拆分與動態命名 (Dynamic Naming)**：
- 針對每一個獨立的根節點，必須分別生成一個獨立的 Java 檔案。
- **命名規則**：將根節點名稱首字母大寫，並強制加上 `Properties` 後綴。例如：根節點為 `bitfinex`，則產出 `BitfinexProperties.java`；根節點為 `kiosk`，則產出 `KioskProperties.java`。
3. **套件與原生 Record 宣告 (Package & Record)**：
- **Package 位置**：第一行必須宣告 `package org.example.framework;`。
- **不可變設計**：必須使用 JDK 25 原生的 `record` 語法宣告，嚴禁使用舊版的 `@Value`、`@Getter` 或傳統 `class`。
- 在 record 上方加上 `@ConfigurationProperties(prefix = "該群組的小寫根節點名稱")`。
4. **欄位生成與型別推斷 (Field Mapping & Type Inference)**：
- **屬性對應**：利用 Spring Boot 的寬鬆綁定 (Relaxed Binding)，移除根節點前綴後，將剩餘的 key 轉換為標準的**駝峰式命名 (camelCase)**。
- **型別推斷**：
  - 若值為數值型態（如 timeout: 10000、rate: 2.15），請宣告為 `BigDecimal`。
  - 若值為 `yes/no`、`true/false`、`on/off`、`0/1`、`Y/N`，請宣告為 `Boolean`。
  - 否則一律宣告為 `String`。
5. **排版要求**：
- 每個欄位之間保留一個空行，以維持程式碼的極致易讀性。
- 若 yml 中有對應的註解，請轉換為 Javadoc `/** ... */` 放置於該變數上方。

## 8. 現代化 Java 語法規範 (Modern Java Syntax Guidelines)
本專案基於 JDK 25，在生成 Service、Controller 或任何業務邏輯的實作程式碼時，請嚴格遵守以下現代化語法慣例，以提升程式碼的整潔度與可讀性：
1. **精準使用 `var` (區域變數型態推斷)**：
  - **綠燈 (必須使用)**：當等號右側已經明確揭露了實體型態時，請一律使用 `var`。例如：`var list = new ArrayList<String>();` 或 `var yaml = new BitfinexYaml(...);`。
  - **綠燈 (推薦使用)**：在 `for` 迴圈與 `try-with-resources` 區塊中，優先使用 `var` 以簡化視覺負擔。
  - **紅燈 (嚴禁使用)**：當呼叫回傳值不明確的方法時（例如：`var result = service.process();`），嚴禁使用 `var`，必須明確寫出回傳型態（如 `BitfinexResponse`），以維持「無 IDE 輔助下的絕對可讀性」。
2. **Pattern Matching (模式匹配)**：
  - 進行型別判斷時，請直接使用帶有變數宣告的 `instanceof`，嚴禁使用舊版的強制轉型。
  - 正確範例：`if (obj instanceof String str) { log.info(str.trim()); }`。
3. **Switch 表達式 (Switch Expressions)**：
  - 遇到條件分支時，請優先使用現代化的箭頭語法 `->` 來取代傳統的 `case : break;`，並善用 `yield` 回傳值。
  - 範例：`var status = switch(code) { case 1 -> "OK"; case 2 -> "FAIL"; default -> "UNKNOWN"; };`
4. **文字區塊 (Text Blocks)**：
  - 遇到多行的 JSON 假資料、SQL 語法或長篇提示詞時，嚴禁使用 `+ \n +` 串接字串。請一律使用 `"""` 文字區塊來排版。

