package com.zekai.util;


/**
 * @author Zekai
 * @date 2025/11/17
 * @Descripson :
 */


import com.zekai.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ========================================
 * DATABASE UTILITY CLASS
 * ========================================
 *
 * Provides common database operations including:
 * - Connection management
 * - Resource cleanup
 * - DDL execution
 * - Query helpers
 * - Result set formatting
 *
 * @author Exam System Team
 * @version 1.0
 */
public class DatabaseUtil {

    // ==================== Connection Management ====================

    /**
     * Get a database connection
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load JDBC driver
            Class.forName(DatabaseConfig.DB_DRIVER);

            if (DatabaseConfig.DEBUG_MODE) {
                System.out.println("→ Connecting to database: " + DatabaseConfig.DB_NAME);
            }

            // Create connection
            Connection conn = DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );

            // Set auto-commit mode
            conn.setAutoCommit(DatabaseConfig.AUTO_COMMIT);

            if (DatabaseConfig.DEBUG_MODE) {
                System.out.println("✓ Database connection established");
            }

            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. " +
                    "Please add mysql-connector-java to your classpath.", e);
        }
    }

    /**
     * Test database connection
     *
     * @return true if connection successful
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("✓ Connection Test Successful");
            System.out.println("  Database: " + metaData.getDatabaseProductName());
            System.out.println("  Version:  " + metaData.getDatabaseProductVersion());
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Connection Test Failed: " + e.getMessage());
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    // ==================== Resource Management ====================

    /**
     * Close database resources safely
     * Handles null checks and exceptions
     *
     * @param rs   ResultSet to close
     * @param stmt Statement to close
     * @param conn Connection to close
     */
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }

    /**
     * Close ResultSet safely
     * @param rs ResultSet to close
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
            }
        }
    }

    /**
     * Close Statement safely
     * @param stmt Statement to close
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing Statement: " + e.getMessage());
            }
        }
    }

    /**
     * Close Connection safely
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                if (DatabaseConfig.DEBUG_MODE) {
                    System.out.println("✓ Database connection closed");
                }
            } catch (SQLException e) {
                System.err.println("Error closing Connection: " + e.getMessage());
            }
        }
    }

    // ==================== DDL Operations ====================

    /**
     * Execute DDL statement (CREATE, ALTER, DROP)
     *
     * @param sql DDL SQL statement
     * @return true if successful, false otherwise
     */
    public static boolean executeDDL(String sql) {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            if (DatabaseConfig.DEBUG_MODE) {
                System.out.println("→ Executing DDL: " +
                        sql.substring(0, Math.min(60, sql.length())) + "...");
            }

            stmt.execute(sql);

            if (DatabaseConfig.DEBUG_MODE) {
                System.out.println("✓ DDL executed successfully");
            }

            return true;

        } catch (SQLException e) {
            System.err.println("✗ DDL Execution Error: " + e.getMessage());
            if (DatabaseConfig.DEBUG_MODE) {
                e.printStackTrace();
            }
            return false;

        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * Execute multiple DDL statements
     *
     * @param sqlStatements Array of SQL statements
     * @return Number of successful executions
     */
    public static int executeDDLBatch(String[] sqlStatements) {
        int successCount = 0;

        for (String sql : sqlStatements) {
            if (executeDDL(sql)) {
                successCount++;
            }
        }

        return successCount;
    }

    // ==================== Query Helpers ====================

    /**
     * Execute a SELECT query and return ResultSet
     * Note: Caller is responsible for closing resources!
     *
     * @param sql SELECT SQL statement
     * @return ResultSet with query results
     * @throws SQLException if query fails
     */
    public static ResultSet executeQuery(String sql) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        if (DatabaseConfig.DEBUG_MODE) {
            System.out.println("→ Executing Query: " +
                    sql.substring(0, Math.min(60, sql.length())) + "...");
        }

        return stmt.executeQuery(sql);
    }

    /**
     * Execute an UPDATE/INSERT/DELETE statement
     *
     * @param sql DML SQL statement
     * @return Number of affected rows
     * @throws SQLException if execution fails
     */
    public static int executeUpdate(String sql) throws SQLException {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            if (DatabaseConfig.DEBUG_MODE) {
                System.out.println("→ Executing Update: " +
                        sql.substring(0, Math.min(60, sql.length())) + "...");
            }

            int affectedRows = stmt.executeUpdate(sql);

            if (DatabaseConfig.DEBUG_MODE) {
                System.out.println("✓ Affected rows: " + affectedRows);
            }

            return affectedRows;

        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * Check if a table exists in the database
     *
     * @param tableName Name of the table
     * @return true if table exists
     */
    public static boolean tableExists(String tableName) {
        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking table existence: " + e.getMessage());
            return false;

        } finally {
            closeResources(rs, null, conn);
        }
    }

    /**
     * Get list of all tables in database
     *
     * @return List of table names
     */
    public static List<String> getAllTables() {
        List<String> tables = new ArrayList<>();
        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(DatabaseConfig.DB_NAME, null, "%", new String[]{"TABLE"});

            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting table list: " + e.getMessage());

        } finally {
            closeResources(rs, null, conn);
        }

        return tables;
    }

    // ==================== Result Set Formatting ====================

    /**
     * Print ResultSet in formatted table
     *
     * @param rs ResultSet to print
     * @throws SQLException if reading fails
     */
    public static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Print header
        System.out.println("\n" + "=".repeat(100));
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("%-25s", metaData.getColumnName(i));
        }
        System.out.println();
        System.out.println("-".repeat(100));

        // Print rows
        int rowCount = 0;
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String value = rs.getString(i);
                if (value != null && value.length() > 22) {
                    value = value.substring(0, 19) + "...";
                }
                System.out.printf("%-25s", value == null ? "NULL" : value);
            }
            System.out.println();
            rowCount++;
        }

        System.out.println("=".repeat(100));
        System.out.println("Total rows: " + rowCount + "\n");
    }

    /**
     * Print ResultSet with custom column widths
     *
     * @param rs ResultSet to print
     * @param columnWidths Array of column widths
     * @throws SQLException if reading fails
     */
    public static void printResultSet(ResultSet rs, int[] columnWidths) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Calculate total width
        int totalWidth = 0;
        for (int width : columnWidths) {
            totalWidth += width + 1;
        }

        // Print header
        System.out.println("\n" + "=".repeat(totalWidth));
        for (int i = 1; i <= columnCount && i <= columnWidths.length; i++) {
            System.out.printf("%-" + columnWidths[i-1] + "s ", metaData.getColumnName(i));
        }
        System.out.println();
        System.out.println("-".repeat(totalWidth));

        // Print rows
        while (rs.next()) {
            for (int i = 1; i <= columnCount && i <= columnWidths.length; i++) {
                String value = rs.getString(i);
                if (value != null && value.length() > columnWidths[i-1]) {
                    value = value.substring(0, columnWidths[i-1] - 3) + "...";
                }
                System.out.printf("%-" + columnWidths[i-1] + "s ", value == null ? "NULL" : value);
            }
            System.out.println();
        }

        System.out.println("=".repeat(totalWidth) + "\n");
    }

    /**
     * Get row count from ResultSet
     *
     * @param rs ResultSet
     * @return Number of rows
     * @throws SQLException if counting fails
     */
    public static int getRowCount(ResultSet rs) throws SQLException {
        int count = 0;
        while (rs.next()) {
            count++;
        }
        rs.beforeFirst(); // Reset cursor
        return count;
    }

    // ==================== Transaction Management ====================

    /**
     * Begin a transaction
     * @param conn Connection object
     * @throws SQLException if operation fails
     */
    public static void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        if (DatabaseConfig.DEBUG_MODE) {
            System.out.println("→ Transaction started");
        }
    }

    /**
     * Commit a transaction
     * @param conn Connection object
     * @throws SQLException if operation fails
     */
    public static void commitTransaction(Connection conn) throws SQLException {
        conn.commit();
        if (DatabaseConfig.DEBUG_MODE) {
            System.out.println("✓ Transaction committed");
        }
    }

    /**
     * Rollback a transaction
     * @param conn Connection object
     */
    public static void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                if (DatabaseConfig.DEBUG_MODE) {
                    System.out.println("⚠ Transaction rolled back");
                }
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Escape special characters in SQL strings
     * @param input Input string
     * @return Escaped string
     */
    public static String escapeSql(String input) {
        if (input == null) return null;
        return input.replace("'", "''")
                .replace("\\", "\\\\");
    }

    /**
     * Print database information
     */
    public static void printDatabaseInfo() {
        Connection conn = null;
        try {
            conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();

            System.out.println("\n========================================");
            System.out.println("DATABASE INFORMATION");
            System.out.println("========================================");
            System.out.println("Database:     " + metaData.getDatabaseProductName());
            System.out.println("Version:      " + metaData.getDatabaseProductVersion());
            System.out.println("Driver:       " + metaData.getDriverName());
            System.out.println("Driver Ver:   " + metaData.getDriverVersion());
            System.out.println("URL:          " + metaData.getURL());
            System.out.println("User:         " + metaData.getUserName());
            System.out.println("========================================\n");

        } catch (SQLException e) {
            System.err.println("Error getting database info: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }
}
