# 在线考试管理系统 | Online Exam Management System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📖 项目简介 | Project Overview

这是一个功能完整的在线考试管理系统，支持教师出题、学生答题、自动评分、成绩统计等完整的考试流程。系统采用Java开发，使用MySQL数据库，实现了37个核心功能。

This is a comprehensive online exam management system that supports the complete exam workflow including teacher question creation, student exam-taking, automatic grading, and grade statistics. The system is developed in Java with MySQL database, implementing 37 core features.

## ✨ 核心功能 | Core Features

### 🔐 用户权限管理 | User & Permission Management (5 features)
- 创建学生/教师账户 | Create student/teacher accounts
- 用户登录认证 | User authentication
- 课程注册管理 | Course enrollment management
- 权限控制 | Permission control

### 📚 课程和教室管理 | Course & Classroom Management (5 features)
- 创建课程 | Create courses
- 创建教室 | Create classrooms
- 学生注册/退课 | Student enrollment/withdrawal
- 查询教室学生 | Query classroom students

### 💡 题库管理 | Question Bank Management (5 features)
- 创建科目分类 | Create subject categories
- 单个/批量创建题目 | Create single/multiple questions
- 添加题目选项 | Add question options
- 题目统计查询 | Question statistics query

### 📝 测验生成管理 | Quiz Generation Management (5 features)
- 创建测验 | Create quizzes
- 随机选题 | Random question selection
- 配置测验设置 | Configure quiz settings
- 查看测验详情 | View quiz details

### 📋 考试管理 | Exam Management (4 features)
- 学生开始测验 | Student start quiz
- 提交答案 | Submit answers
- 完成测验 | Complete quiz submission
- 查看可用测验 | View available quizzes

### ⚡ 自动评分 | Auto Grading (3 features)
- 客观题自动评分 | Automatic grading for objective questions
- 计算总分 | Calculate total score
- 发布成绩 | Publish grades

### 📊 成绩查询与统计 | Grade Query & Statistics (5 features)
- 学生查看成绩 | Student view grades
- 查看答案详情 | View answer details
- 教师查看班级成绩 | Teacher view class grades
- 题目难度分析 | Question difficulty analysis
- 生成成绩报告 | Generate grade report

### 🚀 高级功能 | Advanced Features (5 features)
- 更新题目统计 | Update question statistics
- 自适应难度评级 | Adaptive difficulty rating
- 查看教师的测验 | View teacher's quizzes
- 题目使用排名 | Question usage ranking
- 科目层级查询 | Subject hierarchy query

## 🛠️ 技术栈 | Tech Stack

- **后端语言 | Backend:** Java 17
- **数据库 | Database:** MySQL 8.0+
- **构建工具 | Build Tool:** Maven 3.6+
- **JDBC驱动 | JDBC Driver:** MySQL Connector/J 8.0.33
- **连接池 | Connection Pool:** HikariCP 5.0.1

## 📋 前置要求 | Prerequisites

在开始之前，请确保您的系统已安装以下软件：

Before you begin, ensure you have the following installed:

- Java JDK 17 或更高版本 | Java JDK 17 or higher
- MySQL 8.0 或更高版本 | MySQL 8.0 or higher
- Maven 3.6 或更高版本 | Maven 3.6 or higher

## 🚀 快速开始 | Quick Start

### 1. 克隆项目 | Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/exam-system.git
cd exam-system
```

### 2. 配置数据库 | Configure Database

1. 创建MySQL数据库 | Create MySQL database:
```sql
CREATE DATABASE exam_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改数据库配置 | Modify database configuration:

编辑 `src/main/java/com/zekai/config/DatabaseConfig.java`，更新以下配置：

Edit `src/main/java/com/zekai/config/DatabaseConfig.java` and update:

```java
public static final String DB_URL = "jdbc:mysql://localhost:3306/exam_system?...";
public static final String DB_USER = "root";          // 您的数据库用户名 | Your DB username
public static final String DB_PASSWORD = "root";      // 您的数据库密码 | Your DB password
```

### 3. 编译项目 | Compile the Project

```bash
mvn clean compile
```

### 4. 运行程序 | Run the Application

```bash
mvn exec:java
```

程序将自动创建所有必需的数据库表，并运行完整的功能测试。

The program will automatically create all required database tables and run a complete feature test.

## 📁 项目结构 | Project Structure

```
Mylab/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── zekai/
│                   ├── comment/          # 主程序 | Main program
│                   │   └── Main.java
│                   ├── config/           # 配置文件 | Configuration
│                   │   └── DatabaseConfig.java
│                   └── util/             # 工具类 | Utilities
│                       └── DatabaseUtil.java
├── pom.xml                               # Maven配置 | Maven config
├── README.md                             # 项目说明 | Project readme
└── FEATURES_DOCUMENTATION.md             # 功能详解 | Feature documentation
```

## 📖 数据库设计 | Database Design

系统包含14张核心数据表：

The system includes 14 core database tables:

1. **users** - 用户基本信息 | User basic information
2. **teachers** - 教师信息 | Teacher information
3. **students** - 学生信息 | Student information
4. **courses** - 课程信息 | Course information
5. **classrooms** - 教室信息 | Classroom information
6. **enrollments** - 学生注册信息 | Student enrollment
7. **subjects** - 科目分类 | Subject categories
8. **questions** - 题目信息 | Question information
9. **question_options** - 题目选项 | Question options
10. **quizzes** - 测验信息 | Quiz information
11. **quiz_questions** - 测验题目关联 | Quiz-question mapping
12. **quiz_settings** - 测验设置 | Quiz settings
13. **student_quizzes** - 学生测验记录 | Student quiz records
14. **student_answers** - 学生答案 | Student answers

详细的数据库设计和功能说明请参考 [FEATURES_DOCUMENTATION.md](FEATURES_DOCUMENTATION.md)

For detailed database design and feature documentation, see [FEATURES_DOCUMENTATION.md](FEATURES_DOCUMENTATION.md)

## 🎯 使用示例 | Usage Example

运行程序后，系统将自动执行以下测试流程：

After running the program, the system will automatically execute the following test workflow:

1. ✅ 创建数据库表 | Create database tables
2. ✅ 创建教师和学生账户 | Create teacher and student accounts
3. ✅ 创建课程和教室 | Create courses and classrooms
4. ✅ 学生注册课程 | Student course enrollment
5. ✅ 创建题库 | Create question bank
6. ✅ 生成测验 | Generate quiz
7. ✅ 学生答题 | Student takes exam
8. ✅ 自动评分 | Automatic grading
9. ✅ 成绩统计分析 | Grade statistics and analysis

## 🔧 配置选项 | Configuration Options

在 `DatabaseConfig.java` 中可以配置：

You can configure in `DatabaseConfig.java`:

- **DEBUG_MODE** - 调试模式，显示详细SQL日志 | Debug mode for detailed SQL logs
- **AUTO_COMMIT** - 事务自动提交 | Transaction auto-commit
- **连接池设置** | Connection pool settings (MAX_POOL_SIZE, MIN_IDLE, etc.)

## 📊 运行示例输出 | Sample Output

```
================================================================================
EXAM SYSTEM - COMPREHENSIVE FUNCTIONALITY TEST
Testing all 37 core features
================================================================================

SETUP: Creating Database Tables
✓ All 14 tables created successfully

SECTION 1: User & Permission Management
✓ Student account created successfully
✓ Teacher account created successfully
✓ User authentication completed

SECTION 2: Course & Classroom Management
✓ Course created successfully
✓ Classroom created successfully
...

✓ ALL 37 FEATURES TESTED SUCCESSFULLY!
================================================================================
```

## 🤝 贡献 | Contributing

欢迎提交问题和拉取请求！

Issues and pull requests are welcome!

1. Fork 本仓库 | Fork the repository
2. 创建特性分支 | Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. 提交更改 | Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 | Push to the branch (`git push origin feature/AmazingFeature`)
5. 打开拉取请求 | Open a Pull Request

## 📝 许可证 | License

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 作者 | Authors

- **Exam System Team** - *Initial work*

## 🙏 致谢 | Acknowledgments

- 感谢所有为这个项目做出贡献的人 | Thanks to all contributors to this project
- 灵感来自现代在线教育平台 | Inspired by modern online education platforms

## 📞 联系方式 | Contact

如有问题或建议，请通过以下方式联系：

For questions or suggestions, please contact via:

- 提交 Issue | Submit an Issue
- 发送邮件 | Email: your-email@example.com

---

**⭐ 如果这个项目对您有帮助，请给我们一个星标！**

**⭐ If this project helps you, please give us a star!**
