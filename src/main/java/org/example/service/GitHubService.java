package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AiResponseWrapper;
import org.example.dto.github.*;
import org.example.framework.prop.GithubProp;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GithubProp githubProp;
    private final RestClient.Builder restClientBuilder;
    private final AiChatService aiChatService;

    /**
     * 與 AI 進行對話，並取得回應
     * @param userPrompt
     * @return
     */
    public AiResponseWrapper<String> chat(String userPrompt) {
        // 系統提示詞
        String systemPrompt = """
        你是一個專業的 AI 助理。

        【重要規則】
        1. 你的回答內容請務必精簡，絕對不可超過 %d 個字元，以免超出系統的傳送限制。
        2. 你的回覆將傳送至通訊軟體，請一律使用「純文字 (Plain Text)」回答。
        3. 絕對禁止在回答中使用任何 Markdown 排版符號（嚴禁出現 **粗體**、*斜體*、`標記` 等符號）。
        """.formatted(githubProp.messageLimit().intValue());
        return aiChatService.chat(systemPrompt, userPrompt);
    }

    /**
     * 查詢待我審核的 Pull Request (Search API)
     */
    public GithubSearchIssueResponse searchPR() {
        log.info("🔍 正在查詢 [{}] 待審核的 Pull Request (PR)...", githubProp.username());

        // 組裝 GitHub 搜尋語法 (狀態為 open + 類型是 PR + 被要求 review 的人是自己)
        String query = "is:open is:pr review-requested:" + githubProp.username();

        return getrestclientclassic().get()
                .uri(uriBuilder -> uriBuilder
                        .path(githubProp.searchIssuesUri())
                        .queryParam("q", query)
                        .queryParam("per_page", 10) // 預設抓最新 10 筆
                        .build())
                .retrieve()
                .body(org.example.dto.github.GithubSearchIssueResponse.class);
    }

    /**
     * 查詢專案流量與訪客統計 (Traffic Views)
     * @param repoName 專案名稱
     */
    public GithubTrafficViewsDto getTrafficView(String repoName) {
        log.info("📈 正在查詢專案 [{}] 的流量統計...", repoName);

        return getrestclientclassic().get()
                // 將 URI 中的 {repo} 替換為實際專案名稱
                .uri(githubProp.trafficViewsUri(), repoName)
                .retrieve()
                .body(org.example.dto.github.GithubTrafficViewsDto.class);
    }

    /**
     * 查詢我的帳號下所有專案清單
     */
    public List<GithubRepoDto> listAllMyRepo() {
        log.info("🔍 正在掃描 [{}] 的所有 GitHub 專案...", githubProp.username());

        // 注意：如果你的專案超過 100 個，這裡需要實作分頁 (Pagination)
        return getRestClient().get()
                .uri(githubProp.allReposUri() + "?per_page=100&sort=updated")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * 取得我的某個 Repo 底下的所有 Workflows (*.yml)
     */
    public List<GithubWorkflowDto> listWorkflowsMyRepo(String repoName) {
        log.info("📂 正在讀取專案 [{}] 的所有 Workflows...", repoName);

        GithubWorkflowResponse response = getRestClient().get()
                .uri(githubProp.workflowsUri(), repoName)
                .retrieve()
                .body(GithubWorkflowResponse.class);

        if (response != null && response.workflows() != null) {
            return response.workflows().stream()
                    .sorted(Comparator.comparing(GithubWorkflowDto::path)) // 排序
                    .toList();
        }
        return List.of();
    }

    /**
     * 查詢指定 Repo 的最新腳本執行狀態 (Workflow Runs)
     * @param repoName 專案名稱
     * @param limit    要抓取的最新筆數 (建議 3~5 筆)
     */
    public GithubWorkflowRunResponse listWorkflowStatus(String repoName, int limit) {
        log.info("🔍 正在查詢專案 [{}] 的最新 {} 筆腳本執行紀錄...", repoName, limit);

        if (limit <= 0) {
            limit = 1; // 預設抓取最新 1 筆
        } else if (limit > 100) {
            limit = 10; // GitHub API 單次最多只能抓取 10 筆
        }

        return getRestClient().get()
                // 將 URI 中的 {repo} 替換為實際專案名稱，並加上分頁參數
                .uri(githubProp.runsUri() + "?per_page=" + limit, repoName)
                .retrieve()
                .body(GithubWorkflowRunResponse.class);
    }

    /**
     * 查詢 GitHub 帳號的通知 (Notifications)
     * @param all   是否包含已讀通知 (true: 全部, false: 僅未讀)
     * @param since 起始時間過濾 (ISO 8601 格式，可為 null)
     * @param limit 要抓取的最新筆數
     */
    public List<GithubNotificationDto> listNotifications(boolean all, String since, int limit) {
        log.info("🔍 查詢 GitHub 通知，包含已讀: {}, 起始時間: {}, 最多: {} 筆", all, since, limit);

        if (limit <= 0) limit = 5;
        if (limit > 50) limit = 50;

        // 動態組裝 URL
        StringBuilder uriBuilder = new StringBuilder(githubProp.notificationsUri());
        uriBuilder.append("?per_page=").append(limit);
        uriBuilder.append("&all=").append(all);

        if (since != null && !since.isBlank()) {
            uriBuilder.append("&since=").append(since);
        }

        return getrestclientclassic().get()
                .uri(uriBuilder.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * 觸發指定的 Workflow (透過 workflow_dispatch)
     * @param repoName 專案名稱 (例如: Demo-SpringBoot-CLI)
     * @param workflowId 腳本檔名
     * @param inputs 傳給腳本的參數 (對應 yml 裡面的 inputs)
     */
    public void triggerMyWorkflow(String repoName, String workflowId, Map<String, Object> inputs) {
        log.info("🚀 準備觸發專案 [{}] 的腳本 [{}]", repoName, workflowId);

        Map<String, Object> payload = Map.of(
                "ref", "main", // 要在哪個分支執行
                "inputs", inputs
        );

        try {
            getRestClient().post()
                    .uri(githubProp.dispatchesUri(), repoName, workflowId)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("✅ 觸發成功！");
        } catch (Exception e) {
            log.error("❌ 觸發失敗: {}", e.getMessage(), e);
        }
    }

    private RestClient getRestClient() {
        return getRestClient(githubProp.godToken());
    }

    private RestClient getrestclientclassic() {
        return getRestClient(githubProp.classicToken());
    }

    /**
     * 建立共用的 RestClient 產生器 (封裝 BaseUrl 與驗證 Header)
     */
    private RestClient getRestClient(String token) {
        return restClientBuilder
                .baseUrl(githubProp.baseUrl())
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    /**
     * 輔助方法：將 GitHub 的通知原因轉換為容易閱讀的中文
     */
    public String translateReason(String reason) {
        if (reason == null) return "未知原因";
        return switch (reason.toLowerCase()) {
            case "assign" -> "被指派任務";
            case "author" -> "自己建立的";
            case "comment" -> "有新留言回覆";
            case "mention" -> "被 @ 標記";
            case "team_mention" -> "團隊被 @ 標記";
            case "review_requested" -> "被請求 Code Review";
            case "state_change" -> "狀態改變";
            case "ci_activity" -> "CI/CD 執行結果";
            default -> reason;
        };
    }
}