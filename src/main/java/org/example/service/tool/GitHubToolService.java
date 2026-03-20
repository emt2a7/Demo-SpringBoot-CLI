package org.example.service.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.github.GithubRepoDto;
import org.example.dto.github.GithubWorkflowDto;
import org.example.service.GitHubService;
import org.example.util.DateUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubToolService {

    private static final String TAG = "🤖 AI 呼叫了 GitHub 工具：";

    private final GitHubService service;

    /**
     * 查詢「我的 GitHub 帳號」下所有專案清單
     * @return
     */
    @Tool(description = "查詢並列出目前「我的 GitHub 帳號」下所有的儲存庫 (Repositories)。當使用者詢問『我有什麼專案』、『我的所有專案』、『列出我的repo』、『list my repo』、『可以發布哪些專案』時，請呼叫此工具。")
    public String listAllMyRepo() {
        log.info("{}listAllMyRepo", TAG);

        StringBuilder result = new StringBuilder();
        List<GithubRepoDto> repos = null;
        GithubRepoDto repo = null;

        try {
            repos = service.listAllMyRepo();

            if (repos == null || repos.isEmpty()) {
                return "目前帳號下沒有找到任何專案。";
            }

            result.append("為您找到以下 ").append(repos.size()).append(" 個專案：\n");

            for (int i = 0; i < repos.size(); i++) {
                repo = repos.get(i);
                result.append(i + 1);
                result.append(". ");
                result.append(repo.name());

                if (repo.isPrivate()) {
                    result.append(" 🔒(私有)");
                }
                result.append("\n");
            }
        } catch (Exception e) {
            result.setLength(0);
            result.append("❌ 抱歉，在查詢 GitHub 專案時發生錯誤：").append(e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result.toString();
    }

    /**
     * 查詢並列出「我的 GitHub 帳號」下某個 GitHub 儲存庫 (Repository) 內所有的工作流程腳本 (Workflows / Actions)
     * @param repositoryName
     * @return
     */
    @Tool(description = "查詢並列出「我的 GitHub 帳號」下某個 GitHub 儲存庫 (Repository) 內所有的工作流程腳本 (Workflows / Actions)。當使用者詢問『某個專案有哪些腳本』、『某個專案可以執行什麼動作』時，請呼叫此工具。")
    public String listWorkflowsMyRepo(String repositoryName) {
        log.info("{}listWorkflowsMyRepo，目標專案：{}", TAG, repositoryName);

        StringBuilder result = new StringBuilder();
        GithubWorkflowDto wf = null;
        List<GithubWorkflowDto> workflows = null;

        try {
            workflows = service.listWorkflowsMyRepo(repositoryName);

            if (workflows == null || workflows.isEmpty()) {
                return "在專案 [" + repositoryName + "] 中沒有找到任何可用的 Workflow 腳本。";
            }

            result.append("專案 [").append(repositoryName).append("] 共有 ").append(workflows.size()).append(" 個腳本：\n");

            for (int i = 0; i < workflows.size(); i++) {
                wf = workflows.get(i);
                result.append(i + 1);
                result.append(". ");
                result.append(wf.name());
                result.append(" (檔案: ").append(wf.path().replace(".github/workflows/", "")).append(")\n");
            }
        } catch (Exception e) {
            result.setLength(0);
            result.append("❌ 抱歉，在查詢 ");
            result.append(repositoryName);
            result.append(" 專案時發生錯誤：").append(e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result.toString();
    }

    /**
     * 觸發腳本
     * @param repositoryName
     * @param workflowFileName
     * @return
     */
    @Tool(description = "觸發/執行「我的 GitHub 帳號」下指定的儲存庫 (Repository) 內的特定工作流程腳本 (*.yml)。當使用者要求『deploy 某專案的某腳本』、『執行某專案的某腳本』、『發布某個專案』或『部署』時，請呼叫此工具。")
    public String triggerMyWorkflow(
            String repositoryName,
            String workflowFileName // 例如: build-prod.yml
    ) {
        log.info("{}triggerMyWorkflow，目標專案：{}，腳本：{}", TAG, repositoryName, workflowFileName);

        StringBuilder result = new StringBuilder();

        try {
            service.triggerMyWorkflow(repositoryName, workflowFileName, Map.of());
            result.append("專案 [");
            result.append(repositoryName);
            result.append("] 的 [");
            result.append(workflowFileName);
            result.append("] 腳本已執行！");
        } catch (Exception e) {
            result.setLength(0);
            result.append("❌ 抱歉，在執行 ");
            result.append(repositoryName);
            result.append(" 腳本時發生錯誤：").append(e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result.toString();
    }
}
