// 학생 - 공통 유틸리티(전화번호 검증 기능)
package OOLL_P_Student;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.JOptionPane;

public class Utils {
   
	public static final String[] PERIOD = {
	        "1교시(09:00 ~ 09:50)",
	        "2교시(10:00 ~ 10:50)",
	        "3교시(11:00 ~ 11:50)",
	        "4교시(13:00 ~ 13:50)",
	        "5교시(14:00 ~ 14:50)",
	        "6교시(15:00 ~ 15:50)",
	        "7교시(16:00 ~ 16:50)",
	        "8교시(17:00 ~ 17:50)",
	        "9교시(19:00 ~ 19:50)",
	        "10교시(20:00 ~ 20:50)",
	        "11교시(21:00 ~ 21:50)",
	        "12교시(22:00 ~ 22:50)"
	};
	
	public static String[] getPeriodTitles() {
        String[] titles = new String[PERIOD.length];
        for (int i = 0; i < PERIOD.length; i++) {
            titles[i] = PERIOD[i].substring(0, PERIOD[i].indexOf("(")); // "1교시"
        }
        return titles;
    }
	
	// 특정 교시 숫자 -> 교시 문자열
	public static String getPeriodTitle(int periodNo) {
	    if (periodNo < 1 || periodNo > PERIOD.length) return periodNo + "교시";
	    return getPeriodTitles()[periodNo - 1]; // 배열 0부터 시작
	}
	
	// 전화번호 검증: 하이픈 제거 후 길이/시작 숫자 체크	
    public static boolean isValidPhone(String phone) {
    	if (phone == null) return false;
        String digits = phone.replaceAll("[^0-9]", "");
        // 최소 길이 체크
        if (digits.length() < 8 || digits.length() > 11) return false;

        // 허용 범위: 010, 070, 02, 031, 032
        if (digits.startsWith("010") || digits.startsWith("070")) {
            // 010/070 은 10~11자리 허용(예: 01012345678)
            return digits.length() == 10 || digits.length() == 11;
        }
        if (digits.startsWith("02")) {
            // 02(서울) 형태는 9자리(02-1234-5678 -> 9) 또는 8자리(짧은 경우)
            return digits.length() == 9 || digits.length() == 8;
        }
        if (digits.startsWith("031") || digits.startsWith("032")) {
            return digits.length() == 10;
        }
        return false;
    }
    
    // 이메일 검증
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        // 일반적인 이메일 형식 체크
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(regex);
    }

    // 특정 도메인만 허용 (예: naver.com, google.com)
    public static boolean isAllowedEmailDomain(String email) {
        if (!isValidEmail(email)) return false;
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return domain.equals("naver.com") || domain.equals("google.com") || domain.equals("daum.net");
    }
    
    // 강의 삭제 확인
    public static boolean confirmLectureDeletion(String lectureName) {
        int result = JOptionPane.showConfirmDialog(
            null,
            lectureName + " 강의를 정말로 삭제하시겠습니까?",
            "강의 삭제 확인",
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }

    // 교시 표현 예시: 시작~종료
    public static String formatClassPeriod(int startHour, int startMinute, int endHour, int endMinute) {
        return String.format("%02d:%02d ~ %02d:%02d", startHour, startMinute, endHour, endMinute);
    }
}
