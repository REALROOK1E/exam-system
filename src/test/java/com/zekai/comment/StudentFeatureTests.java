package com.zekai.comment;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ========================================
 * STUDENT FEATURE TESTS - 学生端功能JUnit测试
 * ========================================
 *
 * 包含11个学生端功能的独立测试:
 * - Feature 1: 创建学生账户
 * - Feature 3: 学生登录认证
 * - Feature 8: 学生注册课程
 * - Feature 9: 查询教室学生
 * - Feature 10: 学生退课
 * - Feature 21: 学生开始测验
 * - Feature 22: 学生提交答案
 * - Feature 23: 完成测验提交
 * - Feature 24: 查看可用测验
 * - Feature 28: 学生查看成绩
 * - Feature 29: 查看答案详情
 *
 * 使用方法:
 * - 点击每个测试方法左侧的绿色运行按钮单独运行
 * - 点击类名左侧的运行按钮运行所有学生端测试
 *
 * @author Exam System Team
 * @version 2.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StudentFeatureTests extends ExamSystemTestBase {

    // ==================================================================================
    // Feature 1: 创建学生账户 | Create Student Account
    // ==================================================================================

    /**
     * 功能1: 创建学生账户
     *
     * 【所需参数】
     * - username: 用户名 (String, 唯一)
     * - password: 密码 (String)
     * - email: 邮箱 (String)
     * - fullName: 全名 (String)
     * - studentNumber: 学号 (String, 唯一)
     * - grade: 年级 (String)
     * - major: 专业 (String)
     *
     * 【返回结果】
     * - studentUserId: 用户表ID
     * - studentId: 学生表ID
     */
    @Test
    @Order(1)
    @DisplayName("Feature 1: 创建学生账户 | Create Student Account")
    void testFeature1_CreateStudentAccount() throws SQLException {
        System.out.println("┌─ FEATURE 1: Create Student Account | 创建学生账户");

        // 先创建教师（用于后续测试）
        createTeacherAccount();

        // 参数
        String username = "alice_student";
        String password = "password123";
        String email = "alice@university.edu";
        String fullName = "Alice Johnson";
        String studentNumber = "STU2025001";
        String grade = "Junior";
        String major = "Computer Science";

        // 创建用户记录
        pstmt = conn.prepareStatement(
            "INSERT INTO users (username, password_hash, email, full_name, role) " +
            "VALUES (?, SHA2(?, 256), ?, ?, 'student')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, email);
        pstmt.setString(4, fullName);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回生成的用户ID");
        studentUserId = rs.getLong(1);
        assertTrue(studentUserId > 0, "用户ID应该大于0");
        System.out.println("  → User created: user_id=" + studentUserId);

        // 创建学生记录
        pstmt = conn.prepareStatement(
            "INSERT INTO students (user_id, student_number, grade, major, enrollment_date) " +
            "VALUES (?, ?, ?, ?, CURRENT_DATE)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, studentUserId);
        pstmt.setString(2, studentNumber);
        pstmt.setString(3, grade);
        pstmt.setString(4, major);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回生成的学生ID");
        studentId = rs.getLong(1);
        assertTrue(studentId > 0, "学生ID应该大于0");
        System.out.println("  → Student record created: student_id=" + studentId);

        // 验证数据
        pstmt = conn.prepareStatement("SELECT * FROM students WHERE student_id = ?");
        pstmt.setLong(1, studentId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该能查询到学生记录");
        assertEquals(studentNumber, rs.getString("student_number"), "学号应该匹配");

        System.out.println("  ✓ Student account created successfully\n");
    }

    // ==================================================================================
    // Feature 3: 学生登录认证 | Student Login Authentication
    // ==================================================================================

    /**
     * 功能3: 学生登录认证
     *
     * 【所需参数】
     * - username: 用户名 (String)
     * - password: 密码 (String)
     *
     * 【返回结果】
     * - 认证成功: 返回用户信息
     * - 认证失败: 返回空结果
     */
    @Test
    @Order(3)
    @DisplayName("Feature 3: 学生登录认证 | Student Login Authentication")
    void testFeature3_StudentLogin() throws SQLException {
        System.out.println("┌─ FEATURE 3: Student Login Authentication | 学生登录认证");

        String username = "alice_student";
        String password = "password123";

        pstmt = conn.prepareStatement(
            "SELECT u.user_id, u.username, u.role, u.full_name, s.student_id, s.student_number " +
            "FROM users u " +
            "LEFT JOIN students s ON u.user_id = s.user_id " +
            "WHERE u.username = ? AND u.password_hash = SHA2(?, 256) AND u.is_active = TRUE"
        );
        pstmt.setString(1, username);
        pstmt.setString(2, password);

        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "登录应该成功");
        assertEquals("student", rs.getString("role"), "角色应该是student");
        assertEquals("Alice Johnson", rs.getString("full_name"), "姓名应该匹配");

        System.out.println("  → Authentication successful!");
        System.out.println("  → User ID: " + rs.getLong("user_id"));
        System.out.println("  → Role: " + rs.getString("role"));

        // 测试错误密码
        pstmt.setString(2, "wrongpassword");
        rs = pstmt.executeQuery();
        assertFalse(rs.next(), "错误密码不应该登录成功");

        System.out.println("  ✓ Login authentication completed\n");
    }

    // ==================================================================================
    // Feature 8: 学生注册课程 | Student Enrollment
    // ==================================================================================

    /**
     * 功能8: 学生注册课程
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     * - classroomId: 教室ID (long)
     *
     * 【返回结果】
     * - enrollmentId: 注册记录ID
     * - 注册状态: active
     */
    @Test
    @Order(8)
    @DisplayName("Feature 8: 学生注册课程 | Student Enrollment")
    void testFeature8_StudentEnrollment() throws SQLException {
        System.out.println("┌─ FEATURE 8: Student Enrollment | 学生注册课程");

        // 先创建课程和教室
        createCourseAndClassroom();

        pstmt = conn.prepareStatement(
            "INSERT INTO enrollments (student_id, classroom_id, status) VALUES (?, ?, 'active')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回注册ID");
        enrollmentId = rs.getLong(1);
        assertTrue(enrollmentId > 0, "注册ID应该大于0");

        // 验证注册状态
        pstmt = conn.prepareStatement("SELECT status FROM enrollments WHERE enrollment_id = ?");
        pstmt.setLong(1, enrollmentId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该能查询到注册记录");
        assertEquals("active", rs.getString("status"), "状态应该是active");

        System.out.println("  → Enrollment created: enrollment_id=" + enrollmentId);
        System.out.println("  → Student ID: " + studentId);
        System.out.println("  → Classroom ID: " + classroomId);
        System.out.println("  ✓ Student enrollment completed\n");
    }

    // ==================================================================================
    // Feature 9: 查询教室学生 | Query Classroom Students
    // ==================================================================================

    /**
     * 功能9: 查询教室学生
     *
     * 【所需参数】
     * - classroomId: 教室ID (long)
     *
     * 【返回结果】
     * - 学生列表: full_name, student_number, grade, major
     */
    @Test
    @Order(9)
    @DisplayName("Feature 9: 查询教室学生 | Query Classroom Students")
    void testFeature9_QueryClassroomStudents() throws SQLException {
        System.out.println("┌─ FEATURE 9: Query Classroom Students | 查询教室学生");

        pstmt = conn.prepareStatement(
            "SELECT s.student_id, u.full_name, s.student_number, s.grade, s.major " +
            "FROM enrollments e " +
            "JOIN students s ON e.student_id = s.student_id " +
            "JOIN users u ON s.user_id = u.user_id " +
            "WHERE e.classroom_id = ? AND e.status = 'active'"
        );
        pstmt.setLong(1, classroomId);

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Students in Classroom ID " + classroomId + ":");
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("full_name") +
                " (" + rs.getString("student_number") + ")");
        }

        assertTrue(count > 0, "应该至少有一个学生注册");
        System.out.println("  → Total students: " + count);
        System.out.println("  ✓ Student query completed\n");
    }

    // ==================================================================================
    // Feature 10: 学生退课 | Student Withdrawal
    // ==================================================================================

    /**
     * 功能10: 学生退课
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     * - classroomId: 教室ID (long)
     *
     * 【操作说明】
     * - 软删除: 将状态改为 'dropped'
     */
    @Test
    @Order(10)
    @DisplayName("Feature 10: 学生退课 | Student Withdrawal")
    void testFeature10_StudentWithdrawal() throws SQLException {
        System.out.println("┌─ FEATURE 10: Student Withdrawal | 学生退课");

        // 执行退课
        pstmt = conn.prepareStatement(
            "UPDATE enrollments SET status = 'dropped' WHERE student_id = ? AND classroom_id = ?"
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        int updated = pstmt.executeUpdate();

        assertEquals(1, updated, "应该更新一条记录");

        // 验证状态变更
        pstmt = conn.prepareStatement(
            "SELECT status FROM enrollments WHERE student_id = ? AND classroom_id = ?"
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该能查询到记录");
        assertEquals("dropped", rs.getString("status"), "状态应该是dropped");

        System.out.println("  → Status changed: active → dropped");

        // 恢复状态（用于后续测试）
        executeUpdate("UPDATE enrollments SET status = 'active' WHERE student_id = " +
                     studentId + " AND classroom_id = " + classroomId);
        System.out.println("  → Status restored to 'active' (for subsequent tests)");
        System.out.println("  ✓ Withdrawal feature demonstrated\n");
    }

    // ==================================================================================
    // Feature 21: 学生开始测验 | Student Start Quiz
    // ==================================================================================

    /**
     * 功能21: 学生开始测验
     *
     * 【所需参数】
     * - quizId: 测验ID (long)
     * - studentId: 学生ID (long)
     *
     * 【返回结果】
     * - studentQuizId: 学生测验会话ID
     * - status: 'in_progress'
     */
    @Test
    @Order(21)
    @DisplayName("Feature 21: 学生开始测验 | Student Start Quiz")
    void testFeature21_StudentStartQuiz() throws SQLException {
        System.out.println("┌─ FEATURE 21: Student Start Quiz | 学生开始测验");

        // 准备测验数据
        prepareQuizData();

        pstmt = conn.prepareStatement(
            "INSERT INTO student_quizzes (quiz_id, student_id, start_time, status) " +
            "VALUES (?, ?, NOW(), 'in_progress')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, quizId);
        pstmt.setLong(2, studentId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回学生测验ID");
        studentQuizId = rs.getLong(1);
        assertTrue(studentQuizId > 0, "学生测验ID应该大于0");

        // 验证状态
        pstmt = conn.prepareStatement("SELECT status FROM student_quizzes WHERE student_quiz_id = ?");
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该能查询到记录");
        assertEquals("in_progress", rs.getString("status"), "状态应该是in_progress");

        System.out.println("  → Session ID: " + studentQuizId);
        System.out.println("  → Status: in_progress");
        System.out.println("  ✓ Quiz started successfully\n");
    }

    // ==================================================================================
    // Feature 22: 学生提交答案 | Student Submit Answers
    // ==================================================================================

    /**
     * 功能22: 学生提交答案
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     * - 每道题的答案
     *
     * 【返回结果】
     * - 每道题的答案记录
     */
    @Test
    @Order(22)
    @DisplayName("Feature 22: 学生提交答案 | Student Submit Answers")
    void testFeature22_StudentSubmitAnswers() throws SQLException {
        System.out.println("┌─ FEATURE 22: Student Submit Answers | 学生提交答案");

        // 获取测验中的所有题目
        pstmt = conn.prepareStatement(
            "SELECT qq.question_id FROM quiz_questions qq WHERE qq.quiz_id = ? ORDER BY qq.question_order"
        );
        pstmt.setLong(1, quizId);
        rs = pstmt.executeQuery();

        List<Long> questionIds = new ArrayList<>();
        while (rs.next()) {
            questionIds.add(rs.getLong("question_id"));
        }

        assertFalse(questionIds.isEmpty(), "测验应该有题目");

        // 为每道题提交答案
        pstmt = conn.prepareStatement(
            "INSERT INTO student_answers (student_quiz_id, question_id, selected_option_id) VALUES (?, ?, ?)"
        );

        int answeredCount = 0;
        for (long qid : questionIds) {
            // 获取该题的第一个选项
            PreparedStatement ps = conn.prepareStatement(
                "SELECT option_id FROM question_options WHERE question_id = ? ORDER BY option_order LIMIT 1"
            );
            ps.setLong(1, qid);
            ResultSet optRs = ps.executeQuery();

            if (optRs.next()) {
                pstmt.setLong(1, studentQuizId);
                pstmt.setLong(2, qid);
                pstmt.setLong(3, optRs.getLong("option_id"));
                pstmt.executeUpdate();
                answeredCount++;
            }
            optRs.close();
            ps.close();
        }

        assertEquals(questionIds.size(), answeredCount, "所有题目都应该有答案");
        System.out.println("  → " + answeredCount + " questions answered");
        System.out.println("  ✓ All answers submitted\n");
    }

    // ==================================================================================
    // Feature 23: 完成测验提交 | Complete Quiz Submission
    // ==================================================================================

    /**
     * 功能23: 完成测验提交
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     *
     * 【返回结果】
     * - status: 'submitted'
     * - submit_time: 提交时间
     */
    @Test
    @Order(23)
    @DisplayName("Feature 23: 完成测验提交 | Complete Quiz Submission")
    void testFeature23_CompleteQuizSubmission() throws SQLException {
        System.out.println("┌─ FEATURE 23: Complete Quiz Submission | 完成测验提交");

        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes SET submit_time = NOW(), status = 'submitted' " +
            "WHERE student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        int updated = pstmt.executeUpdate();

        assertEquals(1, updated, "应该更新一条记录");

        // 验证状态
        pstmt = conn.prepareStatement("SELECT status, submit_time FROM student_quizzes WHERE student_quiz_id = ?");
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该能查询到记录");
        assertEquals("submitted", rs.getString("status"), "状态应该是submitted");
        assertNotNull(rs.getTimestamp("submit_time"), "提交时间不应为空");

        System.out.println("  → Status changed: in_progress → submitted");
        System.out.println("  ✓ Quiz submitted successfully\n");
    }

    // ==================================================================================
    // Feature 24: 查看可用测验 | View Available Quizzes
    // ==================================================================================

    /**
     * 功能24: 查看可用测验
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     *
     * 【返回结果】
     * - 测验列表: quiz_id, title, course_name, status
     */
    @Test
    @Order(24)
    @DisplayName("Feature 24: 查看可用测验 | View Available Quizzes")
    void testFeature24_ViewAvailableQuizzes() throws SQLException {
        System.out.println("┌─ FEATURE 24: View Available Quizzes | 查看可用测验");

        pstmt = conn.prepareStatement(
            "SELECT q.quiz_id, q.title, q.duration_minutes, c.course_name, sq.status as quiz_status " +
            "FROM enrollments e " +
            "JOIN classrooms cl ON e.classroom_id = cl.classroom_id " +
            "JOIN courses c ON cl.course_id = c.course_id " +
            "JOIN quizzes q ON cl.classroom_id = q.classroom_id " +
            "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id AND sq.student_id = e.student_id " +
            "WHERE e.student_id = ? AND e.status = 'active'"
        );
        pstmt.setLong(1, studentId);

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Available Quizzes:");
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("title"));
            System.out.println("        Course: " + rs.getString("course_name"));
            String status = rs.getString("quiz_status");
            System.out.println("        Status: " + (status != null ? status : "Not Started"));
        }

        assertTrue(count > 0, "应该至少有一个测验");
        System.out.println("  → Total quizzes: " + count);
        System.out.println("  ✓ Quiz list retrieved\n");
    }

    // ==================================================================================
    // Feature 28: 学生查看成绩 | Student View Grades
    // ==================================================================================

    /**
     * 功能28: 学生查看成绩
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     *
     * 【前置条件】
     * - 成绩已评分和发布
     *
     * 【返回结果】
     * - 成绩列表: title, score, percentage, result
     */
    @Test
    @Order(28)
    @DisplayName("Feature 28: 学生查看成绩 | Student View Grades")
    void testFeature28_StudentViewGrades() throws SQLException {
        System.out.println("┌─ FEATURE 28: Student View Grades | 学生查看成绩");

        // 先执行自动评分
        performAutoGrading();

        pstmt = conn.prepareStatement(
            "SELECT q.title, sq.score, q.total_points, sq.percentage, " +
            "CASE WHEN sq.score >= q.passing_score THEN 'Passed' ELSE 'Failed' END as result " +
            "FROM student_quizzes sq " +
            "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
            "WHERE sq.student_id = ? AND sq.published = TRUE AND sq.graded = TRUE"
        );
        pstmt.setLong(1, studentId);

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Published Grades:");
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("title"));
            System.out.println("        Score: " + rs.getDouble("score") + "/" + rs.getInt("total_points"));
            System.out.println("        Result: " + rs.getString("result"));
        }

        assertTrue(count > 0, "应该有已发布的成绩");
        System.out.println("  ✓ Grades displayed\n");
    }

    // ==================================================================================
    // Feature 29: 查看答案详情 | View Answer Details
    // ==================================================================================

    /**
     * 功能29: 查看答案详情
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     *
     * 【返回结果】
     * - 每道题的详情: question_text, your_answer, is_correct, points_earned
     */
    @Test
    @Order(29)
    @DisplayName("Feature 29: 查看答案详情 | View Answer Details")
    void testFeature29_ViewAnswerDetails() throws SQLException {
        System.out.println("┌─ FEATURE 29: View Answer Details | 查看答案详情");

        pstmt = conn.prepareStatement(
            "SELECT q.question_text, qo.option_text, sa.is_correct, sa.points_earned, qq.points " +
            "FROM student_answers sa " +
            "JOIN questions q ON sa.question_id = q.question_id " +
            "JOIN question_options qo ON sa.selected_option_id = qo.option_id " +
            "JOIN quiz_questions qq ON sa.question_id = qq.question_id AND qq.quiz_id = " +
            "(SELECT quiz_id FROM student_quizzes WHERE student_quiz_id = sa.student_quiz_id) " +
            "WHERE sa.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Answer Details:");
        while (rs.next()) {
            count++;
            String text = rs.getString("question_text");
            if (text.length() > 40) text = text.substring(0, 40) + "...";
            System.out.println("     Q" + count + ": " + text);
            System.out.println("        Your Answer: " + rs.getString("option_text"));
            System.out.println("        Result: " + (rs.getBoolean("is_correct") ? "✓ Correct" : "✗ Incorrect"));
            System.out.println("        Points: " + rs.getDouble("points_earned") + "/" + rs.getInt("points"));
        }

        assertTrue(count > 0, "应该有答案详情");
        System.out.println("  ✓ Answer details retrieved\n");
    }

    // ==================================================================================
    // 辅助方法
    // ==================================================================================

    private void prepareQuizData() throws SQLException {
        // 创建科目
        createSubject();

        // 创建题目
        createQuestionWithOptions("What is the time complexity of binary search?",
            new String[]{"O(n)", "O(log n)", "O(n²)", "O(1)"},
            new boolean[]{false, true, false, false});
        questionId1 = getLastQuestionId();

        createQuestionWithOptions("Which data structure uses LIFO principle?",
            new String[]{"Queue", "Stack", "Array", "Tree"},
            new boolean[]{false, true, false, false});
        questionId2 = getLastQuestionId();

        createQuestionWithOptions("What is a balanced binary tree?",
            new String[]{"AVL Tree", "Linked List", "Hash Table", "Graph"},
            new boolean[]{true, false, false, false});
        questionId3 = getLastQuestionId();

        // 创建测验
        createQuiz();

        // 添加题目到测验
        addQuestionsToQuiz();
    }

    private void performAutoGrading() throws SQLException {
        // 自动评分 - 更新每道题的正确性和得分
        pstmt = conn.prepareStatement(
            "UPDATE student_answers sa " +
            "INNER JOIN question_options qo ON sa.selected_option_id = qo.option_id " +
            "INNER JOIN quiz_questions qq ON sa.question_id = qq.question_id " +
            "INNER JOIN student_quizzes sq ON sa.student_quiz_id = sq.student_quiz_id " +
            "SET sa.is_correct = qo.is_correct, " +
            "    sa.points_earned = CASE WHEN qo.is_correct THEN qq.points ELSE 0 END " +
            "WHERE sa.student_quiz_id = ? AND qq.quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.setLong(2, quizId);
        pstmt.executeUpdate();

        // 计算总分并发布
        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes sq " +
            "SET sq.score = (SELECT COALESCE(SUM(points_earned), 0) FROM student_answers WHERE student_quiz_id = ?), " +
            "    sq.percentage = (SELECT COALESCE(SUM(points_earned), 0) * 100.0 / 100 FROM student_answers WHERE student_quiz_id = ?), " +
            "    sq.graded = TRUE, sq.published = TRUE, sq.status = 'completed' " +
            "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.setLong(2, studentQuizId);
        pstmt.setLong(3, studentQuizId);
        pstmt.executeUpdate();
    }
}

