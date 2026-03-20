package org.example.framework.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自動生成的配置類別：bitfinex
 * <p>本類別由程式碼產生器自動維護，請勿手動修改。</p>
 */
@ConfigurationProperties(prefix = "bitfinex")
public record BitfinexProp(
        String apiAlgorithm,

        String apiKey,

        String apiSecret,

        String webSocketUri
) {
}
