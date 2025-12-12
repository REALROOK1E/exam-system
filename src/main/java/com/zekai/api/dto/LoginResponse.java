package com.zekai.api.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private Long userId;
    private Long roleId; // teacher_id or student_id
    private String fullName;

    public LoginResponse() {}

    public LoginResponse(String token, String username, String role, Long userId, Long roleId, String fullName) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.roleId = roleId;
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

