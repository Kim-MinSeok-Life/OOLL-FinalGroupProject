package teamwork;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

    // [DB 연결 정보]
    // DB명: academy_lms (이전 대화에서 설정)
    // URL: jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC
    // User: root
    // Password: java2025

    // 이전에 주신 URL에서 DB명을 academy_lms로 가정하여 수정했습니다.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "java2025";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // 데이터베이스 연결을 반환하는 메서드
    public static Connection getConnection() throws SQLException {
        try {
            // 1. JDBC 드라이버 로드
            Class.forName(DB_DRIVER);
            // 2. 연결 반환
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            // 드라이버를 찾지 못했을 때의 예외 처리
            System.err.println("MySQL JDBC Driver not found.");
            throw new SQLException("데이터베이스 드라이버를 로드할 수 없습니다.", e);
        }
    }

    // Connection, Statement, ResultSet을 닫는 유틸리티 메서드 (자원 해제)
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // 닫는 중 발생하는 예외는 무시하거나 로깅할 수 있습니다.
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }
}