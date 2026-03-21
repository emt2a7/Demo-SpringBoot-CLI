package org.example.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GithubSearchIssueResponse(
        @JsonProperty("total_count")
        int totalCount,

        List<GithubSearchIssueDto> items
) {}