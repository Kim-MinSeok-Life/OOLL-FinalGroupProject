// ConnectDB.java
package OOLL_P_Teacher;

import java.sql.*;

public class ConnectDB {

    /// 드라이버 로드
    // 드라이버 호출을 한 번만 시도한다.
    // static이므로 클래스가 JVM에 의해 만들어질 때 딱 한번 호출하고 끝.
    static {
        try {
            // 자바 소스 프로그램 내에 사용할 JDBC 드라이버를 로드하는 문장 [MySql용]
            Class.forName("com.mysql.cj.jdbc.Driver"); // mysql JDBC Driver 연결
            System.err.println("JDBC-ODBC 드라이버를 정상적으로 로드함");
        } catch (ClassNotFoundException e) {
            System.err.println("드라이버 로드에 실패했습니다.");
            e.printStackTrace(); // 에러 위치 추적용
        } // try-catch
    } // static

    // 필요할 때마다 DB 연결
    public static Connection getConnection() {
        // try 성공 시: DB와 연결된 Connection 객체 내용이 con에 저장되어 반환
        // try 실패 시: null 반환
        Connection con = null;
        try {
            // DriverManager를 통해  DB와 연결
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");
            System.out.println("DB 연결 완료.");
        } catch (SQLException e) {
            System.out.println("SQLException: "+e.getMessage());
            e.printStackTrace(); // 에러 위치 추적용
        } // try-catch
        return con;
    } // getConnection 끝

    // DB 자원 한 번에 닫는 메소드
    public static void close(AutoCloseable[] resources) {
        for (int i = 0; i < resources.length; i++) {
            AutoCloseable res = resources[i];
            if (res != null) {
                try{res.close();} // 닫기 시도
                catch(Exception e){System.err.println("자원 해제 중 오류 발생: "+e.getMessage());}
            }
        } // for
    }
}



//            Statement dbSt = con.createStatement();
//            System.out.println("JDBC 드라이버가 정상적으로 연결되었습니다.");
