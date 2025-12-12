package com.zekai.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * ========================================
 * EXAM SYSTEM REST API APPLICATION
 * ========================================
 *
 * Spring Boot主应用程序入口
 * 提供RESTful API接口，支持JSON通信
 *
 * 启动方式：
 * - 运行此类的main方法
 * - 或使用命令：mvn spring-boot:run
 *
 * API文档地址：http://localhost:8080/api-docs.json
 *
 * @author Exam System Team
 * @version 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.zekai"})
public class ExamSystemApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamSystemApiApplication.class, args);
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 Exam System REST API Started Successfully!");
        System.out.println("📍 Server: http://localhost:8080");
        System.out.println("📚 API Documentation: http://localhost:8080/api-docs.json");
        System.out.println("=".repeat(80) + "\n");
    }
}

