package OOLL_FinalTeamProject;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// 월 단위 달력 패널
public class CalendarPanel extends JPanel {

    // 날짜 선택 콜백 인터페이스
    public interface DateSelectionListener {
        void dateSelected(Date date);
    }

    private Calendar cal;                  // 현재 표시 중인 달
    private JLabel lblMonth;               // "2025-11" 같은 월 표시
    private JPanel gridPanel;              // 요일 + 날짜 그리드
    private DateSelectionListener listener;
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
    private Date selectedDate;             // 현재 선택된 날짜

    public CalendarPanel(Date initialDate, DateSelectionListener listener) {
        this.listener = listener;
        if (initialDate == null) {
            initialDate = new Date();
        }
        cal = Calendar.getInstance();
        cal.setTime(initialDate);

        setLayout(new BorderLayout());
        initHeader();
        initGrid();
        setDisplayDate(initialDate);
    }

    // 상단: 이전/다음 월 버튼 + 월 표시 라벨
    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JButton btnPrev = new JButton("◀");
        JButton btnNext = new JButton("▶");
        lblMonth = new JLabel("", SwingConstants.CENTER);

        btnPrev.addActionListener(e -> {
            cal.add(Calendar.MONTH, -1);
            setDisplayDate(cal.getTime());
        });

        btnNext.addActionListener(e -> {
            cal.add(Calendar.MONTH, 1);
            setDisplayDate(cal.getTime());
        });

        header.add(btnPrev, BorderLayout.WEST);
        header.add(lblMonth, BorderLayout.CENTER);
        header.add(btnNext, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // 가운데: 요일 + 날짜 박스 그리드
    private void initGrid() {
        gridPanel = new JPanel(new GridLayout(0, 7)); // 7열, 행은 자동
        add(gridPanel, BorderLayout.CENTER);
    }

    // 외부에서 달력에 표시할 기준 날짜 지정 (선택 날짜도 이 날로)
    public void setDisplayDate(Date date) {
        if (date == null) return;

        selectedDate = date;
        cal.setTime(date);
        lblMonth.setText(monthFormat.format(cal.getTime()));

        // 1일로 세팅해서 요일/마지막날 계산
        Calendar temp = (Calendar) cal.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK); // 1=일요일
        int maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 그리드 초기화 후, 요일 헤더 + 날짜 박스 다시 그림
        gridPanel.removeAll();

        String[] weekNames = {"일", "월", "화", "수", "목", "금", "토"};
        for (String w : weekNames) {
            JLabel lbl = new JLabel(w, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            gridPanel.add(lbl);
        }

        // 1일 시작 요일까지 공백 채우기
        for (int i = 1; i < firstDayOfWeek; i++) {
            gridPanel.add(new JLabel(""));
        }

        // 1일부터 마지막날까지 DateBox 생성
        for (int day = 1; day <= maxDay; day++) {
            temp.set(Calendar.DAY_OF_MONTH, day);
            Date dayDate = temp.getTime();

            DateBox box = new DateBox(dayDate, listener);
            if (isSameDay(dayDate, selectedDate)) {
                box.setSelected(true);
            }
            gridPanel.add(box);
        }

        revalidate();
        repaint();
    }

    // 같은 날짜인지 비교 (연, 일 기준)
    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
