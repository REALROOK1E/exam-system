package com.zekai.comment;

import com.zekai.util.DatabaseUtil;

import java.sql.*;

/**
 * ========================================
 * TEST BASE - 共享测试基础类
 * ========================================
 *
 * 提供所有测试文件共享的功能:
 * - 数据库连接管理
 * - 测试数据ID存储
 * - 通用工具方法
 * - 数据库初始化和清理
 *
 * @author Exam System Team
 * @version 2.0
 */
public class TestBase {

    // 数据库连接
    protected static Connection conn;
    protected static PreparedStatement pstmt;
    protected static ResultSet rs;

    // 测试数据ID (在测试过程中设置和使用)
    protected static long teacherUserId;
    protected static long teacherId;
    protected static long studentUserId;
    protected static long studentId;
    protected static long courseId;
    protected static long classroomId;
    protected static long subjectId;
    protected static long questionId1, questionId2, questionId3;
    protected static long quizId;
    protected static long studentQuizId;
    protected static long enrollmentId;

    // ==================== 初始化方法 ====================

    /**
     * 初始化数据库连接
     */
    public static void initConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
        }
    }

    /**
     * 提交事务
     */
    public static void commit() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.commit();
        }
    }

    /**
     * 回滚事务
     */
    public static void rollback() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭资源
     */
    public static void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置所有测试数据 - 清空数据库并重建表
     */
    public static void resetTestData() throws SQLException {
        try (Connection resetConn = DatabaseUtil.getConnection();
             Statement stmt = resetConn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            stmt.execute("DROP TABLE IF EXISTS student_answers, student_quizzes, quiz_settings, " +
                        "quiz_questions, quizzes, question_options, questions, subjects, " +
                        "enrollments, classrooms, courses, students, teachers, users");
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
        // 重置所有ID
        teacherUserId = teacherId = studentUserId = studentId = 0;
        courseId = classroomId = subjectId = 0;
        questionId1 = questionId2 = questionId3 = 0;
        quizId = studentQuizId = enrollmentId = 0;
    }

    /**
     * 创建所有数据库表
     */
    public static void setupDatabase() throws SQLException {
        printSectionHeader("SETUP: Creating Database Tables");

        executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                "user_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "role ENUM('admin','teacher','student') NOT NULL DEFAULT 'student', " +
                "is_active BOOLEAN NOT NULL DEFAULT TRUE, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "last_login TIMESTAMP NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ users table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS teachers (" +
                "teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT NOT NULL UNIQUE, " +
                "department VARCHAR(100), " +
                "hire_date DATE, " +
                "phone VARCHAR(20), " +
                "office VARCHAR(100), " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ teachers table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                "student_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT NOT NULL UNIQUE, " +
                "student_number VARCHAR(50) NOT NULL UNIQUE, " +
                "grade VARCHAR(20), " +
                "major VARCHAR(100), " +
                "enrollment_date DATE NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ students table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS courses (" +
                "course_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "course_code VARCHAR(20) NOT NULL UNIQUE, " +
                "course_name VARCHAR(200) NOT NULL, " +
                "description TEXT, " +
                "credit_hours INT NOT NULL, " +
                "created_by BIGINT NOT NULL, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ courses table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS classrooms (" +
                "classroom_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "course_id BIGINT NOT NULL, " +
                "teacher_id BIGINT NOT NULL, " +
                "class_name VARCHAR(50), " +
                "semester VARCHAR(20) NOT NULL, " +
                "year INT NOT NULL, " +
                "max_students INT NOT NULL DEFAULT 50, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ classrooms table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS enrollments (" +
                "enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "student_id BIGINT NOT NULL, " +
                "classroom_id BIGINT NOT NULL, " +
                "enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "status ENUM('active','dropped','completed') NOT NULL DEFAULT 'active', " +
                "final_grade DECIMAL(5,2), " +
                "UNIQUE KEY uk_student_class (student_id, classroom_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ enrollments table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS subjects (" +
                "subject_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "subject_name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "parent_subject_id BIGINT, " +
                "level INT NOT NULL DEFAULT 1, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ subjects table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS questions (" +
                "question_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "subject_id BIGINT NOT NULL, " +
                "question_text LONGTEXT NOT NULL, " +
                "question_type ENUM('multiple_choice','true_false','essay','fill_blank','short_answer') NOT NULL, " +
                "difficulty_level INT NOT NULL DEFAULT 3, " +
                "created_by BIGINT NOT NULL, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "times_used INT NOT NULL DEFAULT 0, " +
                "correct_count INT NOT NULL DEFAULT 0, " +
                "total_attempts INT NOT NULL DEFAULT 0, " +
                "is_deleted BOOLEAN NOT NULL DEFAULT FALSE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ questions table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS question_options (" +
                "option_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "question_id BIGINT NOT NULL, " +
                "option_text LONGTEXT NOT NULL, " +
                "is_correct BOOLEAN NOT NULL DEFAULT FALSE, " +
                "option_order INT NOT NULL, " +
                "UNIQUE KEY uk_question_order (question_id, option_order)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ question_options table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS quizzes (" +
                "quiz_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "classroom_id BIGINT NOT NULL, " +
                "title VARCHAR(200) NOT NULL, " +
                "description TEXT, " +
                "created_by BIGINT NOT NULL, " +
                "start_time DATETIME NOT NULL, " +
                "end_time DATETIME NOT NULL, " +
                "duration_minutes INT NOT NULL, " +
                "total_points INT NOT NULL DEFAULT 100, " +
                "passing_score INT NOT NULL DEFAULT 60, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ quizzes table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS quiz_questions (" +
                "quiz_question_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "quiz_id BIGINT NOT NULL, " +
                "question_id BIGINT NOT NULL, " +
                "question_order INT NOT NULL, " +
                "points INT NOT NULL, " +
                "UNIQUE KEY uk_quiz_question (quiz_id, question_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ quiz_questions table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS quiz_settings (" +
                "setting_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "quiz_id BIGINT NOT NULL UNIQUE, " +
                "shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE, " +
                "shuffle_options BOOLEAN NOT NULL DEFAULT FALSE, " +
                "show_results_immediately BOOLEAN NOT NULL DEFAULT FALSE, " +
                "allow_review BOOLEAN NOT NULL DEFAULT TRUE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ quiz_settings table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS student_quizzes (" +
                "student_quiz_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "quiz_id BIGINT NOT NULL, " +
                "student_id BIGINT NOT NULL, " +
                "start_time DATETIME NOT NULL, " +
                "submit_time DATETIME, " +
                "score DECIMAL(10,2), " +
                "percentage DECIMAL(5,2), " +
                "graded BOOLEAN NOT NULL DEFAULT FALSE, " +
                "published BOOLEAN NOT NULL DEFAULT FALSE, " +
                "status ENUM('in_progress','submitted','grading','completed') NOT NULL DEFAULT 'in_progress', " +
                "UNIQUE KEY uk_student_quiz (quiz_id, student_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ student_quizzes table created");

        executeUpdate("CREATE TABLE IF NOT EXISTS student_answers (" +
                "answer_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "student_quiz_id BIGINT NOT NULL, " +
                "question_id BIGINT NOT NULL, " +
                "selected_option_id BIGINT, " +
                "answer_text LONGTEXT, " +
                "is_correct BOOLEAN, " +
                "points_earned DECIMAL(10,2), " +
                "UNIQUE KEY uk_student_question (student_quiz_id, question_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ student_answers table created");

        System.out.println("\n✓ All 14 tables created successfully\n");
    }

    // ==================== 工具方法 ====================

    protected static int executeUpdate(String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        int result = stmt.executeUpdate(sql);
        stmt.close();
        return result;
    }

    protected static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(title);
        System.out.println("=".repeat(80) + "\n");
    }

    protected static void printSectionHeader(String title) {
        System.out.println("\n" + "─".repeat(80));
        System.out.println(title);
        System.out.println("─".repeat(80) + "\n");
    }

    protected static void printFeature(int number, String name) {
        if (number > 0) {
            System.out.println("┌─ FEATURE " + number + ": " + name);
        } else {
            System.out.println("┌─ " + name);
        }
        System.out.println("│");
    }

    protected static void printSuccess(String message) {
        System.out.println("  ✓ " + message + "\n");
    }

    protected static void printInfo(String message) {
        System.out.println("  → " + message);
    }
}
