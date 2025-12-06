// CalendarPanel.java
package OOLL_P_Teacher; // TeacherMain / TeacherStudentList와 동일 패키지

import javax.swing.*;                       // 스윙
import java.awt.*;                          // 레이아웃, Color
import java.text.SimpleDateFormat;          // 날짜 포맷
import java.util.Calendar;                  // Calendar
import java.util.Date;                      // Date

// 월 단위 달력 패널
public class CalendarPanel extends JPanel {

    // 날짜 선택 콜백 인터페이스
    public interface DateSelectionListener { // 날짜가 선택됐을 때 호출될 콜백
        void dateSelected(Date date);        // 선택된 날짜를 전달
    }

    private Calendar cal;                    // 현재 표시중인 달 정보
    private JLabel lblMonth;                 // "2025-11" 같은 월 표시 라벨
    private JPanel gridPanel;                // 요일 + 날짜 그리드
    private DateSelectionListener listener;  // 날짜 선택 리스너
    private SimpleDateFormat monthFormat =   // "yyyy-MM" 포맷
            new SimpleDateFormat("yyyy-MM");
    private Date selectedDate;               // 현재 선택된 날짜

    public CalendarPanel(Date initialDate, DateSelectionListener listener) {
        this.listener = listener;            // 리스너 저장
        if (initialDate == null) {           // 초기 날짜가 null이면
            initialDate = new Date();        // 오늘 날짜로 설정
        }
        cal = Calendar.getInstance();        // Calendar 인스턴스 생성
        cal.setTime(initialDate);            // 현재 Calendar에 초기 날짜 설정

        setLayout(new BorderLayout());       // BorderLayout 사용
        initHeader();                        // 상단 헤더(월/이전/다음) 초기화
        initGrid();                          // 가운데 그리드(요일+날짜) 초기화
        setDisplayDate(initialDate);         // 초기 표시 날짜 설정
    }

    // 상단: 이전/다음 월 버튼 + 월 표시 라벨
    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout()); // 헤더 패널

        JButton btnPrev = new JButton("◀");             // 이전 달 버튼
        JButton btnNext = new JButton("▶");             // 다음 달 버튼
        lblMonth = new JLabel("", SwingConstants.CENTER); // 월 표시 라벨

        btnPrev.addActionListener(e -> {                // 이전 달 클릭 시
            cal.add(Calendar.MONTH, -1);                // 한 달 감소
            setDisplayDate(cal.getTime());              // 표시 날짜 갱신
        });

        btnNext.addActionListener(e -> {                // 다음 달 클릭 시
            cal.add(Calendar.MONTH, 1);                 // 한 달 증가
            setDisplayDate(cal.getTime());              // 표시 날짜 갱신
        });

        header.add(btnPrev, BorderLayout.WEST);         // 좌측에 이전 버튼
        header.add(lblMonth, BorderLayout.CENTER);      // 중앙에 월 라벨
        header.add(btnNext, BorderLayout.EAST);         // 우측에 다음 버튼

        add(header, BorderLayout.NORTH);                // 패널 상단에 헤더 부착
    }

    // 가운데: 요일 + 날짜 박스 그리드
    private void initGrid() {
        gridPanel = new JPanel(new GridLayout(0, 7));   // 7열, 행은 자동
        add(gridPanel, BorderLayout.CENTER);            // 중앙에 그리드 패널 배치
    }

    // 외부에서 달력에 표시할 기준 날짜 지정 (선택 날짜도 이 날로)
    public void setDisplayDate(Date date) {
        if (date == null) return;                       // null이면 무시

        selectedDate = date;                            // 선택된 날짜 기억
        cal.setTime(date);                              // Calendar 기준 날짜 설정
        lblMonth.setText(monthFormat.format(cal.getTime())); // 상단 월 라벨 갱신

        // 1일로 세팅해서 요일/마지막날 계산
        Calendar temp = (Calendar) cal.clone();         // 복사본 Calendar 생성
        temp.set(Calendar.DAY_OF_MONTH, 1);             // 1일로 설정

        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK);    // 1일의 요일 (1=일요일)
        int maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH); // 해당 월의 마지막 날

        gridPanel.removeAll();                          // 기존 그리드 전부 제거

        String[] weekNames = {"일", "월", "화", "수", "목", "금", "토"}; // 요일 이름
        for (String w : weekNames) {                    // 요일 라인 추가
            JLabel lbl = new JLabel(w, SwingConstants.CENTER);  // 가운데 정렬 라벨
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));   // 볼드 처리
            gridPanel.add(lbl);                        // 그리드에 추가
        }

        // 1일 시작 요일까지 공백 채우기
        for (int i = 1; i < firstDayOfWeek; i++) {      // 첫날 요일 전까지
            gridPanel.add(new JLabel(""));              // 빈 라벨로 채워서 위치 맞추기
        }

        // 1일부터 마지막날까지 DateBox 생성
        for (int day = 1; day <= maxDay; day++) {       // 1 ~ maxDay
            temp.set(Calendar.DAY_OF_MONTH, day);       // 오늘 날짜 설정
            Date dayDate = temp.getTime();              // Date로 변환

            DateBox box = new DateBox(dayDate, listener); // 한 날짜를 나타내는 버튼 생성
            if (isSameDay(dayDate, selectedDate)) {     // 선택된 날짜와 같으면
                box.setSelected(true);                  // 강조 표시
            }
            gridPanel.add(box);                         // 그리드에 추가
        }

        revalidate();                                   // 레이아웃 재계산
        repaint();                                      // 다시 그리기
    }

    // 같은 날짜인지 비교 (연 + "1년 중 몇 번째 날" 기준)
    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;     // null이면 false

        Calendar c1 = Calendar.getInstance();           // Calendar 1
        Calendar c2 = Calendar.getInstance();           // Calendar 2
        c1.setTime(d1);                                 // 첫 번째 날짜 설정
        c2.setTime(d2);                                 // 두 번째 날짜 설정

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && // 연도 같고
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR); // 1년 중 날짜 일치
    }
}
