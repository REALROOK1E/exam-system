package com.zekai.comment;

import org.junit.jupiter.api.*;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ========================================
 * ADMIN FEATURE TESTS - 管理后台功能JUnit测试
 * ========================================
 *
 * 包含7个管理后台功能的独立测试:
 * - Feature 33: 更新题目统计
 * - Feature 34: 自适应难度评级
 * - Feature 36: 题目使用排名
 * - Feature 37: 科目层级查询
 * - 用户管理
 * - 系统总览
 * - 数据维护
 *
 * @author Exam System Team
 * @version 2.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminFeatureTests extends ExamSystemTestBase {

    @BeforeAll
    @Override
    public void setupAll() throws SQLException {
        super.setupAll();
        // 准备完整测试数据
        prepareFullTestData();
    }

    // ==================================================================================
    // Feature 33: 更新题目统计 | Update Question Statistics
    // ==================================================================================

    @Test
    @Order(33)
    @DisplayName("Feature 33: 更新题目统计 | Update Question Statistics")
    void testFeature33_UpdateQuestionStatistics() throws SQLException {
        System.out.println("┌─ FEATURE 33: Update Question Statistics | 更新题目统计");

        pstmt = conn.prepareStatement(
            "UPDATE questions q " +
            "SET times_used = (SELECT COUNT(DISTINCT sa.student_quiz_id) FROM student_answers sa WHERE sa.question_id = q.question_id), " +
            "    total_attempts = (SELECT COUNT(*) FROM student_answers sa WHERE sa.question_id = q.question_id), " +
            "    correct_count = (SELECT COUNT(*) FROM student_answers sa WHERE sa.question_id = q.question_id AND sa.is_correct = TRUE) " +
            "WHERE q.question_id IN (SELECT DISTINCT question_id FROM student_answers)"
        );
        int updated = pstmt.executeUpdate();

        assertTrue(updated > 0, "应该有题目被更新");
        System.out.println("  → Statistics updated for " + updated + " questions");

        // 验证
        pstmt = conn.prepareStatement("SELECT question_id, times_used, total_attempts FROM questions WHERE times_used > 0");
        rs = pstmt.executeQuery();
        System.out.println("  → Updated Statistics:");
        while (rs.next()) {
            System.out.println("     • Q" + rs.getLong("question_id") +
                ": Used " + rs.getInt("times_used") + " times, " +
                rs.getInt("total_attempts") + " attempts");
        }

        System.out.println("  ✓ Question statistics updated\n");
    }

    // ==================================================================================
    // Feature 34: 自适应难度评级 | Adaptive Difficulty Rating
    // ==================================================================================

    @Test
    @Order(34)
    @DisplayName("Feature 34: 自适应难度评级 | Adaptive Difficulty Rating")
    void testFeature34_AdaptiveDifficultyRating() throws SQLException {
        System.out.println("┌─ FEATURE 34: Adaptive Difficulty Rating | 自适应难度评级");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.difficulty_level, q.total_attempts, q.correct_count, " +
            "CASE WHEN q.total_attempts = 0 THEN 'No Data' " +
            "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 80 THEN 'Too Easy' " +
            "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 60 THEN 'Appropriate' " +
            "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 40 THEN 'Slightly Hard' " +
            "     ELSE 'Too Hard' END as actual_difficulty " +
            "FROM questions q WHERE q.times_used > 0"
        );

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Adaptive Difficulty Analysis:");
        while (rs.next()) {
            count++;
            int attempts = rs.getInt("total_attempts");
            int correct = rs.getInt("correct_count");
            double rate = attempts > 0 ? (correct * 100.0 / attempts) : 0;

            System.out.println("     • Q" + rs.getLong("question_id"));
            System.out.println("       Preset: " + rs.getInt("difficulty_level") + "/5");
            System.out.println("       Correct Rate: " + String.format("%.1f", rate) + "%");
            System.out.println("       Rating: " + rs.getString("actual_difficulty"));
        }

        assertTrue(count > 0, "应该有题目分析数据");
        System.out.println("  ✓ Adaptive rating completed\n");
    }

    // ==================================================================================
    // Feature 36: 题目使用排名 | Question Usage Ranking
    // ==================================================================================

    @Test
    @Order(36)
    @DisplayName("Feature 36: 题目使用排名 | Question Usage Ranking")
    void testFeature36_QuestionUsageRanking() throws SQLException {
        System.out.println("┌─ FEATURE 36: Question Usage Ranking | 题目使用排名");

        pstmt = conn.prepareStatement(
            "SELECT q.question_id, q.question_text, q.times_used, q.total_attempts, " +
            "CASE WHEN q.total_attempts = 0 THEN NULL " +
            "     ELSE ROUND(q.correct_count * 100.0 / q.total_attempts, 2) END as correct_rate " +
            "FROM questions q " +
            "WHERE q.times_used > 0 AND q.is_deleted = FALSE " +
            "ORDER BY q.times_used DESC, q.total_attempts DESC LIMIT 5"
        );

        rs = pstmt.executeQuery();
        int rank = 0;
        System.out.println("  → Top Used Questions:");
        while (rs.next()) {
            rank++;
            String text = rs.getString("question_text");
            if (text.length() > 40) text = text.substring(0, 40) + "...";

            System.out.println("     " + rank + ". Q" + rs.getLong("question_id") + ": " + text);
            System.out.println("        Times Used: " + rs.getInt("times_used") +
                ", Attempts: " + rs.getInt("total_attempts"));
        }

        assertTrue(rank > 0, "应该有使用排名");
        System.out.println("  ✓ Usage ranking generated\n");
    }

    // ==================================================================================
    // Feature 37: 科目层级查询 | Subject Hierarchy Query
    // ==================================================================================

    @Test
    @Order(37)
    @DisplayName("Feature 37: 科目层级查询 | Subject Hierarchy Query")
    void testFeature37_SubjectHierarchyQuery() throws SQLException {
        System.out.println("┌─ FEATURE 37: Subject Hierarchy Query | 科目层级查询");

        pstmt = conn.prepareStatement(
            "SELECT s.subject_id, s.subject_name, s.level, " +
            "COUNT(q.question_id) as question_count " +
            "FROM subjects s " +
            "LEFT JOIN questions q ON s.subject_id = q.subject_id AND q.is_deleted = FALSE " +
            "GROUP BY s.subject_id ORDER BY s.level, s.subject_id"
        );

        rs = pstmt.executeQuery();
        int count = 0;
        System.out.println("  → Subject Hierarchy:");
        while (rs.next()) {
            count++;
            int level = rs.getInt("level");
            String indent = "  ".repeat(level);
            System.out.println("     " + indent + "• " + rs.getString("subject_name") +
                " (Level " + level + ")");
            System.out.println("     " + indent + "  Questions: " + rs.getInt("question_count"));
        }

        assertTrue(count > 0, "应该有科目");
        System.out.println("  ✓ Hierarchy query completed\n");
    }

    // ==================================================================================
    // 管理功能: 用户管理 | User Management
    // ==================================================================================

    @Test
    @Order(100)
    @DisplayName("Admin: 用户管理 | User Management")
    void testAdminFeature_UserManagement() throws SQLException {
        System.out.println("┌─ Admin: User Management | 管理员: 用户管理");

        // 查看所有用户
        pstmt = conn.prepareStatement(
            "SELECT user_id, username, role, full_name, is_active FROM users ORDER BY created_at DESC"
        );

        rs = pstmt.executeQuery();
        int userCount = 0;
        System.out.println("  → All Users:");
        while (rs.next()) {
            userCount++;
            System.out.println("     • ID: " + rs.getLong("user_id") +
                " | " + rs.getString("username") +
                " (" + rs.getString("role") + ")");
        }

        assertTrue(userCount >= 2, "应该至少有2个用户");

        // 用户统计
        pstmt = conn.prepareStatement("SELECT role, COUNT(*) as count FROM users GROUP BY role");
        rs = pstmt.executeQuery();
        System.out.println("  → User Statistics:");
        while (rs.next()) {
            System.out.println("     • " + rs.getString("role") + ": " + rs.getInt("count") + " users");
        }

        // 演示禁用/启用
        if (studentUserId > 0) {
            executeUpdate("UPDATE users SET is_active = FALSE WHERE user_id = " + studentUserId);
            System.out.println("  → User " + studentUserId + " disabled (demo)");

            executeUpdate("UPDATE users SET is_active = TRUE WHERE user_id = " + studentUserId);
            System.out.println("  → User " + studentUserId + " re-enabled");
        }

        System.out.println("  ✓ User management completed\n");
    }

    // ==================================================================================
    // 管理功能: 系统总览 | System Overview
    // ==================================================================================

    @Test
    @Order(101)
    @DisplayName("Admin: 系统总览 | System Overview")
    void testAdminFeature_SystemOverview() throws SQLException {
        System.out.println("┌─ Admin: System Overview | 管理员: 系统总览");

        System.out.println("  → System Statistics:");

        String[][] stats = {
            {"users", "Total Users"},
            {"teachers", "Total Teachers"},
            {"students", "Total Students"},
            {"courses", "Total Courses"},
            {"classrooms", "Total Classrooms"},
            {"subjects", "Total Subjects"},
            {"questions", "Total Questions"},
            {"quizzes", "Total Quizzes"},
            {"student_quizzes", "Total Quiz Attempts"}
        };

        for (String[] stat : stats) {
            String sql = "SELECT COUNT(*) as cnt FROM " + stat[0];
            if (stat[0].equals("questions")) {
                sql += " WHERE is_deleted = FALSE";
            }
            rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt("cnt");
                System.out.println("     • " + stat[1] + ": " + count);
                assertTrue(count >= 0, stat[1] + " 不应该为负数");
            }
        }

        System.out.println("  ✓ System overview generated\n");
    }

    // ==================================================================================
    // 管理功能: 数据维护 | Data Maintenance
    // ==================================================================================

    @Test
    @Order(102)
    @DisplayName("Admin: 数据维护 | Data Maintenance")
    void testAdminFeature_DataMaintenance() throws SQLException {
        System.out.println("┌─ Admin: Data Maintenance | 管理员: 数据维护");

        // 演示软删除
        if (questionId1 > 0) {
            executeUpdate("UPDATE questions SET is_deleted = TRUE WHERE question_id = " + questionId1);
            System.out.println("  → Question " + questionId1 + " soft-deleted (demo)");

            // 验证软删除
            pstmt = conn.prepareStatement("SELECT is_deleted FROM questions WHERE question_id = ?");
            pstmt.setLong(1, questionId1);
            rs = pstmt.executeQuery();
            assertTrue(rs.next(), "应该能查询到记录");
            assertTrue(rs.getBoolean("is_deleted"), "应该被标记为已删除");

            // 恢复
            executeUpdate("UPDATE questions SET is_deleted = FALSE WHERE question_id = " + questionId1);
            System.out.println("  → Question " + questionId1 + " restored");
        }

        // 显示表统计
        System.out.println("  → Table Statistics:");
        String[] tables = {"users", "teachers", "students", "courses", "classrooms",
                          "subjects", "questions", "quizzes", "student_quizzes", "student_answers"};

        for (String table : tables) {
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) as cnt FROM " + table);
            if (rs.next()) {
                System.out.println("     • " + table + ": " + rs.getInt("cnt") + " rows");
            }
        }

        System.out.println("  ✓ Data maintenance completed\n");
    }

    // ==================================================================================
    // 辅助方法 - 准备完整测试数据
    // ==================================================================================

    private void prepareFullTestData() throws SQLException {
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
        System.out.println("Preparing Full Test Data");
        System.out.println("────────────────────────────────────────────────────────────────────────────────\n");

        // 1. 创建教师
        createTeacherAccount();
        System.out.println("  → Teacher created: ID=" + teacherId);

        // 2. 创建学生
        createStudentAccount();
        System.out.println("  → Student created: ID=" + studentId);

        // 3. 创建课程和教室
        createCourseAndClassroom();
        System.out.println("  → Course and Classroom created");

        // 4. 学生注册
        enrollStudent();

        // 5. 创建科目（包含子科目）
        createSubject();
        pstmt = conn.prepareStatement(
            "INSERT INTO subjects (subject_name, description, level, parent_subject_id) VALUES ('Trees', 'Tree structures', 2, ?)"
        );
        pstmt.setLong(1, subjectId);
        pstmt.executeUpdate();
        System.out.println("  → Subjects created");

        // 6. 创建题目
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
        System.out.println("  → Questions created: Q" + questionId1 + ", Q" + questionId2 + ", Q" + questionId3);

        // 7. 创建测验
        createQuiz();
        addQuestionsToQuiz();
        System.out.println("  → Quiz created: ID=" + quizId);

        // 8. 创建学生答题记录
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

        // 9. 自动评分
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

        // 计算总分
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

        System.out.println("  → Student quiz graded: Session=" + studentQuizId);
        System.out.println("\n  ✓ Full test data prepared\n");
    }
}

