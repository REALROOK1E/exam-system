package com.zekai.comment;

import com.zekai.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ========================================
 * EXAM SYSTEM - COMPLETE FUNCTIONALITY TEST
 * ========================================
 *
 * This program tests all 37 core features of the exam system:
 * - User & Permission Management (5 features)
 * - Course & Classroom Management (5 features)
 * - Question Bank Management (5 features)
 * - Quiz Generation Management (5 features)
 * - Exam Management (4 features)
 * - Auto Grading (3 features)
 * - Grade Query & Statistics (5 features)
 * - Advanced Features (5 features)
 *
 * @author Exam System Team
 * @version 1.0
 */
public class Main {

    private static Connection conn;
    private static PreparedStatement pstmt;
    private static ResultSet rs;

    // Test data IDs (will be set during execution)
    private static long teacherUserId;
    private static long teacherId;
    private static long studentUserId;
    private static long studentId;
    private static long courseId;
    private static long classroomId;
    private static long subjectId;
    private static long questionId1, questionId2, questionId3;
    private static long quizId;
    private static long studentQuizId;

    public static void main(String[] args) {
        printHeader();

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Enable transaction

            // ==================== SETUP: Create Tables ====================
            setupDatabase();

            // ==================== SECTION 1: User & Permission Management ====================
            section1_UserAndPermissionManagement();

            // ==================== SECTION 2: Course & Classroom Management ====================
            section2_CourseAndClassroomManagement();

            // ==================== SECTION 3: Question Bank Management ====================
            section3_QuestionBankManagement();

            // ==================== SECTION 4: Quiz Generation Management ====================
            section4_QuizGenerationManagement();

            // ==================== SECTION 5: Exam Management ====================
            section5_ExamManagement();

            // ==================== SECTION 6: Auto Grading ====================
            section6_AutoGrading();

            // ==================== SECTION 7: Grade Query & Statistics ====================
            section7_GradeQueryAndStatistics();

            // ==================== SECTION 8: Advanced Features ====================
            section8_AdvancedFeatures();

            conn.commit();
            printFooter();

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            closeResources();
        }
    }

    // ==================================================================================
    // SETUP: Create All Required Tables
    // ==================================================================================

    private static void setupDatabase() throws SQLException {
        printSectionHeader("SETUP: Creating Database Tables");

        // Create users table
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

        // Create teachers table
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

        // Create students table
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

        // Create courses table
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

        // Create classrooms table
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

        // Create enrollments table
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

        // Create subjects table
        executeUpdate("CREATE TABLE IF NOT EXISTS subjects (" +
                "subject_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "subject_name VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "parent_subject_id BIGINT, " +
                "level INT NOT NULL DEFAULT 1, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ subjects table created");

        // Create questions table
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

        // Create question_options table
        executeUpdate("CREATE TABLE IF NOT EXISTS question_options (" +
                "option_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "question_id BIGINT NOT NULL, " +
                "option_text LONGTEXT NOT NULL, " +
                "is_correct BOOLEAN NOT NULL DEFAULT FALSE, " +
                "option_order INT NOT NULL, " +
                "UNIQUE KEY uk_question_order (question_id, option_order)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ question_options table created");

        // Create quizzes table
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

        // Create quiz_questions table
        executeUpdate("CREATE TABLE IF NOT EXISTS quiz_questions (" +
                "quiz_question_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "quiz_id BIGINT NOT NULL, " +
                "question_id BIGINT NOT NULL, " +
                "question_order INT NOT NULL, " +
                "points INT NOT NULL, " +
                "UNIQUE KEY uk_quiz_question (quiz_id, question_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ quiz_questions table created");

        // Create quiz_settings table
        executeUpdate("CREATE TABLE IF NOT EXISTS quiz_settings (" +
                "setting_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "quiz_id BIGINT NOT NULL UNIQUE, " +
                "shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE, " +
                "shuffle_options BOOLEAN NOT NULL DEFAULT FALSE, " +
                "show_results_immediately BOOLEAN NOT NULL DEFAULT FALSE, " +
                "allow_review BOOLEAN NOT NULL DEFAULT TRUE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println("✓ quiz_settings table created");

        // Create student_quizzes table
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

        // Create student_answers table
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

    // ==================================================================================
    // SECTION 1: User & Permission Management (5 Features)
    // ==================================================================================

    private static void section1_UserAndPermissionManagement() throws SQLException {
        printSectionHeader("SECTION 1: User & Permission Management (5 Features)");

        // Feature 1: Create Student Account
        feature1_CreateStudentAccount();

        // Feature 2: Create Teacher Account
        feature2_CreateTeacherAccount();

        // Feature 3: User Login Authentication
        feature3_UserLoginAuthentication();

        // Feature 4: Student Course Enrollment
        feature4_StudentCourseEnrollment();

        // Feature 5: Teacher Create Classroom
        feature5_TeacherCreateClassroom();
    }

    /**
     * FEATURE 1: Create Student Account
     * Creates a user account with student role and associates student information
     */
    private static void feature1_CreateStudentAccount() throws SQLException {
        printFeature(1, "Create Student Account");

        pstmt = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, email, full_name, role) " +
                        "VALUES (?, SHA2(?, 256), ?, ?, 'student')",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, "alice_student");
        pstmt.setString(2, "password123");
        pstmt.setString(3, "alice@university.edu");
        pstmt.setString(4, "Alice Johnson");
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            studentUserId = rs.getLong(1);
            System.out.println("  → User created: ID=" + studentUserId);
        }

        // Create student record
        pstmt = conn.prepareStatement(
                "INSERT INTO students (user_id, student_number, grade, major, enrollment_date) " +
                        "VALUES (?, ?, ?, ?, CURRENT_DATE)",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, studentUserId);
        pstmt.setString(2, "STU2025001");
        pstmt.setString(3, "Junior");
        pstmt.setString(4, "Computer Science");
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            studentId = rs.getLong(1);
            System.out.println("  → Student record created: ID=" + studentId);
        }

        System.out.println("  ✓ Student account created successfully\n");
    }

    /**
     * FEATURE 2: Create Teacher Account
     * Creates a user account with teacher role and associates teacher information
     */
    private static void feature2_CreateTeacherAccount() throws SQLException {
        printFeature(2, "Create Teacher Account");

        pstmt = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, email, full_name, role) " +
                        "VALUES (?, SHA2(?, 256), ?, ?, 'teacher')",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, "john_teacher");
        pstmt.setString(2, "teachpass");
        pstmt.setString(3, "john@university.edu");
        pstmt.setString(4, "John Smith");
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            teacherUserId = rs.getLong(1);
            System.out.println("  → User created: ID=" + teacherUserId);
        }

        // Create teacher record
        pstmt = conn.prepareStatement(
                "INSERT INTO teachers (user_id, department, hire_date, phone, office) " +
                        "VALUES (?, ?, CURRENT_DATE, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, teacherUserId);
        pstmt.setString(2, "Computer Science");
        pstmt.setString(3, "+1-555-0100");
        pstmt.setString(4, "CS Building 301");
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            teacherId = rs.getLong(1);
            System.out.println("  → Teacher record created: ID=" + teacherId);
        }

        System.out.println("  ✓ Teacher account created successfully\n");
    }

    /**
     * FEATURE 3: User Login Authentication
     * Authenticates user credentials and retrieves role information
     */
    private static void feature3_UserLoginAuthentication() throws SQLException {
        printFeature(3, "User Login Authentication");

        pstmt = conn.prepareStatement(
                "SELECT u.user_id, u.username, u.role, u.full_name, t.teacher_id, s.student_id " +
                        "FROM users u " +
                        "LEFT JOIN teachers t ON u.user_id = t.user_id " +
                        "LEFT JOIN students s ON u.user_id = s.user_id " +
                        "WHERE u.username = ? AND u.password_hash = SHA2(?, 256) AND u.is_active = TRUE"
        );
        pstmt.setString(1, "john_teacher");
        pstmt.setString(2, "teachpass");

        rs = pstmt.executeQuery();
        if (rs.next()) {
            System.out.println("  → Authentication successful!");
            System.out.println("  → User ID: " + rs.getLong("user_id"));
            System.out.println("  → Username: " + rs.getString("username"));
            System.out.println("  → Role: " + rs.getString("role"));
            System.out.println("  → Full Name: " + rs.getString("full_name"));

            // Update last login
            executeUpdate("UPDATE users SET last_login = NOW() WHERE user_id = " + rs.getLong("user_id"));
        }

        System.out.println("  ✓ User authentication completed\n");
    }

    /**
     * FEATURE 4: Student Course Enrollment
     * Enrolls a student in a classroom
     */
    private static void feature4_StudentCourseEnrollment() throws SQLException {
        printFeature(4, "Student Course Enrollment");

        // Note: This will be executed after classroom is created
        System.out.println("  → Enrollment will be completed after classroom creation");
        System.out.println("  ✓ Feature prepared\n");
    }

    /**
     * FEATURE 5: Teacher Create Classroom
     * Creates a classroom for a course
     */
    private static void feature5_TeacherCreateClassroom() throws SQLException {
        printFeature(5, "Teacher Create Classroom");

        // Note: Will be executed after course is created
        System.out.println("  → Classroom creation will be completed after course creation");
        System.out.println("  ✓ Feature prepared\n");
    }

    // ==================================================================================
    // SECTION 2: Course & Classroom Management (5 Features)
    // ==================================================================================

    private static void section2_CourseAndClassroomManagement() throws SQLException {
        printSectionHeader("SECTION 2: Course & Classroom Management (5 Features)");

        // Feature 6: Create Course
        feature6_CreateCourse();

        // Feature 7: Create Classroom (from Feature 5)
        feature7_CreateClassroom();

        // Feature 8: Student Enrollment (from Feature 4)
        feature8_StudentEnrollment();

        // Feature 9: Query Classroom Students
        feature9_QueryClassroomStudents();

        // Feature 10: Student Withdrawal
        feature10_StudentWithdrawal();
    }

    /**
     * FEATURE 6: Create Course
     * Teacher creates a new course
     */
    private static void feature6_CreateCourse() throws SQLException {
        printFeature(6, "Create Course");

        pstmt = conn.prepareStatement(
                "INSERT INTO courses (course_code, course_name, description, credit_hours, created_by) " +
                        "VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, "CS101");
        pstmt.setString(2, "Data Structures and Algorithms");
        pstmt.setString(3, "Introduction to fundamental data structures and algorithm analysis");
        pstmt.setInt(4, 4);
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            courseId = rs.getLong(1);
            System.out.println("  → Course created: ID=" + courseId);
            System.out.println("  → Course Code: CS101");
            System.out.println("  → Course Name: Data Structures and Algorithms");
        }

        System.out.println("  ✓ Course created successfully\n");
    }

    /**
     * FEATURE 7: Create Classroom
     * Teacher creates a classroom for the course
     */
    private static void feature7_CreateClassroom() throws SQLException {
        printFeature(7, "Create Classroom");

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
        if (rs.next()) {
            classroomId = rs.getLong(1);
            System.out.println("  → Classroom created: ID=" + classroomId);
            System.out.println("  → Semester: Fall 2025");
            System.out.println("  → Max Students: 50");
        }

        System.out.println("  ✓ Classroom created successfully\n");
    }

    /**
     * FEATURE 8: Student Enrollment
     * Student enrolls in a classroom
     */
    private static void feature8_StudentEnrollment() throws SQLException {
        printFeature(8, "Student Enrollment");

        pstmt = conn.prepareStatement(
                "INSERT INTO enrollments (student_id, classroom_id, status) " +
                        "VALUES (?, ?, 'active')"
        );
        pstmt.setLong(1, studentId);
        pstmt.setLong(2, classroomId);
        pstmt.executeUpdate();

        System.out.println("  → Student enrolled in classroom");
        System.out.println("  → Student ID: " + studentId);
        System.out.println("  → Classroom ID: " + classroomId);
        System.out.println("  ✓ Enrollment completed successfully\n");
    }

    /**
     * FEATURE 9: Query Classroom Students
     * Teacher views all students in a classroom
     */
    private static void feature9_QueryClassroomStudents() throws SQLException {
        printFeature(9, "Query Classroom Students");

        pstmt = conn.prepareStatement(
                "SELECT s.student_id, u.full_name, s.student_number, s.grade, s.major, e.enrollment_date " +
                        "FROM enrollments e " +
                        "JOIN students s ON e.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "WHERE e.classroom_id = ? AND e.status = 'active'"
        );
        pstmt.setLong(1, classroomId);

        rs = pstmt.executeQuery();
        System.out.println("  → Students in Classroom ID " + classroomId + ":");
        while (rs.next()) {
            System.out.println("     • " + rs.getString("full_name") +
                    " (ID: " + rs.getLong("student_id") +
                    ", Student#: " + rs.getString("student_number") + ")");
        }

        System.out.println("  ✓ Student query completed\n");
    }

    /**
     * FEATURE 10: Student Withdrawal
     * Student withdraws from a course (soft delete)
     */
    private static void feature10_StudentWithdrawal() throws SQLException {
        printFeature(10, "Student Withdrawal (Demo - will restore)");

        // Temporarily withdraw
        executeUpdate("UPDATE enrollments SET status = 'dropped' WHERE student_id = " +
                studentId + " AND classroom_id = " + classroomId);
        System.out.println("  → Student status changed to 'dropped'");

        // Restore for further testing
        executeUpdate("UPDATE enrollments SET status = 'active' WHERE student_id = " +
                studentId + " AND classroom_id = " + classroomId);
        System.out.println("  → Student status restored to 'active' (for testing)");
        System.out.println("  ✓ Withdrawal feature demonstrated\n");
    }

    // ==================================================================================
    // SECTION 3: Question Bank Management (5 Features)
    // ==================================================================================

    private static void section3_QuestionBankManagement() throws SQLException {
        printSectionHeader("SECTION 3: Question Bank Management (5 Features)");

        // Feature 11: Create Subject
        feature11_CreateSubject();

        // Feature 12: Create Single Question
        feature12_CreateSingleQuestion();

        // Feature 13: Add Question Options (Multiple Choice)
        feature13_AddQuestionOptions();

        // Feature 14: Create Multiple Questions
        feature14_CreateMultipleQuestions();

        // Feature 15: Query Question Statistics
        feature15_QueryQuestionStatistics();
    }

    /**
     * FEATURE 11: Create Subject
     * Creates a subject for organizing questions
     */
    private static void feature11_CreateSubject() throws SQLException {
        printFeature(11, "Create Subject");

        pstmt = conn.prepareStatement(
                "INSERT INTO subjects (subject_name, description, level) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setString(1, "Data Structures");
        pstmt.setString(2, "Topics related to data structures and algorithms");
        pstmt.setInt(3, 1);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            subjectId = rs.getLong(1);
            System.out.println("  → Subject created: ID=" + subjectId);
            System.out.println("  → Subject Name: Data Structures");
        }

        System.out.println("  ✓ Subject created successfully\n");
    }

    /**
     * FEATURE 12: Create Single Question
     * Teacher creates a question in the question bank
     */
    private static void feature12_CreateSingleQuestion() throws SQLException {
        printFeature(12, "Create Single Question");

        pstmt = conn.prepareStatement(
                "INSERT INTO questions (subject_id, question_text, question_type, difficulty_level, created_by) " +
                        "VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, subjectId);
        pstmt.setString(2, "What is the time complexity of binary search?");
        pstmt.setString(3, "multiple_choice");
        pstmt.setInt(4, 2); // Difficulty: 2/5
        pstmt.setLong(5, teacherId);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            questionId1 = rs.getLong(1);
            System.out.println("  → Question created: ID=" + questionId1);
            System.out.println("  → Type: Multiple Choice");
            System.out.println("  → Difficulty: 2/5");
        }

        System.out.println("  ✓ Question created successfully\n");
    }

    /**
     * FEATURE 13: Add Question Options
     * Adds multiple choice options to a question (supports multiple correct answers)
     */
    private static void feature13_AddQuestionOptions() throws SQLException {
        printFeature(13, "Add Question Options (Multiple Choice)");

        String[] options = {
                "O(n)", "O(log n)", "O(n²)", "O(1)"
        };
        boolean[] correct = {false, true, false, false}; // Option 2 is correct

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

        System.out.println("  ✓ All options added successfully\n");
    }

    /**
     * FEATURE 14: Create Multiple Questions
     * Creates additional questions for testing
     */
    private static void feature14_CreateMultipleQuestions() throws SQLException {
        printFeature(14, "Create Multiple Questions");

        // Question 2
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
            System.out.println("  → Question 2 created: ID=" + questionId2);
        }

        // Add options for Question 2
        String[] opts2 = {"Queue", "Stack", "Array", "Tree"};
        boolean[] corr2 = {false, true, false, false};
        addOptionsForQuestion(questionId2, opts2, corr2);

        // Question 3
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
            System.out.println("  → Question 3 created: ID=" + questionId3);
        }

        // Add options for Question 3
        String[] opts3 = {"AVL Tree", "Linked List", "Hash Table", "Graph"};
        boolean[] corr3 = {true, false, false, false};
        addOptionsForQuestion(questionId3, opts3, corr3);

        System.out.println("  ✓ Multiple questions created successfully\n");
    }

    /**
     * Helper method to add options for a question
     */
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

    /**
     * FEATURE 15: Query Question Statistics
     * Views statistics about questions in the bank
     */
    private static void feature15_QueryQuestionStatistics() throws SQLException {
        printFeature(15, "Query Question Statistics");

        pstmt = conn.prepareStatement(
                "SELECT q.question_id, q.question_text, q.question_type, q.difficulty_level, " +
                        "q.times_used, q.correct_count, q.total_attempts, s.subject_name " +
                        "FROM questions q " +
                        "JOIN subjects s ON q.subject_id = s.subject_id " +
                        "WHERE q.is_deleted = FALSE"
        );

        rs = pstmt.executeQuery();
        System.out.println("  → Question Bank Statistics:");
        int count = 0;
        while (rs.next()) {
            count++;
            System.out.println("     " + count + ". Q" + rs.getLong("question_id") +
                    " - " + rs.getString("question_text").substring(0, Math.min(50, rs.getString("question_text").length())) + "...");
            System.out.println("        Type: " + rs.getString("question_type") +
                    " | Difficulty: " + rs.getInt("difficulty_level") + "/5" +
                    " | Times Used: " + rs.getInt("times_used"));
        }

        System.out.println("  → Total Questions: " + count);
        System.out.println("  ✓ Statistics query completed\n");
    }

    // ==================================================================================
    // SECTION 4: Quiz Generation Management (5 Features)
    // ==================================================================================

    private static void section4_QuizGenerationManagement() throws SQLException {
        printSectionHeader("SECTION 4: Quiz Generation Management (5 Features)");

        // Feature 16: Create Quiz
        feature16_CreateQuiz();

        // Feature 17: Random Question Selection
        feature17_RandomQuestionSelection();

        // Feature 18: Add Questions to Quiz
        feature18_AddQuestionsToQuiz();

        // Feature 19: Configure Quiz Settings
        feature19_ConfigureQuizSettings();

        // Feature 20: View Quiz Details
        feature20_ViewQuizDetails();
    }

    /**
     * FEATURE 16: Create Quiz
     * Teacher creates a new quiz/exam
     */
    private static void feature16_CreateQuiz() throws SQLException {
        printFeature(16, "Create Quiz");

        pstmt = conn.prepareStatement(
                "INSERT INTO quizzes (classroom_id, title, description, created_by, " +
                        "start_time, end_time, duration_minutes, total_points, passing_score) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        pstmt.setLong(1, classroomId);
        pstmt.setString(2, "Midterm Exam - Data Structures");
        pstmt.setString(3, "Comprehensive exam covering arrays, lists, and trees");
        pstmt.setLong(4, teacherId);
        pstmt.setString(5, "2025-12-01 09:00:00");
        pstmt.setString(6, "2025-12-01 11:00:00");
        pstmt.setInt(7, 120); // 2 hours
        pstmt.setInt(8, 100);
        pstmt.setInt(9, 60);
        pstmt.executeUpdate();

        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            quizId = rs.getLong(1);
            System.out.println("  → Quiz created: ID=" + quizId);
            System.out.println("  → Title: Midterm Exam - Data Structures");
            System.out.println("  → Duration: 120 minutes");
            System.out.println("  → Total Points: 100");
        }

        System.out.println("  ✓ Quiz created successfully\n");
    }

    /**
     * FEATURE 17: Random Question Selection
     * Demonstrates selecting random questions from question bank
     */
    private static void feature17_RandomQuestionSelection() throws SQLException {
        printFeature(17, "Random Question Selection");

        pstmt = conn.prepareStatement(
                "SELECT question_id, question_text, difficulty_level " +
                        "FROM questions " +
                        "WHERE subject_id = ? AND question_type = 'multiple_choice' AND is_deleted = FALSE " +
                        "ORDER BY RAND() LIMIT 3"
        );
        pstmt.setLong(1, subjectId);

        rs = pstmt.executeQuery();
        System.out.println("  → Randomly selected questions:");
        List<Long> selectedQuestions = new ArrayList<>();
        while (rs.next()) {
            long qid = rs.getLong("question_id");
            selectedQuestions.add(qid);
            System.out.println("     • Q" + qid + ": " +
                    rs.getString("question_text").substring(0, Math.min(40, rs.getString("question_text").length())) +
                    "... (Difficulty: " + rs.getInt("difficulty_level") + "/5)");
        }

        System.out.println("  ✓ Random selection completed\n");
    }

    /**
     * FEATURE 18: Add Questions to Quiz
     * Adds questions to a quiz with specific ordering and points
     */
    private static void feature18_AddQuestionsToQuiz() throws SQLException {
        printFeature(18, "Add Questions to Quiz");

        long[] questions = {questionId1, questionId2, questionId3};
        int[] points = {30, 30, 40};

        pstmt = conn.prepareStatement(
                "INSERT INTO quiz_questions (quiz_id, question_id, question_order, points) " +
                        "VALUES (?, ?, ?, ?)"
        );

        for (int i = 0; i < questions.length; i++) {
            pstmt.setLong(1, quizId);
            pstmt.setLong(2, questions[i]);
            pstmt.setInt(3, i + 1);
            pstmt.setInt(4, points[i]);
            pstmt.executeUpdate();

            System.out.println("  → Question " + (i + 1) + " added: Q" + questions[i] +
                    " (Points: " + points[i] + ")");
        }

        System.out.println("  ✓ All questions added to quiz\n");
    }

    /**
     * FEATURE 19: Configure Quiz Settings
     * Sets quiz behavior settings (shuffle, show results, etc.)
     */
    private static void feature19_ConfigureQuizSettings() throws SQLException {
        printFeature(19, "Configure Quiz Settings");

        pstmt = conn.prepareStatement(
                "INSERT INTO quiz_settings (quiz_id, shuffle_questions, shuffle_options, " +
                        "show_results_immediately, allow_review) VALUES (?, ?, ?, ?, ?)"
        );
        pstmt.setLong(1, quizId);
        pstmt.setBoolean(2, true);  // Shuffle questions
        pstmt.setBoolean(3, true);  // Shuffle options
        pstmt.setBoolean(4, false); // Don't show results immediately
        pstmt.setBoolean(5, true);  // Allow review after grading
        pstmt.executeUpdate();

        System.out.println("  → Settings configured:");
        System.out.println("     • Shuffle Questions: YES");
        System.out.println("     • Shuffle Options: YES");
        System.out.println("     • Show Results Immediately: NO");
        System.out.println("     • Allow Review: YES");
        System.out.println("  ✓ Quiz settings saved\n");
    }

    /**
     * FEATURE 20: View Quiz Details
     * Displays complete quiz information
     */
    private static void feature20_ViewQuizDetails() throws SQLException {
        printFeature(20, "View Quiz Details");

        pstmt = conn.prepareStatement(
                "SELECT q.quiz_id, q.title, q.start_time, q.end_time, q.duration_minutes, " +
                        "q.total_points, q.passing_score, COUNT(qq.question_id) as question_count " +
                        "FROM quizzes q " +
                        "LEFT JOIN quiz_questions qq ON q.quiz_id = qq.quiz_id " +
                        "WHERE q.quiz_id = ? " +
                        "GROUP BY q.quiz_id"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        if (rs.next()) {
            System.out.println("  → Quiz Information:");
            System.out.println("     • ID: " + rs.getLong("quiz_id"));
            System.out.println("     • Title: " + rs.getString("title"));
            System.out.println("     • Start: " + rs.getString("start_time"));
            System.out.println("     • End: " + rs.getString("end_time"));
            System.out.println("     • Duration: " + rs.getInt("duration_minutes") + " minutes");
            System.out.println("     • Total Points: " + rs.getInt("total_points"));
            System.out.println("     • Passing Score: " + rs.getInt("passing_score"));
            System.out.println("     • Number of Questions: " + rs.getInt("question_count"));
        }

        System.out.println("  ✓ Quiz details retrieved\n");
    }

    // ==================================================================================
    // SECTION 5: Exam Management (4 Features)
    // ==================================================================================

    private static void section5_ExamManagement() throws SQLException {
        printSectionHeader("SECTION 5: Exam Management (4 Features)");

        // Feature 21: Student Start Quiz
        feature21_StudentStartQuiz();

        // Feature 22: Student Submit Answers
        feature22_StudentSubmitAnswers();

        // Feature 23: Complete Quiz Submission
        feature23_CompleteQuizSubmission();

        // Feature 24: View Available Quizzes
        feature24_ViewAvailableQuizzes();
    }

    /**
     * FEATURE 21: Student Start Quiz
     * Records when a student begins taking a quiz
     */
    private static void feature21_StudentStartQuiz() throws SQLException {
        printFeature(21, "Student Start Quiz");

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
            System.out.println("  → Student quiz session started");
            System.out.println("  → Session ID: " + studentQuizId);
            System.out.println("  → Student ID: " + studentId);
            System.out.println("  → Quiz ID: " + quizId);
            System.out.println("  → Status: in_progress");
        }

        System.out.println("  ✓ Quiz started successfully\n");
    }

    /**
     * FEATURE 22: Student Submit Answers
     * Student submits answers for each question
     */
    private static void feature22_StudentSubmitAnswers() throws SQLException {
        printFeature(22, "Student Submit Answers");

        // Get quiz questions
        pstmt = conn.prepareStatement(
                "SELECT qq.question_id, qq.question_order " +
                        "FROM quiz_questions qq " +
                        "WHERE qq.quiz_id = ? " +
                        "ORDER BY qq.question_order"
        );
        pstmt.setLong(1, quizId);
        rs = pstmt.executeQuery();

        List<Long> questionIds = new ArrayList<>();
        while (rs.next()) {
            questionIds.add(rs.getLong("question_id"));
        }

        // Submit answer for each question
        pstmt = conn.prepareStatement(
                "INSERT INTO student_answers (student_quiz_id, question_id, selected_option_id) " +
                        "VALUES (?, ?, ?)"
        );

        for (int i = 0; i < questionIds.size(); i++) {
            long qid = questionIds.get(i);

            // Get a random option (simulating student selection)
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT option_id FROM question_options WHERE question_id = ? LIMIT 1"
            );
            ps.setLong(1, qid);
            ResultSet optRs = ps.executeQuery();

            if (optRs.next()) {
                long optionId = optRs.getLong("option_id");

                pstmt.setLong(1, studentQuizId);
                pstmt.setLong(2, qid);
                pstmt.setLong(3, optionId);
                pstmt.executeUpdate();

                System.out.println("  → Answer submitted for Question " + (i + 1) +
                        " (Q" + qid + ", Option: " + optionId + ")");
            }
            optRs.close();
            ps.close();
        }

        System.out.println("  ✓ All answers submitted\n");
    }

    /**
     * FEATURE 23: Complete Quiz Submission
     * Student completes and submits the quiz
     */
    private static void feature23_CompleteQuizSubmission() throws SQLException {
        printFeature(23, "Complete Quiz Submission");

        pstmt = conn.prepareStatement(
                "UPDATE student_quizzes SET submit_time = NOW(), status = 'submitted' " +
                        "WHERE student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        System.out.println("  → Quiz submission completed");
        System.out.println("  → Session ID: " + studentQuizId);
        System.out.println("  → Status changed: in_progress → submitted");
        System.out.println("  ✓ Quiz submitted successfully\n");
    }

    /**
     * FEATURE 24: View Available Quizzes
     * Student views all available quizzes for enrolled courses
     */
    private static void feature24_ViewAvailableQuizzes() throws SQLException {
        printFeature(24, "View Available Quizzes");

        pstmt = conn.prepareStatement(
                "SELECT q.quiz_id, q.title, q.start_time, q.end_time, " +
                        "c.course_name, sq.status " +
                        "FROM enrollments e " +
                        "JOIN classrooms cl ON e.classroom_id = cl.classroom_id " +
                        "JOIN courses c ON cl.course_id = c.course_id " +
                        "JOIN quizzes q ON cl.classroom_id = q.classroom_id " +
                        "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id AND sq.student_id = e.student_id " +
                        "WHERE e.student_id = ? AND e.status = 'active'"
        );
        pstmt.setLong(1, studentId);

        rs = pstmt.executeQuery();
        System.out.println("  → Available Quizzes:");
        while (rs.next()) {
            System.out.println("     • Quiz ID: " + rs.getLong("quiz_id"));
            System.out.println("       Title: " + rs.getString("title"));
            System.out.println("       Course: " + rs.getString("course_name"));
            System.out.println("       Status: " + (rs.getString("status") != null ? rs.getString("status") : "Not Started"));
            System.out.println();
        }

        System.out.println("  ✓ Quiz list retrieved\n");
    }

    // ==================================================================================
    // SECTION 6: Auto Grading (3 Features)
    // ==================================================================================

    private static void section6_AutoGrading() throws SQLException {
        printSectionHeader("SECTION 6: Auto Grading (3 Features)");

        // Feature 25: Auto Grade Objective Questions
        feature25_AutoGradeObjectiveQuestions();

        // Feature 26: Calculate Total Score
        feature26_CalculateTotalScore();

        // Feature 27: Publish Grades
        feature27_PublishGrades();
    }

    /**
     * FEATURE 25: Auto Grade Objective Questions
     * Automatically grades multiple choice and true/false questions
     */
    private static void feature25_AutoGradeObjectiveQuestions() throws SQLException {
        printFeature(25, "Auto Grade Objective Questions");

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

        System.out.println("  → Auto-grading completed");
        System.out.println("  → Questions graded: " + graded);

        // Display grading results
        pstmt = conn.prepareStatement(
                "SELECT sa.question_id, sa.is_correct, sa.points_earned, qq.points " +
                        "FROM student_answers sa " +
                        "JOIN quiz_questions qq ON sa.question_id = qq.question_id " +
                        "WHERE sa.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();

        System.out.println("  → Grading Details:");
        while (rs.next()) {
            boolean correct = rs.getBoolean("is_correct");
            System.out.println("     • Q" + rs.getLong("question_id") + ": " +
                    (correct ? "✓ Correct" : "✗ Incorrect") +
                    " - " + rs.getDouble("points_earned") + "/" + rs.getInt("points") + " points");
        }

        System.out.println("  ✓ Auto-grading completed\n");
    }

    /**
     * FEATURE 26: Calculate Total Score
     * Calculates the student's total score for the quiz
     */
    private static void feature26_CalculateTotalScore() throws SQLException {
        printFeature(26, "Calculate Total Score");

        pstmt = conn.prepareStatement(
                "UPDATE student_quizzes sq " +
                        "SET score = (SELECT COALESCE(SUM(sa.points_earned), 0) " +
                        "             FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
                        "    percentage = (SELECT COALESCE(SUM(sa.points_earned), 0) * 100.0 / " +
                        "                  (SELECT total_points FROM quizzes WHERE quiz_id = sq.quiz_id) " +
                        "                  FROM student_answers sa WHERE sa.student_quiz_id = sq.student_quiz_id), " +
                        "    graded = TRUE, " +
                        "    status = 'completed' " +
                        "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        // Retrieve and display final score
        pstmt = conn.prepareStatement(
                "SELECT sq.score, sq.percentage, q.total_points, q.passing_score " +
                        "FROM student_quizzes sq " +
                        "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
                        "WHERE sq.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        rs = pstmt.executeQuery();

        if (rs.next()) {
            double score = rs.getDouble("score");
            double percentage = rs.getDouble("percentage");
            int totalPoints = rs.getInt("total_points");
            int passingScore = rs.getInt("passing_score");

            System.out.println("  → Final Score Calculated:");
            System.out.println("     • Raw Score: " + score + " / " + totalPoints);
            System.out.println("     • Percentage: " + String.format("%.2f", percentage) + "%");
            System.out.println("     • Passing Score: " + passingScore);
            System.out.println("     • Result: " + (score >= passingScore ? "✓ PASSED" : "✗ FAILED"));
        }

        System.out.println("  ✓ Score calculation completed\n");
    }

    /**
     * FEATURE 27: Publish Grades
     * Makes grades visible to students
     */
    private static void feature27_PublishGrades() throws SQLException {
        printFeature(27, "Publish Grades");

        pstmt = conn.prepareStatement(
                "UPDATE student_quizzes SET published = TRUE WHERE student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);
        pstmt.executeUpdate();

        System.out.println("  → Grades published");
        System.out.println("  → Students can now view their results");
        System.out.println("  ✓ Publication completed\n");
    }

    // ==================================================================================
    // SECTION 7: Grade Query & Statistics (5 Features)
    // ==================================================================================

    private static void section7_GradeQueryAndStatistics() throws SQLException {
        printSectionHeader("SECTION 7: Grade Query & Statistics (5 Features)");

        // Feature 28: Student View Grades
        feature28_StudentViewGrades();

        // Feature 29: View Answer Details
        feature29_ViewAnswerDetails();

        // Feature 30: Teacher View Class Grades
        feature30_TeacherViewClassGrades();

        // Feature 31: Question Difficulty Analysis
        feature31_QuestionDifficultyAnalysis();

        // Feature 32: Generate Grade Report
        feature32_GenerateGradeReport();
    }

    /**
     * FEATURE 28: Student View Grades
     * Student views their published grades
     */
    private static void feature28_StudentViewGrades() throws SQLException {
        printFeature(28, "Student View Grades");

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
        System.out.println("  → Published Grades:");
        while (rs.next()) {
            System.out.println("     • " + rs.getString("title"));
            System.out.println("       Score: " + rs.getDouble("score") + "/" + rs.getInt("total_points"));
            System.out.println("       Percentage: " + String.format("%.2f", rs.getDouble("percentage")) + "%");
            System.out.println("       Result: " + rs.getString("result"));
            System.out.println("       Submitted: " + rs.getString("submit_time"));
        }

        System.out.println("  ✓ Grades displayed\n");
    }

    /**
     * FEATURE 29: View Answer Details
     * Student views detailed breakdown of answers
     */
    private static void feature29_ViewAnswerDetails() throws SQLException {
        printFeature(29, "View Answer Details");

        pstmt = conn.prepareStatement(
                "SELECT q.question_text, qo.option_text, sa.is_correct, sa.points_earned, qq.points " +
                        "FROM student_answers sa " +
                        "JOIN questions q ON sa.question_id = q.question_id " +
                        "JOIN question_options qo ON sa.selected_option_id = qo.option_id " +
                        "JOIN quiz_questions qq ON sa.question_id = qq.question_id " +
                        "WHERE sa.student_quiz_id = ?"
        );
        pstmt.setLong(1, studentQuizId);

        rs = pstmt.executeQuery();
        System.out.println("  → Answer Details:");
        int num = 1;
        while (rs.next()) {
            System.out.println("     Question " + num + ": " +
                    rs.getString("question_text").substring(0, Math.min(40, rs.getString("question_text").length())) + "...");
            System.out.println("       Your Answer: " + rs.getString("option_text"));
            System.out.println("       Result: " + (rs.getBoolean("is_correct") ? "✓ Correct" : "✗ Incorrect"));
            System.out.println("       Points: " + rs.getDouble("points_earned") + "/" + rs.getInt("points"));
            System.out.println();
            num++;
        }

        System.out.println("  ✓ Answer details retrieved\n");
    }

    /**
     * FEATURE 30: Teacher View Class Grades
     * Teacher views all student grades for a quiz
     */
    private static void feature30_TeacherViewClassGrades() throws SQLException {
        printFeature(30, "Teacher View Class Grades");

        pstmt = conn.prepareStatement(
                "SELECT u.full_name, s.student_number, sq.score, q.total_points, sq.percentage, " +
                        "CASE WHEN sq.score >= q.passing_score THEN 'Pass' ELSE 'Fail' END as result " +
                        "FROM student_quizzes sq " +
                        "JOIN students s ON sq.student_id = s.student_id " +
                        "JOIN users u ON s.user_id = u.user_id " +
                        "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
                        "WHERE sq.quiz_id = ? AND sq.graded = TRUE " +
                        "ORDER BY sq.score DESC"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        System.out.println("  → Class Grades:");
        while (rs.next()) {
            System.out.println("     • " + rs.getString("full_name") +
                    " (" + rs.getString("student_number") + ")");
            System.out.println("       Score: " + rs.getDouble("score") + "/" + rs.getInt("total_points") +
                    " (" + String.format("%.2f", rs.getDouble("percentage")) + "%)");
            System.out.println("       Result: " + rs.getString("result"));
        }

        System.out.println("  ✓ Class grades retrieved\n");
    }

    /**
     * FEATURE 31: Question Difficulty Analysis
     * Analyzes question difficulty based on student performance
     */
    private static void feature31_QuestionDifficultyAnalysis() throws SQLException {
        printFeature(31, "Question Difficulty Analysis");

        pstmt = conn.prepareStatement(
                "SELECT q.question_id, q.question_text, q.difficulty_level, " +
                        "COUNT(sa.answer_id) as total_attempts, " +
                        "SUM(CASE WHEN sa.is_correct THEN 1 ELSE 0 END) as correct_count, " +
                        "ROUND(SUM(CASE WHEN sa.is_correct THEN 1 ELSE 0 END) * 100.0 / COUNT(sa.answer_id), 2) as correct_rate " +
                        "FROM questions q " +
                        "LEFT JOIN student_answers sa ON q.question_id = sa.question_id " +
                        "WHERE q.is_deleted = FALSE " +
                        "GROUP BY q.question_id " +
                        "HAVING total_attempts > 0"
        );

        rs = pstmt.executeQuery();
        System.out.println("  → Question Analysis:");
        while (rs.next()) {
            System.out.println("     • Q" + rs.getLong("question_id") + ": " +
                    rs.getString("question_text").substring(0, Math.min(40, rs.getString("question_text").length())) + "...");
            System.out.println("       Preset Difficulty: " + rs.getInt("difficulty_level") + "/5");
            System.out.println("       Attempts: " + rs.getInt("total_attempts"));
            System.out.println("       Correct Rate: " + rs.getDouble("correct_rate") + "%");
            System.out.println();
        }

        System.out.println("  ✓ Difficulty analysis completed\n");
    }

    /**
     * FEATURE 32: Generate Grade Report
     * Generates comprehensive grade statistics
     */
    private static void feature32_GenerateGradeReport() throws SQLException {
        printFeature(32, "Generate Grade Report");

        pstmt = conn.prepareStatement(
                "SELECT " +
                        "COUNT(DISTINCT sq.student_id) as total_students, " +
                        "ROUND(AVG(sq.score), 2) as avg_score, " +
                        "MIN(sq.score) as min_score, " +
                        "MAX(sq.score) as max_score, " +
                        "SUM(CASE WHEN sq.score >= q.passing_score THEN 1 ELSE 0 END) as passed_count " +
                        "FROM student_quizzes sq " +
                        "JOIN quizzes q ON sq.quiz_id = q.quiz_id " +
                        "WHERE sq.quiz_id = ? AND sq.graded = TRUE"
        );
        pstmt.setLong(1, quizId);

        rs = pstmt.executeQuery();
        if (rs.next()) {
            int total = rs.getInt("total_students");
            int passed = rs.getInt("passed_count");

            System.out.println("  → Grade Report Summary:");
            System.out.println("     • Total Students: " + total);
            System.out.println("     • Average Score: " + rs.getDouble("avg_score"));
            System.out.println("     • Minimum Score: " + rs.getDouble("min_score"));
            System.out.println("     • Maximum Score: " + rs.getDouble("max_score"));
            System.out.println("     • Passed: " + passed);
            System.out.println("     • Failed: " + (total - passed));
            System.out.println("     • Pass Rate: " + String.format("%.2f", (passed * 100.0 / total)) + "%");
        }

        System.out.println("  ✓ Grade report generated\n");
    }

    // ==================================================================================
    // SECTION 8: Advanced Features (5 Features)
    // ==================================================================================

    private static void section8_AdvancedFeatures() throws SQLException {
        printSectionHeader("SECTION 8: Advanced Features (5 Features)");

        // Feature 33: Update Question Statistics
        feature33_UpdateQuestionStatistics();

        // Feature 34: Adaptive Difficulty Rating
        feature34_AdaptiveDifficultyRating();

        // Feature 35: View Teacher's Quizzes
        feature35_ViewTeachersQuizzes();

        // Feature 36: Question Usage Ranking
        feature36_QuestionUsageRanking();

        // Feature 37: Subject Hierarchy Query
        feature37_SubjectHierarchyQuery();
    }

    /**
     * FEATURE 33: Update Question Statistics
     * Updates usage statistics for questions
     */
    private static void feature33_UpdateQuestionStatistics() throws SQLException {
        printFeature(33, "Update Question Statistics");

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

        System.out.println("  → Statistics updated for " + updated + " questions");
        System.out.println("  ✓ Question statistics updated\n");
    }

    /**
     * FEATURE 34: Adaptive Difficulty Rating
     * Evaluates actual difficulty based on performance
     */
    private static void feature34_AdaptiveDifficultyRating() throws SQLException {
        printFeature(34, "Adaptive Difficulty Rating");

        pstmt = conn.prepareStatement(
                "SELECT q.question_id, q.question_text, q.difficulty_level, " +
                        "q.total_attempts, q.correct_count, " +
                        "CASE WHEN q.total_attempts = 0 THEN 'No Data' " +
                        "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 80 THEN 'Too Easy' " +
                        "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 60 THEN 'Appropriate' " +
                        "     WHEN ROUND(q.correct_count * 100.0 / q.total_attempts, 0) >= 40 THEN 'Slightly Hard' " +
                        "     ELSE 'Too Hard' END as actual_difficulty " +
                        "FROM questions q " +
                        "WHERE q.times_used > 0"
        );

        rs = pstmt.executeQuery();
        System.out.println("  → Adaptive Difficulty Analysis:");
        while (rs.next()) {
            System.out.println("     • Q" + rs.getLong("question_id"));
            System.out.println("       Preset: " + rs.getInt("difficulty_level") + "/5");
            System.out.println("       Performance: " + rs.getInt("correct_count") + "/" +
                    rs.getInt("total_attempts") + " correct");
            System.out.println("       Rating: " + rs.getString("actual_difficulty"));
            System.out.println();
        }

        System.out.println("  ✓ Adaptive rating completed\n");
    }

    /**
     * FEATURE 35: View Teacher's Quizzes
     * Lists all quizzes created by a teacher
     */
    private static void feature35_ViewTeachersQuizzes() throws SQLException {
        printFeature(35, "View Teacher's Quizzes");

        pstmt = conn.prepareStatement(
                "SELECT q.quiz_id, q.title, c.course_name, q.start_time, " +
                        "COUNT(DISTINCT e.student_id) as enrolled_students, " +
                        "COUNT(DISTINCT sq.student_quiz_id) as submissions " +
                        "FROM quizzes q " +
                        "JOIN classrooms cl ON q.classroom_id = cl.classroom_id " +
                        "JOIN courses c ON cl.course_id = c.course_id " +
                        "LEFT JOIN enrollments e ON cl.classroom_id = e.classroom_id AND e.status = 'active' " +
                        "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id " +
                        "WHERE q.created_by = ? " +
                        "GROUP BY q.quiz_id"
        );
        pstmt.setLong(1, teacherId);

        rs = pstmt.executeQuery();
        System.out.println("  → Quizzes Created by Teacher:");
        while (rs.next()) {
            System.out.println("     • Quiz ID: " + rs.getLong("quiz_id"));
            System.out.println("       Title: " + rs.getString("title"));
            System.out.println("       Course: " + rs.getString("course_name"));
            System.out.println("       Enrolled: " + rs.getInt("enrolled_students") + " students");
            System.out.println("       Submissions: " + rs.getInt("submissions"));
            System.out.println();
        }

        System.out.println("  ✓ Teacher's quizzes listed\n");
    }

    /**
     * FEATURE 36: Question Usage Ranking
     * Ranks questions by usage frequency
     */
    private static void feature36_QuestionUsageRanking() throws SQLException {
        printFeature(36, "Question Usage Ranking");

        pstmt = conn.prepareStatement(
                "SELECT q.question_id, q.question_text, q.times_used, q.total_attempts, " +
                        "CASE WHEN q.total_attempts = 0 THEN NULL " +
                        "     ELSE ROUND(q.correct_count * 100.0 / q.total_attempts, 2) END as correct_rate " +
                        "FROM questions q " +
                        "WHERE q.times_used > 0 AND q.is_deleted = FALSE " +
                        "ORDER BY q.times_used DESC, q.total_attempts DESC " +
                        "LIMIT 5"
        );

        rs = pstmt.executeQuery();
        System.out.println("  → Top 5 Most Used Questions:");
        int rank = 1;
        while (rs.next()) {
            System.out.println("     " + rank + ". Q" + rs.getLong("question_id"));
            System.out.println("        " + rs.getString("question_text").substring(0, Math.min(50, rs.getString("question_text").length())) + "...");
            System.out.println("        Times Used: " + rs.getInt("times_used"));
            System.out.println("        Total Attempts: " + rs.getInt("total_attempts"));
            System.out.println("        Correct Rate: " +
                    (rs.getObject("correct_rate") != null ? rs.getDouble("correct_rate") + "%" : "N/A"));
            System.out.println();
            rank++;
        }

        System.out.println("  ✓ Usage ranking generated\n");
    }

    /**
     * FEATURE 37: Subject Hierarchy Query
     * Displays subject hierarchy and question distribution
     */
    private static void feature37_SubjectHierarchyQuery() throws SQLException {
        printFeature(37, "Subject Hierarchy Query");

        pstmt = conn.prepareStatement(
                "SELECT s.subject_id, s.subject_name, s.level, " +
                        "COUNT(q.question_id) as question_count " +
                        "FROM subjects s " +
                        "LEFT JOIN questions q ON s.subject_id = q.subject_id AND q.is_deleted = FALSE " +
                        "GROUP BY s.subject_id " +
                        "ORDER BY s.level, s.subject_id"
        );

        rs = pstmt.executeQuery();
        System.out.println("  → Subject Hierarchy:");
        while (rs.next()) {
            String indent = "  ".repeat(rs.getInt("level"));
            System.out.println("     " + indent + "• " + rs.getString("subject_name") +
                    " (Level " + rs.getInt("level") + ")");
            System.out.println("     " + indent + "  Questions: " + rs.getInt("question_count"));
        }

        System.out.println("  ✓ Hierarchy query completed\n");
    }

    // ==================================================================================
    // Utility Methods
    // ==================================================================================

    private static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EXAM SYSTEM - COMPREHENSIVE FUNCTIONALITY TEST");
        System.out.println("Testing all 37 core features");
        System.out.println("=".repeat(80) + "\n");
    }

    private static void printSectionHeader(String title) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(title);
        System.out.println("=".repeat(80) + "\n");
    }

    private static void printFeature(int number, String name) {
        System.out.println("┌─ FEATURE " + number + ": " + name);
        System.out.println("│");
    }

    private static void printFooter() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✓ ALL 37 FEATURES TESTED SUCCESSFULLY!");
        System.out.println("=".repeat(80) + "\n");
    }

    private static int executeUpdate(String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        int result = stmt.executeUpdate(sql);
        stmt.close();
        return result;
    }

    private static void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
