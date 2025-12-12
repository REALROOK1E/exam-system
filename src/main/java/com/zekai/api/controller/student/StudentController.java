package com.zekai.api.controller.student;

import com.zekai.api.dto.ApiResponse;
import com.zekai.util.DatabaseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

/**
 * 学生端控制器
 * Student Controller - 11个功能
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    /**
     * Feature 1: 创建学生账户
     * POST /student/register
     */
    @PostMapping("/register")
    public ApiResponse<?> registerStudent(@RequestBody Map<String, Object> request) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String email = (String) request.get("email");
            String fullName = (String) request.get("fullName");
            String studentNumber = (String) request.get("studentNumber");
            String grade = (String) request.get("grade");
            String major = (String) request.get("major");

            // 创建用户
            String sql = "INSERT INTO users (username, password_hash, email, full_name, role) " +
                        "VALUES (?, SHA2(?, 256), ?, ?, 'student')";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long userId = 0;
            if (rs.next()) userId = rs.getLong(1);

            // 创建学生记录
            sql = "INSERT INTO students (user_id, student_number, grade, major, enrollment_date) " +
                  "VALUES (?, ?, ?, ?, CURRENT_DATE)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, userId);
            pstmt.setString(2, studentNumber);
            pstmt.setString(3, grade);
            pstmt.setString(4, major);
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            long studentId = 0;
            if (rs.next()) studentId = rs.getLong(1);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("studentId", studentId);
            data.put("username", username);
            data.put("studentNumber", studentNumber);

            return ApiResponse.success("学生账户创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 8: 学生注册课程
     * POST /student/enrollments
     */
    @PostMapping("/enrollments")
    public ApiResponse<?> enrollCourse(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long studentId = (Long) httpRequest.getAttribute("roleId");
        Long classroomId = ((Number) request.get("classroomId")).longValue();

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "INSERT INTO enrollments (student_id, classroom_id, status) " +
                        "VALUES (?, ?, 'active')";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, studentId);
            pstmt.setLong(2, classroomId);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long enrollmentId = 0;
            if (rs.next()) enrollmentId = rs.getLong(1);

            Map<String, Object> data = new HashMap<>();
            data.put("enrollmentId", enrollmentId);
            data.put("studentId", studentId);
            data.put("classroomId", classroomId);
            data.put("status", "active");

            return ApiResponse.success("注册成功", data);
        } catch (Exception e) {
            return ApiResponse.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * Feature 9: 查询教室学生
     * GET /student/classrooms/{classroomId}/students
     */
    @GetMapping("/classrooms/{classroomId}/students")
    public ApiResponse<?> getClassroomStudents(@PathVariable Long classroomId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT s.student_id, u.full_name, s.student_number, s.grade, s.major " +
                        "FROM enrollments e " +
                        "JOIN students s ON e.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "WHERE e.classroom_id = ? AND e.status = 'active'";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, classroomId);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> students = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("studentId", rs.getLong("student_id"));
                student.put("fullName", rs.getString("full_name"));
                student.put("studentNumber", rs.getString("student_number"));
                student.put("grade", rs.getString("grade"));
                student.put("major", rs.getString("major"));
                students.add(student);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("classroomId", classroomId);
            data.put("students", students);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Feature 10: 学生退课
     * DELETE /student/enrollments/{classroomId}
     */
    @DeleteMapping("/enrollments/{classroomId}")
    public ApiResponse<?> dropCourse(
            @PathVariable Long classroomId,
            HttpServletRequest request) {

        Long studentId = (Long) request.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE enrollments SET status = 'dropped' " +
                        "WHERE student_id = ? AND classroom_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentId);
            pstmt.setLong(2, classroomId);
            pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("classroomId", classroomId);
            data.put("status", "dropped");

            return ApiResponse.success("退课成功", data);
        } catch (Exception e) {
            return ApiResponse.error("退课失败: " + e.getMessage());
        }
    }

    /**
     * Feature 24: 查看可用测验
     * GET /student/quizzes
     */
    @GetMapping("/quizzes")
    public ApiResponse<?> getAvailableQuizzes(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.quiz_id, q.title, q.duration_minutes, q.total_points, " +
                        "q.start_time, q.end_time, c.course_name, sq.status as quiz_status " +
                        "FROM enrollments e " +
                        "JOIN classrooms cl ON e.classroom_id = cl.classroom_id " +
                        "JOIN courses c ON cl.course_id = c.course_id " +
                        "JOIN quizzes q ON cl.classroom_id = q.classroom_id " +
                        "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id AND sq.student_id = e.student_id " +
                        "WHERE e.student_id = ? AND e.status = 'active'";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> quizzes = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("quizId", rs.getLong("quiz_id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("courseName", rs.getString("course_name"));
                quiz.put("durationMinutes", rs.getInt("duration_minutes"));
                quiz.put("totalPoints", rs.getInt("total_points"));
                quiz.put("startTime", rs.getTimestamp("start_time"));
                quiz.put("endTime", rs.getTimestamp("end_time"));
                quiz.put("myStatus", rs.getString("quiz_status"));
                quizzes.add(quiz);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("quizzes", quizzes);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Feature 21: 开始测验
     * POST /student/quizzes/{quizId}/start
     */
    @PostMapping("/quizzes/{quizId}/start")
    public ApiResponse<?> startQuiz(
            @PathVariable Long quizId,
            HttpServletRequest request) {

        Long studentId = (Long) request.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            // 创建学生测验会话
            String sql = "INSERT INTO student_quizzes (quiz_id, student_id, start_time, status) " +
                        "VALUES (?, ?, NOW(), 'in_progress')";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, quizId);
            pstmt.setLong(2, studentId);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long studentQuizId = 0;
            if (rs.next()) studentQuizId = rs.getLong(1);

            // 获取题目列表
            sql = "SELECT qq.question_id, qq.question_order, qq.points, " +
                  "q.question_text, q.question_type " +
                  "FROM quiz_questions qq " +
                  "JOIN questions q ON qq.question_id = q.question_id " +
                  "WHERE qq.quiz_id = ? ORDER BY qq.question_order";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, quizId);
            rs = pstmt.executeQuery();

            List<Map<String, Object>> questions = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> question = new HashMap<>();
                long questionId = rs.getLong("question_id");
                question.put("questionId", questionId);
                question.put("questionOrder", rs.getInt("question_order"));
                question.put("questionText", rs.getString("question_text"));
                question.put("questionType", rs.getString("question_type"));
                question.put("points", rs.getInt("points"));

                // 获取选项
                String optSql = "SELECT option_id, option_text, option_order " +
                               "FROM question_options WHERE question_id = ? ORDER BY option_order";
                PreparedStatement optPstmt = conn.prepareStatement(optSql);
                optPstmt.setLong(1, questionId);
                ResultSet optRs = optPstmt.executeQuery();

                List<Map<String, Object>> options = new ArrayList<>();
                while (optRs.next()) {
                    Map<String, Object> option = new HashMap<>();
                    option.put("optionId", optRs.getLong("option_id"));
                    option.put("optionText", optRs.getString("option_text"));
                    option.put("optionOrder", optRs.getInt("option_order"));
                    options.add(option);
                }
                question.put("options", options);
                questions.add(question);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("studentQuizId", studentQuizId);
            data.put("quizId", quizId);
            data.put("status", "in_progress");
            data.put("questions", questions);

            return ApiResponse.success("测验已开始", data);
        } catch (Exception e) {
            return ApiResponse.error("开始失败: " + e.getMessage());
        }
    }

    /**
     * Feature 22: 提交答案
     * POST /student/quizzes/{studentQuizId}/answers
     */
    @PostMapping("/quizzes/{studentQuizId}/answers")
    public ApiResponse<?> submitAnswer(
            @PathVariable Long studentQuizId,
            @RequestBody Map<String, Object> request) {

        Long questionId = ((Number) request.get("questionId")).longValue();
        Long selectedOptionId = request.get("selectedOptionId") != null ?
            ((Number) request.get("selectedOptionId")).longValue() : null;
        String answerText = (String) request.get("answerText");

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "INSERT INTO student_answers (student_quiz_id, question_id, selected_option_id, answer_text) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE selected_option_id = ?, answer_text = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentQuizId);
            pstmt.setLong(2, questionId);
            if (selectedOptionId != null) {
                pstmt.setLong(3, selectedOptionId);
                pstmt.setLong(5, selectedOptionId);
            } else {
                pstmt.setNull(3, Types.BIGINT);
                pstmt.setNull(5, Types.BIGINT);
            }
            pstmt.setString(4, answerText);
            pstmt.setString(6, answerText);
            pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("studentQuizId", studentQuizId);
            data.put("questionId", questionId);
            data.put("saved", true);

            return ApiResponse.success("答案已保存", data);
        } catch (Exception e) {
            return ApiResponse.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * Feature 23: 完成测验
     * POST /student/quizzes/{studentQuizId}/submit
     */
    @PostMapping("/quizzes/{studentQuizId}/submit")
    public ApiResponse<?> submitQuiz(@PathVariable Long studentQuizId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE student_quizzes SET submit_time = NOW(), status = 'submitted' " +
                        "WHERE student_quiz_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentQuizId);
            pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("studentQuizId", studentQuizId);
            data.put("status", "submitted");

            return ApiResponse.success("测验已提交", data);
        } catch (Exception e) {
            return ApiResponse.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * Feature 28: 查看成绩
     * GET /student/grades
     */
    @GetMapping("/grades")
    public ApiResponse<?> getGrades(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.quiz_id, q.title, c.course_name, sq.score, q.total_points, " +
                        "sq.percentage, sq.submit_time, " +
                        "CASE WHEN sq.score >= q.passing_score THEN 'Passed' ELSE 'Failed' END as result " +
                        "FROM student_quizzes sq " +
                        "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
                        "JOIN classrooms cl ON q.classroom_id = cl.classroom_id " +
                        "JOIN courses c ON cl.course_id = c.course_id " +
                        "WHERE sq.student_id = ? AND sq.published = TRUE AND sq.graded = TRUE";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> grades = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> grade = new HashMap<>();
                grade.put("quizId", rs.getLong("quiz_id"));
                grade.put("title", rs.getString("title"));
                grade.put("courseName", rs.getString("course_name"));
                grade.put("score", rs.getDouble("score"));
                grade.put("totalPoints", rs.getInt("total_points"));
                grade.put("percentage", rs.getDouble("percentage"));
                grade.put("result", rs.getString("result"));
                grade.put("submitTime", rs.getTimestamp("submit_time"));
                grades.add(grade);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("grades", grades);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Feature 29: 查看答案详情
     * GET /student/quizzes/{studentQuizId}/details
     */
    @GetMapping("/quizzes/{studentQuizId}/details")
    public ApiResponse<?> getAnswerDetails(@PathVariable Long studentQuizId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.question_text, qo.option_text, sa.is_correct, " +
                        "sa.points_earned, qq.points " +
                        "FROM student_answers sa " +
                        "JOIN questions q ON sa.question_id = q.question_id " +
                        "LEFT JOIN question_options qo ON sa.selected_option_id = qo.option_id " +
                        "JOIN quiz_questions qq ON sa.question_id = qq.question_id " +
                        "WHERE sa.student_quiz_id = ? AND qq.quiz_id = " +
                        "(SELECT quiz_id FROM student_quizzes WHERE student_quiz_id = ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentQuizId);
            pstmt.setLong(2, studentQuizId);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> questions = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> question = new HashMap<>();
                question.put("questionText", rs.getString("question_text"));
                question.put("yourAnswer", rs.getString("option_text"));
                question.put("isCorrect", rs.getBoolean("is_correct"));
                question.put("pointsEarned", rs.getDouble("points_earned"));
                question.put("totalPoints", rs.getInt("points"));
                questions.add(question);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("questions", questions);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
}

