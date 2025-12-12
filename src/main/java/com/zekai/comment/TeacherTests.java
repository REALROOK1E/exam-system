package com.zekai.comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ========================================
 * TEACHER TESTS - 教师端功能测试
 * ========================================
 *
 * 包含以下教师端功能 (18个):
 * - Feature 2: 创建教师账户
 * - Feature 3: 用户登录认证 (教师)
 * - Feature 5: 教师创建教室
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
 * 使用方法:
 * - 运行 main() 执行所有教师端功能测试
 * - 直接调用单个 testFeatureXX() 方法执行单独测试
 *
 * @author Exam System Team
 * @version 2.0
 */
public class TeacherTests extends TestBase {

    public static void main(String[] args) {
        runAllTeacherTests();
    }

    /**
     * 运行所有教师端功能测试
     */
    public static void runAllTeacherTests() {
        printHeader("TEACHER TESTS - 教师端功能测试");

        try {
            resetTestData();
            initConnection();
            setupDatabase();

            // 教师账户和认证
            testFeature2_CreateTeacherAccount();
            testFeature3_TeacherLogin();

            // 课程和教室管理
            testFeature6_CreateCourse();
            testFeature7_CreateClassroom();

            // 准备学生数据用于后续测试
            prepareStudentData();

            // 题库管理
            testFeature11_CreateSubject();
            testFeature12_CreateSingleQuestion();
            testFeature13_AddQuestionOptions();
            testFeature14_CreateMultipleQuestions();
            testFeature15_QueryQuestionStatistics();

            // 测验管理
            testFeature16_CreateQuiz();
            testFeature17_RandomQuestionSelection();
            testFeature18_AddQuestionsToQuiz();
            testFeature19_ConfigureQuizSettings();
            testFeature20_ViewQuizDetails();

            // 准备学生答题数据
            prepareStudentQuizData();

            // 评分管理
            testFeature25_AutoGradeObjectiveQuestions();
            testFeature26_CalculateTotalScore();
            testFeature27_PublishGrades();

            // 成绩查询和统计
            testFeature30_TeacherViewClassGrades();
            testFeature31_QuestionDifficultyAnalysis();
            testFeature32_GenerateGradeReport();
            testFeature35_ViewTeachersQuizzes();

            commit();
            printHeader("✓ ALL TEACHER TESTS COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            rollback();
        } finally {
            closeResources();
        }
    }

    // ==================================================================================
    // Feature 2: 创建教师账户 | Create Teacher Account
    // ==================================================================================

    /**
     * FEATURE 2: 创建教师账户
     *
     * 【所需参数】
     * - username: 用户名 (String, 唯一)
     * - password: 密码 (String)
     * - email: 邮箱 (String)
     * - fullName: 全名 (String)
     * - department: 院系 (String)
     * - phone: 电话 (String)
     * - office: 办公室 (String)
     *
     * 【返回结果】
     * - teacherUserId: 用户表ID
     * - teacherId: 教师表ID
     *
     * 【反向操作】
     * - cleanupFeature2(): 删除创建的教师账户
     */
    public static void testFeature2_CreateTeacherAccount() throws SQLException {
        printFeature(2, "Create Teacher Account | 创建教师账户");

        String username = "john_teacher";
        String password = "teachpass";
        String email = "john@university.edu";
        String fullName = "John Smith";
        String department = "Computer Science";
        String phone = "+1-555-0100";
        String office = "CS Building 301";

        // 创建用户记录
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
        if (rs.next()) {
            teacherUserId = rs.getLong(1);
            printInfo("User created: user_id=" + teacherUserId);
        }

        // 创建教师记录
        pstmt = conn.prepareStatement(
            "INSERT INTO teachers (user_id, department, hire_date, phone, office) " +
            "VALUES (?, ?, CURRENT_DATE, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, teacherUserId);
        pstmt.setString(2, department);
        pstmt.setString(3, phone);
        pstmt.setString(4, office);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            teacherId = rs.getLong(1);
            printInfo("Teacher record created: teacher_id=" + teacherId);
        }

        printInfo("Username: " + username);
        printInfo("Department: " + department);
        printInfo("Office: " + office);
        printSuccess("Teacher account created successfully");
    }

    /**
     * 反向操作: 删除教师账户
     */
    public static void cleanupFeature2() throws SQLException {
        if (teacherUserId > 0) {
            executeUpdate("DELETE FROM users WHERE user_id = " + teacherUserId);
            teacherUserId = 0;
            teacherId = 0;
            System.out.println("  ↺ Cleanup: Teacher account deleted");
        }
    }

    // ==================================================================================
    // Feature 3: 用户登录认证 (教师) | Teacher Login Authentication
    // ==================================================================================

    /**
     * FEATURE 3: 用户登录认证 (教师端)
     *
     * 【所需参数】
     * - username: 用户名 (String)
     * - password: 密码 (String)
     *
     * 【返回结果】
     * - 认证成功: 返回用户信息 (user_id, username, role, full_name, teacher_id)
     * - 认证失败: 返回空结果
     *
     * 【副作用】
     * - 更新 last_login 时间戳
     */
    public static void testFeature3_TeacherLogin() throws SQLException {
        printFeature(3, "Teacher Login Authentication | 教师登录认证");

        String username = "john_teacher";
        String password = "teachpass";

        pstmt = conn.prepareStatement(
            "SELECT u.user_id, u.username, u.role, u.full_name, t.teacher_id, t.department " +
            "FROM users u " +
            "LEFT JOIN teachers t ON u.user_id = t.user_id " +
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
            printInfo("Teacher ID: " + rs.getLong("teacher_id"));
            printInfo("Department: " + rs.getString("department"));

            executeUpdate("UPDATE users SET last_login = NOW() WHERE user_id = " + rs.getLong("user_id"));
        }

        printSuccess("Login authentication completed");
    }

    // ==================================================================================
    // Feature 6: 创建课程 | Create Course
    // ==================================================================================

    /**
     * FEATURE 6: 创建课程
     *
     * 【所需参数】
     * - courseCode: 课程代码 (String, 唯一)
     * - courseName: 课程名称 (String)
     * - description: 课程描述 (String)
     * - creditHours: 学分 (int)
     * - createdBy: 创建者ID (long, 教师ID)
     *
     * 【返回结果】
     * - courseId: 课程ID
     *
     * 【反向操作】
     * - cleanupFeature6(): 删除课程
     */
    public static void testFeature6_CreateCourse() throws SQLException {
        printFeature(6, "Create Course | 创建课程");

        String courseCode = "CS101";
        String courseName = "Data Structures and Algorithms";
        String description = "Introduction to fundamental data structures and algorithm analysis";
        int creditHours = 4;

        pstmt = conn.prepareStatement(
            "INSERT INTO courses (course_code, course_name, description, credit_hours, created_by) " +
            "VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, courseCode);
        pstmt.setString(2, courseName);
        pstmt.setString(3, description);
        pstmt.setInt(4, creditHours);
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            courseId = rs.getLong(1);
            printInfo("Course created: course_id=" + courseId);
        }

        printInfo("Course Code: " + courseCode);
        printInfo("Course Name: " + courseName);
        printInfo("Credit Hours: " + creditHours);
        printSuccess("Course created successfully");
    }

    /**
     * 反向操作: 删除课程
     */
    public static void cleanupFeature6() throws SQLException {
        if (courseId > 0) {
            executeUpdate("DELETE FROM courses WHERE course_id = " + courseId);
            courseId = 0;
            System.out.println("  ↺ Cleanup: Course deleted");
        }
    }

    // ==================================================================================
    // Feature 7: 创建教室 | Create Classroom
    // ==================================================================================

    /**
     * FEATURE 7: 创建教室
     *
     * 【所需参数】
     * - courseId: 课程ID (long)
     * - teacherId: 教师ID (long)
     * - className: 班级名称 (String)
     * - semester: 学期 (String)
     * - year: 年份 (int)
     * - maxStudents: 最大学生数 (int)
     *
     * 【返回结果】
     * - classroomId: 教室ID
     *
     * 【反向操作】
     * - cleanupFeature7(): 删除教室
     */
    public static void testFeature7_CreateClassroom() throws SQLException {
        printFeature(7, "Create Classroom | 创建教室");

        String className = "Section 01";
        String semester = "Fall 2025";
        int year = 2025;
        int maxStudents = 50;

        pstmt = conn.prepareStatement(
            "INSERT INTO classrooms (course_id, teacher_id, class_name, semester, year, max_students) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, courseId);
        pstmt.setLong(2, teacherId);
        pstmt.setString(3, className);
        pstmt.setString(4, semester);
        pstmt.setInt(5, year);
        pstmt.setInt(6, maxStudents);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            classroomId = rs.getLong(1);
            printInfo("Classroom created: classroom_id=" + classroomId);
        }

        printInfo("Class Name: " + className);
        printInfo("Semester: " + semester);
        printInfo("Max Students: " + maxStudents);
        printSuccess("Classroom created successfully");
    }

    /**
     * 反向操作: 删除教室
     */
    public static void cleanupFeature7() throws SQLException {
        if (classroomId > 0) {
            executeUpdate("DELETE FROM classrooms WHERE classroom_id = " + classroomId);
            classroomId = 0;
            System.out.println("  ↺ Cleanup: Classroom deleted");
        }
    }

    // ==================================================================================
    // Feature 11: 创建科目 | Create Subject
    // ==================================================================================

    /**
     * FEATURE 11: 创建科目
     *
     * 【所需参数】
     * - subjectName: 科目名称 (String)
     * - description: 描述 (String)
     * - level: 层级 (int, 默认1)
     * - parentSubjectId: 父科目ID (long, 可选)
     *
     * 【返回结果】
     * - subjectId: 科目ID
     *
     * 【反向操作】
     * - cleanupFeature11(): 删除科目
     */
    public static void testFeature11_CreateSubject() throws SQLException {
        printFeature(11, "Create Subject | 创建科目");

        String subjectName = "Data Structures";
        String description = "Topics related to data structures and algorithms";
        int level = 1;

        pstmt = conn.prepareStatement(
            "INSERT INTO subjects (subject_name, description, level) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, subjectName);
        pstmt.setString(2, description);
        pstmt.setInt(3, level);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            subjectId = rs.getLong(1);
            printInfo("Subject created: subject_id=" + subjectId);
        }

        printInfo("Subject Name: " + subjectName);
        printInfo("Level: " + level);
        printSuccess("Subject created successfully");
    }

    /**
     * 反向操作: 删除科目
     */
    public static void cleanupFeature11() throws SQLException {
        if (subjectId > 0) {
            executeUpdate("DELETE FROM subjects WHERE subject_id = " + subjectId);
            subjectId = 0;
            System.out.println("  ↺ Cleanup: Subject deleted");
        }
    }

    // ==================================================================================
    // Feature 12: 创建单个题目 | Create Single Question
    // ==================================================================================

    /**
     * FEATURE 12: 创建单个题目
     *
     * 【所需参数】
     * - subjectId: 科目ID (long)
     * - questionText: 题目文本 (String)
     * - questionType: 题目类型 (String: multiple_choice, true_false, essay等)
     * - difficultyLevel: 难度等级 (int, 1-5)
     * - createdBy: 创建者ID (long, 教师ID)
     *
     * 【返回结果】
     * - questionId: 题目ID
     *
     * 【反向操作】
     * - cleanupFeature12(): 删除题目
     */
    public static void testFeature12_CreateSingleQuestion() throws SQLException {
        printFeature(12, "Create Single Question | 创建单个题目");

        String questionText = "What is the time complexity of binary search?";
        String questionType = "multiple_choice";
        int difficultyLevel = 2;

        pstmt = conn.prepareStatement(
            "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
            "VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, subjectId);
        pstmt.setString(2, questionText);
        pstmt.setString(3, questionType);
        pstmt.setInt(4, difficultyLevel);
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            questionId1 = rs.getLong(1);
            printInfo("Question created: question_id=" + questionId1);
        }

        printInfo("Type: " + questionType);
        printInfo("Difficulty: " + difficultyLevel + "/5");
        printSuccess("Question created successfully");
    }

    // ==================================================================================
    // Feature 13: 添加题目选项 | Add Question Options
    // ==================================================================================

    /**
     * FEATURE 13: 添加题目选项
     *
     * 【所需参数】
     * - questionId: 题目ID (long)
     * - options: 选项文本数组 (String[])
     * - correctFlags: 正确标记数组 (boolean[])
     *
     * 【返回结果】
     * - 每个选项的 option_id
     *
     * 【反向操作】
     * - cleanupFeature13(): 删除所有选项
     */
    public static void testFeature13_AddQuestionOptions() throws SQLException {
        printFeature(13, "Add Question Options | 添加题目选项");

        String[] options = {"O(n)", "O(log n)", "O(n²)", "O(1)"};
        boolean[] correct = {false, true, false, false};

        pstmt = conn.prepareStatement(
            "INSERT INTO question_options (question_id, option_text, is_correct, option_order) " +
            "VALUES (?, ?, ?, ?)"
        );

        for (int i = 0; i < options.length; i++) {
            pstmt.setLong(1, questionId1);
            pstmt.setString(2, options[i]);
            pstmt.setBoolean(3, correct[i]);
            pstmt.setInt(4, i + 1);
            pstmt.executeUpdate();

            System.out.println("  → Option " + (i + 1) + ": " + options[i] +
                (correct[i] ? " (CORRECT)" : ""));
        }

        printSuccess("All options added successfully");
    }

    // ==================================================================================
    // Feature 14: 批量创建题目 | Create Multiple Questions
    // ==================================================================================

    /**
     * FEATURE 14: 批量创建题目
     *
     * 【所需参数】
     * - 多组题目数据 (题目文本、选项、正确答案)
     *
     * 【返回结果】
     * - questionId2, questionId3: 新创建的题目ID
     *
     * 【反向操作】
     * - cleanupFeature14(): 删除批量创建的题目
     */
    public static void testFeature14_CreateMultipleQuestions() throws SQLException {
        printFeature(14, "Create Multiple Questions | 批量创建题目");

        // 题目2
        pstmt = conn.prepareStatement(
            "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
            "VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, subjectId);
        pstmt.setString(2, "Which data structure uses LIFO principle?");
        pstmt.setString(3, "multiple_choice");
        pstmt.setInt(4, 1);
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            questionId2 = rs.getLong(1);
            printInfo("Question 2 created: question_id=" + questionId2);
        }

        addOptionsForQuestion(questionId2,
            new String[]{"Queue", "Stack", "Array", "Tree"},
            new boolean[]{false, true, false, false});

        // 题目3
        pstmt = conn.prepareStatement(
            "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
            "VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, subjectId);
        pstmt.setString(2, "What is a balanced binary tree?");
        pstmt.setString(3, "multiple_choice");
        pstmt.setInt(4, 3);
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            questionId3 = rs.getLong(1);
            printInfo("Question 3 created: question_id=" + questionId3);
        }

        addOptionsForQuestion(questionId3,
            new String[]{"AVL Tree", "Linked List", "Hash Table", "Graph"},
            new boolean[]{true, false, false, false});

        printSuccess("Multiple questions created successfully");
    }

    private static void addOptionsForQuestion(long qId, String[] options, boolean[] correct) throws SQLException {
        pstmt = conn.prepareStatement(
            "INSERT INTO question_options (question_id, option_text, is_correct, option_order) " +
            "VALUES (?, ?, ?, ?)"
        );
        for (int i = 0; i < options.length; i++) {
            pstmt.setLong(1, qId);
            pstmt.setString(2, options[i]);
            pstmt.setBoolean(3, correct[i]);
            pstmt.setInt(4, i + 1);
            pstmt.executeUpdate();
        }
    }

    // ==================================================================================
    // Feature 15: 查询题目统计 | Query Question Statistics
    // ==================================================================================

    /**
     * FEATURE 15: 查询题目统计
     *
     * 【所需参数】
     * - 无 (查询所有题目)
     *
     * 【返回结果】
     * - 题目列表: question_id, question_text, question_type, difficulty_level, times_used, subject_name
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature15_QueryQuestionStatistics() throws SQLException {
        printFeature(15, "Query Question Statistics | 查询题目统计");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.question_text, q.question_type, q.difficulty_level, " +
            "q.times_used, q.correct_count, q.total_attempts, s.subject_name " +
            "FROM questions q " +
            "JOIN subjects s ON q.subject_id = s.subject_id " +
            "WHERE q.is_deleted = FALSE"
        );

        rs = pstmt.executeQuery();
        printInfo("Question Bank Statistics:");
        int count = 0;
        while (rs.next()) {
            count++;
            String text = rs.getString("question_text");
            if (text.length() > 50) text = text.substring(0, 50) + "...";
            System.out.println("     " + count + ". Q" + rs.getLong("question_id") + " - " + text);
            System.out.println("        Type: " + rs.getString("question_type") +
                " | Difficulty: " + rs.getInt("difficulty_level") + "/5" +
                " | Times Used: " + rs.getInt("times_used"));
        }

        printInfo("Total Questions: " + count);
        printSuccess("Statistics query completed");
    }

    // ==================================================================================
    // Feature 16: 创建测验 | Create Quiz
    // ==================================================================================

    /**
     * FEATURE 16: 创建测验
     *
     * 【所需参数】
     * - classroomId: 教室ID (long)
     * - title: 测验标题 (String)
     * - description: 描述 (String)
     * - createdBy: 创建者ID (long)
     * - startTime: 开始时间 (String, datetime格式)
     * - endTime: 结束时间 (String, datetime格式)
     * - durationMinutes: 时长 (int, 分钟)
     * - totalPoints: 总分 (int)
     * - passingScore: 及格分 (int)
     *
     * 【返回结果】
     * - quizId: 测验ID
     *
     * 【反向操作】
     * - cleanupFeature16(): 删除测验
     */
    public static void testFeature16_CreateQuiz() throws SQLException {
        printFeature(16, "Create Quiz | 创建测验");

        String title = "Midterm Exam - Data Structures";
        String description = "Comprehensive exam covering arrays, lists, and trees";
        int durationMinutes = 120;
        int totalPoints = 100;
        int passingScore = 60;

        pstmt = conn.prepareStatement(
            "INSERT INTO quizzes (classroom_id, title, description, created_by, " +
            "start_time, end_time, duration_minutes, total_points, passing_score) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, classroomId);
        pstmt.setString(2, title);
        pstmt.setString(3, description);
        pstmt.setLong(4, teacherId);
        pstmt.setString(5, "2025-01-01 09:00:00");
        pstmt.setString(6, "2025-12-31 23:59:59");
        pstmt.setInt(7, durationMinutes);
        pstmt.setInt(8, totalPoints);
        pstmt.setInt(9, passingScore);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            quizId = rs.getLong(1);
            printInfo("Quiz created: quiz_id=" + quizId);
        }

        printInfo("Title: " + title);
        printInfo("Duration: " + durationMinutes + " minutes");
        printInfo("Total Points: " + totalPoints);
        printSuccess("Quiz created successfully");
    }

    // ==================================================================================
    // Feature 17: 随机选题 | Random Question Selection
    // ==================================================================================

    /**
     * FEATURE 17: 随机选题
     *
     * 【所需参数】
     * - subjectId: 科目ID (long)
     * - limit: 选择数量 (int)
     *
     * 【返回结果】
     * - 随机选择的题目列表
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature17_RandomQuestionSelection() throws SQLException {
        printFeature(17, "Random Question Selection | 随机选题");

        pstmt = conn.prepareStatement(
            "SELECT question_id, question_text, difficulty_level " +
            "FROM questions " +
            "WHERE subject_id = ? AND question_type = 'multiple_choice' AND is_deleted = FALSE " +
            "ORDER BY RAND() LIMIT 3"
        );
        pstmt.setLong(1, subjectId);

        rs = pstmt.executeQuery();
        printInfo("Randomly selected questions:");
        while (rs.next()) {
            String text = rs.getString("question_text");
            if (text.length() > 40) text = text.substring(0, 40) + "...";
            System.out.println("     • Q" + rs.getLong("question_id") + ": " + text +
                " (Difficulty: " + rs.getInt("difficulty_level") + "/5)");
        }

        printSuccess("Random selection completed");
    }

    // ==================================================================================
    // Feature 18: 添加题目到测验 | Add Questions to Quiz
    // ==================================================================================

    /**
     * FEATURE 18: 添加题目到测验
     *
     * 【所需参数】
     * - quizId: 测验ID (long)
     * - questionId: 题目ID (long)
     * - questionOrder: 题目顺序 (int)
     * - points: 分值 (int)
     *
     * 【返回结果】
     * - quiz_question_id: 关联记录ID
     *
     * 【反向操作】
     * - cleanupFeature18(): 删除测验题目关联
     */
    public static void testFeature18_AddQuestionsToQuiz() throws SQLException {
        printFeature(18, "Add Questions to Quiz | 添加题目到测验");

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

            printInfo("Question " + (i + 1) + " added: Q" + questions[i] + " (Points: " + points[i] + ")");
        }

        printSuccess("All questions added to quiz");
    }

    // ==================================================================================
    // Feature 19: 配置测验设置 | Configure Quiz Settings
    // ==================================================================================

    /**
     * FEATURE 19: 配置测验设置
     *
     * 【所需参数】
     * - quizId: 测验ID (long)
     * - shuffleQuestions: 打乱题目顺序 (boolean)
     * - shuffleOptions: 打乱选项顺序 (boolean)
     * - showResultsImmediately: 立即显示结果 (boolean)
     * - allowReview: 允许复查 (boolean)
     *
     * 【返回结果】
     * - setting_id: 设置记录ID
     *
     * 【反向操作】
     * - cleanupFeature19(): 删除设置
     */
    public static void testFeature19_ConfigureQuizSettings() throws SQLException {
        printFeature(19, "Configure Quiz Settings | 配置测验设置");

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

        printInfo("Settings configured:");
        System.out.println("     • Shuffle Questions: YES");
        System.out.println("     • Shuffle Options: YES");
        System.out.println("     • Show Results Immediately: NO");
        System.out.println("     • Allow Review: YES");
        printSuccess("Quiz settings saved");
    }

    // ==================================================================================
    // Feature 20: 查看测验详情 | View Quiz Details
    // ==================================================================================

    /**
     * FEATURE 20: 查看测验详情
     *
     * 【所需参数】
     * - quizId: 测验ID (long)
     *
     * 【返回结果】
     * - 测验详情: title, start_time, end_time, duration, total_points, passing_score, question_count
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature20_ViewQuizDetails() throws SQLException {
        printFeature(20, "View Quiz Details | 查看测验详情");

        pstmt = conn.prepareStatement(
            "SELECT q.quiz_id, q.title, q.start_time, q.end_time, q.duration_minutes, " +
            "q.total_points, q.passing_score, COUNT(qq.question_id) as question_count " +
            "FROM quizzes q " +
            "LEFT JOIN quiz_questions qq ON q.quiz_id = qq.quiz_id " +
            "WHERE q.quiz_id = ? GROUP BY q.quiz_id"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        if (rs.next()) {
            printInfo("Quiz Information:");
            System.out.println("     • ID: " + rs.getLong("quiz_id"));
            System.out.println("     • Title: " + rs.getString("title"));
            System.out.println("     • Start: " + rs.getString("start_time"));
            System.out.println("     • End: " + rs.getString("end_time"));
            System.out.println("     • Duration: " + rs.getInt("duration_minutes") + " minutes");
            System.out.println("     • Total Points: " + rs.getInt("total_points"));
            System.out.println("     • Passing Score: " + rs.getInt("passing_score"));
            System.out.println("     • Questions: " + rs.getInt("question_count"));
        }

        printSuccess("Quiz details retrieved");
    }

    // ==================================================================================
    // Feature 25: 自动评分客观题 | Auto Grade Objective Questions
    // ==================================================================================

    /**
     * FEATURE 25: 自动评分客观题
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     *
     * 【操作说明】
     * - 对比学生答案与正确答案
     * - 自动计算每道题得分
     *
     * 【返回结果】
     * - 每道题的 is_correct 和 points_earned
     *
     * 【反向操作】
     * - reverseFeature25(): 清除评分结果
     */
    public static void testFeature25_AutoGradeObjectiveQuestions() throws SQLException {
        printFeature(25, "Auto Grade Objective Questions | 自动评分客观题");

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
        int graded = pstmt.executeUpdate();

        printInfo("Auto-grading completed");
        printInfo("Questions graded: " + graded);

        // 显示评分结果
        pstmt = conn.prepareStatement(
            "SELECT sa.question_id, sa.is_correct, sa.points_earned, qq.points " +
            "FROM student_answers sa " +
            "JOIN quiz_questions qq ON sa.question_id = qq.question_id AND qq.quiz_id = " +
            "(SELECT quiz_id FROM student_quizzes WHERE student_quiz_id = sa.student_quiz_id) " +
            "WHERE sa.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();

        printInfo("Grading Details:");
        while (rs.next()) {
            boolean correct = rs.getBoolean("is_correct");
            System.out.println("     • Q" + rs.getLong("question_id") + ": " +
                (correct ? "✓ Correct" : "✗ Incorrect") +
                " - " + rs.getDouble("points_earned") + "/" + rs.getInt("points") + " points");
        }

        printSuccess("Auto-grading completed");
    }

    /**
     * 反向操作: 清除评分结果
     */
    public static void reverseFeature25() throws SQLException {
        if (studentQuizId > 0) {
            executeUpdate("UPDATE student_answers SET is_correct = NULL, points_earned = NULL " +
                         "WHERE student_quiz_id = " + studentQuizId);
            System.out.println("  ↺ Reversed: Grading results cleared");
        }
    }

    // ==================================================================================
    // Feature 26: 计算总分 | Calculate Total Score
    // ==================================================================================

    /**
     * FEATURE 26: 计算总分
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     *
     * 【操作说明】
     * - 汇总所有题目得分
     * - 计算百分比
     * - 更新状态为 completed
     *
     * 【返回结果】
     * - score: 总分
     * - percentage: 百分比
     *
     * 【反向操作】
     * - reverseFeature26(): 清除总分
     */
    public static void testFeature26_CalculateTotalScore() throws SQLException {
        printFeature(26, "Calculate Total Score | 计算总分");

        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes sq " +
            "SET score = (SELECT COALESCE(SUM(sa.points_earned), 0) " +
            "             FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
            "    percentage = (SELECT COALESCE(SUM(sa.points_earned), 0) * 100.0 / " +
            "                  (SELECT total_points FROM quizzes WHERE quiz_id = sq.quiz_id) " +
            "                  FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
            "    graded = TRUE, status = 'completed' " +
            "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        // 显示最终成绩
        pstmt = conn.prepareStatement(
            "SELECT sq.score, sq.percentage, q.total_points, q.passing_score " +
            "FROM student_quizzes sq JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
            "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();

        if (rs.next()) {
            double score = rs.getDouble("score");
            double percentage = rs.getDouble("percentage");
            int passingScore = rs.getInt("passing_score");

            printInfo("Final Score Calculated:");
            System.out.println("     • Raw Score: " + score + " / " + rs.getInt("total_points"));
            System.out.println("     • Percentage: " + String.format("%.2f", percentage) + "%");
            System.out.println("     • Result: " + (score >= passingScore ? "✓ PASSED" : "✗ FAILED"));
        }

        printSuccess("Score calculation completed");
    }

    // ==================================================================================
    // Feature 27: 发布成绩 | Publish Grades
    // ==================================================================================

    /**
     * FEATURE 27: 发布成绩
     *
     * 【所需参数】
     * - studentQuizId: 学生测验会话ID (long)
     *
     * 【操作说明】
     * - 将 published 设为 TRUE
     * - 学生可以查看成绩
     *
     * 【返回结果】
     * - published: TRUE
     *
     * 【反向操作】
     * - reverseFeature27(): 取消发布
     */
    public static void testFeature27_PublishGrades() throws SQLException {
        printFeature(27, "Publish Grades | 发布成绩");

        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes SET published = TRUE WHERE student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        printInfo("Grades published");
        printInfo("Students can now view their results");
        printSuccess("Publication completed");
    }

    /**
     * 反向操作: 取消发布
     */
    public static void reverseFeature27() throws SQLException {
        if (studentQuizId > 0) {
            executeUpdate("UPDATE student_quizzes SET published = FALSE WHERE student_quiz_id = " + studentQuizId);
            System.out.println("  ↺ Reversed: Grades unpublished");
        }
    }

    // ==================================================================================
    // Feature 30: 教师查看班级成绩 | Teacher View Class Grades
    // ==================================================================================

    /**
     * FEATURE 30: 教师查看班级成绩
     *
     * 【所需参数】
     * - quizId: 测验ID (long)
     *
     * 【返回结果】
     * - 学生成绩列表: full_name, student_number, score, percentage, result
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature30_TeacherViewClassGrades() throws SQLException {
        printFeature(30, "Teacher View Class Grades | 教师查看班级成绩");

        pstmt = conn.prepareStatement(
            "SELECT u.full_name, s.student_number, sq.score, q.total_points, sq.percentage, " +
            "CASE WHEN sq.score >= q.passing_score THEN 'Pass' ELSE 'Fail' END as result " +
            "FROM student_quizzes sq " +
            "JOIN students s ON sq.student_id = s.student_id " +
            "JOIN users u ON s.user_id = u.user_id " +
            "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
            "WHERE sq.quiz_id = ? AND sq.graded = TRUE ORDER BY sq.score DESC"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        printInfo("Class Grades:");
        while (rs.next()) {
            System.out.println("     • " + rs.getString("full_name") + " (" + rs.getString("student_number") + ")");
            System.out.println("       Score: " + rs.getDouble("score") + "/" + rs.getInt("total_points") +
                " (" + String.format("%.2f", rs.getDouble("percentage")) + "%)");
            System.out.println("       Result: " + rs.getString("result"));
        }

        printSuccess("Class grades retrieved");
    }

    // ==================================================================================
    // Feature 31: 题目难度分析 | Question Difficulty Analysis
    // ==================================================================================

    /**
     * FEATURE 31: 题目难度分析
     *
     * 【所需参数】
     * - 无 (分析所有题目)
     *
     * 【返回结果】
     * - 每道题的: difficulty_level, total_attempts, correct_count, correct_rate
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature31_QuestionDifficultyAnalysis() throws SQLException {
        printFeature(31, "Question Difficulty Analysis | 题目难度分析");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.question_text, q.difficulty_level, " +
            "COUNT(sa.answer_id) as total_attempts, " +
            "SUM(CASE WHEN sa.is_correct THEN 1 ELSE 0 END) as correct_count, " +
            "ROUND(SUM(CASE WHEN sa.is_correct THEN 1 ELSE 0 END) * 100.0 / COUNT(sa.answer_id), 2) as correct_rate " +
            "FROM questions q " +
            "LEFT JOIN student_answers sa ON q.question_id = sa.question_id " +
            "WHERE q.is_deleted = FALSE GROUP BY q.question_id HAVING total_attempts > 0"
        );

        rs = pstmt.executeQuery();
        printInfo("Question Analysis:");
        while (rs.next()) {
            String text = rs.getString("question_text");
            if (text.length() > 40) text = text.substring(0, 40) + "...";
            System.out.println("     • Q" + rs.getLong("question_id") + ": " + text);
            System.out.println("       Preset Difficulty: " + rs.getInt("difficulty_level") + "/5");
            System.out.println("       Attempts: " + rs.getInt("total_attempts"));
            System.out.println("       Correct Rate: " + rs.getDouble("correct_rate") + "%");
        }

        printSuccess("Difficulty analysis completed");
    }

    // ==================================================================================
    // Feature 32: 生成成绩报告 | Generate Grade Report
    // ==================================================================================

    /**
     * FEATURE 32: 生成成绩报告
     *
     * 【所需参数】
     * - quizId: 测验ID (long)
     *
     * 【返回结果】
     * - 统计数据: total_students, avg_score, min_score, max_score, passed_count, pass_rate
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature32_GenerateGradeReport() throws SQLException {
        printFeature(32, "Generate Grade Report | 生成成绩报告");

        pstmt = conn.prepareStatement(
            "SELECT COUNT(DISTINCT sq.student_id) as total_students, " +
            "ROUND(AVG(sq.score), 2) as avg_score, " +
            "MIN(sq.score) as min_score, MAX(sq.score) as max_score, " +
            "SUM(CASE WHEN sq.score >= q.passing_score THEN 1 ELSE 0 END) as passed_count " +
            "FROM student_quizzes sq JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
            "WHERE sq.quiz_id = ? AND sq.graded = TRUE"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        if (rs.next()) {
            int total = rs.getInt("total_students");
            int passed = rs.getInt("passed_count");

            printInfo("Grade Report Summary:");
            System.out.println("     • Total Students: " + total);
            System.out.println("     • Average Score: " + rs.getDouble("avg_score"));
            System.out.println("     • Min Score: " + rs.getDouble("min_score"));
            System.out.println("     • Max Score: " + rs.getDouble("max_score"));
            System.out.println("     • Passed: " + passed);
            System.out.println("     • Failed: " + (total - passed));
            if (total > 0) {
                System.out.println("     • Pass Rate: " + String.format("%.2f", (passed * 100.0 / total)) + "%");
            }
        }

        printSuccess("Grade report generated");
    }

    // ==================================================================================
    // Feature 35: 查看教师的测验 | View Teacher's Quizzes
    // ==================================================================================

    /**
     * FEATURE 35: 查看教师的测验
     *
     * 【所需参数】
     * - teacherId: 教师ID (long)
     *
     * 【返回结果】
     * - 测验列表: quiz_id, title, course_name, enrolled_students, submissions
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature35_ViewTeachersQuizzes() throws SQLException {
        printFeature(35, "View Teacher's Quizzes | 查看教师的测验");

        pstmt = conn.prepareStatement(
            "SELECT q.quiz_id, q.title, c.course_name, " +
            "COUNT(DISTINCT e.student_id) as enrolled_students, " +
            "COUNT(DISTINCT sq.student_quiz_id) as submissions " +
            "FROM quizzes q " +
            "JOIN classrooms cl ON q.classroom_id = cl.classroom_id " +
            "JOIN courses c ON cl.course_id = c.course_id " +
            "LEFT JOIN enrollments e ON cl.classroom_id = e.classroom_id AND e.status = 'active' " +
            "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id " +
            "WHERE q.created_by = ? GROUP BY q.quiz_id"
        );
        pstmt.setLong(1, teacherId);

        rs = pstmt.executeQuery();
        printInfo("Quizzes Created by Teacher:");
        while (rs.next()) {
            System.out.println("     • Quiz ID: " + rs.getLong("quiz_id"));
            System.out.println("       Title: " + rs.getString("title"));
            System.out.println("       Course: " + rs.getString("course_name"));
            System.out.println("       Enrolled: " + rs.getInt("enrolled_students") + " students");
            System.out.println("       Submissions: " + rs.getInt("submissions"));
        }

        printSuccess("Teacher's quizzes listed");
    }

    // ==================================================================================
    // 辅助方法 - 准备测试数据
    // ==================================================================================

    /**
     * 准备学生数据 (用于班级成绩等测试)
     */
    private static void prepareStudentData() throws SQLException {
        printSectionHeader("Preparing Student Data (Prerequisites)");

        // 创建学生
        pstmt = conn.prepareStatement(
            "INSERT INTO users (username, password_hash, email, full_name, role) " +
            "VALUES ('alice_student', SHA2('password123', 256), 'alice@university.edu', 'Alice Johnson', 'student')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) studentUserId = rs.getLong(1);

        pstmt = conn.prepareStatement(
            "INSERT INTO students (user_id, student_number, grade, major, enrollment_date) " +
            "VALUES (?, 'STU2025001', 'Junior', 'Computer Science', CURRENT_DATE)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, studentUserId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) studentId = rs.getLong(1);

        // 注册课程
        pstmt = conn.prepareStatement(
            "INSERT INTO enrollments (student_id, classroom_id, status) VALUES (?, ?, 'active')"
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        pstmt.executeUpdate();

        printInfo("Student ID: " + studentId + " created and enrolled");
        printSuccess("Student data prepared");
    }

    /**
     * 准备学生测验答题数据 (用于评分测试)
     */
    private static void prepareStudentQuizData() throws SQLException {
        printSectionHeader("Preparing Student Quiz Data (Prerequisites for Grading)");

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

        // 获取题目并提交答案
        pstmt = conn.prepareStatement(
            "SELECT qq.question_id FROM quiz_questions qq WHERE qq.quiz_id = ? ORDER BY qq.question_order"
        );
        pstmt.setLong(1, quizId);
        rs = pstmt.executeQuery();

        List<Long> questionIds = new ArrayList<>();
        while (rs.next()) {
            questionIds.add(rs.getLong("question_id"));
        }

        // 为每道题提交答案
        for (long qid : questionIds) {
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

        printInfo("Student Quiz Session: " + studentQuizId + " created with answers");
        printSuccess("Student quiz data prepared");
    }
}

