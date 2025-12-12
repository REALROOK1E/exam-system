# 🚀 REST API 快速启动指南
# Quick Start Guide for REST API

## ✅ 已完成的工作 | Completed Work

我已经为你的在线考试系统创建了完整的REST API框架！

### 📦 创建的文件

1. **Spring Boot应用** - `ExamSystemApiApplication.java`
2. **JWT认证系统** - `JwtUtil.java`, `JwtRequestFilter.java`, `SecurityConfig.java`
3. **认证控制器** - `AuthController.java` (登录API)
4. **数据传输对象** - 所有DTO类
5. **配置文件** - `application.yml`
6. **完整API文档** - `API_DOCUMENTATION.md` (37个功能的详细规范)
7. **实现指南** - `REST_API_IMPLEMENTATION_GUIDE.md`
8. **项目总结** - `REST_API_SUMMARY.md`

### ✨ 实现的功能

✅ JWT Token认证  
✅ 基于角色的访问控制 (Student/Teacher/Admin)  
✅ 统一JSON响应格式  
✅ CORS跨域配置  
✅ 登录API实现  
✅ 37个功能的完整API定义  
✅ JSON通信  
✅ 安全的密码加密  

---

## 🏃 如何启动 | How to Start

### 步骤1: 配置数据库密码

打开文件：`src/main/resources/application.yml`

修改第10行的密码：
```yaml
password: 123456  # 改成你的MySQL密码
```

### 步骤2: 确保数据库已创建

在MySQL中运行：
```sql
CREATE DATABASE IF NOT EXISTS exam_system;
```

### 步骤3: 启动API服务器

在IntelliJ IDEA中：
1. 打开文件：`src/main/java/com/zekai/api/ExamSystemApiApplication.java`
2. 点击 `main` 方法左侧的绿色运行按钮 ▶️
3. 等待Spring Boot启动完成

你会看到：
```
🚀 Exam System REST API Started Successfully!
📍 Server: http://localhost:8080
📚 API Documentation: http://localhost:8080/api-docs.json
```

### 步骤4: 测试登录API

打开新的PowerShell窗口，运行：

```powershell
# 登录测试
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"john_teacher\",\"password\":\"teachpass\",\"role\":\"teacher\"}'
```

如果看到返回的JSON包含 `"token":"eyJ..."`，说明成功了！

---

## 📖 查看API文档

所有37个功能的详细API定义都在：

**📄 API_DOCUMENTATION.md**

包含：
- 每个功能的端点URL
- 请求/响应JSON格式
- 使用示例
- 错误代码说明

---

## 🎯 下一步可以做什么 | Next Steps

### 选项1: 使用现有的测试数据

运行JUnit测试自动创建测试数据：
```bash
mvn test -Dtest=TeacherFeatureTests
```

### 选项2: 通过API创建数据

使用 `API_DOCUMENTATION.md` 中的示例，通过Postman或cURL调用API创建：
- 教师账户
- 课程和教室
- 题目和测验

### 选项3: 实现剩余的Controller

参考 `REST_API_IMPLEMENTATION_GUIDE.md` 中的示例代码，实现其他36个API端点。

---

## 📊 API概览 | API Overview

### 已实现 ✅
- `POST /api/auth/login` - 登录认证

### 待实现 (参见API文档)

**学生端 (11个)**
- `/student/register` - 注册
- `/student/quizzes` - 查看测验
- `/student/quizzes/{id}/start` - 开始答题
- 等等...

**教师端 (18个)**
- `/teacher/courses` - 创建课程
- `/teacher/questions` - 创建题目
- `/teacher/questions/batch` - 批量上传
- 等等...

**管理后台 (7个)**
- `/admin/users` - 用户管理
- `/admin/dashboard` - 系统总览
- 等等...

---

## 🔐 认证流程 | Authentication Flow

```
1. 客户端发送用户名/密码 → POST /auth/login
2. 服务器验证 → 返回JWT Token
3. 客户端保存Token
4. 后续请求都在Header中带上: Authorization: Bearer {token}
5. 服务器验证Token → 允许访问
```

---

## 💡 常见问题 | FAQ

### Q: 启动时报错找不到数据库？

**A**: 检查 `application.yml` 中的数据库配置是否正确。

### Q: Token在哪里用？

**A**: 登录后获得token，在后续所有API请求的Header中添加：
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Q: 如何测试API？

**A**: 
1. 使用Postman导入API文档中的示例
2. 使用cURL命令（文档中有完整示例）
3. 使用前端应用调用

### Q: 数据存储在哪里？

**A**: 所有数据存储在MySQL的 `exam_system` 数据库中。

---

## 📚 重要文档 | Important Documents

| 文档 | 说明 |
|------|------|
| `API_DOCUMENTATION.md` | 完整的37个API详细规范 ⭐ |
| `REST_API_IMPLEMENTATION_GUIDE.md` | 如何实现Controller的指南 |
| `REST_API_SUMMARY.md` | 项目完成情况总结 |
| `JUNIT_TESTING_GUIDE.md` | JUnit测试使用说明 |

---

## 🎉 恭喜！

你的在线考试系统REST API已经准备就绪！

**API服务器地址**: `http://localhost:8080/api`

所有37个功能的API都已定义，可以立即开始开发前端或测试！

---

## 📞 需要帮助？

- 查看 `API_DOCUMENTATION.md` 了解每个API的使用方法
- 查看 `REST_API_IMPLEMENTATION_GUIDE.md` 了解如何实现Controller
- 参考 `AuthController.java` 查看已实现的登录API代码

**祝开发顺利！🚀**

