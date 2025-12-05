package teamPro;
//package OOLL_FinalTeamProject;

public class Utils {
   
//	public static final String[] PERIOD = {
//	        "1교시(09:00 ~ 09:50)",
//	        "2교시(10:00 ~ 10:50)",
//	        "3교시(11:00 ~ 11:50)",
//	        "4교시(13:00 ~ 13:50)",
//	        "5교시(14:00 ~ 14:50)",
//	        "6교시(15:00 ~ 15:50)",
//	        "7교시(16:00 ~ 16:50)",
//	        "8교시(17:00 ~ 17:50)",
//	        "9교시(19:00 ~ 19:50)",
//	        "10교시(20:00 ~ 20:50)",
//	        "11교시(21:00 ~ 21:50)",
//	        "12교시(22:00 ~ 22:50)"
//	};
//	
//	public static String[] getPeriodTitles() {
//        String[] titles = new String[PERIOD.length];
//        for (int i = 0; i < PERIOD.length; i++) {
//            titles[i] = PERIOD[i].substring(0, PERIOD[i].indexOf("(")); // "1교시"
//        }
//        return titles;
//    }
	
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
}
