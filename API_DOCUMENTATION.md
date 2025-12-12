# Exam System REST API Documentation
# 在线考试系统 REST API 文档

## 📋 目录 | Table of Contents

- [1. 概述 | Overview](#1-概述--overview)
- [2. 认证 | Authentication](#2-认证--authentication)
- [3. 通用响应格式 | Common Response Format](#3-通用响应格式--common-response-format)
- [4. 学生端API | Student APIs](#4-学生端api--student-apis)
- [5. 教师端API | Teacher APIs](#5-教师端api--teacher-apis)
- [6. 管理后台API | Admin APIs](#6-管理后台api--admin-apis)
- [7. 错误代码 | Error Codes](#7-错误代码--error-codes)

---

## 1. 概述 | Overview

### 基础信息 | Basic Information

- **Base URL**: `http://localhost:8080/api`
- **数据格式 | Data Format**: JSON
- **字符编码 | Character Encoding**: UTF-8
- **认证方式 | Authentication**: JWT (JSON Web Token)

### 技术栈 | Technology Stack

- **后端框架**: Spring Boot 3.1.5
- **安全框架**: Spring Security
- **数据库**: MySQL 8.0
- **JWT库**: jjwt 0.11.5

---

## 2. 认证 | Authentication

### 2.1 登录获取Token | Login to Get Token

**端点**: `POST /auth/login`

**请求头**: 无需认证

**请求体**:
```json
{
  "username": "john_teacher",
  "password": "teachpass",
  "role": "teacher"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VybmFtZSI6ImpvaG4iLCJyb2xlIjoidGVhY2hlciIsInVzZXJJZCI6MSwidGVhY2hlcklkIjoxLCJpYXQiOjE3MDIzNjk2MDAsImV4cCI6MTcwMjQ1NjAwMH0...",
    "username": "john_teacher",
    "role": "teacher",
    "userId": 1,
    "roleId": 1,
    "fullName": "John Smith"
  },
  "timestamp": 1702369600000
}
```

### 2.2 使用Token | Using Token

所有需要认证的API请求必须在请求头中包含JWT令牌：

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**示例 cURL**:
```bash
curl -X GET http://localhost:8080/api/student/quizzes \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

---

## 3. 通用响应格式 | Common Response Format

所有API响应都遵循统一的JSON格式：

```json
{
  "code": 200,
  "message": "Success",
  "data": { },
  "timestamp": 1702369600000
}
```

### 字段说明 | Field Description

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码（200=成功，400=参数错误，401=未授权，500=服务器错误）|
| message | string | 响应消息 |
| data | object/array | 响应数据（可能为null）|
| timestamp | long | 响应时间戳（毫秒）|

---

## 4. 学生端API | Student APIs

### 4.1 创建学生账户 | Create Student Account

**Feature 1**: 注册新学生账户

**端点**: `POST /student/register`

**请求头**: 无需认证

**请求体**:
```json
{
  "username": "alice_student",
  "password": "password123",
  "email": "alice@university.edu",
  "fullName": "Alice Johnson",
  "studentNumber": "STU2025001",
  "grade": "Junior",
  "major": "Computer Science"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "学生账户创建成功",
  "data": {
    "userId": 10,
    "studentId": 5,
    "username": "alice_student",
    "studentNumber": "STU2025001"
  }
}
```

---

### 4.2 学生登录 | Student Login

**Feature 3**: 学生登录认证

**端点**: `POST /auth/login`

**请求体**:
```json
{
  "username": "alice_student",
  "password": "password123",
  "role": "student"
}
```

**响应**: 见 [2.1 登录获取Token](#21-登录获取token--login-to-get-token)

---

### 4.3 注册课程 | Enroll in Course

**Feature 8**: 学生注册课程

**端点**: `POST /student/enrollments`

**请求头**: 
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "classroomId": 201
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "enrollmentId": 100,
    "studentId": 5,
    "classroomId": 201,
    "status": "active",
    "enrollmentDate": "2025-12-12 10:30:00"
  }
}
```

---

### 4.4 查询教室学生 | Query Classroom Students

**Feature 9**: 查询同班同学

**端点**: `GET /student/classrooms/{classroomId}/students`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `classroomId`: 教室ID

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "classroomId": 201,
    "courseName": "Data Structures",
    "students": [
      {
        "studentId": 5,
        "fullName": "Alice Johnson",
        "studentNumber": "STU2025001",
        "grade": "Junior",
        "major": "Computer Science"
      },
      {
        "studentId": 6,
        "fullName": "Bob Smith",
        "studentNumber": "STU2025002",
        "grade": "Senior",
        "major": "Computer Science"
      }
    ]
  }
}
```

---

### 4.5 退课 | Drop Course

**Feature 10**: 学生退课

**端点**: `DELETE /student/enrollments/{classroomId}`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `classroomId`: 要退出的教室ID

**响应**:
```json
{
  "code": 200,
  "message": "退课成功",
  "data": {
    "classroomId": 201,
    "status": "dropped",
    "dropTime": "2025-12-12 11:00:00"
  }
}
```

---

### 4.6 查看可用测验 | View Available Quizzes

**Feature 24**: 查看所有可参加的测验

**端点**: `GET /student/quizzes`

**请求头**: 
```
Authorization: Bearer {token}
```

**查询参数**:
- `status` (可选): 过滤状态 - `available`, `in_progress`, `submitted`, `completed`

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "quizzes": [
      {
        "quizId": 301,
        "title": "Midterm Exam - Data Structures",
        "courseName": "Data Structures",
        "startTime": "2025-12-15 09:00:00",
        "endTime": "2025-12-15 11:00:00",
        "durationMinutes": 120,
        "totalPoints": 100,
        "status": "available",
        "myStatus": null
      },
      {
        "quizId": 302,
        "title": "Quiz 1 - Arrays",
        "courseName": "Data Structures",
        "startTime": "2025-12-10 09:00:00",
        "endTime": "2025-12-10 10:00:00",
        "durationMinutes": 60,
        "totalPoints": 50,
        "status": "closed",
        "myStatus": "submitted"
      }
    ]
  }
}
```

---

### 4.7 开始测验 | Start Quiz

**Feature 21**: 学生开始答题

**端点**: `POST /student/quizzes/{quizId}/start`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `quizId`: 测验ID

**响应**:
```json
{
  "code": 200,
  "message": "测验已开始",
  "data": {
    "studentQuizId": 5001,
    "quizId": 301,
    "startTime": "2025-12-12 10:00:00",
    "endTime": "2025-12-12 12:00:00",
    "status": "in_progress",
    "questions": [
      {
        "questionId": 1001,
        "questionOrder": 1,
        "questionText": "What is the time complexity of binary search?",
        "questionType": "multiple_choice",
        "points": 30,
        "options": [
          {
            "optionId": 2001,
            "optionText": "O(n)",
            "optionOrder": 1
          },
          {
            "optionId": 2002,
            "optionText": "O(log n)",
            "optionOrder": 2
          },
          {
            "optionId": 2003,
            "optionText": "O(n²)",
            "optionOrder": 3
          },
          {
            "optionId": 2004,
            "optionText": "O(1)",
            "optionOrder": 4
          }
        ]
      }
    ]
  }
}
```

---

### 4.8 提交答案 | Submit Answers

**Feature 22**: 提交单道题目答案

**端点**: `POST /student/quizzes/{studentQuizId}/answers`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `studentQuizId`: 学生测验会话ID

**请求体**:
```json
{
  "questionId": 1001,
  "selectedOptionId": 2002,
  "answerText": null
}
```

**响应**:
```json
{
  "code": 200,
  "message": "答案已保存",
  "data": {
    "answerId": 8001,
    "questionId": 1001,
    "saved": true
  }
}
```

---

### 4.9 完成测验 | Complete Quiz

**Feature 23**: 提交整个测验

**端点**: `POST /student/quizzes/{studentQuizId}/submit`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `studentQuizId`: 学生测验会话ID

**响应**:
```json
{
  "code": 200,
  "message": "测验已提交",
  "data": {
    "studentQuizId": 5001,
    "submitTime": "2025-12-12 11:30:00",
    "status": "submitted",
    "answeredQuestions": 3,
    "totalQuestions": 3
  }
}
```

---

### 4.10 查看成绩 | View Grades

**Feature 28**: 查看已发布的成绩

**端点**: `GET /student/grades`

**请求头**: 
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "grades": [
      {
        "quizId": 301,
        "title": "Midterm Exam",
        "courseName": "Data Structures",
        "score": 85,
        "totalPoints": 100,
        "percentage": 85.0,
        "result": "Passed",
        "submitTime": "2025-12-10 11:30:00"
      },
      {
        "quizId": 302,
        "title": "Quiz 1",
        "courseName": "Data Structures",
        "score": 45,
        "totalPoints": 50,
        "percentage": 90.0,
        "result": "Passed",
        "submitTime": "2025-12-08 10:00:00"
      }
    ]
  }
}
```

---

### 4.11 查看答案详情 | View Answer Details

**Feature 29**: 查看某次测验的详细答题情况

**端点**: `GET /student/quizzes/{studentQuizId}/details`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `studentQuizId`: 学生测验会话ID

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "quizTitle": "Midterm Exam",
    "totalScore": 85,
    "totalPoints": 100,
    "questions": [
      {
        "questionId": 1001,
        "questionText": "What is the time complexity of binary search?",
        "yourAnswer": "O(log n)",
        "correctAnswer": "O(log n)",
        "isCorrect": true,
        "pointsEarned": 30,
        "totalPoints": 30
      },
      {
        "questionId": 1002,
        "questionText": "Which data structure uses LIFO?",
        "yourAnswer": "Queue",
        "correctAnswer": "Stack",
        "isCorrect": false,
        "pointsEarned": 0,
        "totalPoints": 30
      }
    ]
  }
}
```

---

## 5. 教师端API | Teacher APIs

### 5.1 创建教师账户 | Create Teacher Account

**Feature 2**: 注册新教师账户

**端点**: `POST /teacher/register`

**请求头**: 无需认证（或需要管理员权限）

**请求体**:
```json
{
  "username": "john_teacher",
  "password": "teachpass",
  "email": "john@university.edu",
  "fullName": "John Smith",
  "department": "Computer Science",
  "phone": "+1-555-0100",
  "office": "CS Building 301"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "教师账户创建成功",
  "data": {
    "userId": 20,
    "teacherId": 10,
    "username": "john_teacher",
    "department": "Computer Science"
  }
}
```

---

### 5.2 创建课程 | Create Course

**Feature 6**: 创建新课程

**端点**: `POST /teacher/courses`

**请求头**: 
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "courseCode": "CS101",
  "courseName": "Data Structures and Algorithms",
  "description": "Introduction to fundamental data structures",
  "creditHours": 4
}
```

**响应**:
```json
{
  "code": 200,
  "message": "课程创建成功",
  "data": {
    "courseId": 501,
    "courseCode": "CS101",
    "courseName": "Data Structures and Algorithms",
    "createdBy": 10,
    "createdAt": "2025-12-12 09:00:00"
  }
}
```

---

### 5.3 创建教室 | Create Classroom

**Feature 7**: 为课程创建教室（班级）

**端点**: `POST /teacher/classrooms`

**请求头**: 
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "courseId": 501,
  "className": "Section 01",
  "semester": "Fall 2025",
  "year": 2025,
  "maxStudents": 50
}
```

**响应**:
```json
{
  "code": 200,
  "message": "教室创建成功",
  "data": {
    "classroomId": 201,
    "courseId": 501,
    "teacherId": 10,
    "className": "Section 01",
    "semester": "Fall 2025",
    "year": 2025,
    "maxStudents": 50
  }
}
```

---

### 5.4 创建科目 | Create Subject

**Feature 11**: 创建题目分类科目

**端点**: `POST /teacher/subjects`

**请求头**: 
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "subjectName": "Data Structures",
  "description": "Topics related to data structures and algorithms",
  "level": 1,
  "parentSubjectId": null
}
```

**响应**:
```json
{
  "code": 200,
  "message": "科目创建成功",
  "data": {
    "subjectId": 101,
    "subjectName": "Data Structures",
    "level": 1,
    "createdAt": "2025-12-12 09:30:00"
  }
}
```

---

### 5.5 创建单个题目 | Create Single Question

**Feature 12**: 创建一道题目

**端点**: `POST /teacher/questions`

**请求头**: 
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "subjectId": 101,
  "questionText": "What is the time complexity of binary search?",
  "questionType": "multiple_choice",
  "difficultyLevel": 2,
  "options": [
    {
      "optionText": "O(n)",
      "isCorrect": false,
      "optionOrder": 1
    },
    {
      "optionText": "O(log n)",
      "isCorrect": true,
      "optionOrder": 2
    },
    {
      "optionText": "O(n²)",
      "isCorrect": false,
      "optionOrder": 3
    },
    {
      "optionText": "O(1)",
      "isCorrect": false,
      "optionOrder": 4
    }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "message": "题目创建成功",
  "data": {
    "questionId": 1001,
    "subjectId": 101,
    "questionType": "multiple_choice",
    "difficultyLevel": 2,
    "optionsCount": 4
  }
}
```

---

### 5.6 批量上传题目 | Batch Upload Questions

**Feature 14**: 批量创建多道题目

**端点**: `POST /teacher/questions/batch`

**请求头**: 
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "questions": [
    {
      "subjectId": 101,
      "questionText": "Question 1...",
      "questionType": "multiple_choice",
      "difficultyLevel": 2,
      "options": [...]
    },
    {
      "subjectId": 101,
      "questionText": "Question 2...",
      "questionType": "multiple_choice",
      "difficultyLevel": 3,
      "options": [...]
    }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "message": "批量创建成功",
  "data": {
    "totalQuestions": 2,
    "successCount": 2,
    "failedCount": 0,
    "questionIds": [1002, 1003]
  }
}
```

---

### 5.7 查询题目统计 | Query Question Statistics

**Feature 15**: 查询题库统计信息

**端点**: `GET /teacher/questions/statistics`

**请求头**: 
```
Authorization: Bearer {token}
```

**查询参数**:
- `subjectId` (可选): 按科目筛选
- `difficultyLevel` (可选): 按难度筛选

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalQuestions": 150,
    "bySubject": {
      "Data Structures": 50,
      "Algorithms": 45,
      "Operating Systems": 55
    },
    "byDifficulty": {
      "1": 30,
      "2": 40,
      "3": 45,
      "4": 25,
      "5": 10
    },
    "byType": {
      "multiple_choice": 120,
      "true_false": 20,
      "essay": 10
    }
  }
}
```

---

### 5.8 创建测验 | Create Quiz

**Feature 16**: 创建新测验

**端点**: `POST /teacher/quizzes`

**请求头**: 
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
  "classroomId": 201,
  "title": "Midterm Exam - Data Structures",
  "description": "Comprehensive exam covering all topics",
  "startTime": "2025-12-15 09:00:00",
  "endTime": "2025-12-15 11:00:00",
  "durationMinutes": 120,
  "totalPoints": 100,
  "passingScore": 60,
  "questions": [
    {
      "questionId": 1001,
      "questionOrder": 1,
      "points": 30
    },
    {
      "questionId": 1002,
      "questionOrder": 2,
      "points": 30
    },
    {
      "questionId": 1003,
      "questionOrder": 3,
      "points": 40
    }
  ],
  "settings": {
    "shuffleQuestions": true,
    "shuffleOptions": true,
    "showResultsImmediately": false,
    "allowReview": true
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "测验创建成功",
  "data": {
    "quizId": 301,
    "title": "Midterm Exam - Data Structures",
    "questionCount": 3,
    "totalPoints": 100,
    "createdAt": "2025-12-12 10:00:00"
  }
}
```

---

### 5.9 随机选题 | Random Question Selection

**Feature 17**: 从题库随机选择题目

**端点**: `GET /teacher/questions/random`

**请求头**: 
```
Authorization: Bearer {token}
```

**查询参数**:
- `subjectId`: 科目ID（必填）
- `questionType`: 题目类型（可选）
- `difficultyLevel`: 难度等级（可选）
- `count`: 选择数量（必填）

**请求示例**:
```
GET /teacher/questions/random?subjectId=101&questionType=multiple_choice&count=5
```

**响应**:
```json
{
  "code": 200,
  "message": "随机选题成功",
  "data": {
    "questions": [
      {
        "questionId": 1005,
        "questionText": "What is a stack?",
        "difficultyLevel": 2
      },
      {
        "questionId": 1012,
        "questionText": "Define recursion...",
        "difficultyLevel": 3
      },
      {
        "questionId": 1008,
        "questionText": "Binary tree properties?",
        "difficultyLevel": 4
      }
    ],
    "selectedCount": 3
  }
}
```

---

### 5.10 自动评分 | Auto Grade Quiz

**Feature 25**: 自动评分客观题

**端点**: `POST /teacher/quizzes/{studentQuizId}/grade`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `studentQuizId`: 学生测验会话ID

**响应**:
```json
{
  "code": 200,
  "message": "评分完成",
  "data": {
    "studentQuizId": 5001,
    "gradedQuestions": 3,
    "score": 60,
    "percentage": 60.0,
    "status": "graded"
  }
}
```

---

### 5.11 发布成绩 | Publish Grades

**Feature 27**: 发布测验成绩

**端点**: `POST /teacher/quizzes/{quizId}/publish`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `quizId`: 测验ID

**响应**:
```json
{
  "code": 200,
  "message": "成绩已发布",
  "data": {
    "quizId": 301,
    "publishedCount": 25,
    "publishTime": "2025-12-12 15:00:00"
  }
}
```

---

### 5.12 查看班级成绩 | View Class Grades

**Feature 30**: 查看某个测验的所有学生成绩

**端点**: `GET /teacher/quizzes/{quizId}/grades`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `quizId`: 测验ID

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "quizId": 301,
    "quizTitle": "Midterm Exam",
    "grades": [
      {
        "studentId": 5,
        "fullName": "Alice Johnson",
        "studentNumber": "STU2025001",
        "score": 85,
        "totalPoints": 100,
        "percentage": 85.0,
        "result": "Passed"
      },
      {
        "studentId": 6,
        "fullName": "Bob Smith",
        "studentNumber": "STU2025002",
        "score": 92,
        "totalPoints": 100,
        "percentage": 92.0,
        "result": "Passed"
      }
    ]
  }
}
```

---

### 5.13 题目难度分析 | Question Difficulty Analysis

**Feature 31**: 分析题目实际难度

**端点**: `GET /teacher/questions/{questionId}/analysis`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `questionId`: 题目ID

**响应**:
```json
{
  "code": 200,
  "message": "分析完成",
  "data": {
    "questionId": 1001,
    "questionText": "What is the time complexity...",
    "presetDifficulty": 2,
    "timesUsed": 10,
    "totalAttempts": 150,
    "correctCount": 50,
    "correctRate": 33.3,
    "actualDifficulty": "Appropriate",
    "recommendation": "Keep using"
  }
}
```

---

### 5.14 生成成绩报告 | Generate Grade Report

**Feature 32**: 生成测验统计报告

**端点**: `GET /teacher/quizzes/{quizId}/report`

**请求头**: 
```
Authorization: Bearer {token}
```

**路径参数**:
- `quizId`: 测验ID

**响应**:
```json
{
  "code": 200,
  "message": "报告生成成功",
  "data": {
    "quizId": 301,
    "quizTitle": "Midterm Exam",
    "statistics": {
      "totalStudents": 25,
      "submittedCount": 23,
      "averageScore": 75.6,
      "minScore": 45,
      "maxScore": 98,
      "passedCount": 20,
      "passRate": 87.0
    },
    "distribution": {
      "A (90-100)": 5,
      "B (80-89)": 8,
      "C (70-79)": 7,
      "D (60-69)": 3,
      "F (<60)": 0
    }
  }
}
```

---

## 6. 管理后台API | Admin APIs

### 6.1 更新题目统计 | Update Question Statistics

**Feature 33**: 更新所有题目的使用统计

**端点**: `POST /admin/questions/update-statistics`

**请求头**: 
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "统计更新完成",
  "data": {
    "updatedQuestions": 150,
    "updateTime": "2025-12-12 16:00:00"
  }
}
```

---

### 6.2 自适应难度评级 | Adaptive Difficulty Rating

**Feature 34**: 根据答题数据评估题目难度

**端点**: `GET /admin/questions/difficulty-rating`

**请求头**: 
```
Authorization: Bearer {token}
```

**查询参数**:
- `minAttempts` (可选): 最小答题次数（默认10）

**响应**:
```json
{
  "code": 200,
  "message": "评级完成",
  "data": {
    "ratedQuestions": [
      {
        "questionId": 1001,
        "presetDifficulty": 2,
        "correctRate": 85.5,
        "actualDifficulty": "Too Easy",
        "recommendation": "Increase difficulty level"
      },
      {
        "questionId": 1002,
        "presetDifficulty": 4,
        "correctRate": 25.3,
        "actualDifficulty": "Too Hard",
        "recommendation": "Simplify question or decrease level"
      }
    ]
  }
}
```

---

### 6.3 题目使用排名 | Question Usage Ranking

**Feature 36**: 查询最常用的题目

**端点**: `GET /admin/questions/ranking`

**请求头**: 
```
Authorization: Bearer {token}
```

**查询参数**:
- `limit`: 返回数量（默认10）
- `orderBy`: 排序字段 - `times_used`, `total_attempts`, `correct_rate`

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "topQuestions": [
      {
        "rank": 1,
        "questionId": 1005,
        "questionText": "Define a binary tree...",
        "timesUsed": 45,
        "totalAttempts": 678,
        "correctRate": 62.5
      },
      {
        "rank": 2,
        "questionId": 1012,
        "questionText": "Explain recursion...",
        "timesUsed": 38,
        "totalAttempts": 542,
        "correctRate": 58.7
      }
    ]
  }
}
```

---

### 6.4 科目层级查询 | Subject Hierarchy Query

**Feature 37**: 查询科目树形结构

**端点**: `GET /admin/subjects/hierarchy`

**请求头**: 
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "subjects": [
      {
        "subjectId": 101,
        "subjectName": "Data Structures",
        "level": 1,
        "questionCount": 50,
        "children": [
          {
            "subjectId": 102,
            "subjectName": "Trees",
            "level": 2,
            "questionCount": 15,
            "parentSubjectId": 101
          },
          {
            "subjectId": 103,
            "subjectName": "Graphs",
            "level": 2,
            "questionCount": 12,
            "parentSubjectId": 101
          }
        ]
      }
    ]
  }
}
```

---

### 6.5 用户管理 | User Management

**端点**: `GET /admin/users`

**请求头**: 
```
Authorization: Bearer {token}
```

**查询参数**:
- `role` (可选): 按角色筛选 - `student`, `teacher`, `admin`
- `isActive` (可选): 按状态筛选 - `true`, `false`

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "users": [
      {
        "userId": 1,
        "username": "john_teacher",
        "role": "teacher",
        "fullName": "John Smith",
        "email": "john@university.edu",
        "isActive": true,
        "createdAt": "2025-01-01 10:00:00"
      }
    ],
    "statistics": {
      "totalUsers": 150,
      "students": 120,
      "teachers": 25,
      "admins": 5
    }
  }
}
```

---

### 6.6 系统总览 | System Overview

**端点**: `GET /admin/dashboard`

**请求头**: 
```
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "users": {
      "total": 150,
      "teachers": 25,
      "students": 120,
      "admins": 5
    },
    "courses": {
      "total": 45,
      "activeClassrooms": 32
    },
    "questions": {
      "total": 1250,
      "byType": {
        "multiple_choice": 950,
        "true_false": 150,
        "essay": 100,
        "fill_blank": 50
      }
    },
    "quizzes": {
      "total": 128,
      "active": 12,
      "completed": 95
    },
    "statistics": {
      "totalSubmissions": 3456,
      "averageScore": 74.5,
      "passRate": 82.3
    }
  }
}
```

---

## 7. 错误代码 | Error Codes

| 代码 | 说明 | 示例 |
|------|------|------|
| 200 | 成功 | 操作成功完成 |
| 400 | 参数错误 | 缺少必填字段，数据格式不正确 |
| 401 | 未授权 | Token缺失或无效 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 未找到 | 资源不存在 |
| 409 | 冲突 | 数据重复（如用户名已存在）|
| 500 | 服务器错误 | 内部错误 |

### 错误响应示例

```json
{
  "code": 401,
  "message": "Token已过期，请重新登录",
  "data": null,
  "timestamp": 1702369600000
}
```

---

## 8. 使用示例 | Usage Examples

### 8.1 完整流程示例 - 学生答题

```bash
# 1. 学生登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_student",
    "password": "password123",
    "role": "student"
  }'

# 响应：获得token
# {"code":200,"data":{"token":"eyJhbG..."}}

# 2. 查看可用测验
curl -X GET http://localhost:8080/api/student/quizzes \
  -H "Authorization: Bearer eyJhbG..."

# 3. 开始测验
curl -X POST http://localhost:8080/api/student/quizzes/301/start \
  -H "Authorization: Bearer eyJhbG..."

# 响应：获得studentQuizId=5001

# 4. 提交答案
curl -X POST http://localhost:8080/api/student/quizzes/5001/answers \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": 1001,
    "selectedOptionId": 2002
  }'

# 5. 完成测验
curl -X POST http://localhost:8080/api/student/quizzes/5001/submit \
  -H "Authorization: Bearer eyJhbG..."

# 6. 查看成绩（成绩发布后）
curl -X GET http://localhost:8080/api/student/grades \
  -H "Authorization: Bearer eyJhbG..."
```

---

### 8.2 完整流程示例 - 教师出题

```bash
# 1. 教师登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_teacher",
    "password": "teachpass",
    "role": "teacher"
  }'

# 2. 创建科目
curl -X POST http://localhost:8080/api/teacher/subjects \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{
    "subjectName": "Data Structures",
    "description": "DS topics",
    "level": 1
  }'

# 3. 批量上传题目
curl -X POST http://localhost:8080/api/teacher/questions/batch \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{
    "questions": [
      {
        "subjectId": 101,
        "questionText": "Question 1...",
        "questionType": "multiple_choice",
        "difficultyLevel": 2,
        "options": [...]
      }
    ]
  }'

# 4. 创建测验
curl -X POST http://localhost:8080/api/teacher/quizzes \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{
    "classroomId": 201,
    "title": "Midterm Exam",
    "startTime": "2025-12-15 09:00:00",
    "endTime": "2025-12-15 11:00:00",
    "durationMinutes": 120,
    "totalPoints": 100,
    "passingScore": 60,
    "questions": [...]
  }'

# 5. 查看班级成绩
curl -X GET http://localhost:8080/api/teacher/quizzes/301/grades \
  -H "Authorization: Bearer eyJhbG..."

# 6. 发布成绩
curl -X POST http://localhost:8080/api/teacher/quizzes/301/publish \
  -H "Authorization: Bearer eyJhbG..."
```

---

## 9. 附录 | Appendix

### 9.1 JSON文件格式规范 | JSON File Format Specification

#### 题目批量导入JSON格式

```json
{
  "version": "1.0",
  "importDate": "2025-12-12",
  "questions": [
    {
      "subjectId": 101,
      "questionText": "What is a binary tree?",
      "questionType": "multiple_choice",
      "difficultyLevel": 2,
      "tags": ["trees", "data-structures"],
      "options": [
        {
          "optionText": "A tree with at most two children",
          "isCorrect": true,
          "optionOrder": 1
        },
        {
          "optionText": "A tree with exactly two children",
          "isCorrect": false,
          "optionOrder": 2
        }
      ]
    }
  ]
}
```

#### 成绩导出JSON格式

```json
{
  "version": "1.0",
  "exportDate": "2025-12-12 16:00:00",
  "quizId": 301,
  "quizTitle": "Midterm Exam",
  "course": "Data Structures",
  "semester": "Fall 2025",
  "grades": [
    {
      "studentNumber": "STU2025001",
      "fullName": "Alice Johnson",
      "score": 85,
      "percentage": 85.0,
      "result": "Passed",
      "submitTime": "2025-12-10 11:30:00"
    }
  ],
  "statistics": {
    "totalStudents": 25,
    "averageScore": 75.6,
    "passRate": 87.0
  }
}
```

---

### 9.2 状态码定义 | Status Code Definitions

#### Quiz Status
- `available`: 测验可用，未开始
- `in_progress`: 正在进行
- `submitted`: 已提交
- `grading`: 评分中
- `completed`: 已完成

#### Enrollment Status
- `active`: 已注册
- `dropped`: 已退课
- `completed`: 已完成

---

## 📞 技术支持 | Technical Support

- **开发团队**: Exam System Team
- **版本**: v1.0
- **最后更新**: 2025-12-12

---

**文档结束 | End of Documentation**

