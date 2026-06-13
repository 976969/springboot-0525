package com.qcby.springboot_0525.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 大模型API调用服务
 * 支持阿里云百炼通义千问等兼容OpenAI格式的API
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static final int MAX_RETRIES = 2;

    @Value("${llm.api-url}")
    private String apiUrl;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用大模型 - 通用方法（含重试逻辑）
     */
    public String chat(String systemPrompt, String userPrompt) throws IOException {
        // 构建请求体
        String requestBody = objectMapper.writeValueAsString(new Object() {
            public String getModel() { return model; }
            public Object[] getMessages() {
                return new Object[]{
                        new Object() {
                            public String getRole() { return "system"; }
                            public String getContent() { return systemPrompt; }
                        },
                        new Object() {
                            public String getRole() { return "user"; }
                            public String getContent() { return userPrompt; }
                        }
                };
            }
            public double getTemperature() { return 0.7; }
        });

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        // 重试逻辑
        IOException lastException = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    lastException = new IOException("大模型API调用失败(HTTP " + response.code() + "): " + errorBody);
                    if (attempt < MAX_RETRIES) {
                        log.warn("大模型API调用失败，第{}次重试...", attempt + 1);
                        Thread.sleep(2000L * (attempt + 1));
                        continue;
                    }
                    throw lastException;
                }
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String content = jsonNode.path("choices").path(0).path("message").path("content").asText();
                if (content == null || content.isEmpty()) {
                    throw new IOException("大模型返回内容为空");
                }
                return content;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("大模型调用被中断", e);
            }
        }
        throw lastException != null ? lastException : new IOException("大模型调用失败");
    }

    /**
     * 智能核查 - 三维度结构化核查
     * 返回包含 completeness、logic、match 三个维度评分和建议的JSON
     */
    public String checkTraining(String requirements, String content) throws IOException {
        String systemPrompt = "你是一位严谨的软件实训教学评审专家。请根据实训要求，对学生的实训成果从三个维度进行核查。\n" +
                "评分规则：每个维度给出0-100分的评分。60分及以上为通过，60分以下为存在问题。\n" +
                "请严格按照以下JSON格式返回，不要添加任何其他内容：\n" +
                "{\"completeness\":{\"score\":分数,\"issue\":\"问题描述，通过则写无\",\"suggestion\":\"改进建议，通过则写良好\"}," +
                "\"logic\":{\"score\":分数,\"issue\":\"问题描述，通过则写无\",\"suggestion\":\"改进建议，通过则写良好\"}," +
                "\"match\":{\"score\":分数,\"issue\":\"问题描述，通过则写无\",\"suggestion\":\"改进建议，通过则写良好\"}," +
                "\"overall_suggestion\":\"总体改进建议\"}";

        String userPrompt = "实训要求：\n" + requirements + "\n\n学生实训成果内容：\n" + content;
        return chat(systemPrompt, userPrompt);
    }

    /**
     * AI评分 - 根据指标评价
     */
    public String evaluateByIndicator(String indicatorName, String indicatorDesc,
                                       String requirements, String content) throws IOException {
        String systemPrompt = "你是一位软件实训教学评审专家。请根据给定的评价指标，对学生的实训成果进行评分。" +
                "评分范围1-100分，请给出客观公正的分数和详细评语。" +
                "请以JSON格式返回：{\"score\": 分数, \"comment\": \"评语\"}";

        String userPrompt = "评价指标：" + indicatorName + "\n指标描述：" + indicatorDesc +
                "\n\n实训要求：\n" + requirements +
                "\n\n学生实训成果内容：\n" + content;

        return chat(systemPrompt, userPrompt);
    }

    /**
     * 生成评价报告总结
     */
    public String generateReportSummary(String evaluationData) throws IOException {
        String systemPrompt = "你是一位软件实训教学评审专家。请根据多维度评价数据，生成一份综合评价报告总结，" +
                "包括学生优势、薄弱点分析、改进建议。内容简洁专业，300字以内。";

        String userPrompt = "评价数据：\n" + evaluationData;
        return chat(systemPrompt, userPrompt);
    }
}
