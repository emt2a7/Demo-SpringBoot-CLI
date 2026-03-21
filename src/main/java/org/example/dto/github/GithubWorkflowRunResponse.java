package org.example.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 接收 GitHub Workflow Runs API 的完整回應
 */
public record GithubWorkflowRunResponse(
        @JsonProperty("total_count")
        int totalCount,

        @JsonProperty("workflow_runs")
        List<GithubWorkflowRunDto> workflowRuns
) {
}
