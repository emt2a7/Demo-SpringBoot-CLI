package org.example.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 接收單筆 GitHub 未讀通知 (Notifications)
 */
public record GithubNotificationDto(
        String id,
        Repository repository,
        Subject subject,
        String reason,  // 收到通知的原因 (例如: mention, comment, assign)
        @JsonProperty("updated_at")
        String updatedAt
) {
    // 內部 Record：對應 repository 欄位
    public record Repository(
            String name,
            @JsonProperty("full_name") String fullName
    ) {}

    // 內部 Record：對應 subject 欄位
    public record Subject(
            String title,
            String type,  // 類型 (例如: Issue, PullRequest, Release)
            String url
    ) {}
}
