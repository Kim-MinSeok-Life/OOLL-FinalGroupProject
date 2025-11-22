package java1010;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbtest {

    // ❗ 본인의 MySQL 접속 정보로 수정하세요.
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/student?serverTimezone=UTC"; // 'test_db'는 존재하는 DB 이름으로 변경
    private static final String USER = "root";
    private static final String PASSWORD = "java2025"; // 실제 비밀번호 입력

    public static void main(String[] args) {
        Connection conn = null;

        try {
            // 1. JDBC 드라이버 클래스가 로드되는지 확인
            Class.forName(DRIVER);
            System.out.println("✅ JDBC 드라이버 로드 성공");

            // 2. 실제 DB 접속 시도
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ 데이터베이스 연결 성공! (이클립스 - MySQL 연동 완료)");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ 오류: JDBC 드라이버를 찾을 수 없습니다. (Build Path 확인 필요)");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ 오류: 데이터베이스 연결 실패. (접속 정보, MySQL 실행 상태 확인 필요)");
            e.printStackTrace();
        } finally {
            // 3. 연결 해제
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}