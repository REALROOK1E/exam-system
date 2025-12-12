package com.zekai.comment;

import org.junit.platform.suite.api.*;

/**
 * ========================================
 * ALL FEATURES TEST SUITE - 全部功能测试套件
 * ========================================
 *
 * 运行所有37个功能测试:
 * - 学生端功能 (11个): Features 1, 3, 8-10, 21-24, 28-29
 * - 教师端功能 (18个): Features 2-3, 6-7, 11-20, 25-27, 30-32, 35
 * - 管理后台功能 (7个): Features 33-34, 36-37 + 用户管理/系统总览/数据维护
 *
 * 使用方法:
 * - 点击类名左侧的绿色运行按钮运行所有测试
 * - 或者分别运行各个测试类
 *
 * @author Exam System Team
 * @version 2.0
 */
@Suite
@SelectClasses({
    StudentFeatureTests.class,
    TeacherFeatureTests.class,
    AdminFeatureTests.class
})
@SuiteDisplayName("考试系统全部37个功能测试 | All 37 Features Test Suite")
public class AllFeaturesTestSuite {
    // 这个类只是一个测试套件容器
    // JUnit 5 会自动运行 @SelectClasses 中指定的所有测试类
}

