package org.example.framework.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自動生成的配置類別：rag
 * <p>本類別由程式碼產生器自動維護，請勿手動修改。</p>
 */
@ConfigurationProperties(prefix = "rag")
public record RagProp(
        String toolsBaby,

        String toolsHr,

        String toolsIt
) {
}
