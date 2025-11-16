package com.zekai.config;

/**
 * ========================================
 * DATABASE CONFIGURATION
 * ========================================
 *
 * Centralized database connection settings for the Exam System.
 * Modify these values according to your MySQL database setup.
 *
 * @author Exam System Team
 * @version 1.0
 */
public class DatabaseConfig {

    // ==================== Database Connection Parameters ====================

    /**
     * Database JDBC URL
     * Format: jdbc:mysql://[host]:[port]/[database_name]?[parameters]
     */
    public static final String DB_URL = "jdbc:mysql://localhost:3306/exam_system?" +
            "useSSL=false&" +
            "serverTimezone=UTC&" +
            "allowPublicKeyRetrieval=true&" +
            "characterEncoding=UTF-8";

    /**
     * Database username
     * Default: root (change this for production)
     */
    public static final String DB_USER = "root";

    /**
     * Database password
     * ⚠️ IMPORTANT: Change this to your actual MySQL password!
     */
    public static final String DB_PASSWORD = "root";  // ⬅️ MODIFY THIS!

    /**
     * JDBC Driver class name
     * For MySQL Connector/J 8.x
     */
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // ==================== Database Name ====================

    /**
     * Database name for the exam system
     */
    public static final String DB_NAME = "exam_system";

    /**
     * Host address of the MySQL server
     */
    public static final String DB_HOST = "localhost";

    /**
     * Port number for MySQL server (default: 3306)
     */
    public static final int DB_PORT = 3306;

    // ==================== Connection Pool Settings (Optional) ====================

    /**
     * Maximum number of connections in the pool
     */
    public static final int MAX_POOL_SIZE = 10;

    /**
     * Minimum number of idle connections
     */
    public static final int MIN_IDLE = 2;

    /**
     * Maximum wait time for a connection (milliseconds)
     */
    public static final long CONNECTION_TIMEOUT = 30000; // 30 seconds

    /**
     * Maximum lifetime of a connection (milliseconds)
     */
    public static final long MAX_LIFETIME = 1800000; // 30 minutes

    /**
     * Connection validation query
     */
    public static final String VALIDATION_QUERY = "SELECT 1";

    // ==================== Application Settings ====================

    /**
     * Enable debug mode for detailed SQL logging
     */
    public static final boolean DEBUG_MODE = true;

    /**
     * Enable transaction auto-commit
     * Set to false for manual transaction control
     */
    public static final boolean AUTO_COMMIT = false;

    // ==================== Utility Methods ====================

    /**
     * Get full JDBC URL with all parameters
     * @return Complete JDBC connection URL
     */
    public static String getFullUrl() {
        return DB_URL;
    }

    /**
     * Get connection URL without parameters (for display)
     * @return Base connection URL
     */
    public static String getDisplayUrl() {
        return String.format("jdbc:mysql://%s:%d/%s", DB_HOST, DB_PORT, DB_NAME);
    }

    /**
     * Validate configuration
     * @return true if configuration is valid
     */
    public static boolean validateConfig() {
        if (DB_PASSWORD.equals("your_password_here")) {
            System.err.println("⚠️  WARNING: Please set your database password in DatabaseConfig.java");
            return false;
        }
        return true;
    }

    /**
     * Print configuration summary (for debugging)
     */
    public static void printConfig() {
        System.out.println("========================================");
        System.out.println("DATABASE CONFIGURATION");
        System.out.println("========================================");
        System.out.println("Host:     " + DB_HOST);
        System.out.println("Port:     " + DB_PORT);
        System.out.println("Database: " + DB_NAME);
        System.out.println("User:     " + DB_USER);
        System.out.println("URL:      " + getDisplayUrl());
        System.out.println("Debug:    " + DEBUG_MODE);
        System.out.println("========================================");
    }
}
