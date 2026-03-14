package org.example.framework.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ExcelImageExtractor 是一個專門用來從 Excel (.xlsx) 文件中提取圖片的工具類別。
 *
 * 功能說明：
 * - 讀取指定的 Excel 文件，遍歷其中的所有圖片。
 * - 將每張圖片保存為獨立的實體檔案，並返回這些檔案的路徑清單。
 * - 使用 Apache POI library 來處理 Excel 文件的解析和圖片提取。
 *
 * 使用場景：
 * - 當需要從 Excel 文件中提取圖片並進行後續處理（如上傳至 RAG 向量資料庫）時，可以使用此類別。
 *
 * 注意事項：
 * - 目前僅支援 .xlsx 格式的 Excel 文件，不支援舊版 .xls 格式。
 * - 輸出圖片的檔名會包含原始 Excel 檔名、圖片索引以及隨機字串，以確保不會有檔名衝突。
 */
@Slf4j
@Component
public class ExcelImageExtractor {

    /**
     * 讀取 excel 檔案中所有圖片的實體路徑。
     *
     * @param excelResource 要處理的 excel 檔案 (必須是 .xlsx 格式)
     * @param outputDirPath 圖片要存放的本地資料夾路徑
     * @return 回傳該 Word 檔案內的所有「實體圖片路徑清單」
     */
    public List<String> extractImages(Resource excelResource, String outputDirPath) {
        List<String> imagePaths = new ArrayList<>();

        try {
            // 確保輸出資料夾存在
            Path outputDir = Paths.get(outputDirPath);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // 使用 Apache POI 的 XSSFWorkbook 讀取 Excel 檔案串流
            try (InputStream is = excelResource.getInputStream();
                 XSSFWorkbook workbook = new XSSFWorkbook(is)) {

                // 直接獲取整個 Excel 檔案內的所有圖片！(跨越所有 Sheet)
                List<XSSFPictureData> pictures = workbook.getAllPictures();

                int imageIndex = 1;
                String originalFileName = excelResource.getFilename() != null ?
                        excelResource.getFilename().replace(".xlsx", "") : "excel_doc";

                for (XSSFPictureData pic : pictures) {
                    // 取得圖片的二進位資料與副檔名 (png, jpeg 等)
                    byte[] pictureBytes = pic.getData();
                    String extension = pic.suggestFileExtension();

                    // 組合不重複的檔名
                    String imageFileName = String.format("%s_img%d_%s.%s",
                            originalFileName,
                            imageIndex,
                            UUID.randomUUID().toString().substring(0, 5),
                            extension);

                    File outputFile = new File(outputDir.toFile(), imageFileName);

                    // 寫入實體檔案到硬碟
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        fos.write(pictureBytes);
                    }

                    // 紀錄成功挖出的圖片路徑
                    imagePaths.add(outputFile.getAbsolutePath());
                    log.info("📊 成功從 Excel 挖出圖片: {}", outputFile.getAbsolutePath());

                    imageIndex++;
                }
            }
        } catch (Exception e) {
            log.error("❌ Excel 挖圖過程發生嚴重錯誤: {}", e.getMessage(), e);
        }
        return imagePaths;
    }
}