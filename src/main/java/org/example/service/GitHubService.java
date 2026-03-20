package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AiResponseWrapper;
import org.example.dto.github.GithubRepoDto;
import org.example.dto.github.GithubWorkflowDto;
import org.example.dto.github.GithubWorkflowResponse;
import org.example.framework.prop.GithubProp;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
        請注意你的回答內容請務必精簡，絕對不可超過 %d 個字元，以免超出系統的傳送限制。
        """.formatted(githubProp.messageLimit().intValue());
        return aiChatService.chat(systemPrompt, userPrompt);
    }

    /**
     * 建立共用的 RestClient 產生器 (封裝 BaseUrl 與驗證 Header)
     */
    private RestClient getRestClient() {
        return restClientBuilder
                .baseUrl(githubProp.baseUrl())
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .defaultHeader("Authorization", "Bearer " + githubProp.godToken())
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
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
                .uri(githubProp.workflowsUri(), githubProp.username(), repoName)
                .retrieve()
                .body(GithubWorkflowResponse.class);

        if (response != null && response.workflows() != null) {
            return response.workflows();
        }
        return List.of();
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
                    .uri(githubProp.dispatchesUri(), githubProp.username(), repoName, workflowId)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("✅ 觸發成功！");
        } catch (Exception e) {
            log.error("❌ 觸發失敗: {}", e.getMessage(), e);
        }
    }
}