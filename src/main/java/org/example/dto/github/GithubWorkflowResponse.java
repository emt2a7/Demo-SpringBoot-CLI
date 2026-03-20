package org.example.dto.github;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubWorkflowResponse(
        @JsonProperty("total_count") int totalCount,
        List<GithubWorkflowDto> workflows
) {}
