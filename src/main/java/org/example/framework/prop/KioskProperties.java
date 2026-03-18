package org.example.framework.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自動生成的 Properties 類別，來源：src/main/resources/application-dev.yml, application-prod.yml
 *
 * 生成時間：2026-03-09
 *
 * 此類別使用 JDK 25 的 record 語法表示 kiosk 根節點下的屬性。
 * kiosk.clear.* 的 enabled 欄位已依照 yes/no/Y/N/0/1 形式轉為 Boolean。
 * 若發現跨環境型別衝突，請參考 target/generated-properties-conflicts.log。
 *
 * @param root           kiosk.root
 *
 * @param home           kiosk.home
 *
 * @param clearDbEnabled kiosk.clear.db.enabled
 *
 * @param clearFileEnabled kiosk.clear.file.enabled
 */
@ConfigurationProperties(prefix = "kiosk")
public record KioskProperties(

    String token,

    String root,

    String home,

    Boolean clearDbEnabled,

    Boolean clearFileEnabled

) {
    // ...existing code...
}
