package com.zekai.comment;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ========================================
 * TEACHER FEATURE TESTS - 教师端功能JUnit测试
 * ========================================
 *
 * 包含18个教师端功能的独立测试:
 * - Feature 2: 创建教师账户
 * - Feature 3: 教师登录认证
 * - Feature 6: 创建课程
 * - Feature 7: 创建教室
 * - Feature 11: 创建科目
 * - Feature 12: 创建单个题目
 * - Feature 13: 添加题目选项
 * - Feature 14: 批量创建题目
 * - Feature 15: 查询题目统计
 * - Feature 16: 创建测验
 * - Feature 17: 随机选题
 * - Feature 18: 添加题目到测验
 * - Feature 19: 配置测验设置
 * - Feature 20: 查看测验详情
 * - Feature 25: 自动评分客观题
 * - Feature 26: 计算总分
 * - Feature 27: 发布成绩
 * - Feature 30: 教师查看班级成绩
 * - Feature 31: 题目难度分析
 * - Feature 32: 生成成绩报告
 * - Feature 35: 查看教师的测验
 *
 * @author Exam System Team
 * @version 2.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeacherFeatureTests extends ExamSystemTestBase {

    // ==================================================================================
    // Feature 2: 创建教师账户 | Create Teacher Account
    // ==================================================================================

    @Test
    @Order(2)
    @DisplayName("Feature 2: 创建教师账户 | Create Teacher Account")
    void testFeature2_CreateTeacherAccount() throws SQLException {
        System.out.println("┌─ FEATURE 2: Create Teacher Account | 创建教师账户");

        String username = "john_teacher";
        String password = "teachpass";
        String email = "john@university.edu";
        String fullName = "John Smith";
        String department = "Computer Science";

        pstmt = conn.prepareStatement(
            "INSERT INTO users (username, password_hash, email, full_name, role) " +
            "VALUES (?, SHA2(?, 256), ?, ?, 'teacher')",
            Statement.RETURN_GENERATED_KEYS
        );

        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, email);
        pstmt.setString(4, fullName);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回生成的用户ID");
        teacherUserId = rs.getLong(1);

        pstmt = conn.prepareStatement(
            "INSERT INTO teachers (user_id, department, hire_date, phone, office) " +
            "VALUES (?, ?, CURRENT_DATE, '+1-555-0100', 'CS Building 301')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, teacherUserId);
        pstmt.setString(2, department);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回教师ID");
        teacherId = rs.getLong(1);
        assertTrue(teacherId > 0, "教师ID应该大于0");

        System.out.println("  → Teacher created: teacher_id=" + teacherId);
        System.out.println("  → Department: " + department);
        System.out.println("  ✓ Teacher account created successfully\n");
    }

    // ==================================================================================
    // Feature 3: 教师登录认证 | Teacher Login Authentication
    // ==================================================================================

    @Test
    @Order(3)
    @DisplayName("Feature 3: 教师登录认证 | Teacher Login Authentication")
    void testFeature3_TeacherLogin() throws SQLException {
        System.out.println("┌─ FEATURE 3: Teacher Login Authentication | 教师登录认证");

        pstmt = conn.prepareStatement(
            "SELECT u.user_id, u.username, u.role, u.full_name, t.teacher_id, t.department " +
            "FROM users u LEFT JOIN teachers t ON u.user_id = t.user_id " +
            "WHERE u.username = ? AND u.password_hash = SHA2(?, 256) AND u.is_active = TRUE"
        );
        pstmt.setString(1, "john_teacher");
        pstmt.setString(2, "teachpass");

        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "登录应该成功");
        assertEquals("teacher", rs.getString("role"), "角色应该是teacher");

        System.out.println("  → Authentication successful!");
        System.out.println("  → Role: " + rs.getString("role"));
        System.out.println("  → Department: " + rs.getString("department"));
        System.out.println("  ✓ Login authentication completed\n");
    }

    // ==================================================================================
    // Feature 6: 创建课程 | Create Course
    // ==================================================================================

    @Test
    @Order(6)
    @DisplayName("Feature 6: 创建课程 | Create Course")
    void testFeature6_CreateCourse() throws SQLException {
        System.out.println("┌─ FEATURE 6: Create Course | 创建课程");

        pstmt = conn.prepareStatement(
            "INSERT INTO courses (course_code, course_name, description, credit_hours, created_by) " +
            "VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, "CS101");
        pstmt.setString(2, "Data Structures and Algorithms");
        pstmt.setString(3, "Introduction to fundamental data structures");
        pstmt.setInt(4, 4);
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回课程ID");
        courseId = rs.getLong(1);
        assertTrue(courseId > 0, "课程ID应该大于0");

        System.out.println("  → Course created: course_id=" + courseId);
        System.out.println("  → Course Code: CS101");
        System.out.println("  ✓ Course created successfully\n");
    }

    // ==================================================================================
    // Feature 7: 创建教室 | Create Classroom
    // ==================================================================================

    @Test
    @Order(7)
    @DisplayName("Feature 7: 创建教室 | Create Classroom")
    void testFeature7_CreateClassroom() throws SQLException {
        System.out.println("┌─ FEATURE 7: Create Classroom | 创建教室");

        pstmt = conn.prepareStatement(
            "INSERT INTO classrooms (course_id, teacher_id, class_name, semester, year, max_students) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, courseId);
        pstmt.setLong(2, teacherId);
        pstmt.setString(3, "Section 01");
        pstmt.setString(4, "Fall 2025");
        pstmt.setInt(5, 2025);
        pstmt.setInt(6, 50);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回教室ID");
        classroomId = rs.getLong(1);
        assertTrue(classroomId > 0, "教室ID应该大于0");

        System.out.println("  → Classroom created: classroom_id=" + classroomId);
        System.out.println("  → Semester: Fall 2025");
        System.out.println("  ✓ Classroom created successfully\n");
    }

    // ==================================================================================
    // Feature 11: 创建科目 | Create Subject
    // ==================================================================================

    @Test
    @Order(11)
    @DisplayName("Feature 11: 创建科目 | Create Subject")
    void testFeature11_CreateSubject() throws SQLException {
        System.out.println("┌─ FEATURE 11: Create Subject | 创建科目");

        pstmt = conn.prepareStatement(
            "INSERT INTO subjects (subject_name, description, level) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, "Data Structures");
        pstmt.setString(2, "Topics related to data structures and algorithms");
        pstmt.setInt(3, 1);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回科目ID");
        subjectId = rs.getLong(1);
        assertTrue(subjectId > 0, "科目ID应该大于0");

        System.out.println("  → Subject created: subject_id=" + subjectId);
        System.out.println("  ✓ Subject created successfully\n");
    }

    // ==================================================================================
    // Feature 12: 创建单个题目 | Create Single Question
    // ==================================================================================

    @Test
    @Order(12)
    @DisplayName("Feature 12: 创建单个题目 | Create Single Question")
    void testFeature12_CreateSingleQuestion() throws SQLException {
        System.out.println("┌─ FEATURE 12: Create Single Question | 创建单个题目");

        pstmt = conn.prepareStatement(
            "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
            "VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, subjectId);
        pstmt.setString(2, "What is the time complexity of binary search?");
        pstmt.setString(3, "multiple_choice");
        pstmt.setInt(4, 2);
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回题目ID");
        questionId1 = rs.getLong(1);
        assertTrue(questionId1 > 0, "题目ID应该大于0");

        System.out.println("  → Question created: question_id=" + questionId1);
        System.out.println("  → Type: multiple_choice");
        System.out.println("  ✓ Question created successfully\n");
    }

    // ==================================================================================
    // Feature 13: 添加题目选项 | Add Question Options
    // ==================================================================================

    @Test
    @Order(13)
    @DisplayName("Feature 13: 添加题目选项 | Add Question Options")
    void testFeature13_AddQuestionOptions() throws SQLException {
        System.out.println("┌─ FEATURE 13: Add Question Options | 添加题目选项");

        String[] options = {"O(n)", "O(log n)", "O(n²)", "O(1)"};
        boolean[] correct = {false, true, false, false};

        pstmt = conn.prepareStatement(
            "INSERT INTO question_options (question_id, option_text, is_correct, option_order) VALUES (?, ?, ?, ?)"
        );

        for (int i = 0; i < options.length; i++) {
            pstmt.setLong(1, questionId1);
            pstmt.setString(2, options[i]);
            pstmt.setBoolean(3, correct[i]);
            pstmt.setInt(4, i + 1);
            pstmt.executeUpdate();
            System.out.println("  → Option " + (i + 1) + ": " + options[i] + (correct[i] ? " (CORRECT)" : ""));
        }

        // 验证选项数量
        pstmt = conn.prepareStatement("SELECT COUNT(*) as cnt FROM question_options WHERE question_id = ?");
        pstmt.setLong(1, questionId1);
        rs = pstmt.executeQuery();
        rs.next();
        assertEquals(4, rs.getInt("cnt"), "应该有4个选项");

        System.out.println("  ✓ All options added successfully\n");
    }

    // ==================================================================================
    // Feature 14: 批量创建题目 | Create Multiple Questions
    // ==================================================================================

    @Test
    @Order(14)
    @DisplayName("Feature 14: 批量创建题目 | Create Multiple Questions")
    void testFeature14_CreateMultipleQuestions() throws SQLException {
        System.out.println("┌─ FEATURE 14: Create Multiple Questions | 批量创建题目");

        // 创建题目2
        createQuestionWithOptions("Which data structure uses LIFO principle?",
            new String[]{"Queue", "Stack", "Array", "Tree"},
            new boolean[]{false, true, false, false});
        questionId2 = getLastQuestionId();
        assertTrue(questionId2 > questionId1, "题目2的ID应该大于题目1");
        System.out.println("  → Question 2 created: question_id=" + questionId2);

        // 创建题目3
        createQuestionWithOptions("What is a balanced binary tree?",
            new String[]{"AVL Tree", "Linked List", "Hash Table", "Graph"},
            new boolean[]{true, false, false, false});
        questionId3 = getLastQuestionId();
        assertTrue(questionId3 > questionId2, "题目3的ID应该大于题目2");
        System.out.println("  → Question 3 created: question_id=" + questionId3);

        // 验证题目数量
        pstmt = conn.prepareStatement("SELECT COUNT(*) as cnt FROM questions WHERE subject_id = ?");
        pstmt.setLong(1, subjectId);
        rs = pstmt.executeQuery();
        rs.next();
        assertEquals(3, rs.getInt("cnt"), "应该有3道题目");

        System.out.println("  ✓ Multiple questions created successfully\n");
    }

    // ==================================================================================
    // Feature 15: 查询题目统计 | Query Question Statistics
    // ==================================================================================

    @Test
    @Order(15)
    @DisplayName("Feature 15: 查询题目统计 | Query Question Statistics")
    void testFeature15_QueryQuestionStatistics() throws SQLException {
        System.out.println("┌─ FEATURE 15: Query Question Statistics | 查询题目统计");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.question_text, q.question_type, q.difficulty_level, " +
            "q.times_used, s.subject_name FROM questions q " +
            "JOIN subjects s ON q.subject_id = s.subject_id WHERE q.is_deleted = FALSE"
        );

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Question Bank Statistics:");
        while (rs.next()) {
            count++;
            String text = rs.getString("question_text");
            if (text.length() > 40) text = text.substring(0, 40) + "...";
            System.out.println("     " + count + ". Q" + rs.getLong("question_id") + " - " + text);
            System.out.println("        Type: " + rs.getString("question_type") +
                " | Difficulty: " + rs.getInt("difficulty_level") + "/5");
        }

        assertTrue(count >= 3, "应该至少有3道题目");
        System.out.println("  → Total Questions: " + count);
        System.out.println("  ✓ Statistics query completed\n");
    }

    // ==================================================================================
    // Feature 16: 创建测验 | Create Quiz
    // ==================================================================================

    @Test
    @Order(16)
    @DisplayName("Feature 16: 创建测验 | Create Quiz")
    void testFeature16_CreateQuiz() throws SQLException {
        System.out.println("┌─ FEATURE 16: Create Quiz | 创建测验");

        pstmt = conn.prepareStatement(
            "INSERT INTO quizzes (classroom_id, title, description, created_by, " +
            "start_time, end_time, duration_minutes, total_points, passing_score) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, classroomId);
        pstmt.setString(2, "Midterm Exam - Data Structures");
        pstmt.setString(3, "Comprehensive exam");
        pstmt.setLong(4, teacherId);
        pstmt.setString(5, "2025-01-01 09:00:00");
        pstmt.setString(6, "2025-12-31 23:59:59");
        pstmt.setInt(7, 120);
        pstmt.setInt(8, 100);
        pstmt.setInt(9, 60);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        assertTrue(rs.next(), "应该返回测验ID");
        quizId = rs.getLong(1);
        assertTrue(quizId > 0, "测验ID应该大于0");

        System.out.println("  → Quiz created: quiz_id=" + quizId);
        System.out.println("  → Duration: 120 minutes");
        System.out.println("  ✓ Quiz created successfully\n");
    }

    // ==================================================================================
    // Feature 17: 随机选题 | Random Question Selection
    // ==================================================================================

    @Test
    @Order(17)
    @DisplayName("Feature 17: 随机选题 | Random Question Selection")
    void testFeature17_RandomQuestionSelection() throws SQLException {
        System.out.println("┌─ FEATURE 17: Random Question Selection | 随机选题");

        pstmt = conn.prepareStatement(
            "SELECT question_id, question_text, difficulty_level FROM questions " +
            "WHERE subject_id = ? AND question_type = 'multiple_choice' AND is_deleted = FALSE " +
            "ORDER BY RAND() LIMIT 3"
        );
        pstmt.setLong(1, subjectId);

        rs = pstmt.executeQuery();
        List<Long> selectedIds = new ArrayList<>();
        System.out.println("  → Randomly selected questions:");
        while (rs.next()) {
            long qid = rs.getLong("question_id");
            selectedIds.add(qid);
            String text = rs.getString("question_text");
            if (text.length() > 40) text = text.substring(0, 40) + "...";
            System.out.println("     • Q" + qid + ": " + text);
        }

        assertEquals(3, selectedIds.size(), "应该选择3道题目");
        System.out.println("  ✓ Random selection completed\n");
    }

    // ==================================================================================
    // Feature 18: 添加题目到测验 | Add Questions to Quiz
    // ==================================================================================

    @Test
    @Order(18)
    @DisplayName("Feature 18: 添加题目到测验 | Add Questions to Quiz")
    void testFeature18_AddQuestionsToQuiz() throws SQLException {
        System.out.println("┌─ FEATURE 18: Add Questions to Quiz | 添加题目到测验");

        long[] questions = {questionId1, questionId2, questionId3};
        int[] points = {30, 30, 40};

        pstmt = conn.prepareStatement(
            "INSERT INTO quiz_questions (quiz_id, question_id, question_order, points) VALUES (?, ?, ?, ?)"
        );

        for (int i = 0; i < questions.length; i++) {
            pstmt.setLong(1, quizId);
            pstmt.setLong(2, questions[i]);
            pstmt.setInt(3, i + 1);
            pstmt.setInt(4, points[i]);
            pstmt.executeUpdate();
            System.out.println("  → Question " + (i + 1) + " added: Q" + questions[i] + " (Points: " + points[i] + ")");
        }

        // 验证
        pstmt = conn.prepareStatement("SELECT COUNT(*) as cnt FROM quiz_questions WHERE quiz_id = ?");
        pstmt.setLong(1, quizId);
        rs = pstmt.executeQuery();
        rs.next();
        assertEquals(3, rs.getInt("cnt"), "应该有3道测验题目");

        System.out.println("  ✓ All questions added to quiz\n");
    }

    // ==================================================================================
    // Feature 19: 配置测验设置 | Configure Quiz Settings
    // ==================================================================================

    @Test
    @Order(19)
    @DisplayName("Feature 19: 配置测验设置 | Configure Quiz Settings")
    void testFeature19_ConfigureQuizSettings() throws SQLException {
        System.out.println("┌─ FEATURE 19: Configure Quiz Settings | 配置测验设置");

        pstmt = conn.prepareStatement(
            "INSERT INTO quiz_settings (quiz_id, shuffle_questions, shuffle_options, " +
            "show_results_immediately, allow_review) VALUES (?, ?, ?, ?, ?)"
        );
        pstmt.setLong(1, quizId);
        pstmt.setBoolean(2, true);
        pstmt.setBoolean(3, true);
        pstmt.setBoolean(4, false);
        pstmt.setBoolean(5, true);
        pstmt.executeUpdate();

        // 验证
        pstmt = conn.prepareStatement("SELECT * FROM quiz_settings WHERE quiz_id = ?");
        pstmt.setLong(1, quizId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该有设置记录");
        assertTrue(rs.getBoolean("shuffle_questions"), "打乱题目应该是true");
        assertTrue(rs.getBoolean("allow_review"), "允许复查应该是true");

        System.out.println("  → Shuffle Questions: YES");
        System.out.println("  → Allow Review: YES");
        System.out.println("  ✓ Quiz settings saved\n");
    }

    // ==================================================================================
    // Feature 20: 查看测验详情 | View Quiz Details
    // ==================================================================================

    @Test
    @Order(20)
    @DisplayName("Feature 20: 查看测验详情 | View Quiz Details")
    void testFeature20_ViewQuizDetails() throws SQLException {
        System.out.println("┌─ FEATURE 20: View Quiz Details | 查看测验详情");

        pstmt = conn.prepareStatement(
            "SELECT q.quiz_id, q.title, q.duration_minutes, q.total_points, q.passing_score, " +
            "COUNT(qq.question_id) as question_count FROM quizzes q " +
            "LEFT JOIN quiz_questions qq ON q.quiz_id = qq.quiz_id " +
            "WHERE q.quiz_id = ? GROUP BY q.quiz_id"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该有测验记录");

        System.out.println("  → Quiz Information:");
        System.out.println("     • ID: " + rs.getLong("quiz_id"));
        System.out.println("     • Title: " + rs.getString("title"));
        System.out.println("     • Duration: " + rs.getInt("duration_minutes") + " minutes");
        System.out.println("     • Total Points: " + rs.getInt("total_points"));
        System.out.println("     • Passing Score: " + rs.getInt("passing_score"));
        System.out.println("     • Questions: " + rs.getInt("question_count"));

        assertEquals(3, rs.getInt("question_count"), "应该有3道题目");
        System.out.println("  ✓ Quiz details retrieved\n");
    }

    // ==================================================================================
    // Feature 25-27, 30-32, 35: 评分和统计功能
    // ==================================================================================

    @Test
    @Order(25)
    @DisplayName("Feature 25: 自动评分客观题 | Auto Grade Objective Questions")
    void testFeature25_AutoGradeObjectiveQuestions() throws SQLException {
        System.out.println("┌─ FEATURE 25: Auto Grade Objective Questions | 自动评分客观题");

        // 准备学生答题数据
        prepareStudentQuizData();

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
        int graded = pstmt.executeUpdate();

        assertTrue(graded > 0, "应该有答案被评分");
        System.out.println("  → Questions graded: " + graded);
        System.out.println("  ✓ Auto-grading completed\n");
    }

    @Test
    @Order(26)
    @DisplayName("Feature 26: 计算总分 | Calculate Total Score")
    void testFeature26_CalculateTotalScore() throws SQLException {
        System.out.println("┌─ FEATURE 26: Calculate Total Score | 计算总分");

        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes sq " +
            "SET sq.score = (SELECT COALESCE(SUM(points_earned), 0) FROM student_answers WHERE student_quiz_id = ?), " +
            "    sq.percentage = (SELECT COALESCE(SUM(points_earned), 0) * 100.0 / 100 FROM student_answers WHERE student_quiz_id = ?), " +
            "    sq.graded = TRUE, sq.status = 'completed' " +
            "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.setLong(2, studentQuizId);
        pstmt.setLong(3, studentQuizId);
        pstmt.executeUpdate();

        // 验证
        pstmt = conn.prepareStatement("SELECT score, graded FROM student_quizzes WHERE student_quiz_id = ?");
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该有记录");
        assertTrue(rs.getBoolean("graded"), "应该已评分");

        System.out.println("  → Score: " + rs.getDouble("score"));
        System.out.println("  ✓ Score calculation completed\n");
    }

    @Test
    @Order(27)
    @DisplayName("Feature 27: 发布成绩 | Publish Grades")
    void testFeature27_PublishGrades() throws SQLException {
        System.out.println("┌─ FEATURE 27: Publish Grades | 发布成绩");

        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes SET published = TRUE WHERE student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        // 验证
        pstmt = conn.prepareStatement("SELECT published FROM student_quizzes WHERE student_quiz_id = ?");
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该有记录");
        assertTrue(rs.getBoolean("published"), "应该已发布");

        System.out.println("  → Grades published");
        System.out.println("  ✓ Publication completed\n");
    }

    @Test
    @Order(30)
    @DisplayName("Feature 30: 教师查看班级成绩 | Teacher View Class Grades")
    void testFeature30_TeacherViewClassGrades() throws SQLException {
        System.out.println("┌─ FEATURE 30: Teacher View Class Grades | 教师查看班级成绩");

        pstmt = conn.prepareStatement(
            "SELECT u.full_name, s.student_number, sq.score, q.total_points, sq.percentage " +
            "FROM student_quizzes sq " +
            "JOIN students s ON sq.student_id = s.student_id " +
            "JOIN users u ON s.user_id = u.user_id " +
            "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
            "WHERE sq.quiz_id = ? AND sq.graded = TRUE ORDER BY sq.score DESC"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Class Grades:");
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("full_name") +
                " - Score: " + rs.getDouble("score") + "/" + rs.getInt("total_points"));
        }

        assertTrue(count > 0, "应该有成绩记录");
        System.out.println("  ✓ Class grades retrieved\n");
    }

    @Test
    @Order(31)
    @DisplayName("Feature 31: 题目难度分析 | Question Difficulty Analysis")
    void testFeature31_QuestionDifficultyAnalysis() throws SQLException {
        System.out.println("┌─ FEATURE 31: Question Difficulty Analysis | 题目难度分析");

        // 先更新题目统计
        executeUpdate("UPDATE questions q SET " +
            "times_used = (SELECT COUNT(DISTINCT sa.student_quiz_id) FROM student_answers sa WHERE sa.question_id = q.question_id), " +
            "total_attempts = (SELECT COUNT(*) FROM student_answers sa WHERE sa.question_id = q.question_id), " +
            "correct_count = (SELECT COUNT(*) FROM student_answers sa WHERE sa.question_id = q.question_id AND sa.is_correct = TRUE) " +
            "WHERE q.question_id IN (SELECT DISTINCT question_id FROM student_answers)");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.difficulty_level, q.total_attempts, q.correct_count " +
            "FROM questions q WHERE q.times_used > 0"
        );

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Question Analysis:");
        while (rs.next()) {
            count++;
            int attempts = rs.getInt("total_attempts");
            int correct = rs.getInt("correct_count");
            double rate = attempts > 0 ? (correct * 100.0 / attempts) : 0;
            System.out.println("     • Q" + rs.getLong("question_id") +
                ": Difficulty " + rs.getInt("difficulty_level") + "/5, Correct Rate: " + String.format("%.1f", rate) + "%");
        }

        assertTrue(count > 0, "应该有题目统计");
        System.out.println("  ✓ Difficulty analysis completed\n");
    }

    @Test
    @Order(32)
    @DisplayName("Feature 32: 生成成绩报告 | Generate Grade Report")
    void testFeature32_GenerateGradeReport() throws SQLException {
        System.out.println("┌─ FEATURE 32: Generate Grade Report | 生成成绩报告");

        pstmt = conn.prepareStatement(
            "SELECT COUNT(DISTINCT sq.student_id) as total_students, " +
            "ROUND(AVG(sq.score), 2) as avg_score, MIN(sq.score) as min_score, MAX(sq.score) as max_score, " +
            "SUM(CASE WHEN sq.score >= q.passing_score THEN 1 ELSE 0 END) as passed_count " +
            "FROM student_quizzes sq JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
            "WHERE sq.quiz_id = ? AND sq.graded = TRUE"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        assertTrue(rs.next(), "应该有统计数据");

        System.out.println("  → Grade Report Summary:");
        System.out.println("     • Total Students: " + rs.getInt("total_students"));
        System.out.println("     • Average Score: " + rs.getDouble("avg_score"));
        System.out.println("     • Min Score: " + rs.getDouble("min_score"));
        System.out.println("     • Max Score: " + rs.getDouble("max_score"));
        System.out.println("     • Passed: " + rs.getInt("passed_count"));
        System.out.println("  ✓ Grade report generated\n");
    }

    @Test
    @Order(35)
    @DisplayName("Feature 35: 查看教师的测验 | View Teacher's Quizzes")
    void testFeature35_ViewTeachersQuizzes() throws SQLException {
        System.out.println("┌─ FEATURE 35: View Teacher's Quizzes | 查看教师的测验");

        pstmt = conn.prepareStatement(
            "SELECT q.quiz_id, q.title, c.course_name, COUNT(DISTINCT sq.student_quiz_id) as submissions " +
            "FROM quizzes q " +
            "JOIN classrooms cl ON q.classroom_id = cl.classroom_id " +
            "JOIN courses c ON cl.course_id = c.course_id " +
            "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id " +
            "WHERE q.created_by = ? GROUP BY q.quiz_id"
        );
        pstmt.setLong(1, teacherId);

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Quizzes Created by Teacher:");
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". " + rs.getString("title"));
            System.out.println("        Course: " + rs.getString("course_name"));
            System.out.println("        Submissions: " + rs.getInt("submissions"));
        }

        assertTrue(count > 0, "应该有测验");
        System.out.println("  ✓ Teacher's quizzes listed\n");
    }

    // ==================================================================================
    // 辅助方法
    // ==================================================================================

    private void prepareStudentQuizData() throws SQLException {
        // 创建学生
        createStudentAccount();
        enrollStudent();

        // 创建学生测验会话
        pstmt = conn.prepareStatement(
            "INSERT INTO student_quizzes (quiz_id, student_id, start_time, submit_time, status) " +
            "VALUES (?, ?, NOW(), NOW(), 'submitted')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, quizId);
        pstmt.setLong(2, studentId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) studentQuizId = rs.getLong(1);

        // 提交答案
        long[] questions = {questionId1, questionId2, questionId3};
        for (long qid : questions) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT option_id FROM question_options WHERE question_id = ? ORDER BY option_order LIMIT 1"
            );
            ps.setLong(1, qid);
            ResultSet optRs = ps.executeQuery();
            if (optRs.next()) {
                pstmt = conn.prepareStatement(
                    "INSERT INTO student_answers (student_quiz_id, question_id, selected_option_id) VALUES (?, ?, ?)"
                );
                pstmt.setLong(1, studentQuizId);
                pstmt.setLong(2, qid);
                pstmt.setLong(3, optRs.getLong("option_id"));
                pstmt.executeUpdate();
            }
            optRs.close();
            ps.close();
        }
    }
}

