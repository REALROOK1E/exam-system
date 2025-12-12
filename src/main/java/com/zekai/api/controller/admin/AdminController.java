package com.zekai.api.controller.admin;

import com.zekai.api.dto.ApiResponse;
import com.zekai.util.DatabaseUtil;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

/**
 * 管理后台控制器
 * Admin Controller - 7个功能
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    /**
     * Feature 33: 更新题目统计
     * POST /admin/questions/update-statistics
     */
    @PostMapping("/questions/update-statistics")
    public ApiResponse<?> updateQuestionStatistics() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE questions q " +
                        "SET times_used = (SELECT COUNT(DISTINCT sa.student_quiz_id) FROM student_answers sa WHERE sa.question_id = q.question_id), " +
                        "    total_attempts = (SELECT COUNT(*) FROM student_answers sa WHERE sa.question_id = q.question_id), " +
                        "    correct_count = (SELECT COUNT(*) FROM student_answers sa WHERE sa.question_id = q.question_id AND sa.is_correct = TRUE) " +
                        "WHERE q.question_id IN (SELECT DISTINCT question_id FROM student_answers)";

            Statement stmt = conn.createStatement();
            int updated = stmt.executeUpdate(sql);

            Map<String, Object> data = new HashMap<>();
            data.put("updatedQuestions", updated);

            return ApiResponse.success("统计更新完成", data);
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * Feature 34: 自适应难度评级
     * GET /admin/questions/difficulty-rating
     */
    @GetMapping("/questions/difficulty-rating")
    public ApiResponse<?> adaptiveDifficultyRating(
            @RequestParam(defaultValue = "10") Integer minAttempts) {

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.question_id, q.question_text, q.difficulty_level, " +
                        "q.total_attempts, q.correct_count, " +
                        "CASE WHEN q.total_attempts > 0 " +
                        "THEN ROUND(q.correct_count * 100.0 / q.total_attempts, 2) " +
                        "ELSE 0 END as correct_rate, " +
                        "CASE " +
                        "  WHEN q.total_attempts = 0 THEN 'No Data' " +
                        "  WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 80 THEN 'Too Easy' " +
                        "  WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 60 THEN 'Appropriate' " +
                        "  WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 40 THEN 'Slightly Hard' " +
                        "  ELSE 'Too Hard' " +
                        "END as actual_difficulty " +
                        "FROM questions q " +
                        "WHERE q.times_used >= ? AND q.is_deleted = FALSE";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, minAttempts);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> ratings = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rating = new HashMap<>();
                rating.put("questionId", rs.getLong("question_id"));
                rating.put("questionText", rs.getString("question_text"));
                rating.put("presetDifficulty", rs.getInt("difficulty_level"));
                rating.put("totalAttempts", rs.getInt("total_attempts"));
                rating.put("correctCount", rs.getInt("correct_count"));
                rating.put("correctRate", rs.getDouble("correct_rate"));
                rating.put("actualDifficulty", rs.getString("actual_difficulty"));
                ratings.add(rating);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("ratedQuestions", ratings);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("评级失败: " + e.getMessage());
        }
    }

    /**
     * Feature 36: 题目使用排名
     * GET /admin/questions/ranking
     */
    @GetMapping("/questions/ranking")
    public ApiResponse<?> questionUsageRanking(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "times_used") String orderBy) {

        try (Connection conn = DatabaseUtil.getConnection()) {
            String validOrderBy = orderBy.matches("times_used|total_attempts|correct_rate") ? orderBy : "times_used";

            String sql = "SELECT q.question_id, q.question_text, q.times_used, q.total_attempts, " +
                        "CASE WHEN q.total_attempts > 0 " +
                        "THEN ROUND(q.correct_count * 100.0 / q.total_attempts, 2) " +
                        "ELSE 0 END as correct_rate " +
                        "FROM questions q " +
                        "WHERE q.times_used > 0 AND q.is_deleted = FALSE " +
                        "ORDER BY " + validOrderBy + " DESC LIMIT ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> rankings = new ArrayList<>();
            int rank = 0;
            while (rs.next()) {
                rank++;
                Map<String, Object> question = new HashMap<>();
                question.put("rank", rank);
                question.put("questionId", rs.getLong("question_id"));
                question.put("questionText", rs.getString("question_text"));
                question.put("timesUsed", rs.getInt("times_used"));
                question.put("totalAttempts", rs.getInt("total_attempts"));
                question.put("correctRate", rs.getDouble("correct_rate"));
                rankings.add(question);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("topQuestions", rankings);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Feature 37: 科目层级查询
     * GET /admin/subjects/hierarchy
     */
    @GetMapping("/subjects/hierarchy")
    public ApiResponse<?> getSubjectHierarchy() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT s.subject_id, s.subject_name, s.level, s.parent_subject_id, " +
                        "COUNT(q.question_id) as question_count " +
                        "FROM subjects s " +
                        "LEFT JOIN questions q ON s.subject_id = q.subject_id AND q.is_deleted = FALSE " +
                        "GROUP BY s.subject_id, s.subject_name, s.level, s.parent_subject_id " +
                        "ORDER BY s.level, s.subject_id";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> subjects = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> subject = new HashMap<>();
                subject.put("subjectId", rs.getLong("subject_id"));
                subject.put("subjectName", rs.getString("subject_name"));
                subject.put("level", rs.getInt("level"));
                subject.put("parentSubjectId", rs.getLong("parent_subject_id"));
                subject.put("questionCount", rs.getInt("question_count"));
                subjects.add(subject);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("subjects", subjects);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 用户管理 - 查询所有用户
     * GET /admin/users
     */
    @GetMapping("/users")
    public ApiResponse<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {

        try (Connection conn = DatabaseUtil.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT user_id, username, role, full_name, email, is_active, created_at " +
                "FROM users WHERE 1=1"
            );

            if (role != null) {
                sql.append(" AND role = ?");
            }
            if (isActive != null) {
                sql.append(" AND is_active = ?");
            }
            sql.append(" ORDER BY created_at DESC");

            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            if (role != null) {
                pstmt.setString(paramIndex++, role);
            }
            if (isActive != null) {
                pstmt.setBoolean(paramIndex++, isActive);
            }

            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> users = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("userId", rs.getLong("user_id"));
                user.put("username", rs.getString("username"));
                user.put("role", rs.getString("role"));
                user.put("fullName", rs.getString("full_name"));
                user.put("email", rs.getString("email"));
                user.put("isActive", rs.getBoolean("is_active"));
                user.put("createdAt", rs.getTimestamp("created_at"));
                users.add(user);
            }

            // 获取统计
            sql = new StringBuilder("SELECT role, COUNT(*) as count FROM users GROUP BY role");
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();

            Map<String, Integer> statistics = new HashMap<>();
            while (rs.next()) {
                statistics.put(rs.getString("role"), rs.getInt("count"));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("statistics", statistics);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 用户管理 - 更新用户状态
     * PUT /admin/users/{userId}
     */
    @PutMapping("/users/{userId}")
    public ApiResponse<?> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {

        try (Connection conn = DatabaseUtil.getConnection()) {
            Boolean isActive = (Boolean) request.get("isActive");

            String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, isActive);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("isActive", isActive);

            return ApiResponse.success("用户状态已更新", data);
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 系统总览
     * GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public ApiResponse<?> getSystemDashboard() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            Map<String, Object> dashboard = new HashMap<>();

            // 用户统计
            String sql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            Map<String, Integer> userStats = new HashMap<>();
            int totalUsers = 0;
            while (rs.next()) {
                int count = rs.getInt("count");
                userStats.put(rs.getString("role"), count);
                totalUsers += count;
            }
            userStats.put("total", totalUsers);
            dashboard.put("users", userStats);

            // 课程统计
            sql = "SELECT COUNT(*) as total FROM courses";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> courseStats = new HashMap<>();
                courseStats.put("total", rs.getInt("total"));
                dashboard.put("courses", courseStats);
            }

            // 题目统计
            sql = "SELECT COUNT(*) as total, question_type FROM questions " +
                  "WHERE is_deleted = FALSE GROUP BY question_type";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            Map<String, Integer> questionStats = new HashMap<>();
            int totalQuestions = 0;
            while (rs.next()) {
                int count = rs.getInt("total");
                questionStats.put(rs.getString("question_type"), count);
                totalQuestions += count;
            }
            questionStats.put("total", totalQuestions);
            dashboard.put("questions", questionStats);

            // 测验统计
            sql = "SELECT COUNT(*) as total FROM quizzes";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> quizStats = new HashMap<>();
                quizStats.put("total", rs.getInt("total"));
                dashboard.put("quizzes", quizStats);
            }

            // 提交统计
            sql = "SELECT COUNT(*) as total_submissions, " +
                  "AVG(score) as avg_score, " +
                  "SUM(CASE WHEN score >= (SELECT passing_score FROM quizzes WHERE quiz_id = student_quizzes.quiz_id) THEN 1 ELSE 0 END) * 100.0 / COUNT(*) as pass_rate " +
                  "FROM student_quizzes WHERE graded = TRUE";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalSubmissions", rs.getInt("total_submissions"));
                stats.put("averageScore", rs.getDouble("avg_score"));
                stats.put("passRate", rs.getDouble("pass_rate"));
                dashboard.put("statistics", stats);
            }

            return ApiResponse.success(dashboard);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 数据维护 - 软删除题目
     * DELETE /admin/questions/{questionId}
     */
    @DeleteMapping("/questions/{questionId}")
    public ApiResponse<?> softDeleteQuestion(@PathVariable Long questionId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE questions SET is_deleted = TRUE WHERE question_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("questionId", questionId);
            data.put("deleted", true);

            return ApiResponse.success("题目已删除", data);
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 数据维护 - 恢复题目
     * POST /admin/questions/{questionId}/restore
     */
    @PostMapping("/questions/{questionId}/restore")
    public ApiResponse<?> restoreQuestion(@PathVariable Long questionId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE questions SET is_deleted = FALSE WHERE question_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("questionId", questionId);
            data.put("restored", true);

            return ApiResponse.success("题目已恢复", data);
        } catch (Exception e) {
            return ApiResponse.error("恢复失败: " + e.getMessage());
        }
    }
}

