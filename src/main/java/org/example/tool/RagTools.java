package org.example.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HrVectorStoreService 是一個示範用的工具服務，模擬查詢公司人資法規相關知識的功能。
 * * 說明: 實作 Function 介面，並以 @Service 註冊為 Spring Bean。
 */
@Slf4j
@Service("ragTool")
@RequiredArgsConstructor
public class RagTools {

    private final VectorStore hrVectorStore;
    private final VectorStore itVectorStore;
    private final VectorStore babyVectorStore;

    // 定義傳入與傳出的資料結構
    public record Request(String query) {}
    public record Response(String result) {}

    @Tool(description = "當使用者詢問『請假、薪資、出差、員工福利、人資』等公司人資規章時，請呼叫此工具。")
    public Response searchHrDatabase(Request request) {
        log.info("[Function Calling] AI 正在呼叫本地端工具(HR 知識庫)...");
        return searchDatabase(request, hrVectorStore);
    }

    @Tool(description = "當使用者詢問『智慧工廠、網路層、感知層、邊緣層、平台層、應用層、合勤(Zyxel)、巴魯夫(Balluff)、倍福(Beckhoff)、無線IO-Link、防火牆、HA、Switch、AI』等與AI人工智慧或智慧工廠時，請呼叫此工具。")
    public Response searchItDatabase(Request request) {
        log.info("[Function Calling] AI 正在呼叫本地端工具(IT 知識庫)...");
        return searchDatabase(request, itVectorStore);
    }

    @Tool(description = "當使用者詢問『台灣人口、新生兒、出生率、生育率、死亡、女性』等與台灣人口新生死亡相關時，請呼叫此工具。")
    public Response searchBabyDatabase(Request request) {
        log.info("[Function Calling] AI 正在呼叫本地端工具(Baby 知識庫)...");
        return searchDatabase(request, babyVectorStore);
    }

    public Response searchDatabase(Request request, VectorStore vectorStore) {
        log.info("使用者詢問：{}", request.query());
        // 檢索 IT 向量資料庫 (Vector Store)
        //   topK(3)：只取最相關的前 3 筆結果
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(request.query()).topK(3).build()
        );

        // 查詢結果
        var result = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        //　回傳給 AI
        return new Response(result);
    }
}
