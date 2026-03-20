export default {
  async fetch(request, env, ctx) {
    if (request.method !== 'POST') return new Response('請改用 POST 重連!"', { status: 405 });

    try {
      // ==========================================
      // 變數準備區
      // ==========================================
      const TOKEN_GITHUB   = (env.TOKEN_GITHUB   || "").trim(); // GitHub Token
      const TOKEN_LINE     = (env.TOKEN_LINE     || "").trim(); // Line Token
      const TOKEN_TELEGRAM = (env.TOKEN_TELEGRAM || "").trim(); // Telegram Token

      const GITHUB_REPO          = "emt2a7/Demo-SpringBoot-CLI"; // GitHub repo
      const GITHUB_WORKFLOW_NAME = "exec-prod-agent.yml"; // GitHub Actions Workflow Name
      const GITHUB_URL           = `https://api.github.com/repos/${GITHUB_REPO}/actions/workflows/${GITHUB_WORKFLOW_NAME}/dispatches`;
      const TELEGRAM_URL         = `https://api.telegram.org/bot${TOKEN_TELEGRAM}/sendMessage`;
      const LINE_URL             = `https://api.line.me/v2/bot/message/reply`; // LINE Reply API 網址
      const WAIT_TEXT            = "🤖 呼叫 GitHub 處理中，請稍候...";

      const url  = new URL(request.url);            // 解析 Request 網址
      const path = url.pathname.replace(/\/$/, ""); // 解析 Request 網址後面的路徑，避免結尾帶有斜線 (例如 /telegram/) 導致判定失敗
      const body = await request.json();            // 解析 Request Body

      let param_userPrompt     = "";             // 傳入參數：平台來源 (line、telegram) (全小寫)
      let param_userId         = "";             // 傳入參數：line、telegram 的 ID
      let param_sourcePlatform = "";             // 傳入參數：使用者提示詞 (Prompt)
      let lineReplyToken       = "";

      // ==========================================
      // 🟢 通道 A：處理 LINE 的 Request
      // ==========================================
      if (path === '/line') {
        if (!body.events || body.events.length === 0) return new Response('OK', { status: 200 });
        const event = body.events[0];
        if (event.type !== 'message' || event.message.type !== 'text') return new Response('OK', { status: 200 });
        
        param_sourcePlatform = "line";
        param_userId = event.source.userId;
        param_userPrompt = event.message.text;
        lineReplyToken = event.replyToken;
      } 
      // ==========================================
      // 🔵 通道 B：處理 Telegram 的 Request
      // ==========================================
      else if (path === '/telegram') {
        if (!body.message || !body.message.text) return new Response('OK', { status: 200 });
        
        param_sourcePlatform = "telegram";
        param_userId = body.message.chat.id.toString();
        param_userPrompt = body.message.text;
      } 
      // ==========================================
      // ❌ 走錯棚的請求
      // ==========================================
      else {
        return new Response('未定義的通道：' + path, { status: 404 });
      }
      
      console.log(`收到 [${param_sourcePlatform}] 訊息: ${param_userPrompt}, 來自 ID: ${param_userId}`);

      // ==========================================
      // 輔助函式：發送訊息回各平台 (Line、Telegram)
      // ==========================================
      const sendFastReply = async (text) => {
        try {
          if (param_sourcePlatform === 'line') {
            await fetch(LINE_URL, {
              method: "POST",
              headers: { "Content-Type": "application/json", "Authorization": `Bearer ${TOKEN_LINE}` },
              body: JSON.stringify({ replyToken: lineReplyToken, messages: [{ type: "text", text: text }] })
            });
          } else if (param_sourcePlatform === 'telegram') {
            await fetch(TELEGRAM_URL, {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ chat_id: param_userId, text: text })
            });
          }
        } catch (err) {
          console.error("❌ sendFastReply 內部錯誤:", err);
        }
        return new Response('OK', { status: 200 }); // 回傳 200 結束這次 Webhook
      };

      // ==========================================
      // 邊緣過濾邏輯 (Edge Filtering) - 防呆措施
      // ==========================================
      const cleanPrompt = param_userPrompt.trim();

      // 超短無意義文字過濾 (防誤觸)
      if (cleanPrompt.length <= 1) {
        return await sendFastReply("🤖 您的訊息太短，至少輸入２字以上！");
      }

      // 系統狀態檢測指令 (0 秒極速回應)
      if (cleanPrompt.toLowerCase() === '/ping' || cleanPrompt.toLowerCase() === 'ping') {
        return await sendFastReply("🤖 Cloudflare live.");
      }

      // 幫助選單指令 (由邊緣節點直接提供說明書)
      if (cleanPrompt.toLowerCase() === '/help' || cleanPrompt.toLowerCase() === '/h') {
        const helpMsg = "【🤖 使用說明】\n1. 閒聊問答：直接輸入您的問題\n2. 系統操作：輸入「發布至正式+OpenAI」\n3. 系統測試：輸入 /ping";
        return await sendFastReply(helpMsg);
      }

      // ==========================================
      // 🚀 統一呼叫 GitHub Actions
      // ==========================================
      await sendFastReply(WAIT_TEXT); // 先發送安撫訊息回各平台

      // 設定 Body 資訊
      const githubPayload = {
        ref: "main",
        inputs: {
          source_platform: param_sourcePlatform.trim(),
          chat_id: param_userId.trim(),
          user_prompt: param_userPrompt.trim()
        }
      };

      // 呼叫 GitHub
      const githubResponse = await fetch(GITHUB_URL, {
        method: 'POST',
        headers: {
          'Accept': 'application/vnd.github.v3+json',
          'Authorization': `Bearer ${TOKEN_GITHUB}`,
          'User-Agent': 'Cloudflare-Worker-Omni'
        },
        body: JSON.stringify(githubPayload)
      });

      // GitHub 異常處理
      if (!githubResponse.ok) {
        const errText = await githubResponse.text();
        console.error("GitHub API Error:", errText);
      }

      return new Response('OK', { status: 200 });
    } catch (error) {
      console.error("Worker 內部錯誤:", error);
      return new Response('Internal Server Error:' + error, { status: 500 });
    }
  }
};