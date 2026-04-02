package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.VectorMainMetadata;
import org.example.entity.VectorMain;
import org.example.service.VectorMainService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 專為 CLI 命令列模式設計的 Controller
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorMainCliController {

    private final VectorMainService service;

    /**
     * 提供給 Main.java 呼叫的示範入口
     */
    public void run() {
        log.info("=== 開始執行 VectorMain CRUD 展示 ===");

        // 1. Create (模擬上傳檔案)
        String demoFileName = "test-doc-" + System.currentTimeMillis() + ".txt";
        byte[] dummyData = "這是一段模擬的 PDF 或 Word 二進位內容".getBytes(StandardCharsets.UTF_8);
        VectorMain saved = service.uploadFile("報告", demoFileName, dummyData);
        log.info("成功寫入資料庫，分配的 UUID: {}", saved.getId());

        // 2. Read (讀取輕量清單，驗證避免 OOM 設計)
        log.info("--- 讀取檔案清單 (無 BYTEA) ---");
        List<VectorMainMetadata> metadataList = service.getAllFilesMetadata();
        for (VectorMainMetadata meta : metadataList) {
            log.info("發現檔案: [{}] {} ({} bytes), 上傳於: {}",
                    meta.category(), meta.fileName(), meta.fileSize(), meta.uploadTime());
        }

        // 3. Read Data (模擬使用者點擊下載)
        log.info("--- 模擬下載剛才上傳的檔案 ---");
        byte[] downloadedData = service.downloadFileData(saved.getId());
        log.info("成功取出二進位資料，還原內容: {}", new String(downloadedData, StandardCharsets.UTF_8));

        // 4. Delete
        log.info("--- 執行資料清理 ---");
        service.deleteFile(saved.getId());
        log.info("=== 執行完畢 ===");
    }
}