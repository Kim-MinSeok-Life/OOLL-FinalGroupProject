package OOLL_P_Student; // 패키지 선언

// import 선언
import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

/* 강의(Lecture) 클래스
 * 강의 이름, 강의 시간(시/분 단위)을 저장하는 데이터 모델 객체
*/
public class Lecture {
	private String name; // 강의명
    private int startHour, startMinute; // 강의 시작 시간(시, 분)
    private int endHour, endMinute; // 강의 종료 시간(시, 분)

    public Lecture(String name, int sh, int sm, int eh, int em) { // 강의 객체 생성자
        this.name = name;
        this.startHour = sh;
        this.startMinute = sm;
        this.endHour = eh;
        this.endMinute = em;
    }

    public String getName() { return name; } // 강의명 반환
    public int getStartHour() { return startHour; } // 강의 시작 시간(시) 반환
    public int getStartMinute() { return startMinute; } // 강의 시작 시간(분) 반환
    public int getEndHour() { return endHour; } // 강의 종료 시간(시) 반환
    public int getEndMinute() { return endMinute; } // 강의 종료 시간(분) 반환
}