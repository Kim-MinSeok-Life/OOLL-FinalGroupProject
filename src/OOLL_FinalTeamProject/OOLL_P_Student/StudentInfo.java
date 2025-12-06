// 학생 - 개인정보 DTO 클래스
// DTO 클래스 -> 데이터를전송하기 위해 사용되는 객체 
package OOLL_P_Student; // 패키지 선언

// import 선언
import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

/* 학생 정보(StudentInfo) 클래스
 * 학생의 기본 개인정보를 담는 DTO 객체
 * 화면 표시 또는 DB 조회 결과 저장 등에 사용
*/
public class StudentInfo {
    public String memberId; // 학생 아이디
    public String name; // 학생 이름
    public String email; // 학생 이메일
    public String phone; // 학생 연락처
    public String address; // 학생 주소
    public int studentNo; // 학생 고유 번호(PK)
}