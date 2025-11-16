# 考试系统功能实现详解 | Exam System Features Implementation Guide

## 项目概述 | Project Overview

这是一个完整的在线考试管理系统，包含用户管理、课程管理、题库管理、考试管理、自动评分等37个核心功能。系统使用Java开发，MySQL作为数据库，实现了教师出题、学生答题、自动评分、成绩统计等完整流程。

This is a comprehensive online exam management system with 37 core features including user management, course management, question bank management, exam management, and automatic grading. The system is developed in Java with MySQL database, implementing complete workflows from teacher question creation, student exam-taking, automatic grading, to grade statistics.

---

## 第一部分：用户权限管理 (5个功能) | Part 1: User & Permission Management (5 Features)

### Feature 1: 创建学生账户 | Create Student Account

**实现原理 | Implementation:**
- 首先在 `users` 表中插入用户基本信息（用户名、密码哈希、邮箱、姓名、角色）
- 获取生成的用户ID后，在 `students` 表中插入学生专属信息（学号、入学年份）
- 使用两张表分离通用用户信息和角色特定信息，便于扩展和管理

**数据库操作 | Database Operations:**
```sql
INSERT INTO users (username, password_hash, email, full_name, role)
VALUES ('alice_student', 'hashed_password', 'alice@email.com', 'Alice Johnson', 'student')

INSERT INTO students (user_id, student_number, enrollment_year)
VALUES (1, 'STU2025001', 2025)
```

---

### Feature 2: 创建教师账户 | Create Teacher Account

**实现原理 | Implementation:**
- 与创建学生账户类似，先在 `users` 表创建基础账户
- 在 `teachers` 表中添加教师特定信息（工号、院系、职称）
- 角色字段设置为 'teacher'，用于权限控制

**数据库操作 | Database Operations:**
```sql
INSERT INTO users (username, password_hash, email, full_name, role)
VALUES ('john_teacher', 'hashed_password', 'john@email.com', 'John Smith', 'teacher')

INSERT INTO teachers (user_id, employee_number, department, title)
VALUES (2, 'T2025001', 'Computer Science', 'Professor')
```

---

### Feature 3: 用户登录认证 | User Login Authentication

**实现原理 | Implementation:**
- 根据用户名和密码哈希在 `users` 表中查询匹配记录
- 验证成功后返回用户的完整信息（ID、用户名、角色、姓名等）
- 同时更新 `last_login` 字段记录最后登录时间
- 这里简化了密码验证，实际应该使用BCrypt等加密算法

**数据库操作 | Database Operations:**
```sql
SELECT user_id, username, role, full_name, email, is_active
FROM users
WHERE username = 'john_teacher' 
  AND password_hash = 'hashed_password'
  AND is_active = TRUE

UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = 2
```

---

### Feature 4: 学生课程注册 | Student Course Enrollment

**实现原理 | Implementation:**
- 将学生ID和教室ID插入到 `enrollments` 表中，建立学生与课程的关联
- 记录注册时间和状态（active, dropped, completed）
- 这个功能依赖于课程和教室已经创建，因此在演示中会先准备课程数据

**数据库操作 | Database Operations:**
```sql
INSERT INTO enrollments (student_id, classroom_id, enrollment_status)
VALUES (1, 1, 'active')
```

---

### Feature 5: 教师创建教室 | Teacher Create Classroom

**实现原理 | Implementation:**
- 在 `classrooms` 表中插入教室信息
- 关联教师ID、课程ID、学期、最大学生数等信息
- 教室是课程的具体实例，一门课程可以有多个教室（不同学期或班级）

**数据库操作 | Database Operations:**
```sql
INSERT INTO classrooms (course_id, teacher_id, semester, max_students)
VALUES (1, 1, 'Fall 2025', 50)
```

---

## 第二部分：课程和教室管理 (5个功能) | Part 2: Course & Classroom Management (5 Features)

### Feature 6: 创建课程 | Create Course

**实现原理 | Implementation:**
- 在 `courses` 表中插入课程基本信息
- 包括课程代码、名称、学分、描述等
- 课程是教学的基础单元，可以被多个教室引用

**数据库操作 | Database Operations:**
```sql
INSERT INTO courses (course_code, course_name, credits, description)
VALUES ('CS101', 'Data Structures and Algorithms', 4, 'Study of fundamental data structures...')
```

---

### Feature 7: 创建教室 | Create Classroom

**实现原理 | Implementation:**
- 基于已存在的课程创建具体的上课班级
- 指定授课教师、学期、学生容量
- 教室是学生实际注册和参与考试的单位

**数据库操作 | Database Operations:**
```sql
INSERT INTO classrooms (course_id, teacher_id, semester, max_students, is_active)
VALUES (1, 1, 'Fall 2025', 50, TRUE)
```

---

### Feature 8: 学生注册 | Student Enrollment

**实现原理 | Implementation:**
- 完成学生加入教室的正式注册流程
- 在 `enrollments` 表中创建关联记录
- 记录注册时间和初始状态为 'active'

**数据库操作 | Database Operations:**
```sql
INSERT INTO enrollments (student_id, classroom_id, enrollment_date, enrollment_status)
VALUES (1, 1, CURRENT_TIMESTAMP, 'active')
```

---

### Feature 9: 查询教室学生 | Query Classroom Students

**实现原理 | Implementation:**
- 通过多表联接查询教室中所有已注册的学生
- 联接 `enrollments`、`students` 和 `users` 表获取完整学生信息
- 只返回状态为 'active' 的学生

**数据库操作 | Database Operations:**
```sql
SELECT u.full_name, s.student_number, e.enrollment_status
FROM enrollments e
JOIN students s ON e.student_id = s.student_id
JOIN users u ON s.user_id = u.user_id
WHERE e.classroom_id = 1 AND e.enrollment_status = 'active'
```

---

### Feature 10: 学生退课 | Student Withdrawal

**实现原理 | Implementation:**
- 将学生的注册状态从 'active' 更新为 'dropped'
- 不删除记录，而是改变状态，保留历史数据
- 演示中为了测试完整流程，会立即恢复状态

**数据库操作 | Database Operations:**
```sql
UPDATE enrollments 
SET enrollment_status = 'dropped'
WHERE student_id = 1 AND classroom_id = 1

-- For testing, restore status
UPDATE enrollments 
SET enrollment_status = 'active'
WHERE student_id = 1 AND classroom_id = 1
```

---

## 第三部分：题库管理 (5个功能) | Part 3: Question Bank Management (5 Features)

### Feature 11: 创建科目 | Create Subject

**实现原理 | Implementation:**
- 在 `subjects` 表中创建科目分类
- 科目用于组织和分类题目
- 可以设置科目层级（parent_id），支持树形结构

**数据库操作 | Database Operations:**
```sql
INSERT INTO subjects (subject_name, description, parent_id, level)
VALUES ('Data Structures', 'Fundamental data structures...', NULL, 1)
```

---

### Feature 12: 创建单个题目 | Create Single Question

**实现原理 | Implementation:**
- 在 `questions` 表中插入题目详细信息
- 包括题目类型（multiple_choice, true_false, short_answer）
- 设置难度等级（1-5）、分数、正确答案等

**数据库操作 | Database Operations:**
```sql
INSERT INTO questions (subject_id, question_type, question_text, difficulty_level, 
                      default_points, correct_answer)
VALUES (1, 'multiple_choice', 'What is the time complexity of binary search?', 
        2, 10, '2')
```

---

### Feature 13: 添加题目选项 | Add Question Options

**实现原理 | Implementation:**
- 为选择题在 `question_options` 表中添加多个选项
- 每个选项有选项文本、顺序、是否正确的标记
- 支持多选题（多个选项标记为正确）

**数据库操作 | Database Operations:**
```sql
INSERT INTO question_options (question_id, option_text, option_order, is_correct)
VALUES 
  (1, 'O(n)', 1, FALSE),
  (1, 'O(log n)', 2, TRUE),
  (1, 'O(n²)', 3, FALSE),
  (1, 'O(1)', 4, FALSE)
```

---

### Feature 14: 批量创建题目 | Create Multiple Questions

**实现原理 | Implementation:**
- 循环执行插入操作，一次性创建多道题目
- 每道题都包含完整的题干、选项、答案信息
- 提高题库建设效率

**数据库操作 | Database Operations:**
```sql
-- Question 2
INSERT INTO questions (...) VALUES (...)
INSERT INTO question_options (...) VALUES (...), (...), (...)

-- Question 3
INSERT INTO questions (...) VALUES (...)
INSERT INTO question_options (...) VALUES (...), (...), (...)
```

---

### Feature 15: 查询题目统计 | Query Question Statistics

**实现原理 | Implementation:**
- 联接 `questions` 和 `subjects` 表获取题目完整信息
- 显示题目文本（截断显示）、类型、难度、使用次数
- 统计每道题目被使用的总次数

**数据库操作 | Database Operations:**
```sql
SELECT q.question_id, q.question_text, q.question_type, 
       q.difficulty_level, q.times_used, s.subject_name
FROM questions q
JOIN subjects s ON q.subject_id = s.subject_id
ORDER BY q.question_id
```

---

## 第四部分：测验生成管理 (5个功能) | Part 4: Quiz Generation Management (5 Features)

### Feature 16: 创建测验 | Create Quiz

**实现原理 | Implementation:**
- 在 `quizzes` 表中创建测验基本框架
- 设置测验标题、时间范围、总分、及格分等
- 测验创建后可以动态添加题目

**数据库操作 | Database Operations:**
```sql
INSERT INTO quizzes (classroom_id, title, description, quiz_type, 
                    start_time, end_time, duration_minutes, total_points, passing_score)
VALUES (1, 'Midterm Exam - Data Structures', 'Comprehensive assessment...', 
        'midterm', '2025-12-01 09:00:00', '2025-12-01 11:00:00', 120, 100, 60)
```

---

### Feature 17: 随机选题 | Random Question Selection

**实现原理 | Implementation:**
- 使用 `ORDER BY RAND()` 从题库中随机抽取题目
- 可以根据科目、难度等条件筛选
- 返回指定数量的随机题目供测验使用

**数据库操作 | Database Operations:**
```sql
SELECT question_id, question_text, difficulty_level
FROM questions
WHERE subject_id = 1
ORDER BY RAND()
LIMIT 3
```

---

### Feature 18: 添加题目到测验 | Add Questions to Quiz

**实现原理 | Implementation:**
- 在 `quiz_questions` 表中建立测验与题目的关联
- 为每道题目设置在本次测验中的分值和顺序
- 同一题目可以出现在不同测验中，且分值可以不同

**数据库操作 | Database Operations:**
```sql
INSERT INTO quiz_questions (quiz_id, question_id, question_order, points)
VALUES 
  (1, 1, 1, 30),
  (1, 2, 2, 30),
  (1, 3, 3, 40)
```

---

### Feature 19: 配置测验设置 | Configure Quiz Settings

**实现原理 | Implementation:**
- 在 `quiz_settings` 表中保存测验的各种配置选项
- 包括是否打乱题目顺序、是否打乱选项、是否立即显示结果等
- 这些设置影响学生答题时的体验和防作弊措施

**数据库操作 | Database Operations:**
```sql
INSERT INTO quiz_settings (quiz_id, shuffle_questions, shuffle_options, 
                           show_results_immediately, allow_review)
VALUES (1, TRUE, TRUE, FALSE, TRUE)
```

---

### Feature 20: 查看测验详情 | View Quiz Details

**实现原理 | Implementation:**
- 联接多个表获取测验的完整信息
- 包括基本信息、配置设置、题目数量等
- 提供测验的全面概览

**数据库操作 | Database Operations:**
```sql
SELECT q.quiz_id, q.title, q.start_time, q.end_time, q.duration_minutes,
       q.total_points, q.passing_score, COUNT(qq.question_id) as num_questions
FROM quizzes q
LEFT JOIN quiz_questions qq ON q.quiz_id = qq.quiz_id
WHERE q.quiz_id = 1
GROUP BY q.quiz_id
```

---

## 第五部分：考试管理 (4个功能) | Part 5: Exam Management (4 Features)

### Feature 21: 学生开始测验 | Student Start Quiz

**实现原理 | Implementation:**
- 在 `student_quizzes` 表中创建学生的测验会话
- 记录开始时间、状态设为 'in_progress'
- 系统可以通过开始时间和限时计算截止时间

**数据库操作 | Database Operations:**
```sql
INSERT INTO student_quizzes (student_id, quiz_id, start_time, status)
VALUES (1, 1, CURRENT_TIMESTAMP, 'in_progress')
```

---

### Feature 22: 学生提交答案 | Student Submit Answers

**实现原理 | Implementation:**
- 将学生的每个答案保存到 `student_answers` 表
- 记录选择的选项ID和答题时间
- 所有答案独立保存，便于分析和评分

**数据库操作 | Database Operations:**
```sql
INSERT INTO student_answers (student_quiz_id, question_id, selected_option_id, 
                             answer_text, answered_at)
VALUES 
  (1, 1, 1, NULL, CURRENT_TIMESTAMP),
  (1, 2, 5, NULL, CURRENT_TIMESTAMP),
  (1, 3, 9, NULL, CURRENT_TIMESTAMP)
```

---

### Feature 23: 完成测验提交 | Complete Quiz Submission

**实现原理 | Implementation:**
- 更新 `student_quizzes` 表中的状态为 'submitted'
- 记录提交时间
- 状态变更后触发自动评分流程

**数据库操作 | Database Operations:**
```sql
UPDATE student_quizzes
SET status = 'submitted',
    submit_time = CURRENT_TIMESTAMP
WHERE student_quiz_id = 1
```

---

### Feature 24: 查看可用测验 | View Available Quizzes

**实现原理 | Implementation:**
- 查询学生所在教室的所有测验
- 联接显示测验标题、课程名称、学生的完成状态
- 帮助学生了解待完成和已完成的测验

**数据库操作 | Database Operations:**
```sql
SELECT q.quiz_id, q.title, c.course_name, sq.status
FROM quizzes q
JOIN classrooms cl ON q.classroom_id = cl.classroom_id
JOIN courses c ON cl.course_id = c.course_id
LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id AND sq.student_id = 1
WHERE cl.classroom_id = 1
```

---

## 第六部分：自动评分 (3个功能) | Part 6: Auto Grading (3 Features)

### Feature 25: 自动评分客观题 | Auto Grade Objective Questions

**实现原理 | Implementation:**
- 对比学生选择的选项与正确选项，判断对错
- 通过联接 `student_answers`、`question_options` 和 `quiz_questions` 表
- 答对得满分，答错得0分，更新到 `student_answers` 表

**数据库操作 | Database Operations:**
```sql
-- Check if answer is correct
SELECT sa.student_answer_id, qo.is_correct, qq.points
FROM student_answers sa
JOIN question_options qo ON sa.selected_option_id = qo.option_id
JOIN quiz_questions qq ON sa.question_id = qq.question_id
WHERE sa.student_quiz_id = 1

-- Update scores
UPDATE student_answers
SET is_correct = TRUE/FALSE,
    points_earned = points/0
WHERE student_answer_id = ?
```

---

### Feature 26: 计算总分 | Calculate Total Score

**实现原理 | Implementation:**
- 汇总学生所有题目的得分
- 计算总分、百分比、判断是否及格
- 更新到 `student_quizzes` 表的成绩字段

**数据库操作 | Database Operations:**
```sql
-- Sum all points earned
SELECT SUM(points_earned) as total_score
FROM student_answers
WHERE student_quiz_id = 1

-- Update final score
UPDATE student_quizzes
SET score = 40.0,
    percentage = 40.00
WHERE student_quiz_id = 1
```

---

### Feature 27: 发布成绩 | Publish Grades

**实现原理 | Implementation:**
- 将 `student_quizzes` 表中的成绩状态设为 'graded'
- 设置成绩发布时间
- 发布后学生可以查看成绩和答案解析

**数据库操作 | Database Operations:**
```sql
UPDATE student_quizzes
SET status = 'graded',
    graded_at = CURRENT_TIMESTAMP
WHERE student_quiz_id = 1
```

---

## 第七部分：成绩查询与统计 (5个功能) | Part 7: Grade Query & Statistics (5 Features)

### Feature 28: 学生查看成绩 | Student View Grades

**实现原理 | Implementation:**
- 查询 `student_quizzes` 表获取学生已发布的成绩
- 联接 `quizzes` 表显示测验名称
- 只显示状态为 'graded' 的成绩

**数据库操作 | Database Operations:**
```sql
SELECT q.title, sq.score, sq.percentage, sq.submit_time,
       CASE WHEN sq.score >= q.passing_score THEN 'Passed' ELSE 'Failed' END as result
FROM student_quizzes sq
JOIN quizzes q ON sq.quiz_id = q.quiz_id
WHERE sq.student_id = 1 AND sq.status = 'graded'
```

---

### Feature 29: 查看答案详情 | View Answer Details

**实现原理 | Implementation:**
- 联接多个表获取学生的答题详情
- 显示每道题的题干、学生答案、正确答案、得分
- 帮助学生了解错题和正确答案

**数据库操作 | Database Operations:**
```sql
SELECT q.question_text, qo.option_text as student_answer, 
       sa.is_correct, sa.points_earned, qq.points as max_points
FROM student_answers sa
JOIN questions q ON sa.question_id = q.question_id
JOIN question_options qo ON sa.selected_option_id = qo.option_id
JOIN quiz_questions qq ON sa.question_id = qq.question_id
WHERE sa.student_quiz_id = 1
```

---

### Feature 30: 教师查看班级成绩 | Teacher View Class Grades

**实现原理 | Implementation:**
- 联接多个表获取教室所有学生的成绩
- 显示学生姓名、学号、分数、及格状态
- 按成绩降序排列，便于教师查看班级整体情况

**数据库操作 | Database Operations:**
```sql
SELECT u.full_name, s.student_number, sq.score, sq.percentage,
       CASE WHEN sq.score >= q.passing_score THEN 'Pass' ELSE 'Fail' END as result
FROM student_quizzes sq
JOIN students s ON sq.student_id = s.student_id
JOIN users u ON s.user_id = u.user_id
JOIN quizzes q ON sq.quiz_id = q.quiz_id
WHERE q.quiz_id = 1 AND sq.status = 'graded'
ORDER BY sq.score DESC
```

---

### Feature 31: 题目难度分析 | Question Difficulty Analysis

**实现原理 | Implementation:**
- 统计每道题目的答题次数和正确率
- 对比预设难度和实际正确率
- 帮助教师评估题目难度是否合理

**数据库操作 | Database Operations:**
```sql
SELECT q.question_id, q.question_text, q.difficulty_level,
       COUNT(*) as total_attempts,
       SUM(CASE WHEN sa.is_correct THEN 1 ELSE 0 END) as correct_count,
       (SUM(CASE WHEN sa.is_correct THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as correct_rate
FROM student_answers sa
JOIN questions q ON sa.question_id = q.question_id
WHERE sa.student_quiz_id IN (SELECT student_quiz_id FROM student_quizzes WHERE quiz_id = 1)
GROUP BY q.question_id
```

---

### Feature 32: 生成成绩报告 | Generate Grade Report

**实现原理 | Implementation:**
- 使用聚合函数统计班级成绩数据
- 计算平均分、最高分、最低分、及格率等关键指标
- 生成测验的整体分析报告

**数据库操作 | Database Operations:**
```sql
SELECT 
    COUNT(*) as total_students,
    AVG(sq.score) as avg_score,
    MIN(sq.score) as min_score,
    MAX(sq.score) as max_score,
    SUM(CASE WHEN sq.score >= q.passing_score THEN 1 ELSE 0 END) as passed_count,
    SUM(CASE WHEN sq.score < q.passing_score THEN 1 ELSE 0 END) as failed_count
FROM student_quizzes sq
JOIN quizzes q ON sq.quiz_id = q.quiz_id
WHERE sq.quiz_id = 1 AND sq.status = 'graded'
```

---

## 第八部分：高级功能 (5个功能) | Part 8: Advanced Features (5 Features)

### Feature 33: 更新题目统计 | Update Question Statistics

**实现原理 | Implementation:**
- 更新 `questions` 表中的统计字段
- 增加题目使用次数、答题次数、正确率等
- 这些数据用于题目质量分析和自适应出题

**数据库操作 | Database Operations:**
```sql
UPDATE questions q
SET times_used = times_used + 1,
    total_attempts = (SELECT COUNT(*) FROM student_answers WHERE question_id = q.question_id),
    correct_attempts = (SELECT COUNT(*) FROM student_answers 
                       WHERE question_id = q.question_id AND is_correct = TRUE)
WHERE question_id IN (1, 2, 3)
```

---

### Feature 34: 自适应难度评级 | Adaptive Difficulty Rating

**实现原理 | Implementation:**
- 根据题目的实际正确率动态调整难度评级
- 对比预设难度和实际表现，判断题目是"太难"、"合适"还是"太容易"
- 帮助优化题库质量

**算法逻辑 | Algorithm Logic:**
```
如果正确率 < 30%: 题目太难
如果正确率在 30%-70%: 难度合适
如果正确率 > 70%: 题目太容易

可以根据实际情况调整阈值
```

---

### Feature 35: 查看教师的测验 | View Teacher's Quizzes

**实现原理 | Implementation:**
- 查询特定教师创建的所有测验
- 联接显示课程、注册学生数、提交数等统计信息
- 帮助教师管理自己的测验

**数据库操作 | Database Operations:**
```sql
SELECT q.quiz_id, q.title, c.course_name,
       COUNT(DISTINCT e.student_id) as enrolled_students,
       COUNT(DISTINCT sq.student_quiz_id) as submissions
FROM quizzes q
JOIN classrooms cl ON q.classroom_id = cl.classroom_id
JOIN courses c ON cl.course_id = c.course_id
LEFT JOIN enrollments e ON cl.classroom_id = e.classroom_id
LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id
WHERE cl.teacher_id = 1
GROUP BY q.quiz_id
```

---

### Feature 36: 题目使用排名 | Question Usage Ranking

**实现原理 | Implementation:**
- 统计所有题目的使用频率、答题次数、正确率
- 按使用次数降序排列
- 识别最常用和高质量的题目

**数据库操作 | Database Operations:**
```sql
SELECT q.question_id, q.question_text, q.times_used, q.total_attempts,
       CASE WHEN q.total_attempts > 0 
            THEN (q.correct_attempts * 100.0 / q.total_attempts)
            ELSE 0 
       END as correct_rate
FROM questions q
WHERE q.times_used > 0
ORDER BY q.times_used DESC, q.total_attempts DESC
LIMIT 5
```

---

### Feature 37: 科目层级查询 | Subject Hierarchy Query

**实现原理 | Implementation:**
- 查询科目的树形结构
- 显示每个科目下的题目数量
- 支持多级科目分类

**数据库操作 | Database Operations:**
```sql
SELECT s.subject_id, s.subject_name, s.level, s.parent_id,
       COUNT(q.question_id) as question_count
FROM subjects s
LEFT JOIN questions q ON s.subject_id = q.subject_id
GROUP BY s.subject_id
ORDER BY s.level, s.subject_id
```

---

## 技术架构总结 | Technical Architecture Summary

### 数据库设计亮点 | Database Design Highlights

1. **用户角色分离** | User Role Separation
   - 使用 `users` 基础表 + `students`/`teachers` 扩展表
   - 便于扩展新角色，保持数据结构清晰

2. **题目与测验解耦** | Question-Quiz Decoupling
   - 题目存储在题库中可重复使用
   - 通过中间表 `quiz_questions` 关联，灵活组卷

3. **状态管理** | Status Management
   - 使用枚举类型管理各种状态（注册、测验、成绩等）
   - 软删除而非物理删除，保留数据历史

4. **统计字段冗余** | Statistical Redundancy
   - 在 `questions` 表中保存使用次数、正确率等统计信息
   - 避免复杂查询，提高性能

### 系统特点 | System Features

✅ **完整的考试流程** | Complete Exam Workflow
✅ **自动评分系统** | Automatic Grading System  
✅ **灵活的题库管理** | Flexible Question Bank Management
✅ **详细的统计分析** | Detailed Statistical Analysis
✅ **用户权限控制** | User Permission Control
✅ **可扩展的架构设计** | Scalable Architecture Design

---

## 使用说明 | Usage Instructions

### 运行程序 | Run the Program
```bash
mvn clean compile exec:java
```

### 配置数据库 | Configure Database
修改 `DatabaseConfig.java` 中的数据库连接信息：
Modify database connection in `DatabaseConfig.java`:
- DB_URL: 数据库地址 | Database URL
- DB_USER: 用户名 | Username  
- DB_PASSWORD: 密码 | Password

### 前置要求 | Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

---

**文档生成时间 | Document Generated:** November 17, 2025  
**系统版本 | System Version:** 1.0  
**作者 | Author:** Exam System Team
