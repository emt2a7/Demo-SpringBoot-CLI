export default {
  async fetch(request, env, ctx) {
    // 1. 只接收 POST 請求 (Telegram Webhook 標準)
    if (request.method !== "POST") {
      return new Response("請改用 POST 重連!", { status: 200 });
    }

    try {
      const payload = await request.json();

      // 2. 過濾掉非文字的事件 (例如使用者傳送貼圖或加入群組)
      if (!payload.message || !payload.message.text) {
        return new Response("請使用純文字傳送!", { status: 200 });
      }

      const chatId = payload.message.chat.id;
      const text = payload.message.text;

      // 3. 【UX 優化】先回傳「處理中」安撫使用者，掩蓋 GitHub 啟動的延遲
      const tgUrl = `https://api.telegram.org/bot${env.TELEGRAM_TOKEN}/sendMessage`;
      await fetch(tgUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chat_id: chatId,
          text: "🦐 收到！小龍蝦正在呼叫 GitHub 雲端主機處理中，請稍候約 15~20 秒..."
        })
      });

      // 4. 【核心】觸發 GitHub Actions
      // ⚠️ 請確認這裡的帳號與 Repo 名稱是否正確
      const githubRepo = "emt2a7/Demo-SpringBoot-CLI"; 
      const workflowName = "deploy-prod-agent.yml"; // 你要觸發的 Workflow 檔案名稱
      
      const ghUrl = `https://api.github.com/repos/${githubRepo}/actions/workflows/${workflowName}/dispatches`;

      const ghResponse = await fetch(ghUrl, {
        method: "POST",
        headers: {
          "Accept": "application/vnd.github.v3+json",
          "Authorization": `Bearer ${env.GITHUB_TOKEN}`,
          "User-Agent": "Telegram-Cloudflare-Worker"
        },
        body: JSON.stringify({
          ref: "main", // 觸發 main 分支
          inputs: {
            user_prompt: text,
            chat_id: chatId.toString() // GitHub Actions inputs 必須是字串
          }
        })
      });

      // 如果 GitHub 拒絕，在 Console 印出錯誤方便除錯
      if (!ghResponse.ok) {
        console.error("GitHub API Error:", await ghResponse.text());
      }

      // 5. 告訴 Telegram 伺服器我們處理完了，不用再重試
      return new Response("OK", { status: 200 });

    } catch (err) {
      console.error(err);
      return new Response("Error", { status: 500 });
    }
  }
};