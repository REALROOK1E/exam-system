# REST API 实现说明
# REST API Implementation Guide

## 📋 已创建的文件 | Created Files

### 1. 核心配置文件 | Core Configuration

```
src/main/
├── java/com/zekai/api/
│   ├── ExamSystemApiApplication.java    # Spring Boot主应用
│   ├── controller/
│   │   └── AuthController.java          # 认证控制器
│   ├── dto/
│   │   ├── ApiResponse.java             # 统一响应格式
│   │   ├── LoginRequest.java            # 登录请求DTO
│   │   ├── LoginResponse.java           # 登录响应DTO
│   │   ├── CreateQuestionRequest.java   # 创建题目请求DTO
│   │   ├── BatchUploadQuestionsRequest.java  # 批量上传DTO
│   │   └── CreateQuizRequest.java       # 创建测验请求DTO
│   ├── security/
│   │   ├── JwtUtil.java                 # JWT工具类
│   │   ├── JwtRequestFilter.java        # JWT过滤器
│   │   └── SecurityConfig.java          # Security配置
│   └── service/                          # 业务逻辑层（待实现）
└── resources/
    └── application.yml                   # 应用配置
```

### 2. API文档

- `API_DOCUMENTATION.md` - 完整的37个功能的REST API文档

---

## 🚀 快速开始 | Quick Start

### 1. 更新数据库配置

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system
    username: root
    password: YOUR_PASSWORD  # 修改为你的密码
```

### 2. 启动API服务器

```bash
# 方式1: 使用Maven
mvn spring-boot:run

# 方式2: 在IDEA中运行
# 打开 ExamSystemApiApplication.java
# 点击main方法左侧的绿色运行按钮
```

### 3. 测试API

```bash
# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_teacher",
    "password": "teachpass",
    "role": "teacher"
  }'
```

---

## 📦 如何实现其他Controller | How to Implement Other Controllers

### 示例：学生端Controller

创建文件：`src/main/java/com/zekai/api/controller/student/StudentQuizController.java`

```java
package com.zekai.api.controller.student;

import com.zekai.api.dto.ApiResponse;
import com.zekai.util.DatabaseUtil;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@RestController
@RequestMapping("/student")
public class StudentQuizController {
    
    /**
     * Feature 24: 查看可用测验
     */
    @GetMapping("/quizzes")
    public ApiResponse<?> getAvailableQuizzes(HttpServletRequest request) {
        // 从request attribute获取当前登录学生的ID
        Long studentId = (Long) request.getAttribute("roleId");
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT q.quiz_id, q.title, q.duration_minutes, c.course_name, " +
                        "sq.status as quiz_status " +
                        "FROM enrollments e " +
                        "JOIN classrooms cl ON e.classroom_id = cl.classroom_id " +
                        "JOIN courses c ON cl.course_id = c.course_id " +
                        "JOIN quizzes q ON cl.classroom_id = q.classroom_id " +
                        "LEFT JOIN student_quizzes sq ON q.quiz_id = sq.quiz_id AND sq.student_id = e.student_id " +
                        "WHERE e.student_id = ? AND e.status = 'active'";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            List<Map<String, Object>> quizzes = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("quizId", rs.getLong("quiz_id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("courseName", rs.getString("course_name"));
                quiz.put("durationMinutes", rs.getInt("duration_minutes"));
                quiz.put("myStatus", rs.getString("quiz_status"));
                quizzes.add(quiz);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("quizzes", quizzes);
            
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * Feature 21: 开始测验
     */
    @PostMapping("/quizzes/{quizId}/start")
    public ApiResponse<?> startQuiz(
            @PathVariable Long quizId,
            HttpServletRequest request) {
        
        Long studentId = (Long) request.getAttribute("roleId");
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 创建学生测验会话
            String sql = "INSERT INTO student_quizzes (quiz_id, student_id, start_time, status) " +
                        "VALUES (?, ?, NOW(), 'in_progress')";
            
            PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, quizId);
            pstmt.setLong(2, studentId);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                Long studentQuizId = rs.getLong(1);
                
                Map<String, Object> data = new HashMap<>();
                data.put("studentQuizId", studentQuizId);
                data.put("status", "in_progress");
                
                return ApiResponse.success("测验已开始", data);
            }
            
            return ApiResponse.error("开始测验失败");
        } catch (Exception e) {
            return ApiResponse.error("操作失败: " + e.getMessage());
        }
    }
}
```

---

## 🔑 关键实现点 | Key Implementation Points

### 1. 获取当前登录用户信息

```java
@GetMapping("/my-data")
public ApiResponse<?> getMyData(HttpServletRequest request) {
    // JWT过滤器已经将用户信息存储在request attribute中
    Long userId = (Long) request.getAttribute("userId");
    Long roleId = (Long) request.getAttribute("roleId");
    String role = (String) request.getAttribute("role");
    
    // 使用这些信息进行业务逻辑处理
}
```

### 2. 统一异常处理

创建 `src/main/java/com/zekai/api/exception/GlobalExceptionHandler.java`:

```java
package com.zekai.api.exception;

import com.zekai.api.dto.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        e.printStackTrace();
        return ApiResponse.error("服务器内部错误: " + e.getMessage());
    }
}
```

### 3. 跨域配置

已在 `SecurityConfig.java` 中配置CORS，允许所有来源。

### 4. 参数校验

使用 `@Valid` 注解和 Jakarta Validation：

```java
@PostMapping("/questions")
public ApiResponse<?> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
    // 如果request中的字段不满足@NotBlank等注解要求，会自动返回400错误
}
```

---

## 📝 待实现的Controller | Controllers to Implement

### 学生端 (11个API)

- [ ] `POST /student/register` - Feature 1: 创建学生账户
- [ ] `POST /student/enrollments` - Feature 8: 注册课程
- [ ] `GET /student/classrooms/{id}/students` - Feature 9: 查询同学
- [ ] `DELETE /student/enrollments/{id}` - Feature 10: 退课
- [ ] `GET /student/quizzes` - Feature 24: 查看可用测验 ✅ (示例已提供)
- [ ] `POST /student/quizzes/{id}/start` - Feature 21: 开始测验 ✅ (示例已提供)
- [ ] `POST /student/quizzes/{id}/answers` - Feature 22: 提交答案
- [ ] `POST /student/quizzes/{id}/submit` - Feature 23: 完成测验
- [ ] `GET /student/grades` - Feature 28: 查看成绩
- [ ] `GET /student/quizzes/{id}/details` - Feature 29: 查看答案详情

### 教师端 (18个API)

- [ ] `POST /teacher/register` - Feature 2: 创建教师账户
- [ ] `POST /teacher/courses` - Feature 6: 创建课程
- [ ] `POST /teacher/classrooms` - Feature 7: 创建教室
- [ ] `POST /teacher/subjects` - Feature 11: 创建科目
- [ ] `POST /teacher/questions` - Feature 12: 创建题目
- [ ] `POST /teacher/questions/batch` - Feature 14: 批量上传题目
- [ ] `GET /teacher/questions/statistics` - Feature 15: 题目统计
- [ ] `POST /teacher/quizzes` - Feature 16: 创建测验
- [ ] `GET /teacher/questions/random` - Feature 17: 随机选题
- [ ] `POST /teacher/quizzes/{id}/grade` - Feature 25: 自动评分
- [ ] `POST /teacher/quizzes/{id}/publish` - Feature 27: 发布成绩
- [ ] `GET /teacher/quizzes/{id}/grades` - Feature 30: 查看班级成绩
- [ ] `GET /teacher/questions/{id}/analysis` - Feature 31: 难度分析
- [ ] `GET /teacher/quizzes/{id}/report` - Feature 32: 成绩报告

### 管理后台 (7个API)

- [ ] `POST /admin/questions/update-statistics` - Feature 33: 更新统计
- [ ] `GET /admin/questions/difficulty-rating` - Feature 34: 难度评级
- [ ] `GET /admin/questions/ranking` - Feature 36: 使用排名
- [ ] `GET /admin/subjects/hierarchy` - Feature 37: 科目层级
- [ ] `GET /admin/users` - 用户管理
- [ ] `GET /admin/dashboard` - 系统总览

---

## 🧪 测试工具 | Testing Tools

### 1. 使用Postman

1. 导入API文档中的请求示例
2. 设置环境变量：
   - `baseUrl`: `http://localhost:8080/api`
   - `token`: 登录后获得的JWT令牌

3. 在每个请求的Headers中添加：
   ```
   Authorization: Bearer {{token}}
   ```

### 2. 使用cURL

```bash
# 保存token到变量
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# 使用token访问受保护的API
curl -X GET http://localhost:8080/api/student/quizzes \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔧 开发建议 | Development Tips

### 1. 代码复用

创建Service层来复用业务逻辑：

```
src/main/java/com/zekai/api/service/
├── StudentService.java
├── TeacherService.java
├── QuizService.java
└── QuestionService.java
```

### 2. 事务管理

对于涉及多表操作的API，使用 `@Transactional` 注解：

```java
@Service
public class QuizService {
    
    @Transactional
    public Long createQuizWithQuestions(CreateQuizRequest request) {
        // 创建测验
        // 添加题目
        // 设置配置
        // 如果任何步骤失败，全部回滚
    }
}
```

### 3. 数据库连接池

已配置HikariCP，DatabaseUtil会自动使用连接池。

---

## 📄 JSON通信示例 | JSON Communication Examples

### 请求示例

```json
POST /api/teacher/questions
Content-Type: application/json
Authorization: Bearer eyJhbGc...

{
  "subjectId": 101,
  "questionText": "What is a binary tree?",
  "questionType": "multiple_choice",
  "difficultyLevel": 2,
  "options": [
    {
      "optionText": "A tree with at most 2 children",
      "isCorrect": true,
      "optionOrder": 1
    },
    {
      "optionText": "A tree with 3 children",
      "isCorrect": false,
      "optionOrder": 2
    }
  ]
}
```

### 响应示例

```json
{
  "code": 200,
  "message": "题目创建成功",
  "data": {
    "questionId": 1001,
    "subjectId": 101,
    "questionType": "multiple_choice",
    "optionsCount": 2
  },
  "timestamp": 1702369600000
}
```

---

## 🎯 下一步 | Next Steps

1. ✅ **已完成**:
   - Spring Boot项目结构
   - JWT认证系统
   - 统一响应格式
   - Security配置
   - 认证Controller
   - 完整API文档

2. **需要实现**:
   - 其余36个Controller方法
   - Service业务逻辑层
   - 全局异常处理
   - 日志记录
   - 单元测试

3. **如何继续开发**:
   - 参考 `AuthController.java` 和示例Controller
   - 参照 `API_DOCUMENTATION.md` 中的接口定义
   - 复用现有的JUnit测试逻辑
   - 使用DatabaseUtil进行数据库操作

---

## 📞 帮助 | Help

如需实现具体的Controller，请告诉我：
1. 要实现哪个Feature（如Feature 8: 学生注册课程）
2. 我会提供完整的Controller代码

所有37个功能的详细API规范已在 `API_DOCUMENTATION.md` 中定义！

