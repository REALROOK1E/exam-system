package com.zekai.comment;

import java.sql.*;

/**
 * ========================================
 * ADMIN TESTS - 管理后台功能测试
 * ========================================
 *
 * 包含以下管理后台功能 (7个):
 * - Feature 33: 更新题目统计
 * - Feature 34: 自适应难度评级
 * - Feature 36: 题目使用排名
 * - Feature 37: 科目层级查询
 * - 系统管理功能:
 *   - 用户管理 (激活/禁用账户)
 *   - 数据统计总览
 *   - 系统数据维护
 *
 * 使用方法:
 * - 运行 main() 执行所有管理后台功能测试
 * - 直接调用单个 testFeatureXX() 方法执行单独测试
 *
 * @author Exam System Team
 * @version 2.0
 */
public class AdminTests extends TestBase {

    public static void main(String[] args) {
        runAllAdminTests();
    }

    /**
     * 运行所有管理后台功能测试
     */
    public static void runAllAdminTests() {
        printHeader("ADMIN TESTS - 管理后台功能测试");

        try {
            resetTestData();
            initConnection();
            setupDatabase();

            // 准备完整的测试数据
            prepareFullTestData();

            // 高级功能测试
            testFeature33_UpdateQuestionStatistics();
            testFeature34_AdaptiveDifficultyRating();
            testFeature36_QuestionUsageRanking();
            testFeature37_SubjectHierarchyQuery();

            // 管理功能测试
            testAdminFeature_UserManagement();
            testAdminFeature_SystemOverview();
            testAdminFeature_DataMaintenance();

            commit();
            printHeader("✓ ALL ADMIN TESTS COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            rollback();
        } finally {
            closeResources();
        }
    }

    // ==================================================================================
    // Feature 33: 更新题目统计 | Update Question Statistics
    // ==================================================================================

    /**
     * FEATURE 33: 更新题目统计
     *
     * 【所需参数】
     * - 无 (更新所有有答题记录的题目)
     *
     * 【操作说明】
     * - 更新 times_used: 题目被使用的测验次数
     * - 更新 total_attempts: 总答题次数
     * - 更新 correct_count: 正确答题次数
     *
     * 【返回结果】
     * - 更新的题目数量
     *
     * 【反向操作】
     * - reverseFeature33(): 重置统计数据为0
     */
    public static void testFeature33_UpdateQuestionStatistics() throws SQLException {
        printFeature(33, "Update Question Statistics | 更新题目统计");

        pstmt = conn.prepareStatement(
            "UPDATE questions q " +
            "SET times_used = (SELECT COUNT(DISTINCT sa.student_quiz_id) " +
            "                  FROM student_answers sa WHERE sa.question_id = q.question_id), " +
            "    total_attempts = (SELECT COUNT(*) " +
            "                     FROM student_answers sa WHERE sa.question_id = q.question_id), " +
            "    correct_count = (SELECT COUNT(*) " +
            "                    FROM student_answers sa WHERE sa.question_id = q.question_id AND sa.is_correct = TRUE) " +
            "WHERE q.question_id IN (SELECT DISTINCT question_id FROM student_answers)"
        );
        int updated = pstmt.executeUpdate();

        printInfo("Statistics updated for " + updated + " questions");

        // 显示更新后的统计
        pstmt = conn.prepareStatement(
            "SELECT question_id, times_used, total_attempts, correct_count " +
            "FROM questions WHERE times_used > 0"
        );
        rs = pstmt.executeQuery();

        printInfo("Updated Statistics:");
        while (rs.next()) {
            System.out.println("     • Q" + rs.getLong("question_id") +
                ": Used " + rs.getInt("times_used") + " times, " +
                rs.getInt("correct_count") + "/" + rs.getInt("total_attempts") + " correct");
        }

        printSuccess("Question statistics updated");
    }

    /**
     * 反向操作: 重置统计数据
     */
    public static void reverseFeature33() throws SQLException {
        executeUpdate("UPDATE questions SET times_used = 0, total_attempts = 0, correct_count = 0");
        System.out.println("  ↺ Reversed: Question statistics reset to 0");
    }

    // ==================================================================================
    // Feature 34: 自适应难度评级 | Adaptive Difficulty Rating
    // ==================================================================================

    /**
     * FEATURE 34: 自适应难度评级
     *
     * 【所需参数】
     * - 无 (分析所有有答题数据的题目)
     *
     * 【评级规则】
     * - 正确率 >= 80%: Too Easy (太容易)
     * - 正确率 >= 60%: Appropriate (难度适中)
     * - 正确率 >= 40%: Slightly Hard (稍难)
     * - 正确率 < 40%: Too Hard (太难)
     *
     * 【返回结果】
     * - 每道题的预设难度 vs 实际难度评级
     *
     * 【反向操作】
     * - 无需反向操作 (只读分析)
     */
    public static void testFeature34_AdaptiveDifficultyRating() throws SQLException {
        printFeature(34, "Adaptive Difficulty Rating | 自适应难度评级");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.question_text, q.difficulty_level, " +
            "q.total_attempts, q.correct_count, " +
            "CASE WHEN q.total_attempts = 0 THEN 'No Data' " +
            "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 80 THEN 'Too Easy' " +
            "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 60 THEN 'Appropriate' " +
            "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 40 THEN 'Slightly Hard' " +
            "     ELSE 'Too Hard' END as actual_difficulty, " +
            "CASE WHEN q.total_attempts = 0 THEN NULL " +
            "     ELSE ROUND(q.correct_count * 100.0 / q.total_attempts, 2) END as correct_rate " +
            "FROM questions q WHERE q.times_used > 0"
        );

        rs = pstmt.executeQuery();
        printInfo("Adaptive Difficulty Analysis:");
        while (rs.next()) {
            String text = rs.getString("question_text");
            if (text.length() > 40) text = text.substring(0, 40) + "...";

            System.out.println("     • Q" + rs.getLong("question_id") + ": " + text);
            System.out.println("       Preset Difficulty: " + rs.getInt("difficulty_level") + "/5");
            System.out.println("       Correct Rate: " +
                (rs.getObject("correct_rate") != null ? rs.getDouble("correct_rate") + "%" : "N/A"));
            System.out.println("       Actual Rating: " + rs.getString("actual_difficulty"));
            System.out.println();
        }

        printSuccess("Adaptive rating completed");
    }

    // ==================================================================================
    // Feature 36: 题目使用排名 | Question Usage Ranking
    // ==================================================================================

    /**
     * FEATURE 36: 题目使用排名
     *
     * 【所需参数】
     * - limit: 显示数量 (int, 默认5)
     *
     * 【返回结果】
     * - 按使用次数排序的题目列表
     * - 包含: question_id, question_text, times_used, total_attempts, correct_rate
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature36_QuestionUsageRanking() throws SQLException {
        printFeature(36, "Question Usage Ranking | 题目使用排名");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.question_text, q.times_used, q.total_attempts, " +
            "CASE WHEN q.total_attempts = 0 THEN NULL " +
            "     ELSE ROUND(q.correct_count * 100.0 / q.total_attempts, 2) END as correct_rate " +
            "FROM questions q " +
            "WHERE q.times_used > 0 AND q.is_deleted = FALSE " +
            "ORDER BY q.times_used DESC, q.total_attempts DESC LIMIT 5"
        );

        rs = pstmt.executeQuery();
        printInfo("Top Used Questions:");
        int rank = 1;
        while (rs.next()) {
            String text = rs.getString("question_text");
            if (text.length() > 50) text = text.substring(0, 50) + "...";

            System.out.println("     " + rank + ". Q" + rs.getLong("question_id"));
            System.out.println("        " + text);
            System.out.println("        Times Used: " + rs.getInt("times_used"));
            System.out.println("        Total Attempts: " + rs.getInt("total_attempts"));
            System.out.println("        Correct Rate: " +
                (rs.getObject("correct_rate") != null ? rs.getDouble("correct_rate") + "%" : "N/A"));
            rank++;
        }

        printSuccess("Usage ranking generated");
    }

    // ==================================================================================
    // Feature 37: 科目层级查询 | Subject Hierarchy Query
    // ==================================================================================

    /**
     * FEATURE 37: 科目层级查询
     *
     * 【所需参数】
     * - 无 (查询所有科目)
     *
     * 【返回结果】
     * - 科目层级结构
     * - 每个科目下的题目数量
     *
     * 【反向操作】
     * - 无需反向操作 (只读操作)
     */
    public static void testFeature37_SubjectHierarchyQuery() throws SQLException {
        printFeature(37, "Subject Hierarchy Query | 科目层级查询");

        pstmt = conn.prepareStatement(
            "SELECT s.subject_id, s.subject_name, s.level, s.parent_subject_id, " +
            "COUNT(q.question_id) as question_count " +
            "FROM subjects s " +
            "LEFT JOIN questions q ON s.subject_id = q.subject_id AND q.is_deleted = FALSE " +
            "GROUP BY s.subject_id ORDER BY s.level, s.subject_id"
        );

        rs = pstmt.executeQuery();
        printInfo("Subject Hierarchy:");
        while (rs.next()) {
            int level = rs.getInt("level");
            String indent = "  ".repeat(level);
            System.out.println("     " + indent + "• " + rs.getString("subject_name") +
                " (Level " + level + ")");
            System.out.println("     " + indent + "  Questions: " + rs.getInt("question_count"));
        }

        printSuccess("Hierarchy query completed");
    }

    // ==================================================================================
    // 管理功能: 用户管理 | User Management
    // ==================================================================================

    /**
     * 管理功能: 用户管理
     *
     * 【功能说明】
     * - 查看所有用户
     * - 激活/禁用用户账户
     * - 用户统计
     *
     * 【所需参数】
     * - userId: 用户ID (用于激活/禁用)
     *
     * 【反向操作】
     * - 可重新激活/禁用
     */
    public static void testAdminFeature_UserManagement() throws SQLException {
        printFeature(0, "Admin: User Management | 管理员: 用户管理");

        // 查看所有用户
        pstmt = conn.prepareStatement(
            "SELECT user_id, username, role, full_name, is_active, created_at, last_login " +
            "FROM users ORDER BY created_at DESC"
        );

        rs = pstmt.executeQuery();
        printInfo("All Users:");
        while (rs.next()) {
            System.out.println("     • ID: " + rs.getLong("user_id") +
                " | " + rs.getString("username") +
                " (" + rs.getString("role") + ")");
            System.out.println("       Name: " + rs.getString("full_name") +
                " | Active: " + (rs.getBoolean("is_active") ? "Yes" : "No"));
        }

        // 用户统计
        pstmt = conn.prepareStatement(
            "SELECT role, COUNT(*) as count FROM users GROUP BY role"
        );
        rs = pstmt.executeQuery();

        printInfo("User Statistics:");
        while (rs.next()) {
            System.out.println("     • " + rs.getString("role") + ": " + rs.getInt("count") + " users");
        }

        // 演示禁用/启用账户
        if (studentUserId > 0) {
            executeUpdate("UPDATE users SET is_active = FALSE WHERE user_id = " + studentUserId);
            printInfo("User " + studentUserId + " disabled (demo)");

            executeUpdate("UPDATE users SET is_active = TRUE WHERE user_id = " + studentUserId);
            printInfo("User " + studentUserId + " re-enabled");
        }

        printSuccess("User management completed");
    }

    // ==================================================================================
    // 管理功能: 系统总览 | System Overview
    // ==================================================================================

    /**
     * 管理功能: 系统数据总览
     *
     * 【功能说明】
     * - 显示系统各项数据统计
     * - 用户数、课程数、题目数、测验数等
     */
    public static void testAdminFeature_SystemOverview() throws SQLException {
        printFeature(0, "Admin: System Overview | 管理员: 系统总览");

        printInfo("System Statistics:");

        // 用户统计
        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM users");
        if (rs.next()) System.out.println("     • Total Users: " + rs.getInt("cnt"));

        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM teachers");
        if (rs.next()) System.out.println("     • Total Teachers: " + rs.getInt("cnt"));

        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM students");
        if (rs.next()) System.out.println("     • Total Students: " + rs.getInt("cnt"));

        // 课程统计
        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM courses");
        if (rs.next()) System.out.println("     • Total Courses: " + rs.getInt("cnt"));

        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM classrooms");
        if (rs.next()) System.out.println("     • Total Classrooms: " + rs.getInt("cnt"));

        // 题库统计
        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM subjects");
        if (rs.next()) System.out.println("     • Total Subjects: " + rs.getInt("cnt"));

        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM questions WHERE is_deleted = FALSE");
        if (rs.next()) System.out.println("     • Total Questions: " + rs.getInt("cnt"));

        // 测验统计
        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM quizzes");
        if (rs.next()) System.out.println("     • Total Quizzes: " + rs.getInt("cnt"));

        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM student_quizzes");
        if (rs.next()) System.out.println("     • Total Quiz Attempts: " + rs.getInt("cnt"));

        rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM student_quizzes WHERE graded = TRUE");
        if (rs.next()) System.out.println("     • Graded Quizzes: " + rs.getInt("cnt"));

        printSuccess("System overview generated");
    }

    // ==================================================================================
    // 管理功能: 数据维护 | Data Maintenance
    // ==================================================================================

    /**
     * 管理功能: 数据维护
     *
     * 【功能说明】
     * - 软删除题目 (标记为已删除)
     * - 恢复已删除题目
     * - 清理过期数据 (演示)
     *
     * 【反向操作】
     * - 所有操作都可逆
     */
    public static void testAdminFeature_DataMaintenance() throws SQLException {
        printFeature(0, "Admin: Data Maintenance | 管理员: 数据维护");

        // 演示软删除题目
        if (questionId1 > 0) {
            executeUpdate("UPDATE questions SET is_deleted = TRUE WHERE question_id = " + questionId1);
            printInfo("Question " + questionId1 + " soft-deleted (demo)");

            // 查看已删除题目
            pstmt = conn.prepareStatement(
                "SELECT question_id, question_text FROM questions WHERE is_deleted = TRUE"
            );
            rs = pstmt.executeQuery();
            printInfo("Deleted Questions:");
            while (rs.next()) {
                String text = rs.getString("question_text");
                if (text.length() > 40) text = text.substring(0, 40) + "...";
                System.out.println("     • Q" + rs.getLong("question_id") + ": " + text);
            }

            // 恢复题目
            executeUpdate("UPDATE questions SET is_deleted = FALSE WHERE question_id = " + questionId1);
            printInfo("Question " + questionId1 + " restored");
        }

        // 显示数据库表占用
        printInfo("Table Statistics:");
        String[] tables = {"users", "teachers", "students", "courses", "classrooms",
                          "enrollments", "subjects", "questions", "question_options",
                          "quizzes", "quiz_questions", "quiz_settings",
                          "student_quizzes", "student_answers"};

        for (String table : tables) {
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM " + table);
            if (rs.next()) {
                System.out.println("     • " + table + ": " + rs.getInt("cnt") + " rows");
            }
        }

        printSuccess("Data maintenance completed");
    }

    // ==================================================================================
    // 辅助方法 - 准备完整测试数据
    // ==================================================================================

    /**
     * 准备完整的测试数据 (用于管理后台测试)
     */
    private static void prepareFullTestData() throws SQLException {
        printSectionHeader("Preparing Full Test Data");

        // 1. 创建教师
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
        printInfo("Teacher created: ID=" + teacherId);

        // 2. 创建学生
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
        printInfo("Student created: ID=" + studentId);

        // 3. 创建课程和教室
        pstmt = conn.prepareStatement(
            "INSERT INTO courses (course_code, course_name, description, credit_hours, created_by) " +
            "VALUES ('CS101', 'Data Structures', 'Introduction to DS', 4, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, teacherId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) courseId = rs.getLong(1);

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
        printInfo("Course and Classroom created");

        // 4. 学生注册
        pstmt = conn.prepareStatement(
            "INSERT INTO enrollments (student_id, classroom_id, status) VALUES (?, ?, 'active')"
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        pstmt.executeUpdate();

        // 5. 创建科目和题目
        pstmt = conn.prepareStatement(
            "INSERT INTO subjects (subject_name, description, level) VALUES ('Data Structures', 'DS Topics', 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) subjectId = rs.getLong(1);

        // 创建子科目
        pstmt = conn.prepareStatement(
            "INSERT INTO subjects (subject_name, description, level, parent_subject_id) VALUES ('Trees', 'Tree structures', 2, ?)"
        );
        pstmt.setLong(1, subjectId);
        pstmt.executeUpdate();

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
        printInfo("Questions created: Q" + questionId1 + ", Q" + questionId2 + ", Q" + questionId3);

        // 6. 创建测验
        pstmt = conn.prepareStatement(
            "INSERT INTO quizzes (classroom_id, title, description, created_by, " +
            "start_time, end_time, duration_minutes, total_points, passing_score) " +
            "VALUES (?, 'Midterm Exam', 'DS Midterm', ?, '2025-01-01 09:00:00', '2025-12-31 23:59:59', 120, 100, 60)",
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
        printInfo("Quiz created: ID=" + quizId);

        // 7. 创建学生测验答题记录
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

        // 8. 自动评分
        pstmt = conn.prepareStatement(
            "UPDATE student_answers sa " +
            "JOIN question_options qo ON sa.selected_option_id = qo.option_id " +
            "JOIN quiz_questions qq ON sa.question_id = qq.question_id AND qq.quiz_id = ? " +
            "SET sa.is_correct = qo.is_correct, " +
            "    sa.points_earned = CASE WHEN qo.is_correct THEN qq.points ELSE 0 END " +
            "WHERE sa.student_quiz_id = ?"
        );
        pstmt.setLong(1, quizId);
        pstmt.setLong(2, studentQuizId);
        pstmt.executeUpdate();

        // 计算总分
        pstmt = conn.prepareStatement(
            "UPDATE student_quizzes sq " +
            "SET score = (SELECT COALESCE(SUM(sa.points_earned), 0) FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
            "    percentage = (SELECT COALESCE(SUM(sa.points_earned), 0) * 100.0 / 100 FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
            "    graded = TRUE, published = TRUE, status = 'completed' " +
            "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();
        printInfo("Student quiz graded: Session=" + studentQuizId);

        printSuccess("Full test data prepared");
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
                "INSERT INTO question_options (question_id, option_text, is_correct, option_order) VALUES (?, ?, ?, ?)"
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

