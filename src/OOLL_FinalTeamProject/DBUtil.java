//package teamPro;
package OOLL_FinalTeamProject;

import java.sql.*;

public class DBUtil {
    public static Connection getConnection() {
        Connection conn = null;

        String url = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC&characterEncoding=utf8";
        String user = "root";
        String pass = "7907";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("DB 연결 성공!");
        } catch (Exception e) {
        	 // 드라이버 로드 실패
            e.printStackTrace();
        }

        return conn;
    }
}
