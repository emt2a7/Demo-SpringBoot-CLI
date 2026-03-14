package org.example.framework.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PdfImageExtractor 是一個專門用來從 PDF 文件中提取圖片的工具類別。
 * 主要功能是讀取 PDF 檔案，找到其中的圖片，將圖片存到本地資料夾，並且回傳一個 Map 結構，讓使用者知道每頁有哪些圖片。
 *
 * 使用說明：
 * 1. 呼叫 extractImagesAndMapToPages() 方法，傳入 PDF 檔案的 Resource 和圖片要存放的資料夾路徑。
 * 2. 方法會自動處理 PDF 解析、圖片提取、檔案命名和儲存，最後回傳一個 Map 結構，Key 是頁碼，Value 是該頁的圖片路徑清單。
 * 3. 處理 PDF 我們用的是 PDFBox library。
 *
 * 注意事項：
 * - 確保輸入的 PDF 檔案存在且可讀取。
 * - 確保指定的輸出資料夾路徑存在或有權限創建。
 * - 提取過程中可能會遇到無法解析的 PDF 結構或損壞的檔案，請做好例外處理。
 */
@Slf4j
@Component
public class PdfImageExtractor {

    /**
     * 讀取 pdf 檔案中所有圖片的實體路徑，並且按照「頁碼」對應到圖片清單。
     *
     * @param pdfResource 要處理的 PDF 檔案
     * @param outputDirPath 圖片要存放的本地資料夾路徑 (例如 "D:/pdf_images")
     * @return 回傳 Map: Key 是「頁碼」，Value 是「該頁的所有圖片實體路徑清單」
     */
    public Map<Integer, List<String>> extractImagesAndMapToPages(Resource pdfResource, String outputDirPath) {
        Map<Integer, List<String>> pageImagesMap = new HashMap<>();

        try {
            // 1. 確保輸出資料夾存在，不存在就建立
            Path outputDir = Paths.get(outputDirPath);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // 2. 💡 呼叫 PDFBox 3.x 的 Loader 讀取 PDF (使用位元組陣列最安全，避免檔案鎖定)
            byte[] pdfBytes = pdfResource.getContentAsByteArray();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                int pageNum = 1; // 💡 PDFBox 預設是 foreach，我們自己維護頁碼(從 1 開始)

                // 3. 逐頁掃描 PDF
                for (PDPage page : document.getPages()) {
                    PDResources resources = page.getResources();
                    List<String> imagesOnThisPage = new ArrayList<>();

                    int imageIndex = 1;

                    // 4. 尋找該頁所有的 XObject (PDF 裡的外部物件，包含圖片)
                    for (COSName xObjectName : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject(xObjectName);

                        // 5. 判斷這個物件是不是圖片
                        if (xObject instanceof PDImageXObject image) {

                            // 取得副檔名 (png, jpg 等)
                            String suffix = image.getSuffix();
                            if (suffix == null || suffix.isEmpty()) {
                                suffix = "png"; // 預設存為 PNG
                            }

                            // 6. 產生不會重複的唯一檔名 (檔名_頁碼_序號_UUID.png)
                            String originalFileName = pdfResource.getFilename() != null ? pdfResource.getFilename().replace(".pdf", "") : "doc";
                            String imageFileName = String.format("%s_page%d_img%d_%s.%s",
                                    originalFileName, pageNum, imageIndex, UUID.randomUUID().toString().substring(0, 5), suffix);

                            File outputFile = new File(outputDir.toFile(), imageFileName);

                            // 7. 將圖片真實寫入硬碟！
                            ImageIO.write(image.getImage(), suffix, outputFile);

                            // 紀錄這張圖片的絕對路徑
                            imagesOnThisPage.add(outputFile.getAbsolutePath());
                            log.info("📸 成功挖出圖片: {}", outputFile.getAbsolutePath());

                            imageIndex++;
                        }
                    }

                    // 8. 如果這頁有挖到圖片，就把清單放進 Map 裡
                    if (!imagesOnThisPage.isEmpty()) {
                        pageImagesMap.put(pageNum, imagesOnThisPage);
                    }

                    pageNum++;
                }
            }
        } catch (Exception e) {
            log.error("❌ PDF 挖圖過程發生嚴重錯誤: {}", e.getMessage(), e);
        }

        return pageImagesMap;
    }
}