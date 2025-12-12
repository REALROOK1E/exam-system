package com.zekai.comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ========================================
 * STUDENT TESTS - 学生端功能测试
 * ========================================
 *
 * 包含以下学生端功能 (12个):
 * - Feature 1: 创建学生账户
 * - Feature 3: 用户登录认证 (学生)
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
 * - 运行 main() 执行所有学生端功能测试
 * - 直接调用单个 testFeatureXX() 方法执行单独测试
 *
 * @author Exam System Team
 * @version 2.0
 */
public class StudentTests extends TestBase {

    public static void main(String[] args) {
        runAllStudentTests();
    }

    /**
     * 运行所有学生端功能测试
     */
    public static void runAllStudentTests() {
        printHeader("STUDENT TESTS - 学生端功能测试");

        try {
            resetTestData();
            initConnection();
            setupDatabase();

            // 需要先准备教师和课程数据
            prepareTeacherAndCourseData();

            // 运行所有学生端功能
            testFeature1_CreateStudentAccount();
            testFeature3_StudentLogin();
            testFeature8_StudentEnrollment();
            testFeature9_QueryClassroomStudents();
            testFeature10_StudentWithdrawal();

            // 需要先准备测验数据才能进行考试
            prepareQuizData();

            testFeature21_StudentStartQuiz();
            testFeature22_StudentSubmitAnswers();
            testFeature23_CompleteQuizSubmission();
            testFeature24_ViewAvailableQuizzes();

            // 需要先评分才能查看成绩
            performAutoGrading();

            testFeature28_StudentViewGrades();
            testFeature29_ViewAnswerDetails();

            commit();
            printHeader("✓ ALL STUDENT TESTS COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            rollback();
        } finally {
            closeResources();
        }
    }

    // ==================================================================================
    // Feature 1: 创建学生账户 | Create Student Account
    // ==================================================================================

    /**
     * FEATURE 1: 创建学生账户
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
     *
     * 【反向操作】
     * - cleanupFeature1(): 删除创建的学生账户
     */
    public static void testFeature1_CreateStudentAccount() throws SQLException {
        printFeature(1, "Create Student Account | 创建学生账户");

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
        if (rs.next()) {
            studentUserId = rs.getLong(1);
            printInfo("User created: user_id=" + studentUserId);
        }

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
        if (rs.next()) {
            studentId = rs.getLong(1);
            printInfo("Student record created: student_id=" + studentId);
        }

        printInfo("Username: " + username);
        printInfo("Student Number: " + studentNumber);
        printInfo("Major: " + major);
        printSuccess("Student account created successfully");
    }

    /**
     * 反向操作: 删除学生账户
     */
    public static void cleanupFeature1() throws SQLException {
        if (studentUserId > 0) {
            executeUpdate("DELETE FROM users WHERE user_id = " + studentUserId);
            studentUserId = 0;
            studentId = 0;
            System.out.println("  ↺ Cleanup: Student account deleted");
        }
    }

    // ==================================================================================
    // Feature 3: 用户登录认证 (学生) | Student Login Authentication
    // ==================================================================================

    /**
     * FEATURE 3: 用户登录认证 (学生端)
     *
     * 【所需参数】
     * - username: 用户名 (String)
     * - password: 密码 (String)
     *
     * 【返回结果】
     * - 认证成功: 返回用户信息 (user_id, username, role, full_name)
     * - 认证失败: 返回空结果
     *
     * 【副作用】
     * - 更新 last_login 时间戳
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature3_StudentLogin() throws SQLException {
        printFeature(3, "Student Login Authentication | 学生登录认证");

        // 参数
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
        if (rs.next()) {
            printInfo("Authentication successful!");
            printInfo("User ID: " + rs.getLong("user_id"));
            printInfo("Username: " + rs.getString("username"));
            printInfo("Role: " + rs.getString("role"));
            printInfo("Full Name: " + rs.getString("full_name"));
            printInfo("Student ID: " + rs.getLong("student_id"));
            printInfo("Student Number: " + rs.getString("student_number"));

            // 更新最后登录时间
            executeUpdate("UPDATE users SET last_login = NOW() WHERE user_id = " + rs.getLong("user_id"));
        } else {
            printInfo("Authentication failed - invalid credentials");
        }

        printSuccess("Login authentication completed");
    }

    // ==================================================================================
    // Feature 8: 学生注册课程 | Student Enrollment
    // ==================================================================================

    /**
     * FEATURE 8: 学生注册课程
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     * - classroomId: 教室ID (long)
     *
     * 【前置条件】
     * - 学生账户必须存在
     * - 教室必须存在
     * - 学生未注册该教室
     *
     * 【返回结果】
     * - enrollmentId: 注册记录ID
     * - 注册状态: active
     *
     * 【反向操作】
     * - cleanupFeature8(): 删除注册记录
     */
    public static void testFeature8_StudentEnrollment() throws SQLException {
        printFeature(8, "Student Enrollment | 学生注册课程");

        pstmt = conn.prepareStatement(
            "INSERT INTO enrollments (student_id, classroom_id, status) VALUES (?, ?, 'active')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            enrollmentId = rs.getLong(1);
            printInfo("Enrollment created: enrollment_id=" + enrollmentId);
        }

        printInfo("Student ID: " + studentId);
        printInfo("Classroom ID: " + classroomId);
        printInfo("Status: active");
        printSuccess("Student enrollment completed");
    }

    /**
     * 反向操作: 删除注册记录
     */
    public static void cleanupFeature8() throws SQLException {
        if (enrollmentId > 0) {
            executeUpdate("DELETE FROM enrollments WHERE enrollment_id = " + enrollmentId);
            enrollmentId = 0;
            System.out.println("  ↺ Cleanup: Enrollment deleted");
        }
    }

    // ==================================================================================
    // Feature 9: 查询教室学生 | Query Classroom Students
    // ==================================================================================

    /**
     * FEATURE 9: 查询教室学生
     *
     * 【所需参数】
     * - classroomId: 教室ID (long)
     *
     * 【返回结果】
     * - 学生列表: full_name, student_number, grade, major, enrollment_date
     * - 只返回状态为 'active' 的学生
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature9_QueryClassroomStudents() throws SQLException {
        printFeature(9, "Query Classroom Students | 查询教室学生");

        pstmt = conn.prepareStatement(
            "SELECT s.student_id, u.full_name, s.student_number, s.grade, s.major, e.enrollment_date " +
            "FROM enrollments e " +
            "JOIN students s ON e.student_id = s.student_id " +
            "JOIN users u ON s.user_id = u.user_id " +
            "WHERE e.classroom_id = ? AND e.status = 'active'"
        );
        pstmt.setLong(1, classroomId);

        rs = pstmt.executeQuery();
        printInfo("Students in Classroom ID " + classroomId + ":");
        int count = 0;
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("full_name") +
                " (" + rs.getString("student_number") + ")");
            System.out.println("        Grade: " + rs.getString("grade") +
                " | Major: " + rs.getString("major"));
        }

        printInfo("Total students: " + count);
        printSuccess("Student query completed");
    }

    // ==================================================================================
    // Feature 10: 学生退课 | Student Withdrawal
    // ==================================================================================

    /**
     * FEATURE 10: 学生退课
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     * - classroomId: 教室ID (long)
     *
     * 【操作说明】
     * - 软删除: 将状态改为 'dropped' 而非物理删除
     * - 保留历史记录
     *
     * 【返回结果】
     * - 更新状态为 'dropped'
     *
     * 【反向操作】
     * - reverseFeature10(): 恢复为 'active' 状态
     */
    public static void testFeature10_StudentWithdrawal() throws SQLException {
        printFeature(10, "Student Withdrawal | 学生退课 (Demo - will restore)");

        // 执行退课
        pstmt = conn.prepareStatement(
            "UPDATE enrollments SET status = 'dropped' WHERE student_id = ? AND classroom_id = ?"
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        int updated = pstmt.executeUpdate();

        printInfo("Student ID: " + studentId);
        printInfo("Classroom ID: " + classroomId);
        printInfo("Status changed: active → dropped");
        printInfo("Affected rows: " + updated);

        // 为了后续测试，立即恢复
        reverseFeature10();

        printSuccess("Withdrawal feature demonstrated");
    }

    /**
     * 反向操作: 恢复注册状态
     */
    public static void reverseFeature10() throws SQLException {
        pstmt = conn.prepareStatement(
            "UPDATE enrollments SET status = 'active' WHERE student_id = ? AND classroom_id = ?"
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        pstmt.executeUpdate();
        System.out.println("  ↺ Reversed: Status restored to 'active'");
    }

    // ==================================================================================
    // Feature 21: 学生开始测验 | Student Start Quiz
    // ==================================================================================

    /**
     * FEATURE 21: 学生开始测验
     *
     * 【所需参数】
     * - quizId: 测验ID (long)
     * - studentId: 学生ID (long)
     *
     * 【前置条件】
     * - 学生已注册该测验所属的教室
     * - 学生未开始该测验
     * - 当前时间在测验时间范围内
     *
     * 【返回结果】
     * - studentQuizId: 学生测验会话ID
     * - status: 'in_progress'
     * - start_time: 开始时间
     *
     * 【反向操作】
     * - cleanupFeature21(): 删除学生测验记录
     */
    public static void testFeature21_StudentStartQuiz() throws SQLException {
        printFeature(21, "Student Start Quiz | 学生开始测验");

        pstmt = conn.prepareStatement(
            "INSERT INTO student_quizzes (quiz_id, student_id, start_time, status) " +
            "VALUES (?, ?, NOW(), 'in_progress')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, quizId);
        pstmt.setLong(2, studentId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            studentQuizId = rs.getLong(1);
            printInfo("Student quiz session started");
            printInfo("Session ID: " + studentQuizId);
        }

        printInfo("Student ID: " + studentId);
        printInfo("Quiz ID: " + quizId);
        printInfo("Status: in_progress");
        printSuccess("Quiz started successfully");
    }

    /**
     * 反向操作: 删除学生测验记录
     */
    public static void cleanupFeature21() throws SQLException {
        if (studentQuizId > 0) {
            executeUpdate("DELETE FROM student_answers WHERE student_quiz_id = " + studentQuizId);
            executeUpdate("DELETE FROM student_quizzes WHERE student_quiz_id = " + studentQuizId);
            studentQuizId = 0;
            System.out.println("  ↺ Cleanup: Student quiz session deleted");
        }
    }

    // ==================================================================================
    // Feature 22: 学生提交答案 | Student Submit Answers
    // ==================================================================================

    /**
     * FEATURE 22: 学生提交答案
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     * - 每道题的答案 (selected_option_id 或 answer_text)
     *
     * 【前置条件】
     * - 学生已开始测验
     * - 测验状态为 'in_progress'
     *
     * 【返回结果】
     * - 每道题的答案记录
     *
     * 【反向操作】
     * - cleanupFeature22(): 删除所有答案记录
     */
    public static void testFeature22_StudentSubmitAnswers() throws SQLException {
        printFeature(22, "Student Submit Answers | 学生提交答案");

        // 获取测验中的所有题目
        pstmt = conn.prepareStatement(
            "SELECT qq.question_id, qq.question_order FROM quiz_questions qq " +
            "WHERE qq.quiz_id = ? ORDER BY qq.question_order"
        );
        pstmt.setLong(1, quizId);
        rs = pstmt.executeQuery();

        List<Long> questionIds = new ArrayList<>();
        while (rs.next()) {
            questionIds.add(rs.getLong("question_id"));
        }

        // 为每道题提交答案 (选择第一个选项作为示例)
        pstmt = conn.prepareStatement(
            "INSERT INTO student_answers (student_quiz_id, question_id, selected_option_id) VALUES (?, ?, ?)"
        );

        for (int i = 0; i < questionIds.size(); i++) {
            long qid = questionIds.get(i);

            // 获取该题的第一个选项
            PreparedStatement ps = conn.prepareStatement(
                "SELECT option_id FROM question_options WHERE question_id = ? ORDER BY option_order LIMIT 1"
            );
            ps.setLong(1, qid);
            ResultSet optRs = ps.executeQuery();

            if (optRs.next()) {
                long optionId = optRs.getLong("option_id");

                pstmt.setLong(1, studentQuizId);
                pstmt.setLong(2, qid);
                pstmt.setLong(3, optionId);
                pstmt.executeUpdate();

                printInfo("Question " + (i + 1) + " answered (Q" + qid + " → Option " + optionId + ")");
            }
            optRs.close();
            ps.close();
        }

        printSuccess("All answers submitted");
    }

    /**
     * 反向操作: 删除所有答案记录
     */
    public static void cleanupFeature22() throws SQLException {
        if (studentQuizId > 0) {
            executeUpdate("DELETE FROM student_answers WHERE student_quiz_id = " + studentQuizId);
            System.out.println("  ↺ Cleanup: Student answers deleted");
        }
    }

    // ==================================================================================
    // Feature 23: 完成测验提交 | Complete Quiz Submission
    // ==================================================================================

    /**
     * FEATURE 23: 完成测验提交
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     *
     * 【前置条件】
     * - 学生已开始测验
     * - 已提交所有答案
     *
     * 【返回结果】
     * - status: 'submitted'
     * - submit_time: 提交时间
     *
     * 【反向操作】
     * - reverseFeature23(): 恢复为 'in_progress' 状态
     */
    public static void testFeature23_CompleteQuizSubmission() throws SQLException {
        printFeature(23, "Complete Quiz Submission | 完成测验提交");

        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes SET submit_time = NOW(), status = 'submitted' " +
            "WHERE student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        printInfo("Quiz submission completed");
        printInfo("Session ID: " + studentQuizId);
        printInfo("Status changed: in_progress → submitted");
        printSuccess("Quiz submitted successfully");
    }

    /**
     * 反向操作: 恢复为进行中状态
     */
    public static void reverseFeature23() throws SQLException {
        if (studentQuizId > 0) {
            executeUpdate("UPDATE student_quizzes SET submit_time = NULL, status = 'in_progress' " +
                         "WHERE student_quiz_id = " + studentQuizId);
            System.out.println("  ↺ Reversed: Status restored to 'in_progress'");
        }
    }

    // ==================================================================================
    // Feature 24: 查看可用测验 | View Available Quizzes
    // ==================================================================================

    /**
     * FEATURE 24: 查看可用测验
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     *
     * 【返回结果】
     * - 测验列表: quiz_id, title, course_name, start_time, end_time, status
     * - 只返回学生已注册教室的测验
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature24_ViewAvailableQuizzes() throws SQLException {
        printFeature(24, "View Available Quizzes | 查看可用测验");

        pstmt = conn.prepareStatement(
            "SELECT q.quiz_id, q.title, q.start_time, q.end_time, q.duration_minutes, " +
            "c.course_name, sq.status as quiz_status " +
            "FROM enrollments e " +
            "JOIN classrooms cl ON e.classroom_id = cl.classroom_id " +
            "JOIN courses c ON cl.course_id = c.course_id " +
            "JOIN quizzes q ON cl.classroom_id = q.classroom_id " +
            "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id AND sq.student_id = e.student_id " +
            "WHERE e.student_id = ? AND e.status = 'active'"
        );
        pstmt.setLong(1, studentId);

        rs = pstmt.executeQuery();
        printInfo("Available Quizzes for Student " + studentId + ":");
        int count = 0;
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("title"));
            System.out.println("        Course: " + rs.getString("course_name"));
            System.out.println("        Duration: " + rs.getInt("duration_minutes") + " minutes");
            String status = rs.getString("quiz_status");
            System.out.println("        Status: " + (status != null ? status : "Not Started"));
        }

        printInfo("Total quizzes: " + count);
        printSuccess("Quiz list retrieved");
    }

    // ==================================================================================
    // Feature 28: 学生查看成绩 | Student View Grades
    // ==================================================================================

    /**
     * FEATURE 28: 学生查看成绩
     *
     * 【所需参数】
     * - studentId: 学生ID (long)
     *
     * 【前置条件】
     * - 成绩已评分 (graded = TRUE)
     * - 成绩已发布 (published = TRUE)
     *
     * 【返回结果】
     * - 成绩列表: title, score, total_points, percentage, result (Passed/Failed)
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature28_StudentViewGrades() throws SQLException {
        printFeature(28, "Student View Grades | 学生查看成绩");

        pstmt = conn.prepareStatement(
            "SELECT q.title, sq.score, q.total_points, sq.percentage, " +
            "CASE WHEN sq.score >= q.passing_score THEN 'Passed' ELSE 'Failed' END as result, " +
            "sq.submit_time " +
            "FROM student_quizzes sq " +
            "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
            "WHERE sq.student_id = ? AND sq.published = TRUE AND sq.graded = TRUE"
        );
        pstmt.setLong(1, studentId);

        rs = pstmt.executeQuery();
        printInfo("Published Grades:");
        int count = 0;
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("title"));
            System.out.println("        Score: " + rs.getDouble("score") + "/" + rs.getInt("total_points"));
            System.out.println("        Percentage: " + String.format("%.2f", rs.getDouble("percentage")) + "%");
            System.out.println("        Result: " + rs.getString("result"));
        }

        if (count == 0) {
            printInfo("No published grades found");
        }
        printSuccess("Grades displayed");
    }

    // ==================================================================================
    // Feature 29: 查看答案详情 | View Answer Details
    // ==================================================================================

    /**
     * FEATURE 29: 查看答案详情
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     *
     * 【前置条件】
     * - 测验已评分
     * - 测验设置允许查看答案 (allow_review = TRUE)
     *
     * 【返回结果】
     * - 每道题的详情: question_text, your_answer, is_correct, points_earned, max_points
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature29_ViewAnswerDetails() throws SQLException {
        printFeature(29, "View Answer Details | 查看答案详情");

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
        printInfo("Answer Details for Session " + studentQuizId + ":");
        int num = 1;
        while (rs.next()) {
            String questionText = rs.getString("question_text");
            if (questionText.length() > 50) {
                questionText = questionText.substring(0, 50) + "...";
            }
            System.out.println("     Q" + num + ": " + questionText);
            System.out.println("        Your Answer: " + rs.getString("option_text"));
            System.out.println("        Result: " + (rs.getBoolean("is_correct") ? "✓ Correct" : "✗ Incorrect"));
            System.out.println("        Points: " + rs.getDouble("points_earned") + "/" + rs.getInt("points"));
            num++;
        }

        printSuccess("Answer details retrieved");
    }

    // ==================================================================================
    // 辅助方法 - 准备测试数据
    // ==================================================================================

    /**
     * 准备教师和课程数据 (学生功能测试的前置条件)
     */
    private static void prepareTeacherAndCourseData() throws SQLException {
        printSectionHeader("Preparing Teacher and Course Data (Prerequisites)");

        // 创建教师
        pstmt = conn.prepareStatement(
            "INSERT INTO users (username, password_hash, email, full_name, role) " +
            "VALUES ('john_teacher', SHA2('teachpass', 256), 'john@university.edu', 'John Smith', 'teacher')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) teacherUserId = rs.getLong(1);

        pstmt = conn.prepareStatement(
            "INSERT INTO teachers (user_id, department, hire_date, phone, office) " +
            "VALUES (?, 'Computer Science', CURRENT_DATE, '+1-555-0100', 'CS Building 301')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, teacherUserId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) teacherId = rs.getLong(1);

        // 创建课程
        pstmt = conn.prepareStatement(
            "INSERT INTO courses (course_code, course_name, description, credit_hours, created_by) " +
            "VALUES ('CS101', 'Data Structures and Algorithms', 'Introduction to data structures', 4, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, teacherId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) courseId = rs.getLong(1);

        // 创建教室
        pstmt = conn.prepareStatement(
            "INSERT INTO classrooms (course_id, teacher_id, class_name, semester, year, max_students) " +
            "VALUES (?, ?, 'Section 01', 'Fall 2025', 2025, 50)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, courseId);
        pstmt.setLong(2, teacherId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) classroomId = rs.getLong(1);

        printInfo("Teacher ID: " + teacherId + " created");
        printInfo("Course ID: " + courseId + " created");
        printInfo("Classroom ID: " + classroomId + " created");
        printSuccess("Prerequisites prepared");
    }

    /**
     * 准备测验数据 (考试功能测试的前置条件)
     */
    private static void prepareQuizData() throws SQLException {
        printSectionHeader("Preparing Quiz Data (Prerequisites for Exam)");

        // 创建科目
        pstmt = conn.prepareStatement(
            "INSERT INTO subjects (subject_name, description, level) VALUES ('Data Structures', 'DS Topics', 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) subjectId = rs.getLong(1);

        // 创建题目1
        createQuestionWithOptions("What is the time complexity of binary search?",
            new String[]{"O(n)", "O(log n)", "O(n²)", "O(1)"},
            new boolean[]{false, true, false, false});
        questionId1 = getLastQuestionId();

        // 创建题目2
        createQuestionWithOptions("Which data structure uses LIFO principle?",
            new String[]{"Queue", "Stack", "Array", "Tree"},
            new boolean[]{false, true, false, false});
        questionId2 = getLastQuestionId();

        // 创建题目3
        createQuestionWithOptions("What is a balanced binary tree?",
            new String[]{"AVL Tree", "Linked List", "Hash Table", "Graph"},
            new boolean[]{true, false, false, false});
        questionId3 = getLastQuestionId();

        // 创建测验
        pstmt = conn.prepareStatement(
            "INSERT INTO quizzes (classroom_id, title, description, created_by, " +
            "start_time, end_time, duration_minutes, total_points, passing_score) " +
            "VALUES (?, 'Midterm Exam', 'Data Structures Midterm', ?, " +
            "'2025-01-01 09:00:00', '2025-12-31 23:59:59', 120, 100, 60)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, classroomId);
        pstmt.setLong(2, teacherId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) quizId = rs.getLong(1);

        // 添加题目到测验
        long[] questions = {questionId1, questionId2, questionId3};
        int[] points = {30, 30, 40};
        for (int i = 0; i < questions.length; i++) {
            pstmt = conn.prepareStatement(
                "INSERT INTO quiz_questions (quiz_id, question_id, question_order, points) VALUES (?, ?, ?, ?)"
            );
            pstmt.setLong(1, quizId);
            pstmt.setLong(2, questions[i]);
            pstmt.setInt(3, i + 1);
            pstmt.setInt(4, points[i]);
            pstmt.executeUpdate();
        }

        // 创建测验设置
        pstmt = conn.prepareStatement(
            "INSERT INTO quiz_settings (quiz_id, shuffle_questions, shuffle_options, " +
            "show_results_immediately, allow_review) VALUES (?, TRUE, TRUE, FALSE, TRUE)"
        );
        pstmt.setLong(1, quizId);
        pstmt.executeUpdate();

        printInfo("Subject ID: " + subjectId + " created");
        printInfo("Questions created: Q" + questionId1 + ", Q" + questionId2 + ", Q" + questionId3);
        printInfo("Quiz ID: " + quizId + " created with 3 questions");
        printSuccess("Quiz data prepared");
    }

    /**
     * 执行自动评分 (查看成绩功能的前置条件)
     */
    private static void performAutoGrading() throws SQLException {
        printSectionHeader("Performing Auto Grading (Prerequisites for Grade View)");

        // 自动评分
        pstmt = conn.prepareStatement(
            "UPDATE student_answers sa " +
            "JOIN question_options qo ON sa.selected_option_id = qo.option_id " +
            "JOIN quiz_questions qq ON sa.question_id = qq.question_id AND qq.quiz_id = " +
            "(SELECT quiz_id FROM student_quizzes WHERE student_quiz_id = sa.student_quiz_id) " +
            "SET sa.is_correct = qo.is_correct, " +
            "    sa.points_earned = CASE WHEN qo.is_correct THEN qq.points ELSE 0 END " +
            "WHERE sa.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        // 计算总分
        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes sq " +
            "SET score = (SELECT COALESCE(SUM(sa.points_earned), 0) " +
            "             FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
            "    percentage = (SELECT COALESCE(SUM(sa.points_earned), 0) * 100.0 / " +
            "                  (SELECT total_points FROM quizzes WHERE quiz_id = sq.quiz_id) " +
            "                  FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
            "    graded = TRUE, published = TRUE, status = 'completed' " +
            "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        printInfo("Auto grading completed for session " + studentQuizId);
        printInfo("Grades published");
        printSuccess("Grading completed");
    }

    /**
     * 创建题目和选项的辅助方法
     */
    private static void createQuestionWithOptions(String questionText, String[] options, boolean[] correct)
            throws SQLException {
        pstmt = conn.prepareStatement(
            "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
            "VALUES (?, ?, 'multiple_choice', 2, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, subjectId);
        pstmt.setString(2, questionText);
        pstmt.setLong(3, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        long qid = 0;
        if (rs.next()) qid = rs.getLong(1);

        for (int i = 0; i < options.length; i++) {
            pstmt = conn.prepareStatement(
                "INSERT INTO question_options (question_id, option_text, is_correct, option_order) " +
                "VALUES (?, ?, ?, ?)"
            );
            pstmt.setLong(1, qid);
            pstmt.setString(2, options[i]);
            pstmt.setBoolean(3, correct[i]);
            pstmt.setInt(4, i + 1);
            pstmt.executeUpdate();
        }
    }

    private static long getLastQuestionId() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(question_id) FROM questions");
        long id = 0;
        if (rs.next()) id = rs.getLong(1);
        rs.close();
        stmt.close();
        return id;
    }
}

