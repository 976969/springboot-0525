package com.qcby.springboot_0525.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qcby.springboot_0525.common.BusinessException;
import com.qcby.springboot_0525.common.Result;
import com.qcby.springboot_0525.entity.AiPractice;
import com.qcby.springboot_0525.entity.Course;
import com.qcby.springboot_0525.entity.CourseStudent;
import com.qcby.springboot_0525.mapper.AiPracticeMapper;
import com.qcby.springboot_0525.mapper.CourseMapper;
import com.qcby.springboot_0525.service.CourseStudentService;
import com.qcby.springboot_0525.service.LlmService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * AI练习题控制器（学生端）
 */
@RestController
@RequestMapping("/ai-practice")
@SaCheckLogin
public class AiPracticeController {

    @Resource
    private AiPracticeMapper aiPracticeMapper;

    @Resource
    private CourseStudentService courseStudentService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private LlmService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从Session中获取真实用户ID
     */
    private Long getRealIdFromSession() {
        Object realIdObj = StpUtil.getSession().get("realId");
        if (realIdObj == null) return null;
        if (realIdObj instanceof Long) return (Long) realIdObj;
        if (realIdObj instanceof Integer) return ((Integer) realIdObj).longValue();
        if (realIdObj instanceof String) return Long.parseLong((String) realIdObj);
        return null;
    }

    /**
     * 获取学生已选课程列表（用于AI练习）
     */
    @GetMapping("/my-courses")
    public Result<List<Map<String, Object>>> getMyCoursesForPractice() {
        Long studentId = getRealIdFromSession();
        if (studentId == null) {
            return Result.fail("用户信息获取失败");
        }
        List<CourseStudent> courses = courseStudentService.getCoursesByStudentId(studentId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (CourseStudent cs : courses) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cs.getCourseId());
            item.put("name", cs.getCourseName());
            item.put("teacherName", cs.getTeacherName());
            // 查询该课程的练习次数
            List<AiPractice> practices = aiPracticeMapper.selectByStudentAndCourse(studentId, cs.getCourseId());
            item.put("practiceCount", practices.size());
            if (!practices.isEmpty()) {
                // 取最高分
                double maxScore = practices.stream()
                        .filter(p -> p.getScore() != null)
                        .mapToDouble(AiPractice::getScore)
                        .max().orElse(0);
                item.put("bestScore", maxScore);
            } else {
                item.put("bestScore", 0);
            }
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * AI生成练习题
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateQuestions(@RequestBody Map<String, Long> params) {
        Long studentId = getRealIdFromSession();
        if (studentId == null) {
            return Result.fail("用户信息获取失败");
        }
        String role = (String) StpUtil.getSession().get("role");
        if (!"student".equals(role)) {
            throw new BusinessException(403, "只有学生可以使用AI练习");
        }

        Long courseId = params.get("courseId");
        if (courseId == null) {
            throw new BusinessException(400, "课程ID不能为空");
        }

        // 验证学生是否选了该课程
        List<CourseStudent> myCourses = courseStudentService.getCoursesByStudentId(studentId);
        boolean enrolled = myCourses.stream().anyMatch(cs -> cs.getCourseId().equals(courseId));
        if (!enrolled) {
            throw new BusinessException(400, "您未选该课程，无法生成练习");
        }

        // 获取课程信息
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        try {
            // 调用AI生成题目
            String questionsJson = llmService.generatePracticeQuestions(course.getName(), course.getDescription());

            // 清理可能的markdown标记
            questionsJson = cleanJson(questionsJson);

            // 验证JSON格式
            JsonNode jsonNode = objectMapper.readTree(questionsJson);
            if (!jsonNode.isArray() || jsonNode.size() == 0) {
                throw new BusinessException(500, "AI生成的题目格式异常，请重试");
            }

            // 保存练习记录（未答题状态）
            AiPractice practice = new AiPractice();
            practice.setStudentId(studentId);
            practice.setCourseId(courseId);
            practice.setCourseName(course.getName());
            practice.setQuestions(questionsJson);
            practice.setTotalScore(100.0);
            aiPracticeMapper.insert(practice);

            // 返回题目和练习ID
            Map<String, Object> result = new HashMap<>();
            result.put("practiceId", practice.getId());
            result.put("questions", objectMapper.readValue(questionsJson, List.class));
            result.put("totalScore", 100.0);
            return Result.success(result);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "AI生成题目失败: " + e.getMessage());
        }
    }

    /**
     * 提交答案并自动评分
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAnswers(@RequestBody Map<String, Object> params) {
        Long studentId = getRealIdFromSession();
        if (studentId == null) {
            return Result.fail("用户信息获取失败");
        }

        Long practiceId = params.get("practiceId") != null ? Long.valueOf(params.get("practiceId").toString()) : null;
        if (practiceId == null) {
            throw new BusinessException(400, "练习ID不能为空");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) params.get("answers");
        if (answers == null || answers.isEmpty()) {
            throw new BusinessException(400, "答案不能为空");
        }

        // 查询练习记录
        AiPractice practice = aiPracticeMapper.selectById(practiceId);
        if (practice == null) {
            throw new BusinessException(404, "练习记录不存在");
        }
        if (!practice.getStudentId().equals(studentId)) {
            throw new BusinessException(403, "无权操作此练习");
        }

        try {
            // 解析题目
            JsonNode questionsNode = objectMapper.readTree(practice.getQuestions());
            double scorePerQuestion = 100.0 / questionsNode.size();
            double totalScore = 0;

            // 逐题判分
            List<Map<String, Object>> results = new ArrayList<>();
            for (JsonNode q : questionsNode) {
                int qId = q.get("id").asInt();
                String correctAnswer = q.get("answer").asText().trim().toUpperCase();
                String studentAnswer = answers.getOrDefault(String.valueOf(qId), "").trim().toUpperCase();
                boolean isCorrect = correctAnswer.equals(studentAnswer);
                if (isCorrect) {
                    totalScore += scorePerQuestion;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("id", qId);
                item.put("question", q.get("question").asText());
                item.put("correctAnswer", correctAnswer);
                item.put("studentAnswer", studentAnswer.isEmpty() ? "未作答" : studentAnswer);
                item.put("isCorrect", isCorrect);
                item.put("explanation", q.has("explanation") ? q.get("explanation").asText() : "");
                results.add(item);
            }

            // 四舍五入保留2位小数
            totalScore = Math.round(totalScore * 100.0) / 100.0;

            // 保存答案和得分
            String answersJson = objectMapper.writeValueAsString(answers);
            aiPracticeMapper.updateScore(practiceId, answersJson, totalScore);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("score", totalScore);
            result.put("totalScore", 100.0);
            result.put("questionCount", questionsNode.size());
            result.put("correctCount", (int) Math.round(totalScore / scorePerQuestion));
            result.put("details", results);
            return Result.success(result);

        } catch (Exception e) {
            throw new BusinessException(500, "评分失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生的练习历史记录
     */
    @GetMapping("/history")
    public Result<List<AiPractice>> getHistory() {
        Long studentId = getRealIdFromSession();
        if (studentId == null) {
            return Result.fail("用户信息获取失败");
        }
        List<AiPractice> list = aiPracticeMapper.selectByStudentId(studentId);
        return Result.success(list);
    }

    /**
     * 获取某次练习的详情（含题目和答案）
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Long studentId = getRealIdFromSession();
        if (studentId == null) {
            return Result.fail("用户信息获取失败");
        }
        AiPractice practice = aiPracticeMapper.selectById(id);
        if (practice == null) {
            throw new BusinessException(404, "练习记录不存在");
        }
        if (!practice.getStudentId().equals(studentId)) {
            throw new BusinessException(403, "无权查看此练习");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", practice.getId());
        result.put("courseName", practice.getCourseName());
        result.put("score", practice.getScore());
        result.put("totalScore", practice.getTotalScore());
        result.put("createTime", practice.getCreateTime());

        try {
            List<?> questions = objectMapper.readValue(practice.getQuestions(), List.class);
            result.put("questions", questions);

            if (practice.getAnswers() != null) {
                Map<?, ?> answers = objectMapper.readValue(practice.getAnswers(), Map.class);
                result.put("answers", answers);
            }
        } catch (Exception e) {
            result.put("questionsRaw", practice.getQuestions());
        }

        return Result.success(result);
    }

    /**
     * 清理AI返回的JSON字符串（去除markdown标记等）
     */
    private String cleanJson(String json) {
        if (json == null) return json;
        json = json.trim();
        // 去除 ```json ... ``` 标记
        if (json.startsWith("```")) {
            int start = json.indexOf('\n');
            int end = json.lastIndexOf("```");
            if (start > 0 && end > start) {
                json = json.substring(start + 1, end).trim();
            }
        }
        return json;
    }
}
