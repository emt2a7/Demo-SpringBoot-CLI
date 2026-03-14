# Demo SpringBoot CLI — 高效能 AI CLI 工具

> 一個基於 Spring Boot + Spring AI 的企業級 CLI 工具，採用 GraalVM Native Image 打包，啟動速度極速（數十毫秒級）。

## 🚀 專案簡介

本專案是一個為企業場景設計的 AI 命令列工具，結合了 Spring Boot（AOT）、Spring AI 與 GraalVM Native Image 技術。
透過 AOT 與 GraalVM 將應用編譯為原生二進位檔，徹底解決 Java 傳統啟動慢的痛點，啟動時間可縮短至數十毫秒，適合在 CLI、Serverless 或短命週期任務中使用。

本工具提供一系列 Agent 範例與實作（包含對話記憶、結構化輸出、以及文件解析與 RAG 向量化流程），可直接整合 PostgreSQL（搭配 pgvector）、文件解析器、與聊天模型。

## 🛠️ 核心技術棧

- Java 25
- Spring Boot 4.x（AOT friendly）
- Spring AI（ChatClient Fluent API）
- GraalVM Native Image（編譯為原生二進位）
- PostgreSQL + pgvector（向量資料庫）
- 常用工具：Apache Commons、Lombok、Jackson（由 Spring 管理）

## ✨ 核心功能（從 `Main.java` 的範例方法註解擷取）

以下為程式碼中已實作或示範的 Agent / 功能：

- 帶有記憶的對話（ChatMemoryController）
  - 支援保存與查詢對話記憶，讓多輪對話具有上下文記憶能力。

- 將 AI 回覆轉換為結構化輸出（StructuredController）
  - 將 LLM 回覆解析、結構化，便於後續自動化處理或儲存。

- 上傳 PDF 到 RAG 向量資料庫（Rag01Controller）
  - 將單一 PDF 檔案轉換為向量並上傳至向量資料庫以供檢索。

- 上傳 PDF/Word/Excel/TXT 並啟用文字解析工具（Rag02Controller）
  - 支援多種文件型別的文字抽取並向量化（包含 txt、docx、xlsx）。

- 上傳 PDF/Word/Excel/TXT 並啟用文字與圖片解析（Rag03Controller）
  - 除了文字，也解析嵌入圖片（OCR/圖像標註）並一併上傳向量庫。

- 處理 PDF/Word/Excel 的特殊 thought_signature 問題（Rag04Controller / Rag05Controller / Rag06Controller）
  - 範例包含針對不同文件型別（pdf / word / excel）進行進階處理與 signature 問題補救機制。

（上述功能對應的 Controller 類別位於 `src/main/java/org/example/controller/`）

## 🏗️ 如何編譯與執行

此專案已啟用 Maven AOT 與 GraalVM Native Image 的建置流程，請依下列步驟進行：

1. 建議使用 Maven Wrapper（若專案未包含 `mvnw`，請使用系統的 Maven）。

2. 在乾淨的環境中執行 AOT 原生編譯：

```powershell
mvn clean -P"native,prod" package -DskipTests
```

- 命令說明：
  - `-P"native,prod"`：啟用 `native` 與 `prod` profile（專案內可能配置 AOT / Native 與生產環境資源過濾）。
  - `-DskipTests`：略過測試以加速原生映像產出。

3. 編譯完成後，執行產出的二進位檔或備援 JAR：

- Windows（若輸出為 .exe）：

```powershell
.
# 範例（請以實際產物名稱替換）
cd target; .\Demo-SpringBoot-CLI.exe --mode=agent --otherArg value
```

- Linux / macOS（若輸出為原生 ELF 二進位）：

```bash
cd target; ./Demo-SpringBoot-CLI --mode=agent --otherArg value
```

- 備援（JAR 模式）：

```powershell
java -jar target/Demo-SpringBoot-CLI-1.0-SNAPSHOT.jar --mode=agent
```

4. CLI 參數提示：

- `--mode=agent`：示範以 agent 模式啟動（程式內會解析 `--mode` 參數）。
- 其他非 `--` 前綴的純文字參數會被收集於 `args.getNonOptionArgs()`。

## ☁️ CI/CD 雲端編譯

本倉庫已配置 GitHub Actions（或其他 CI）以自動化原生編譯流程：

- 推送程式碼後，CI 會在 Linux 雲端環境執行 AOT + GraalVM 的建置，產出極速的原生二進位檔（適合部署於容器或作為下載資產）。

- 若需針對不同平台產出二進位檔，建議在 CI 中使用跨平台的 runner（或透過交叉編譯映像）。

## 📦 發行與相容性

- 相容 Java 25 與 Spring Boot 4.x。
- 使用 Spring AI BOM 管理 Spring AI 相關版本（專案依賴由 BOM 統一管理）。
- 向量資料庫建議使用 PostgreSQL + pgvector（請確保資料庫已啟用相關 extension 並正確設定連線）。

## 🛡️ 運維與安全建議

- 請將敏感設定（如資料庫密碼、API Keys）放在 `application-prod.yml` 並在 CI/CD 的 secrets 管理中注入，不要將生產機密打包入 artifact。
- 建議在 `pom.xml` 中保留 profile 資源過濾設定（例如 `dev` / `prod`），以避免不同環境的敏感檔案混入。

## 🤝 貢獻指南

歡迎 PR 與 Issue，請遵守以下約定：

- 使用繁體中文撰寫重要註解與 Commit Message。
- 所有新增功能請附上單元測試（不得引入 JUnit4/Vintage）。
- 新增依賴請遵循專案的 BOM 與 properties 管理慣例。

---

若需要我產生更詳細的使用範例（例如：如何連接 pgvector、如何將具體文件上傳並執行 RAG 檢索範例），或需要我幫你把 `README` 擴充成多語系版本（EN / 繁中），請告訴我要加哪些範例或目錄結構，我會繼續補齊。
