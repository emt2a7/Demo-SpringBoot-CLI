package org.example.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubSearchIssueDto(
        String title,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("repository_url") String repositoryUrl, // 用來推算專案名稱
        String state,
        @JsonProperty("created_at") String createdAt,
        User user
) {
    // 內部 Record：對應發起 PR 的作者
    public record User(String login) {}
}
