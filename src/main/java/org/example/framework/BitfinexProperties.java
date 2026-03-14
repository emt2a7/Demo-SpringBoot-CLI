package org.example.framework;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自動生成的 Properties 類別，來源：src/main/resources/application.yml
 *
 * 生成時間：2026-03-09
 *
 * 此類別使用 JDK 25 的 record 語法表示 bitfinex 根節點下的屬性。
 * 若某屬性在不同 environment 有型別衝突，已記錄於 target/generated-properties-conflicts.log。
 *
 * @param apiKey      bitfinex.api.key
 *
 * @param apiSecret   bitfinex.api.secret
 *
 * @param apiAlgorithm 登入密碼的演算法 (bitfinex.api.algorithm)
 *
 * @param webSocketUri webSocket URI (bitfinex.webSocket.uri)
 */
@ConfigurationProperties(prefix = "bitfinex")
public record BitfinexProperties(

    String apiKey,

    String apiSecret,

    String apiAlgorithm,

    String webSocketUri

) {
    // ...existing code...
}
