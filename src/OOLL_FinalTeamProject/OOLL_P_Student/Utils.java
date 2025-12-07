// 학생 - 공통 유틸리티(전화번호 검증 기능)
package OOLL_P_Student; // 패키지 선언

// import 선언
import javax.swing.JOptionPane; // 메세지창(알림창 등) UI를 사용하기 위한 컴포넌트
// 패키지별 클래스 전체 가져오기
import OOLL_P_Student.*; // 학생 기능 관련 패키지 불러오기
import OOLL_P_Teacher.*; // 강사 기능 관련 패키지 불러오기
import OOLL_P_Login.*; // 로그인 기능 관련 패키지 불러오기
import OOLL_P_Manager.*; // 관리자 기능 관련 패키지 불러오기

public class Utils {
   
	// 교시(수업 시간) 관련 상수
	public static final String[] PERIOD = { // 교시(PERIOD) 상수 배열
	        "1교시(09:00 ~ 09:50)",
	        "2교시(10:00 ~ 10:50)",
	        "3교시(11:00 ~ 11:50)",
	        "4교시(13:00 ~ 13:50)",
	        "5교시(14:00 ~ 14:50)",
	        "6교시(15:00 ~ 15:50)",
	        "7교시(16:00 ~ 16:50)"
	};
	
	// PERIOD 배열에서 "1교시"와 같은 텍스트를 새 배열로 반환
	public static String[] getPeriodTitles() {
        String[] titles = new String[PERIOD.length]; // PERIOD 배열과 같은 길이의 새 배열 생성
        for (int i = 0; i < PERIOD.length; i++) {
            titles[i] = PERIOD[i].substring(0, PERIOD[i].indexOf("(")); // "(" 위치를 찾아 앞부분만 자름
        }
        return titles;
    }
	
	// 숫자(1~7) 입력하면 해당하는 시간의 교시로 반환(예: 1교시)
	public static String getPeriodTitle(int periodNo) {
	    if (periodNo < 1 || periodNo > PERIOD.length) return periodNo + "교시"; // 직접 표현
	    return getPeriodTitles()[periodNo - 1]; // 배열 0부터 시작
	}
	
	// 전화번호 형식 검증(하이픈 제거 후 길이/시작 숫자 체크)	
    public static boolean isValidPhone(String phone) {
    	if (phone == null) return false;
        String digits = phone.replaceAll("[^0-9]", ""); // 숫자만 추출
        if (digits.length() < 8 || digits.length() > 11) return false; // 최소/최대 길이 검사

        // 허용 범위(010, 070, 02, 031, 032)
        if (digits.startsWith("010") || digits.startsWith("070")) {
            // 010 또는 070(10~11자리_예: 01012345678)
            return digits.length() == 10 || digits.length() == 11;
        }
        if (digits.startsWith("02")) {
            // 서울 02 형태는 9자리(02-1234-5678 -> 9) 또는 8자리(짧은 경우)
            return digits.length() == 9 || digits.length() == 8;
        }
        if (digits.startsWith("031") || digits.startsWith("032")) {
        	// 경기 또는 인천 10자리
            return digits.length() == 10;
        }
        return false; // 이 외는 허용하지 않음
    }
    
    // 이메일 형식 검증
    public static boolean isValidEmail(String email) {
        if (email == null) return false; // null 값 시, false 반환
        // 일반적인 이메일 형식 체크
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(regex); // 비교
    }

    // 특정 도메인만 허용(예: naver.com, google.com 등)
    public static boolean isAllowedEmailDomain(String email) {
        if (!isValidEmail(email)) return false; // isValidEmail
        // @ 뒤 도메인 추출
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase(); // @ 뒤부터 끝까지 잘라서 추출 
        return domain.equals("naver.com") || domain.equals("google.com") || domain.equals("daum.net");
    }
    
    // 강의 삭제 확인(YES 선택 시 true 반환)
    public static boolean confirmLectureDeletion(String lectureName) {
        int result = JOptionPane.showConfirmDialog(
            null,
            lectureName + " 강의를 정말로 삭제하시겠습니까?",
            "강의 삭제 확인",
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }

    // 교시 표현: 시작~종료(HH:MM ~ HH:MM 형태 반환)
    public static String formatClassPeriod(int startHour, int startMinute, int endHour, int endMinute) {
        return String.format("%02d:%02d ~ %02d:%02d", startHour, startMinute, endHour, endMinute);
    }
}
