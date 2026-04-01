---
description: 【程式碼產生器】生成 pom.xml
---

# 任務目標
- 請在專案根目錄下，直接生成或覆寫 `pom.xml` 檔案。
- 這是一份符合企業級規範的 Maven `pom.xml`。
- 請絕對嚴格遵循以下的結構順序與規範，不准遺漏任何一個 plugin 或設定。

# 專案基本資訊 (Project Metadata)
- name: Demo-SpringBoot-CLI
- description: 首次將 Spring Boot 結合 AI 的範例專案，展示諸多實際應用場景之練習。
- groupId: org.example
- artifactId: demo-springboot-cli
- version: 1.0
- packaging: jar
- Java 版本: 21
- Spring Boot 版本: 3.4.3

# 結構與規範約束 (Structural Constraints)
## 1. Parent 區塊
- 繼承 `spring-boot-starter-parent`，並將版本設為 `3.4.3`。

## 2. Properties 區塊 (Single Source of Truth)
* 定義基本屬性
  - `java.version`：21
  - `project.build.sourceEncoding`：UTF-8 (編碼)
  - `project.reporting.outputEncoding`：UTF-8 (編碼)
* 定義 Dependency Management 屬性
  - `spring-ai-bom.version`：1.1.0
* 定義 Dependencies 屬性
  - `spring-boot.version`：3.4.3
  - `spring-boot-maven-plugin.version`：3.4.3
  - `commons-lang3.version`：3.19.0
  - `commons-io.version`：2.19.0
  - `commons-compress.version`：1.28.0
  - `commons-cli.version`：1.11.0
  - `lombok.version`：1.18.42
* 定義 plugin 屬性
  - `maven-compiler-plugin.version`：3.11.0
  - `maven-surefire-plugin.version`：3.1.2
  - `start-class`：org.example.Main (主程式位置)
* 定義自訂屬性
  - `ai.type`：openai
  - `env.type`：dev

## 3. Dependency Management 區塊 (BOM 管理)
* 引入 `spring-ai-bom`，版本綁定為 `${spring-ai-bom.version}` (scope 為 import，type 為 pom)，用於統一管理 Spring AI 相關模組的版本。

## 4. Dependencies 區塊 (核心依賴)
* **【版本宣告紀律】**：
  - 若套件已由 Parent 或 Spring AI BOM 管理（如 `spring-boot-starter-*` 或 `spring-ai-*`），請「省略」 `<version>` 標籤。
  - 若套件未被管理且需要指定版本，**禁止寫死絕對數字 (如 1.28.0)，必須明確寫出 `<version>` 標籤，並嚴格使用 `${...}` 變數引用。**
  - 正確範例：`<version>${commons-compress.version}</version>`

* 設定 `org.springframework.boot:spring-boot-starter-web`
* 設定 `org.springframework.boot:spring-boot-starter-test` (scope 為 test，排除 `junit-vintage-engine`)
* 設定 `org.springframework.boot:spring-boot-starter-jdbc` (JDBC 模組)
* 設定 `org.springframework.boot:spring-boot-starter-mail` (Mail 模組)
* 設定 `org.springframework.ai:spring-ai-starter-model-chat-memory` (Chat Memory 模組)
* 設定 `org.springframework.ai:spring-ai-vector-store` (Vector Store 模組)
* 設定 `org.springframework.ai:spring-ai-advisors-vector-store` (Advisors Vector Store 模組)
* 設定 `org.springframework.ai:spring-ai-pgvector-store` (PostgreSQL: pgvector 向量資料庫模組)
* 設定 `org.springframework.ai:spring-ai-tika-document-reader` (Tika Document Reader 模組，讓 AI 讀懂 Word/Excel/PDF)
* 設定 `org.springframework.ai:spring-ai-pdf-document-reader` (PDF Document Reader 模組)(for Rag01Service、Rag03Service.java)
* 設定 `org.springframework.ai:spring-ai-starter-model-openai` (OpenAI 模組)
* 設定 `org.apache.commons:commons-lang3`，必須加上 `${commons-lang3.version}` (字串處理與其他常用工具)
* 設定 `org.apache.commons:commons-compress`，必須加上 `${commons-compress.version}` (壓縮檔案)
* 設定 `commons-io:commons-io`，必須加上 `${commons-io.version}` (檔案處理)
* 設定 `commons-cli:commons-cli`，必須加上 `${commons-cli.version}` (解析命令列參數)
* 設定 `org.projectlombok:lombok`，必須加上 `${lombok.version}` (scope 為 provided)
* 設定 `org.postgresql:postgresql` (scope 為 runtime)

## 5. Profiles 區塊 (環境與功能切換)
* 定義 `dev` 環境
  - 預設啟用
  - 在其對應的 `<properties>` 覆寫設定為 `<env.type>dev</env.type>`
* 定義 `prod` 環境
  - 在其對應的 `<properties>` 覆寫設定為 `<env.type>prod</env.type>`
* 定義 `openai` 環境
  - 預設啟用
  - 在其對應的 `<properties>` 覆寫設定為 `<ai.type>openai</ai.type>`

## 6. Build 區塊 (Plugins 與 Resources)
* **Resources:**
  - 設定過濾規則 `filtering` 為 `true`
  - 加入 `application.yml`
  - 加入 `application-{env.type}.yml`
  - 加入 `application-{ai.type}.yml`。
* **Plugins:**
  - 設定 `org.apache.maven.plugins:maven-compiler-plugin`
    - **版本與基礎設定綁定**
      - `<version>` 必須綁定 `${maven-compiler-plugin.version}`，維持版本集中管理
      - `<source>` 與 `<target>` 必須綁定 `${java.version}`
      - `<encoding>` 必須綁定 `${project.build.sourceEncoding}`
    - **進階編譯參數 (`<compilerArgs>`)**
      - 必須加入 `-parameters` 參數（保留方法參數名稱，供 Spring Boot 反射與 DI 使用）
      - 必須加入 `-proc:full` 參數（確保在較新版 JDK 下正確執行 Annotation Processing）
    - **註解處理器 (嚴格結構要求)**
      - 【警告】絕對禁止在 plugin 層級使用舊版的 `<dependencies>` 來設定 Lombok。
      - 必須將 `<annotationProcessorPaths>` 區塊 **嚴格放置在 `<configuration>` 標籤內部**。
      - 建立 `<path>` 標籤，並在其中設定 `groupId` 為 `org.projectlombok`，`artifactId` 為 `lombok`，且 `<version>` 必須綁定 `${lombok.version}`。
  - 設定 `org.springframework.boot:spring-boot-maven-plugin`
    - **版本綁定**：
      - `<version>` 必須綁定 `${spring-boot-maven-plugin.version}`，維持版本集中管理
    - **核心打包機制 (`<executions>`)**：
      - 必須設定 `<execution>` 區塊。
      - 在 `<goals>` 中，必須加入 `<goal>repackage</goal>`。
      - 說明：此設定確保 Maven 打包時能將所有相依套件與內建伺服器（如 Tomcat）一併打包，產生可直接以 `java -jar` 啟動的 Fat Jar。
  - 設定 `org.apache.maven.plugins:maven-surefire-plugin`
    - **版本綁定**：
      - `<version>` 必須綁定 `${maven-surefire-plugin.version}`，維持版本集中管理
    - **測試運行環境配置 (`<configuration>`)**：
      - 必須明確設定 `<useModulePath>false</useModulePath>`。
      - 說明：此設定為強制關閉 Java 模組路徑 (JPMS)，讓測試在傳統的 Classpath 下執行。這是為了防止 JUnit 5 與 Mockito 執行測試時，產生類別載入與權限 (Visibility) 的衝突。
  - 設定 `org.graalvm.buildtools:native-maven-plugin`
    - **核心配置 (`<configuration>`) 變數綁定**：
      - `<mainClass>` 必須綁定 `${start-class}`
      - `<imageName>` 必須綁定 `${project.artifactId}`，確保編譯出的執行檔名稱與專案一致
      - 必須設定 `<buildArgs>`，並加入 `<buildArg>--no-fallback</buildArg>` 參數
      - 說明：`--no-fallback` 是強制要求 GraalVM 產生獨立的原生執行檔，若遇到無法原生的動態反射程式碼則直接報錯，絕不退回傳統 JVM 模式
    - **建置生命週期綁定 (`<executions>`)**：
      - 建立一個 id 為 `build-native` 的 execution。
      - 將目標 `<goal>compile-no-fork</goal>` 明確綁定到 Maven 的 `<phase>package</phase>` 階段
      - 說明：這確保了當開發者執行 `mvn clean package` 且啟用相關 Profile 時，Maven 會自動觸發 AOT (提前編譯) 與原生打包流程
  - 設定 `org.jacoco:jacoco-maven-plugin`
    - **版本綁定**：
      - `<version>` 必須綁定 `${jacoco-maven-plugin.version}`，維持版本集中管理。
    - **代理程式與報告產生 (`<executions>`)**：
      - 必須設定一個 id 為 `prepare-agent` 的 execution，在 `<goals>` 中加入 `<goal>prepare-agent</goal>`。
      - 必須設定一個 id 為 `report` 的 execution，並將階段綁定為 `<phase>test</phase>`，在 `<goals>` 中加入 `<goal>report</goal>`。
      - 說明：這兩段設定確保在測試執行前啟動 JaCoCo Agent 收集資料，並在測試階段完成後自動產生測試覆蓋率報表。
    - **測試覆蓋率嚴格檢驗 (`<execution id="jacoco-check">`)**：
      - 必須設定一個 id 為 `jacoco-check` 的 execution。
      - 必須將階段綁定為 `<phase>verify</phase>`，並在 `<goals>` 中加入 `<goal>check</goal>`。
    - **嚴格規則配置 (`<configuration> / <rules>`)**：
      - 必須建立一個 `<rule>`，設定 `<element>CLASS</element>` 以類別為檢驗單位。
      - 必須在 `<limits>` 中加入一個 `<limit>`，設定 `<counter>LINE</counter>` 與 `<value>COVEREDRATIO</value>`。
      - 必須設定 `<minimum>1.00</minimum>`。
      - 說明：這項設定會在專案執行 `mvn verify` 時，強制檢查每個 Class 的行覆蓋率 (Line Coverage)。若未達到 100% (1.00)，將會阻斷 Maven 的建置流程，確保程式碼品質。
