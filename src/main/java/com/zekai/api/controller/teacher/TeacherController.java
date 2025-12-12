package com.zekai.api.controller.teacher;

import com.zekai.api.dto.ApiResponse;
import com.zekai.util.DatabaseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

/**
 * 教师端控制器
 * Teacher Controller - 18个功能
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    /**
     * Feature 2: 创建教师账户
     * POST /teacher/register
     */
    @PostMapping("/register")
    public ApiResponse<?> registerTeacher(@RequestBody Map<String, Object> request) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String email = (String) request.get("email");
            String fullName = (String) request.get("fullName");
            String department = (String) request.get("department");
            String phone = (String) request.get("phone");
            String office = (String) request.get("office");

            // 创建用户
            String sql = "INSERT INTO users (username, password_hash, email, full_name, role) " +
                        "VALUES (?, SHA2(?, 256), ?, ?, 'teacher')";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long userId = 0;
            if (rs.next()) userId = rs.getLong(1);

            // 创建教师记录
            sql = "INSERT INTO teachers (user_id, department, hire_date, phone, office) " +
                  "VALUES (?, ?, CURRENT_DATE, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, userId);
            pstmt.setString(2, department);
            pstmt.setString(3, phone);
            pstmt.setString(4, office);
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            long teacherId = 0;
            if (rs.next()) teacherId = rs.getLong(1);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("teacherId", teacherId);
            data.put("username", username);
            data.put("department", department);

            return ApiResponse.success("教师账户创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 6: 创建课程
     * POST /teacher/courses
     */
    @PostMapping("/courses")
    public ApiResponse<?> createCourse(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long teacherId = (Long) httpRequest.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            String courseCode = (String) request.get("courseCode");
            String courseName = (String) request.get("courseName");
            String description = (String) request.get("description");
            Integer creditHours = (Integer) request.get("creditHours");

            String sql = "INSERT INTO courses (course_code, course_name, description, credit_hours, created_by) " +
                        "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, courseCode);
            pstmt.setString(2, courseName);
            pstmt.setString(3, description);
            pstmt.setInt(4, creditHours);
            pstmt.setLong(5, teacherId);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long courseId = 0;
            if (rs.next()) courseId = rs.getLong(1);

            Map<String, Object> data = new HashMap<>();
            data.put("courseId", courseId);
            data.put("courseCode", courseCode);
            data.put("courseName", courseName);

            return ApiResponse.success("课程创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 7: 创建教室
     * POST /teacher/classrooms
     */
    @PostMapping("/classrooms")
    public ApiResponse<?> createClassroom(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long teacherId = (Long) httpRequest.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            Long courseId = ((Number) request.get("courseId")).longValue();
            String className = (String) request.get("className");
            String semester = (String) request.get("semester");
            Integer year = (Integer) request.get("year");
            Integer maxStudents = (Integer) request.get("maxStudents");

            String sql = "INSERT INTO classrooms (course_id, teacher_id, class_name, semester, year, max_students) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, courseId);
            pstmt.setLong(2, teacherId);
            pstmt.setString(3, className);
            pstmt.setString(4, semester);
            pstmt.setInt(5, year);
            pstmt.setInt(6, maxStudents);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long classroomId = 0;
            if (rs.next()) classroomId = rs.getLong(1);

            Map<String, Object> data = new HashMap<>();
            data.put("classroomId", classroomId);
            data.put("courseId", courseId);
            data.put("className", className);
            data.put("semester", semester);

            return ApiResponse.success("教室创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 11: 创建科目
     * POST /teacher/subjects
     */
    @PostMapping("/subjects")
    public ApiResponse<?> createSubject(@RequestBody Map<String, Object> request) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String subjectName = (String) request.get("subjectName");
            String description = (String) request.get("description");
            Integer level = (Integer) request.get("level");
            Long parentSubjectId = request.get("parentSubjectId") != null ?
                ((Number) request.get("parentSubjectId")).longValue() : null;

            String sql = "INSERT INTO subjects (subject_name, description, level, parent_subject_id) " +
                        "VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, subjectName);
            pstmt.setString(2, description);
            pstmt.setInt(3, level);
            if (parentSubjectId != null) {
                pstmt.setLong(4, parentSubjectId);
            } else {
                pstmt.setNull(4, Types.BIGINT);
            }
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long subjectId = 0;
            if (rs.next()) subjectId = rs.getLong(1);

            Map<String, Object> data = new HashMap<>();
            data.put("subjectId", subjectId);
            data.put("subjectName", subjectName);
            data.put("level", level);

            return ApiResponse.success("科目创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 12+13: 创建单个题目（含选项）
     * POST /teacher/questions
     */
    @PostMapping("/questions")
    public ApiResponse<?> createQuestion(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long teacherId = (Long) httpRequest.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            Long subjectId = ((Number) request.get("subjectId")).longValue();
            String questionText = (String) request.get("questionText");
            String questionType = (String) request.get("questionType");
            Integer difficultyLevel = (Integer) request.get("difficultyLevel");

            // 创建题目
            String sql = "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
                        "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, subjectId);
            pstmt.setString(2, questionText);
            pstmt.setString(3, questionType);
            pstmt.setInt(4, difficultyLevel);
            pstmt.setLong(5, teacherId);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long questionId = 0;
            if (rs.next()) questionId = rs.getLong(1);

            // 添加选项
            List<Map<String, Object>> options = (List<Map<String, Object>>) request.get("options");
            if (options != null && !options.isEmpty()) {
                sql = "INSERT INTO question_options (question_id, option_text, is_correct, option_order) " +
                      "VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);

                for (Map<String, Object> option : options) {
                    pstmt.setLong(1, questionId);
                    pstmt.setString(2, (String) option.get("optionText"));
                    pstmt.setBoolean(3, (Boolean) option.get("isCorrect"));
                    pstmt.setInt(4, (Integer) option.get("optionOrder"));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("questionId", questionId);
            data.put("subjectId", subjectId);
            data.put("questionType", questionType);
            data.put("optionsCount", options != null ? options.size() : 0);

            return ApiResponse.success("题目创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 14: 批量上传题目
     * POST /teacher/questions/batch
     */
    @PostMapping("/questions/batch")
    public ApiResponse<?> batchCreateQuestions(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long teacherId = (Long) httpRequest.getAttribute("roleId");
        List<Map<String, Object>> questions = (List<Map<String, Object>>) request.get("questions");

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            List<Long> questionIds = new ArrayList<>();

            for (Map<String, Object> q : questions) {
                Long subjectId = ((Number) q.get("subjectId")).longValue();
                String questionText = (String) q.get("questionText");
                String questionType = (String) q.get("questionType");
                Integer difficultyLevel = (Integer) q.get("difficultyLevel");

                String sql = "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
                            "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setLong(1, subjectId);
                pstmt.setString(2, questionText);
                pstmt.setString(3, questionType);
                pstmt.setInt(4, difficultyLevel);
                pstmt.setLong(5, teacherId);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                long questionId = 0;
                if (rs.next()) questionId = rs.getLong(1);
                questionIds.add(questionId);

                // 添加选项
                List<Map<String, Object>> options = (List<Map<String, Object>>) q.get("options");
                if (options != null && !options.isEmpty()) {
                    sql = "INSERT INTO question_options (question_id, option_text, is_correct, option_order) " +
                          "VALUES (?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);

                    for (Map<String, Object> option : options) {
                        pstmt.setLong(1, questionId);
                        pstmt.setString(2, (String) option.get("optionText"));
                        pstmt.setBoolean(3, (Boolean) option.get("isCorrect"));
                        pstmt.setInt(4, (Integer) option.get("optionOrder"));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            conn.commit();

            Map<String, Object> data = new HashMap<>();
            data.put("totalQuestions", questions.size());
            data.put("successCount", questionIds.size());
            data.put("questionIds", questionIds);

            return ApiResponse.success("批量创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("批量创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 15: 查询题目统计
     * GET /teacher/questions/statistics
     */
    @GetMapping("/questions/statistics")
    public ApiResponse<?> getQuestionStatistics(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer difficultyLevel) {

        try (Connection conn = DatabaseUtil.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) as total, " +
                "question_type, difficulty_level, s.subject_name " +
                "FROM questions q " +
                "JOIN subjects s ON q.subject_id = s.subject_id " +
                "WHERE q.is_deleted = FALSE"
            );

            if (subjectId != null) {
                sql.append(" AND q.subject_id = ?");
            }
            if (difficultyLevel != null) {
                sql.append(" AND q.difficulty_level = ?");
            }
            sql.append(" GROUP BY q.question_type, q.difficulty_level, s.subject_name");

            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            if (subjectId != null) {
                pstmt.setLong(paramIndex++, subjectId);
            }
            if (difficultyLevel != null) {
                pstmt.setInt(paramIndex++, difficultyLevel);
            }

            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> statistics = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("total", rs.getInt("total"));
                stat.put("questionType", rs.getString("question_type"));
                stat.put("difficultyLevel", rs.getInt("difficulty_level"));
                stat.put("subjectName", rs.getString("subject_name"));
                statistics.add(stat);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("statistics", statistics);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Feature 16+18+19: 创建测验（含题目和设置）
     * POST /teacher/quizzes
     */
    @PostMapping("/quizzes")
    public ApiResponse<?> createQuiz(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long teacherId = (Long) httpRequest.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            Long classroomId = ((Number) request.get("classroomId")).longValue();
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String startTime = (String) request.get("startTime");
            String endTime = (String) request.get("endTime");
            Integer durationMinutes = (Integer) request.get("durationMinutes");
            Integer totalPoints = (Integer) request.get("totalPoints");
            Integer passingScore = (Integer) request.get("passingScore");

            // 创建测验
            String sql = "INSERT INTO quizzes (classroom_id, title, description, created_by, " +
                        "start_time, end_time, duration_minutes, total_points, passing_score) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, classroomId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setLong(4, teacherId);
            pstmt.setString(5, startTime);
            pstmt.setString(6, endTime);
            pstmt.setInt(7, durationMinutes);
            pstmt.setInt(8, totalPoints);
            pstmt.setInt(9, passingScore);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            long quizId = 0;
            if (rs.next()) quizId = rs.getLong(1);

            // 添加题目
            List<Map<String, Object>> questions = (List<Map<String, Object>>) request.get("questions");
            if (questions != null && !questions.isEmpty()) {
                sql = "INSERT INTO quiz_questions (quiz_id, question_id, question_order, points) " +
                      "VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);

                for (Map<String, Object> q : questions) {
                    pstmt.setLong(1, quizId);
                    pstmt.setLong(2, ((Number) q.get("questionId")).longValue());
                    pstmt.setInt(3, (Integer) q.get("questionOrder"));
                    pstmt.setInt(4, (Integer) q.get("points"));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // 添加设置
            Map<String, Object> settings = (Map<String, Object>) request.get("settings");
            if (settings != null) {
                sql = "INSERT INTO quiz_settings (quiz_id, shuffle_questions, shuffle_options, " +
                      "show_results_immediately, allow_review) VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, quizId);
                pstmt.setBoolean(2, (Boolean) settings.getOrDefault("shuffleQuestions", false));
                pstmt.setBoolean(3, (Boolean) settings.getOrDefault("shuffleOptions", false));
                pstmt.setBoolean(4, (Boolean) settings.getOrDefault("showResultsImmediately", false));
                pstmt.setBoolean(5, (Boolean) settings.getOrDefault("allowReview", true));
                pstmt.executeUpdate();
            }

            conn.commit();

            Map<String, Object> data = new HashMap<>();
            data.put("quizId", quizId);
            data.put("title", title);
            data.put("questionCount", questions != null ? questions.size() : 0);

            return ApiResponse.success("测验创建成功", data);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * Feature 17: 随机选题
     * GET /teacher/questions/random
     */
    @GetMapping("/questions/random")
    public ApiResponse<?> randomSelectQuestions(
            @RequestParam Long subjectId,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam Integer count) {

        try (Connection conn = DatabaseUtil.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT question_id, question_text, question_type, difficulty_level " +
                "FROM questions WHERE subject_id = ? AND is_deleted = FALSE"
            );

            if (questionType != null) {
                sql.append(" AND question_type = ?");
            }
            if (difficultyLevel != null) {
                sql.append(" AND difficulty_level = ?");
            }
            sql.append(" ORDER BY RAND() LIMIT ?");

            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            pstmt.setLong(paramIndex++, subjectId);
            if (questionType != null) {
                pstmt.setString(paramIndex++, questionType);
            }
            if (difficultyLevel != null) {
                pstmt.setInt(paramIndex++, difficultyLevel);
            }
            pstmt.setInt(paramIndex++, count);

            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> questions = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> question = new HashMap<>();
                question.put("questionId", rs.getLong("question_id"));
                question.put("questionText", rs.getString("question_text"));
                question.put("questionType", rs.getString("question_type"));
                question.put("difficultyLevel", rs.getInt("difficulty_level"));
                questions.add(question);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("questions", questions);
            data.put("selectedCount", questions.size());

            return ApiResponse.success("随机选题成功", data);
        } catch (Exception e) {
            return ApiResponse.error("选题失败: " + e.getMessage());
        }
    }

    /**
     * Feature 20: 查看测验详情
     * GET /teacher/quizzes/{quizId}
     */
    @GetMapping("/quizzes/{quizId}")
    public ApiResponse<?> getQuizDetails(@PathVariable Long quizId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.*, COUNT(qq.question_id) as question_count " +
                        "FROM quizzes q " +
                        "LEFT JOIN quiz_questions qq ON q.quiz_id = qq.quiz_id " +
                        "WHERE q.quiz_id = ? GROUP BY q.quiz_id";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, quizId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("quizId", rs.getLong("quiz_id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quiz.put("startTime", rs.getTimestamp("start_time"));
                quiz.put("endTime", rs.getTimestamp("end_time"));
                quiz.put("durationMinutes", rs.getInt("duration_minutes"));
                quiz.put("totalPoints", rs.getInt("total_points"));
                quiz.put("passingScore", rs.getInt("passing_score"));
                quiz.put("questionCount", rs.getInt("question_count"));

                return ApiResponse.success(quiz);
            }

            return ApiResponse.error("测验不存在");
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Feature 25: 自动评分客观题
     * POST /teacher/quizzes/{studentQuizId}/grade
     */
    @PostMapping("/quizzes/{studentQuizId}/grade")
    public ApiResponse<?> autoGradeQuiz(@PathVariable Long studentQuizId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 获取quiz_id
            String sql = "SELECT quiz_id FROM student_quizzes WHERE student_quiz_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentQuizId);
            ResultSet rs = pstmt.executeQuery();

            long quizId = 0;
            if (rs.next()) quizId = rs.getLong("quiz_id");

            // 自动评分
            sql = "UPDATE student_answers sa " +
                  "INNER JOIN question_options qo ON sa.selected_option_id = qo.option_id " +
                  "INNER JOIN quiz_questions qq ON sa.question_id = qq.question_id " +
                  "SET sa.is_correct = qo.is_correct, " +
                  "    sa.points_earned = CASE WHEN qo.is_correct THEN qq.points ELSE 0 END " +
                  "WHERE sa.student_quiz_id = ? AND qq.quiz_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentQuizId);
            pstmt.setLong(2, quizId);
            int graded = pstmt.executeUpdate();

            // 计算总分
            sql = "UPDATE student_quizzes sq " +
                  "SET sq.score = (SELECT COALESCE(SUM(points_earned), 0) FROM student_answers WHERE student_quiz_id = ?), " +
                  "    sq.percentage = (SELECT COALESCE(SUM(points_earned), 0) * 100.0 / 100 FROM student_answers WHERE student_quiz_id = ?), " +
                  "    sq.graded = TRUE, sq.status = 'completed' " +
                  "WHERE sq.student_quiz_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentQuizId);
            pstmt.setLong(2, studentQuizId);
            pstmt.setLong(3, studentQuizId);
            pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("studentQuizId", studentQuizId);
            data.put("gradedQuestions", graded);
            data.put("status", "graded");

            return ApiResponse.success("评分完成", data);
        } catch (Exception e) {
            return ApiResponse.error("评分失败: " + e.getMessage());
        }
    }

    /**
     * Feature 27: 发布成绩
     * POST /teacher/quizzes/{quizId}/publish
     */
    @PostMapping("/quizzes/{quizId}/publish")
    public ApiResponse<?> publishGrades(@PathVariable Long quizId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE student_quizzes SET published = TRUE " +
                        "WHERE quiz_id = ? AND graded = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, quizId);
            int published = pstmt.executeUpdate();

            Map<String, Object> data = new HashMap<>();
            data.put("quizId", quizId);
            data.put("publishedCount", published);

            return ApiResponse.success("成绩已发布", data);
        } catch (Exception e) {
            return ApiResponse.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * Feature 30: 查看班级成绩
     * GET /teacher/quizzes/{quizId}/grades
     */
    @GetMapping("/quizzes/{quizId}/grades")
    public ApiResponse<?> getClassGrades(@PathVariable Long quizId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT u.full_name, s.student_number, sq.score, q.total_points, sq.percentage " +
                        "FROM student_quizzes sq " +
                        "JOIN students s ON sq.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
                        "WHERE sq.quiz_id = ? AND sq.graded = TRUE " +
                        "ORDER BY sq.score DESC";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, quizId);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> grades = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> grade = new HashMap<>();
                grade.put("fullName", rs.getString("full_name"));
                grade.put("studentNumber", rs.getString("student_number"));
                grade.put("score", rs.getDouble("score"));
                grade.put("totalPoints", rs.getInt("total_points"));
                grade.put("percentage", rs.getDouble("percentage"));
                grades.add(grade);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("quizId", quizId);
            data.put("grades", grades);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * Feature 31: 题目难度分析
     * GET /teacher/questions/{questionId}/analysis
     */
    @GetMapping("/questions/{questionId}/analysis")
    public ApiResponse<?> analyzeQuestionDifficulty(@PathVariable Long questionId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.question_id, q.question_text, q.difficulty_level, " +
                        "q.times_used, q.total_attempts, q.correct_count, " +
                        "CASE WHEN q.total_attempts > 0 " +
                        "THEN ROUND(q.correct_count * 100.0 / q.total_attempts, 2) " +
                        "ELSE 0 END as correct_rate " +
                        "FROM questions q WHERE q.question_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("questionId", rs.getLong("question_id"));
                analysis.put("questionText", rs.getString("question_text"));
                analysis.put("presetDifficulty", rs.getInt("difficulty_level"));
                analysis.put("timesUsed", rs.getInt("times_used"));
                analysis.put("totalAttempts", rs.getInt("total_attempts"));
                analysis.put("correctCount", rs.getInt("correct_count"));
                analysis.put("correctRate", rs.getDouble("correct_rate"));

                return ApiResponse.success(analysis);
            }

            return ApiResponse.error("题目不存在");
        } catch (Exception e) {
            return ApiResponse.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * Feature 32: 生成成绩报告
     * GET /teacher/quizzes/{quizId}/report
     */
    @GetMapping("/quizzes/{quizId}/report")
    public ApiResponse<?> generateGradeReport(@PathVariable Long quizId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT COUNT(DISTINCT sq.student_id) as total_students, " +
                        "ROUND(AVG(sq.score), 2) as avg_score, " +
                        "MIN(sq.score) as min_score, " +
                        "MAX(sq.score) as max_score, " +
                        "SUM(CASE WHEN sq.score >= q.passing_score THEN 1 ELSE 0 END) as passed_count " +
                        "FROM student_quizzes sq " +
                        "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
                        "WHERE sq.quiz_id = ? AND sq.graded = TRUE";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, quizId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("quizId", quizId);
                report.put("totalStudents", rs.getInt("total_students"));
                report.put("avgScore", rs.getDouble("avg_score"));
                report.put("minScore", rs.getDouble("min_score"));
                report.put("maxScore", rs.getDouble("max_score"));
                report.put("passedCount", rs.getInt("passed_count"));

                return ApiResponse.success(report);
            }

            return ApiResponse.error("无成绩数据");
        } catch (Exception e) {
            return ApiResponse.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * Feature 35: 查看教师的测验
     * GET /teacher/quizzes
     */
    @GetMapping("/quizzes")
    public ApiResponse<?> getTeacherQuizzes(HttpServletRequest request) {
        Long teacherId = (Long) request.getAttribute("roleId");

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.quiz_id, q.title, c.course_name, " +
                        "COUNT(DISTINCT sq.student_quiz_id) as submissions " +
                        "FROM quizzes q " +
                        "JOIN classrooms cl ON q.classroom_id = cl.classroom_id " +
                        "JOIN courses c ON cl.course_id = c.course_id " +
                        "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id " +
                        "WHERE q.created_by = ? " +
                        "GROUP BY q.quiz_id, q.title, c.course_name";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            List<Map<String, Object>> quizzes = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("quizId", rs.getLong("quiz_id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("courseName", rs.getString("course_name"));
                quiz.put("submissions", rs.getInt("submissions"));
                quizzes.add(quiz);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("quizzes", quizzes);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
}

