package org.example.framework.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.math.BigDecimal;

/**
 * 自動生成的配置類別：telegram
 * <p>本類別由程式碼產生器自動維護，請勿手動修改。</p>
 */
@ConfigurationProperties(prefix = "telegram")
public record TelegramProp(
        String apiUrl,

        BigDecimal messageLimit,

        String sendUrl,

        String token
) {
}
