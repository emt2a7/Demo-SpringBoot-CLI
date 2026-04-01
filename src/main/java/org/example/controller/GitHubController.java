package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.github.GithubRepoDto;
import org.example.dto.github.GithubWorkflowDto;
import org.example.service.GitHubService;
import org.example.framework.util.DateUtil;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 範例 Controller，示範如何使用 Constructor Injection 與 Lombok。
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService service;

    public void run() {
        log.info("run#START");
        String repoName = "emt2a7/Demo-SpringBoot-CLI";
        String workflowId = "";
        OffsetDateTime 執行起始時間 = null;
        Map<String, Object> inputs = new HashMap<String, Object>();
        List<GithubRepoDto> repos = null;
        List<GithubWorkflowDto> workflows = null;

        try {
            執行起始時間 = OffsetDateTime.now();

            // 掃描帳號下所有的 Repository
            repos = service.listAllMyRepo();

            if (repos != null && !repos.isEmpty()) {
                log.info("列出我的 GitHub Repo 清單");
                for (GithubRepoDto repo : repos) {
                    // Name: Demo-SpringBoot-CLI, FullName: emt2a7/Demo-SpringBoot-CLI, Is Private: false
                    // Name: Demo,                FullName: emt2a7/Demo,                Is Private: false
                    log.info("Name: {}, FullName: {}, Is Private: {}", repo.name(), repo.fullName(), repo.isPrivate());
                }
            }

            // 取得某個 Repo 底下的所有 Workflows
            workflows = service.listWorkflowsMyRepo(repoName);

            if (workflows != null && !workflows.isEmpty()) {
                log.info("列出 {} 底下的所有 Workflows", repoName);
                for (GithubWorkflowDto workflow : workflows) {
                    // ID: 246326793, Name: 【✅ AI助理】小龍蝦營業中 (監聽 Issue), State: active, Path: .github/workflows/listener-issue-agent.yml
                    // ID: 246718200, Name: 🚀 發布至「開發+OpenAI」環境,          State: active, Path: .github/workflows/deploy-dev-openai-agent.yml
                    // ID: 246718201, Name: 🚀 發布至「正式+OpenAI」環境,          State: active, Path: .github/workflows/deploy-prod-openai-agent.yml
                    // ID: 246753709, Name: 【共用模組】發布環境,                   State: active, Path: .github/workflows/module-exec.yml
                    // ID: 246869025, Name: 🧹 刪除前 500 筆 workflow 紀錄,       State: active, Path: .github/workflows/delete-workflow-runs-agent.yml
                    // ID: 246904363, Name: 👷 手動執行「開發」環境,                State: active, Path: .github/workflows/exec-dev-agent.yml
                    // ID: 246904364, Name: 👷 手動執行「正式」環境,                State: active, Path: .github/workflows/exec-prod-agent.yml
                    // ID: 246904365, Name: 【共用模組】執行環境,                   State: active, Path: .github/workflows/module-deploy.yml
                    log.info("ID: {}, Name: {}, State: {}, Path: {}", workflow.id(), workflow.name(), workflow.state(), workflow.path());
                }
            }

            // 觸發指定的 Workflow
            workflowId = "exec-prod-agent.yml";
            inputs.put("source_platform", "telegram");
            inputs.put("chat_id", "5015499247");
            inputs.put("user_prompt", "日本首相是誰？");
            service.triggerMyWorkflow(repoName, workflowId, inputs);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        } finally {
            log.info("-------------------------------------------------------------");
            log.info("　　　　　　　　　　　　GitHub 執行結果");
            log.info("-------------------------------------------------------------");
            log.info("【花費時間】 {}", DateUtil.計算相差多少時間(執行起始時間, OffsetDateTime.now()));
            log.info("-------------------------------------------------------------");
            log.info("run()#END");
        }
    }
}

