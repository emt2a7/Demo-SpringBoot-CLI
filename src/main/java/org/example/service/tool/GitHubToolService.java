package org.example.service.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.github.*;
import org.example.service.GitHubService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubToolService {

    private static final String TAG = "🤖 AI 呼叫了 GitHub 工具：";

    private final GitHubService service;

    /**
     * 查詢專案流量與訪客統計 (Traffic API)
     * @param repositoryName
     * @return
     */
    @Tool(description = """
    查詢指定 GitHub 儲存庫 (Repository) 過去 14 天內的流量與訪客統計 (Traffic Views)。
    當使用者詢問下列情境時，請呼叫此工具：
    『某專案的流量』
    『有多少人看過某專案』
    『某專案的訪客統計』
    『某專案的熱度』
    
    【參數傳遞規則】
    - repositoryName: (字串) 專案名稱。
    """)
    public String getTrafficView(String repositoryName) {
        log.info("{}getTrafficView，目標專案：{}", TAG, repositoryName);

        GithubTrafficViewsDto traffic = null;
        StringBuilder result = new StringBuilder();
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        // 流量統計通常只看「日期」，所以時間格式可以簡化到 yyyy-MM-dd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            traffic = service.getTrafficView(repositoryName);

            if (traffic == null || traffic.views() == null || traffic.views().isEmpty()) {
                return "專案 [" + repositoryName + "] 目前沒有足夠的流量數據，或過去 14 天內無人造訪。";
            }

            result.append("專案 ").append(repositoryName).append(" 的近期流量統計 (近 14 天)：\n");
            result.append("總瀏覽次數 (Views)：").append(traffic.count()).append(" 次\n");
            result.append("不重複訪客 (Unique)：").append(traffic.uniques()).append(" 人\n\n");
            result.append("每日明細 (僅列出有造訪的日期)：\n");

            for (org.example.dto.github.GithubTrafficViewsDto.DailyView view : traffic.views()) {
                // UTC 時間轉換為台灣日期
                String twDate = view.timestamp();
                try {
                    ZonedDateTime utcDate = ZonedDateTime.parse(view.timestamp());
                    twDate = utcDate.withZoneSameInstant(taipeiZone).format(formatter);
                } catch (Exception e) {
                    log.warn("時間解析失敗: {}", view.timestamp());
                }

                result.append(twDate).append("：")
                        .append(view.count()).append(" 次瀏覽 (")
                        .append(view.uniques()).append(" 人)\n");
            }
        } catch (Exception e) {
            result.setLength(0);
            result.append("❌ 抱歉，在查詢專案流量時發生錯誤：").append(e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result.toString();
    }

    /**
     * 查詢「我的 GitHub 帳號」下所有專案清單
     * @return
     */
    @Tool(description = """
    查詢並列出目前「我的 GitHub 帳號」下所有的儲存庫 (Repositories)。
    當使用者詢問下列情境時，請呼叫此工具：
    『我有什麼專案』
    『我的所有專案』
    『列出我的repo』
    『list my repo』
    『可以發布哪些專案』
    """)
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

            result.append("找到 ").append(repos.size()).append(" 個專案：\n");

            for (int i = 0; i < repos.size(); i++) {
                repo = repos.get(i);
                result.append(i + 1);
                result.append(".");
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
    @Tool(description = """
    查詢並列出「我的 GitHub 帳號」下某個 GitHub 儲存庫 (Repository) 內所有的工作流程腳本 (Workflows / Actions)。
    當使用者詢問下列情境時，請呼叫此工具：
    『某個專案有哪些腳本』
    『某個專案可以執行什麼動作』
    """)
    public String listWorkflowsMyRepo(String repositoryName) {
        log.info("{}listWorkflowsMyRepo，目標專案：{}", TAG, repositoryName);

        StringBuilder result = new StringBuilder();
        GithubWorkflowDto wf = null;
        List<GithubWorkflowDto> workflows = null;

        try {
            workflows = service.listWorkflowsMyRepo(repositoryName);

            if (workflows == null || workflows.isEmpty()) {
                return "在專案 " + repositoryName + " 中沒有找到任何可用的 Workflow 腳本。";
            }

            result.append("專案 ").append(repositoryName).append(" 共有 ").append(workflows.size()).append(" 個腳本：\n");

            for (int i = 0; i < workflows.size(); i++) {
                wf = workflows.get(i);
                result.append(i + 1);
                result.append(".");
                result.append(wf.name());
                result.append(" (").append(wf.path().replace(".github/workflows/", "")).append(")\n");
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
     * 查詢腳本執行狀態
     * @param repositoryName
     * @return
     */
    @Tool(description = """
    查詢指定 GitHub 儲存庫 (Repository) 的最新腳本執行狀態 (Workflow Runs)。
    當使用者詢問下列情境時，請呼叫此工具：
    『某專案 status』
    『某專案發布成功了嗎』
    『某專案更版狀態』
    『某專案最近5筆的狀態』
    
    【參數傳遞規則】
    - limit: 預設為 1 筆，若使用者有指定數量請帶入。
    """)
    public String listWorkflowStatus(String repositoryName, int limit) {
        log.info("{}listWorkflowStatus，目標專案：{}，筆數：{}", TAG, repositoryName, limit);

        // ✨ 定義台灣時區與易讀的時間格式
        ZonedDateTime utcDate = null;
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        GithubWorkflowRunResponse response = null;
        StringBuilder result = new StringBuilder();

        try {
            // 抓取最新 N 筆紀錄
            response = service.listWorkflowStatus(repositoryName, limit);

            if (response == null || response.workflowRuns() == null || response.workflowRuns().isEmpty()) {
                return "專案 [" + repositoryName + "] 目前沒有任何腳本執行紀錄。";
            }

            result.append("專案 ").append(repositoryName).append(" 的最新執行紀錄：\n");

            for (org.example.dto.github.GithubWorkflowRunDto run : response.workflowRuns()) {
                // 防呆：如果還在執行中，conclusion 會是 null
                String conclusionStr = run.conclusion() != null ? run.conclusion() : (run.status().equals("in_progress") ? "🔄 執行中" : "⏳ 等待中");

                // ✨ UTC 時間轉換為台灣時間 (加防呆機制)
                String twTime = run.createdAt();
                try {
                    utcDate = ZonedDateTime.parse(run.createdAt());
                    twTime = utcDate.withZoneSameInstant(taipeiZone).format(formatter);
                } catch (Exception e) {
                    log.warn("時間解析失敗: {}", run.createdAt());
                }

                result.append("腳本名稱：").append(run.name()).append("\n");
                result.append("當前狀態：").append(run.status()).append("\n");
                result.append("執行結果：").append(conclusionStr).append("\n");
                result.append("觸發時間：").append(run.createdAt()).append("\n");
                result.append("詳細網址：").append(run.htmlUrl()).append("\n\n");
            }
        } catch (Exception e) {
            result.setLength(0);
            result.append("❌ 抱歉，在查詢執行紀錄時發生錯誤：").append(e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result.toString();
    }

    /**
     * 查詢專屬通知中心 (Notifications)
     */
    @Tool(description = """
    查詢並列出「我的 GitHub 帳號」的通知 (Notifications)。
    當使用者詢問下列情境時，請呼叫此工具：
    『有未讀通知嗎』(all=false, since=null, limit=5)
    『本週有消息嗎』(all=false, since=本週, limit=5)
    『有通知嗎』(all=false, since=null, limit=5)
    『今天有誰標記我』(all=false, since=今天日期, limit=5)
    『列出所有歷史通知』(all=true, since=null, limit=50)
    『最近三天的消息』(all=false, since=最近三天, limit=5)
    
    【參數傳遞規則】
    - all: (布林值) 極度重要！只要使用者的語意中包含「所有」、「全部」、「歷史」或「已讀」，你【必須】傳入 true。只有在明確要求「未讀」或毫無指定時，才傳入 false。
    - since: (字串) 請參考系統提示詞提供的「現在的系統時間」，精準推算出使用者要求的時間點。例如今天是 2026-03-21，若使用者要求「本週」，請推算出本週一的日期，並強制轉換為 ISO 8601 UTC 格式 (例如 2026-03-16T00:00:00Z) 傳入。若無指定時間請傳入 null。
    - limit: 使用者想看「所有、歷史」時，預設為 50 筆；若使用者有指定數量請帶入；若使用者沒指定數量，預設為 5 筆。
    """)
    public String listNotifications(boolean all, String since, int limit) {
        log.info("{}listNotifications，包含已讀:{}，自從:{}，筆數:{}", TAG, all, since, limit);

        ZonedDateTime utcDate = null;
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder result = new StringBuilder();
        List<GithubNotificationDto> notifications = null;

        try {
            // 呼叫更新後的底層服務
            notifications = service.listNotifications(all, since, limit);

            if (notifications == null || notifications.isEmpty()) {
                String statusMsg = all ? "歷史" : "未讀";
                return "🎉 您目前沒有符合條件的 " + statusMsg + " GitHub 通知。";
            }

            result.append("為您列出符合條件的通知 (共 ").append(notifications.size()).append(" 筆)：\n\n");

            for (org.example.dto.github.GithubNotificationDto notif : notifications) {
                // UTC 時間轉換為台灣時間
                String twTime = notif.updatedAt();
                try {
                    utcDate = ZonedDateTime.parse(notif.updatedAt());
                    twTime = utcDate.withZoneSameInstant(taipeiZone).format(formatter);
                } catch (Exception e) {
                    log.warn("時間解析失敗: {}", notif.updatedAt());
                }

                String reasonTw = service.translateReason(notif.reason());

                result.append("專案：").append(notif.repository().fullName()).append("\n");
                result.append("標題：").append(notif.subject().title()).append("\n");
                result.append("類型：").append(notif.subject().type()).append(" (").append(reasonTw).append(")\n");
                result.append("時間：").append(twTime).append("\n\n");
            }
        } catch (Exception e) {
            result.setLength(0);
            result.append("❌ 抱歉，在查詢通知時發生錯誤：").append(e.getMessage());
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
    @Tool(description = """
    觸發/執行「我的 GitHub 帳號」下指定的儲存庫 (Repository) 內的特定工作流程腳本 (*.yml)。
    當使用者要求下列情境時，請呼叫此工具：
    『deploy 某專案的某腳本』
    『執行某專案的某腳本』
    『發布某專案的某腳本』
    """)
    public String triggerMyWorkflow(
            String repositoryName,
            String workflowFileName // 例如: build-prod.yml
    ) {
        log.info("{}triggerMyWorkflow，目標專案：{}，腳本：{}", TAG, repositoryName, workflowFileName);

        StringBuilder result = new StringBuilder();

        try {
            service.triggerMyWorkflow(repositoryName, workflowFileName, Map.of());
            result.append("專案 ");
            result.append(repositoryName);
            result.append(" 的 ");
            result.append(workflowFileName);
            result.append(" 腳本已執行！");
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
