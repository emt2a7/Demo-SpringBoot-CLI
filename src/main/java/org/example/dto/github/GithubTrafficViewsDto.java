package org.example.dto.github;

import java.util.List;

/**
 * 接收 GitHub 專案流量與訪客統計 (Traffic Views)
 */
public record GithubTrafficViewsDto(
        long count,             // 總瀏覽次數
        long uniques,           // 總不重複訪客數
        List<DailyView> views   // 每日流量明細陣列
) {
    // 內部 Record：對應每日的流量明細
    public record DailyView(
            String timestamp,   // 時間戳記
            long count,         // 單日瀏覽次數
            long uniques        // 單日不重複訪客數
    ) {}
}
