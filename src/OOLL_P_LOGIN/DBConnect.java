//DB연동
package OOLL_P_LOGIN;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DBConnect {

    // [DB 연결 정보]
    // DB명: academy_lms (이전 대화에서 설정)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "java2025";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * 데이터베이스 연결(Connection) 객체를 생성하여 반환합니다.
     * @return 새로운 Connection 객체
     * @throws SQLException DB 연결 또는 드라이버 로드 실패 시 예외 발생
     */
    public static Connection getConnection() throws SQLException {
        try {
            // 1. MySQL JDBC 드라이버를 메모리에 로드
            Class.forName(DB_DRIVER);
            // 2. 연결 정보로 Connection 객체 생성 및 반환
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            // SQLException으로 래핑하여 호출한 곳에서 처리하도록 전달
            throw new SQLException("데이터베이스 드라이버를 로드할 수 없습니다.", e);
        }
    }

    /**
     * 가변 인수를 사용하여 AutoCloseable 인터페이스를 구현하는 모든 DB 자원(Connection, Statement, ResultSet)을 안전하게 닫습니다.
     * @param resources 닫을 자원 목록
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // 자원 해제 중 발생하는 예외는 시스템 에러로 출력만 하고 무시
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }

    // 오버로드 메서드: 자주 사용하는 조합을 편리하게 닫기 위한 오버로드

    public static void close(Statement stmt, Connection conn) {
        close((AutoCloseable) stmt, (AutoCloseable) conn);
    }

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        close((AutoCloseable) rs, (AutoCloseable) stmt, (AutoCloseable) conn);
    }
}