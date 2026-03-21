package org.example.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 接收單筆 Workflow 執行紀錄
 */
public record GithubWorkflowRunDto(
        Long id,
        String name,
        String status,      // 狀態: queued, in_progress, completed
        String conclusion,  // 結果: success, failure, cancelled, skipped 等 (執行中會是 null)
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("html_url")
        String htmlUrl
) {
}
