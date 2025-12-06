package OOLL_P_Login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBConnect {

    // [DB 연결 정보]
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
            // MySQL JDBC 드라이버를 메모리에 로드 (JDBC 4.0 이상에서는 자동 로드되나, 안정성을 위해 유지)
            Class.forName(DB_DRIVER);
            // 연결 정보로 Connection 객체 생성 및 반환
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            // SQLException으로 래핑하여 호출한 곳에서 처리하도록 전달
            throw new SQLException("데이터베이스 드라이버를 로드할 수 없습니다.", e);
        }
    }

    /**
     * 현재 등록된 강사 중 가장 최근에 등록된 강사의 시급(hourly_rate)을 조회합니다.
     * 강사가 한 명도 없을 경우 기본값(30000)을 반환합니다.
     * @param extConn 외부에서 받은 Connection 객체 (트랜잭션 일관성을 위해 사용)
     * @return 조회된 시급, 강사가 없을 경우 기본 시급 (30000)
     */
    public static int getLatestTeacherRate(Connection extConn) {
        // extConn을 사용하므로 Connection 객체를 새로 선언하거나 닫지 않습니다.
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 강사 테이블에 데이터가 없을 경우 사용할 기본 시급
        int latestRate = 30000;

        // teacher_id를 기준으로 내림차순 정렬하여 가장 최근에 추가된 하나의 레코드를 조회
        String sql = "SELECT hourly_rate FROM teacher ORDER BY member_id DESC LIMIT 1";

        try {
            pstmt = extConn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 결과가 있다면 해당 시급을 사용
                latestRate = rs.getInt("hourly_rate");
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching latest teacher rate: " + ex.getMessage());
            // 예외 발생 시 기본값(30000)이 반환됨
        } finally {
            // 외부 연결(extConn)은 닫지 않고, 내부에서 생성한 자원(rs, pstmt)만 닫습니다.
            close(rs, pstmt);
        }
        return latestRate;
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
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }

    // 오버로드 메서드는 가변 인수로 대체하여 제거했습니다.
}