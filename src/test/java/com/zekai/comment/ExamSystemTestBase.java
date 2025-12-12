package com.zekai.comment;

import com.zekai.util.DatabaseUtil;
import org.junit.jupiter.api.*;

import java.sql.*;

/**
 * ========================================
 * EXAM SYSTEM TEST BASE - JUnit测试基础类
 * ========================================
 *
 * 提供所有JUnit测试共享的功能:
 * - 数据库连接管理
 * - 测试数据ID存储
 * - 表创建和清理
 *
 * @author Exam System Team
 * @version 2.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExamSystemTestBase {

    // 数据库连接
    protected static Connection conn;
    protected static PreparedStatement pstmt;
    protected static ResultSet rs;

    // 测试数据ID
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

    @BeforeAll
    public void setupAll() throws SQLException {
        resetTestData();
        initConnection();
        setupDatabase();
    }

    @AfterAll
    public void tearDownAll() {
        closeResources();
    }

    // ==================== 初始化方法 ====================

    protected void initConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(true);
        }
    }

    protected void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void resetTestData() throws SQLException {
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

    protected void setupDatabase() throws SQLException {
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

        executeUpdate("CREATE TABLE IF NOT EXISTS teachers (" +
                "teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT NOT NULL UNIQUE, " +
                "department VARCHAR(100), " +
                "hire_date DATE, " +
                "phone VARCHAR(20), " +
                "office VARCHAR(100), " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                "student_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT NOT NULL UNIQUE, " +
                "student_number VARCHAR(50) NOT NULL UNIQUE, " +
                "grade VARCHAR(20), " +
                "major VARCHAR(100), " +
                "enrollment_date DATE NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        executeUpdate("CREATE TABLE IF NOT EXISTS courses (" +
                "course_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "course_code VARCHAR(20) NOT NULL UNIQUE, " +
                "course_name VARCHAR(200) NOT NULL, " +
                "description TEXT, " +
                "credit_hours INT NOT NULL, " +
                "created_by BIGINT NOT NULL, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

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

        executeUpdate("CREATE TABLE IF NOT EXISTS enrollments (" +
                "enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "student_id BIGINT NOT NULL, " +
                "classroom_id BIGINT NOT NULL, " +
                "enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "status ENUM('active','dropped','completed') NOT NULL DEFAULT 'active', " +
                "final_grade DECIMAL(5,2), " +
                "UNIQUE KEY uk_student_class (student_id, classroom_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        executeUpdate("CREATE TABLE IF NOT EXISTS subjects (" +
                "subject_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "subject_name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "parent_subject_id BIGINT, " +
                "level INT NOT NULL DEFAULT 1, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

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

        executeUpdate("CREATE TABLE IF NOT EXISTS question_options (" +
                "option_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "question_id BIGINT NOT NULL, " +
                "option_text LONGTEXT NOT NULL, " +
                "is_correct BOOLEAN NOT NULL DEFAULT FALSE, " +
                "option_order INT NOT NULL, " +
                "UNIQUE KEY uk_question_order (question_id, option_order)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

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

        executeUpdate("CREATE TABLE IF NOT EXISTS quiz_questions (" +
                "quiz_question_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "quiz_id BIGINT NOT NULL, " +
                "question_id BIGINT NOT NULL, " +
                "question_order INT NOT NULL, " +
                "points INT NOT NULL, " +
                "UNIQUE KEY uk_quiz_question (quiz_id, question_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        executeUpdate("CREATE TABLE IF NOT EXISTS quiz_settings (" +
                "setting_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "quiz_id BIGINT NOT NULL UNIQUE, " +
                "shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE, " +
                "shuffle_options BOOLEAN NOT NULL DEFAULT FALSE, " +
                "show_results_immediately BOOLEAN NOT NULL DEFAULT FALSE, " +
                "allow_review BOOLEAN NOT NULL DEFAULT TRUE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

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
    }

    protected int executeUpdate(String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        int result = stmt.executeUpdate(sql);
        stmt.close();
        return result;
    }

    // ==================== 辅助方法 ====================

    protected void createTeacherAccount() throws SQLException {
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
    }

    protected void createStudentAccount() throws SQLException {
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
    }

    protected void createCourseAndClassroom() throws SQLException {
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
    }

    protected void enrollStudent() throws SQLException {
        pstmt = conn.prepareStatement(
            "INSERT INTO enrollments (student_id, classroom_id, status) VALUES (?, ?, 'active')",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) enrollmentId = rs.getLong(1);
    }

    protected void createSubject() throws SQLException {
        pstmt = conn.prepareStatement(
            "INSERT INTO subjects (subject_name, description, level) VALUES ('Data Structures', 'DS Topics', 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) subjectId = rs.getLong(1);
    }

    protected void createQuestionWithOptions(String questionText, String[] options, boolean[] correct) throws SQLException {
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

    protected long getLastQuestionId() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(question_id) FROM questions");
        long id = 0;
        if (rs.next()) id = rs.getLong(1);
        rs.close();
        stmt.close();
        return id;
    }

    protected void createQuiz() throws SQLException {
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
    }

    protected void addQuestionsToQuiz() throws SQLException {
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
    }
}

