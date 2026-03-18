package org.example.framework.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Telegram 設定 Properties。
 *
 * 來源：`application.yml` 中的 telegram 區塊。
 * 此類別為不可變的 record，透過 Spring Boot 的 @ConfigurationProperties 綁定。
 */
@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(

    /**
     * 透過環境變數注入，冒號後面留空代表預設為空字串 (防呆，避免沒設 Token 時啟動報錯)
     */
    String botToken,

    /**
     * Telegram Bot API 的基礎 URL
     */
    String botUrlRoot,

    /**
     * Telegram Bot API 的發送訊息路徑
     */
    String botUrlSend

) {
}

