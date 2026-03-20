package codegen;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 自動化配置類別產生器 (YAML to Java Record)
 * <p>
 * 負責掃描資源目錄下的 yml 檔案，並動態生成 JDK 21 Record 類別。
 */
public class GenMyApplication {

    // =========================================================================
    // ⚙️ [維護清單] 核心設定與規範 (OOP 設計)
    // =========================================================================
    public record GeneratorConfig(
            String resourceDir,        // YAML 檔案來源目錄
            String outputDir,          // Java 檔案輸出目錄
            String packageName,        // 產生的 Java 類別的 Package 名稱
            String classSuffix,        // 類別名稱後綴
            Set<String> excludePrefixes // 框架保留字，不進行類別生成
    ) {
        public static GeneratorConfig defaultConfig() {
            String resourceDir          = "src/main/resources";                                         // YAML 檔案來源目錄
            String outputDir            = "src/main/java/org/example/framework/prop";                   // Java 檔案輸出目錄
            String packageName          = "org.example.framework.prop";                                 // 產生的 Java 類別的 Package 名稱
            String classSuffix          = "Prop";                                                       // 類別名稱後綴
            Set<String> excludePrefixes =
                    Set.of("spring", "logging", "server", "management", "endpoints", "debug", "trace"); // 框架保留字，不進行類別生成
            return new GeneratorConfig(resourceDir, outputDir, packageName, classSuffix, excludePrefixes);
        }
        // 檢查該根節點是否屬於排除清單
        public boolean isExcluded(String rootKey) {
            return excludePrefixes.contains(rootKey);
        }
    }

    // =========================================================================
    // 🚀 主程式進入點
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("🚀 開始執行自動化配置類別產生器...");
        GeneratorConfig config = GeneratorConfig.defaultConfig();

        try {
            // 1. 取得所有 YAML 檔案
            List<File> ymlFiles = findYamlFiles(config.resourceDir());
            if (ymlFiles.isEmpty()) {
                System.out.println("⚠️ 在 " + config.resourceDir() + " 中找不到任何 .yml 檔案，程式結束。");
                return;
            }

            // 2. 擷取 YAML 註解 (用於生成 JavaDoc)
            Map<String, String> commentsMap = extractCommentsFromYaml(ymlFiles);

            // 3. 使用 Spring Boot 內建解析器取得展平後的 Properties (自動去重與聯集)
            Properties properties = loadYamlProperties(ymlFiles);

            // 4. 分群與資料轉換 (以第一層 Root Key 為群組)
            Map<String, List<PropertyMeta>> groupedProps = groupProperties(properties, commentsMap, config);

            // 5. 動態生成 Java Record 檔案
            generateJavaFiles(groupedProps, config);

            System.out.println("✅ 程式碼產生作業順利完成！");

        } catch (Exception e) {
            System.err.println("❌ 發生預期外的錯誤，程式終止！錯誤訊息: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // =========================================================================
    // 🛠️ 內部邏輯與輔助類別
    // =========================================================================

    /**
     * 尋找指定目錄下的所有 .yml 檔案
     */
    private static List<File> findYamlFiles(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) return Collections.emptyList();

        return Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    /**
     * 使用 Spring Boot 的 YamlPropertiesFactoryBean 載入並展平所有配置
     */
    private static Properties loadYamlProperties(List<File> files) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        Resource[] resources = files.stream()
                .map(FileSystemResource::new)
                .toArray(Resource[]::new);
        factory.setResources(resources);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * 解析 YAML 檔案，將註解 (#) 綁定到對應的完整 Key 路徑上
     */
    private static Map<String, String> extractCommentsFromYaml(List<File> files) throws IOException {
        Map<String, String> commentsMap = new HashMap<>();
        Pattern keyPattern = Pattern.compile("^(\\s*)([a-zA-Z0-9_.-]+):.*$");
        Pattern commentPattern = Pattern.compile("^\\s*#\\s*(.*)$");

        for (File file : files) {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            StringBuilder currentComment = new StringBuilder();
            Map<Integer, String> pathStack = new HashMap<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                Matcher commentMatcher = commentPattern.matcher(line);
                if (commentMatcher.matches()) {
                    if (!currentComment.isEmpty()) currentComment.append("\n");
                    currentComment.append(commentMatcher.group(1).trim());
                    continue;
                }

                Matcher keyMatcher = keyPattern.matcher(line);
                if (keyMatcher.matches()) {
                    int indent = keyMatcher.group(1).length();
                    String key = keyMatcher.group(2);
                    int level = indent / 2; // 假設 YAML 縮排為 2 空格

                    pathStack.put(level, key);
                    // 清除更深層的路徑
                    pathStack.keySet().removeIf(k -> k > level);

                    if (!currentComment.isEmpty()) {
                        String fullKey = pathStack.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(Map.Entry::getValue)
                                .collect(Collectors.joining("."));
                        commentsMap.put(fullKey, currentComment.toString());
                        currentComment.setLength(0); // 重置
                    }
                } else {
                    currentComment.setLength(0); // 非註解且非 Key 行，清除暫存的註解
                }
            }
        }
        return commentsMap;
    }

    /**
     * 將扁平化的 Properties 進行分群與型別推斷
     */
    private static Map<String, List<PropertyMeta>> groupProperties(
            Properties properties, Map<String, String> commentsMap, GeneratorConfig config) {

        Map<String, List<PropertyMeta>> grouped = new HashMap<>();

        // 🚨 修正：改用 entrySet() 才能抓到被 SnakeYAML 解析為 Integer/Boolean 的純數字與布林值
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String fullKey = String.valueOf(entry.getKey());

            int firstDotIndex = fullKey.indexOf('.');
            if (firstDotIndex == -1) continue; // 忽略沒有子節點的屬性

            String rootKey = fullKey.substring(0, firstDotIndex);
            if (config.isExcluded(rootKey)) continue;

            String remainingKey = fullKey.substring(firstDotIndex + 1);

            // 🚨 修正：使用 String.valueOf 強制將 Integer 等轉回字串，供後續型別推斷
            String value = String.valueOf(entry.getValue());
            String comment = commentsMap.getOrDefault(fullKey, "");

            String camelCaseName = toCamelCase(remainingKey);
            String javaType = inferJavaType(value);

            PropertyMeta meta = new PropertyMeta(camelCaseName, javaType, comment);
            grouped.computeIfAbsent(rootKey, k -> new ArrayList<>()).add(meta);
        }

        // 確保欄位按照字母排序，維持產出程式碼的穩定性
        grouped.values().forEach(list -> list.sort(Comparator.comparing(PropertyMeta::fieldName)));
        return grouped;
    }

    /**
     * 產出 Java Record 檔案
     */
    private static void generateJavaFiles(Map<String, List<PropertyMeta>> groupedProps, GeneratorConfig config) throws IOException {
        Path outputDirPath = Paths.get(config.outputDir());
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
            System.out.println("📁 建立輸出目錄: " + outputDirPath.toAbsolutePath());
        }

        for (Map.Entry<String, List<PropertyMeta>> entry : groupedProps.entrySet()) {
            String rootKey = entry.getKey();
            List<PropertyMeta> fields = entry.getValue();

            String className = capitalize(rootKey) + config.classSuffix();
            Path filePath = outputDirPath.resolve(className + ".java");

            StringBuilder javaCode = new StringBuilder();

            // 1. Package 宣告
            javaCode.append("package ").append(config.packageName()).append(";\n\n");

            // 2. Imports
            javaCode.append("import org.springframework.boot.context.properties.ConfigurationProperties;\n");
            if (fields.stream().anyMatch(f -> f.javaType().equals("BigDecimal"))) {
                javaCode.append("import java.math.BigDecimal;\n");
            }
            javaCode.append("\n");

            // 3. Class JavaDoc & Annotation
            javaCode.append("/**\n");
            javaCode.append(" * 自動生成的配置類別：").append(rootKey).append("\n");
            javaCode.append(" * <p>本類別由程式碼產生器自動維護，請勿手動修改。</p>\n");
            javaCode.append(" */\n");
            javaCode.append("@ConfigurationProperties(prefix = \"").append(rootKey.toLowerCase()).append("\")\n");

            // 4. Record 宣告
            javaCode.append("public record ").append(className).append("(\n");

            // 5. 欄位宣告
            for (int i = 0; i < fields.size(); i++) {
                PropertyMeta field = fields.get(i);

                // 產生 JavaDoc
                if (!field.comment().isEmpty()) {
                    javaCode.append("        /**\n");
                    for (String commentLine : field.comment().split("\n")) {
                        javaCode.append("         * ").append(commentLine).append("\n");
                    }
                    javaCode.append("         */\n");
                }

                // 產生欄位
                javaCode.append("        ").append(field.javaType()).append(" ").append(field.fieldName());

                if (i < fields.size() - 1) {
                    javaCode.append(",\n\n"); // 保留空行提升易讀性
                } else {
                    javaCode.append("\n");
                }
            }

            javaCode.append(") {\n}\n");

            // 寫入檔案
            Files.writeString(filePath, javaCode.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("📄 成功生成檔案: " + className + ".java");
        }
    }

    // =========================================================================
    // 🧰 工具方法區
    // =========================================================================

    /**
     * 屬性中介資料結構
     */
    private record PropertyMeta(String fieldName, String javaType, String comment) {}

    /**
     * 型別推斷邏輯
     */
    private static String inferJavaType(String value) {
        if (value == null || value.isBlank()) return "String";
        value = value.trim();

        // 判斷是否為布林值 (支援 yes/no, true/false, on/off, 1/0, Y/N)
        if (value.matches("^(?i)(true|false|yes|no|on|off|1|0|y|n)$")) {
            return "Boolean";
        }
        // 判斷是否為數值型態 (支援整數與小數)
        if (value.matches("^-?\\d+(\\.\\d+)?$")) {
            return "BigDecimal";
        }
        return "String";
    }

    /**
     * 將帶有符號的字串轉換為駝峰式命名 (Camel Case)
     * 例如: api-key -> apiKey, db.pool.size -> dbPoolSize
     */
    private static String toCamelCase(String text) {
        String[] parts = text.split("[\\.\\-_]");
        StringBuilder camelCaseString = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(capitalize(parts[i]));
        }
        return camelCaseString.toString();
    }

    /**
     * 首字母大寫轉換
     */
    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}