// 학생 - 공통 DB 연결 유틸 클래스
package OOLL_P_Student; // 패키지 선언

// import 선언
import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import java.sql.*;

public class DBUtil {
	// MySQL DB 연결을 생성해 반환하는 메소드
    public static Connection getConnection() { // DB 접속 정보로 Connection 생성
        Connection conn = null;

        // DB 접속 정보
        String url = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC&characterEncoding=utf8";
        String user = "root";
        String pass = "java2025";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL JDBC 드라이버 로드
            conn = DriverManager.getConnection(url, user, pass); // DB 연결 시도
            System.out.println("DB 연결 성공!");
        } catch (Exception e) {
        	 // 드라이버 로드&DB 연결 실패 시
            e.printStackTrace();
        }
        return conn; // 연결 객체 반환(실패 시 null)
    }
}
