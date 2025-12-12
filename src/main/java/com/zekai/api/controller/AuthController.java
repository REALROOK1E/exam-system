package com.zekai.api.controller;

import com.zekai.api.dto.ApiResponse;
import com.zekai.api.dto.LoginRequest;
import com.zekai.api.dto.LoginResponse;
import com.zekai.api.security.JwtUtil;
import com.zekai.util.DatabaseUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * ========================================
 * 认证控制器 - Authentication Controller
 * ========================================
 *
 * 处理用户登录认证，生成JWT令牌
 *
 * 端点：
 * - POST /auth/login - 用户登录
 *
 * @author Exam System Team
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * POST /auth/login
     *
     * 请求体：
     * {
     *   "username": "john_teacher",
     *   "password": "teachpass",
     *   "role": "teacher"
     * }
     *
     * 响应：
     * {
     *   "code": 200,
     *   "message": "登录成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzUxMiJ9...",
     *     "username": "john_teacher",
     *     "role": "teacher",
     *     "userId": 1,
     *     "roleId": 1,
     *     "fullName": "John Smith"
     *   }
     * }
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql;

            // 根据角色选择不同的查询SQL
            if ("student".equalsIgnoreCase(request.getRole())) {
                sql = "SELECT u.user_id, u.username, u.role, u.full_name, s.student_id " +
                      "FROM users u " +
                      "LEFT JOIN students s ON u.user_id = s.user_id " +
                      "WHERE u.username = ? AND u.password_hash = SHA2(?, 256) AND u.is_active = TRUE";
            } else if ("teacher".equalsIgnoreCase(request.getRole())) {
                sql = "SELECT u.user_id, u.username, u.role, u.full_name, t.teacher_id " +
                      "FROM users u " +
                      "LEFT JOIN teachers t ON u.user_id = t.user_id " +
                      "WHERE u.username = ? AND u.password_hash = SHA2(?, 256) AND u.is_active = TRUE";
            } else {
                sql = "SELECT u.user_id, u.username, u.role, u.full_name, u.user_id as admin_id " +
                      "FROM users u " +
                      "WHERE u.username = ? AND u.password_hash = SHA2(?, 256) AND u.is_active = TRUE";
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, request.getUsername());
            pstmt.setString(2, request.getPassword());

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Long userId = rs.getLong("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                String fullName = rs.getString("full_name");
                Long roleId = rs.getLong(5); // student_id, teacher_id, or admin_id

                // 生成JWT令牌
                String token = jwtUtil.generateToken(username, role, userId, roleId);

                LoginResponse response = new LoginResponse(token, username, role, userId, roleId, fullName);

                return ApiResponse.success("登录成功", response);
            } else {
                return ApiResponse.unauthorized("用户名或密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }
}

