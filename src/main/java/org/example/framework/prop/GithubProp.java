package org.example.framework.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.math.BigDecimal;

/**
 * 自動生成的配置類別：github
 * <p>本類別由程式碼產生器自動維護，請勿手動修改。</p>
 */
@ConfigurationProperties(prefix = "github")
public record GithubProp(
        String allReposUri,

        String baseUrl,

        String dispatchesUri,

        String godToken,

        BigDecimal messageLimit,

        String username,

        String workflowsUri
) {
}
