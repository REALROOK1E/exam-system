package com.zekai.comment;

/**
 * ========================================
 * EXAM SYSTEM - MAIN ENTRY POINT
 * ========================================
 *
 * 考试系统主入口，支持以下运行模式:
 *
 * 1. 运行所有测试 (默认):
 *    java Main
 *    java Main --mode=all
 *
 * 2. 只运行学生端功能测试:
 *    java Main --mode=student
 *
 * 3. 只运行教师端功能测试:
 *    java Main --mode=teacher
 *
 * 4. 只运行管理后台功能测试:
 *    java Main --mode=admin
 *
 * 功能分布 (37个功能):
 * - 学生端 (11个): 1, 3, 8, 9, 10, 21, 22, 23, 24, 28, 29
 * - 教师端 (18个): 2, 3, 5, 6, 7, 11-20, 25-27, 30-32, 35
 * - 管理后台 (7个): 33, 34, 36, 37 + 用户管理/系统总览/数据维护
 *
 * @author Exam System Team
 * @version 2.0
 */
public class Main {

    public static void main(String[] args) {
        String mode = parseMode(args);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("EXAM SYSTEM - COMPREHENSIVE FUNCTIONALITY TEST");
        System.out.println("Testing all 37 core features");
        System.out.println("Mode: " + mode.toUpperCase());
        System.out.println("=".repeat(80) + "\n");

        switch (mode) {
            case "student":
                runStudentTests();
                break;
            case "teacher":
                runTeacherTests();
                break;
            case "admin":
                runAdminTests();
                break;
            case "all":
            default:
                runAllTests();
                break;
        }
    }

    /**
     * 解析运行模式参数
     */
    private static String parseMode(String[] args) {
        if (args == null || args.length == 0) {
            return "all";
        }

        for (String arg : args) {
            if (arg.startsWith("--mode=")) {
                return arg.substring("--mode=".length()).trim().toLowerCase();
            }
        }

        return "all";
    }

    /**
     * 运行所有测试 (按顺序: 学生端 → 教师端 → 管理后台)
     */
    private static void runAllTests() {
        System.out.println("Running ALL tests in sequence...\n");

        // 运行学生端测试
        System.out.println("\n" + "━".repeat(80));
        System.out.println("PART 1: STUDENT TESTS");
        System.out.println("━".repeat(80));
        StudentTests.runAllStudentTests();

        // 运行教师端测试
        System.out.println("\n" + "━".repeat(80));
        System.out.println("PART 2: TEACHER TESTS");
        System.out.println("━".repeat(80));
        TeacherTests.runAllTeacherTests();

        // 运行管理后台测试
        System.out.println("\n" + "━".repeat(80));
        System.out.println("PART 3: ADMIN TESTS");
        System.out.println("━".repeat(80));
        AdminTests.runAllAdminTests();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("✓ ALL 37 FEATURES TESTED SUCCESSFULLY!");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * 只运行学生端测试
     */
    private static void runStudentTests() {
        System.out.println("Running STUDENT tests only...\n");
        StudentTests.runAllStudentTests();
    }

    /**
     * 只运行教师端测试
     */
    private static void runTeacherTests() {
        System.out.println("Running TEACHER tests only...\n");
        TeacherTests.runAllTeacherTests();
    }

    /**
     * 只运行管理后台测试
     */
    private static void runAdminTests() {
        System.out.println("Running ADMIN tests only...\n");
        AdminTests.runAllAdminTests();
    }
}
