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

    /**
     * AI智能生成评价指标配置
     * 根据教师的教学要求，自动生成或调整评价指标及权重
     */
    public String generateIndicators(String teacherRequirements, String existingIndicators) throws IOException {
        String systemPrompt = "你是一位智能教学设计助手。请根据老师的要求和现有指标，生成评价指标配置方案。\n" +
                "规则：保留所有系统指标（isSystem=1），可调权重不删；根据需求新增自定义指标（isSystem=0）；权重0-100，总和接近100。\n" +
                "你必须只返回一行纯JSON，不要加任何其他文字、解释或markdown标记。格式如下：\n" +
                "{\"indicators\": [{\"name\":\"xx\",\"description\":\"xx\",\"category\":\"xx\",\"weight\":30,\"isSystem\":1}],\"suggestion\":\"xx\"}";

        String userPrompt = "要求：" + teacherRequirements +
                "\n现有指标：" + existingIndicators;
        return chat(systemPrompt, userPrompt);
    }

    /**
     * AI生成课程练习题（10道选择题，每题10分）
     * @param courseName 课程名称
     * @param courseDesc 课程描述
     * @return JSON格式的练习题数组
     */
    public String generatePracticeQuestions(String courseName, String courseDesc) throws IOException {
        String systemPrompt = "你是一位高校计算机专业教师，擅长出题。请根据课程信息生成10道单项选择题，用于学生课后自测练习。\n" +
                "要求：题目要真实准确，覆盖课程核心知识点，难度适中。\n" +
                "你必须只返回纯JSON数组，不要加任何其他文字、解释或markdown标记。格式如下：\n" +
                "[{\"id\":1,\"question\":\"题目内容\",\"options\":{\"A\":\"选项A\",\"B\":\"选项B\",\"C\":\"选项C\",\"D\":\"选项D\"},\"answer\":\"A\",\"explanation\":\"解析\"}]";

        String userPrompt = "课程名称：" + courseName + "\n课程描述：" + courseDesc;
        return chat(systemPrompt, userPrompt);
    }
}
