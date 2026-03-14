package org.example.framework.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
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
 * WordImageExtractor 是一個專門用來從 Word (.docx) 文件中提取圖片的工具類別。
 * 主要功能是讀取 Word 檔案，找到其中的圖片，將圖片存到本地資料夾，並且回傳一個圖片路徑清單。
 *
 * 使用說明：
 * 1. 呼叫 extractImages() 方法，傳入 Word 檔案的 Resource 和圖片要存放的資料夾路徑。
 * 2. 方法會自動處理 Word 解析、圖片提取、檔案命名和儲存，最後回傳一個 List 結構，包含所有提取到的圖片實體路徑。
 * 3. 處理 Word 我們用的是 Apache POI library。
 *
 * 注意事項：
 * - 確保輸入的 Word 檔案存在且可讀取，且必須是 .docx 格式（不支援 .doc）。
 * - 確保指定的輸出資料夾路徑存在或有權限創建。
 * - 提取過程中可能會遇到無法解析的 Word 結構或損壞的檔案，請做好例外處理。
 */
@Slf4j
@Component
public class WordImageExtractor {

    /**
     * 讀取 word 檔案中所有圖片的實體路徑。
     *
     * @param wordResource 要處理的 Word 檔案 (必須是 .docx 格式)
     * @param outputDirPath 圖片要存放的本地資料夾路徑
     * @return 回傳該 Word 檔案內的所有「實體圖片路徑清單」
     */
    public List<String> extractImages(Resource wordResource, String outputDirPath) {
        List<String> imagePaths = new ArrayList<>();

        try {
            // 確保輸出資料夾存在
            Path outputDir = Paths.get(outputDirPath);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // 使用 Apache POI 的 XWPFDocument 讀取 Word 檔案串流
            try (InputStream is = wordResource.getInputStream();
                 XWPFDocument document = new XWPFDocument(is)) {

                // 直接獲取文件內的所有圖片！
                List<XWPFPictureData> pictures = document.getAllPictures();

                int imageIndex = 1;
                String originalFileName = wordResource.getFilename() != null ?
                        wordResource.getFilename().replace(".docx", "") : "word_doc";

                for (XWPFPictureData pic : pictures) {
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
                    log.info("📝 成功從 Word 挖出圖片: {}", outputFile.getAbsolutePath());

                    imageIndex++;
                }
            }
        } catch (Exception e) {
            log.error("❌ Word 挖圖過程發生嚴重錯誤: {}", e.getMessage(), e);
        }
        return imagePaths;
    }
}