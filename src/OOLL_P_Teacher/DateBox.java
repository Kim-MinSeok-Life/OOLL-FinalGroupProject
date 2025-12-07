// DateBox.java
package OOLL_P_Teacher; // CalendarPanel과 동일 패키지

import javax.swing.*;                       // JButton, UIManager 등
import java.awt.*;                          // Insets, Color
import java.text.SimpleDateFormat;          // 날짜 포맷
import java.util.Date;                      // Date

// 달력 안에서 하루를 나타내는 버튼
public class DateBox extends JButton {

    private Date date; // 이 버튼이 나타내는 날짜
    private CalendarPanel.DateSelectionListener listener;   // 날짜 선택 리스너
    private SimpleDateFormat dayFormat =           // "d" (1~31) 포맷
            new SimpleDateFormat("d");

    public DateBox(Date date, CalendarPanel.DateSelectionListener listener) {
        this.date = date; // 날짜 저장
        this.listener = listener; // 리스너 저장

        setMargin(new Insets(1, 1, 1, 1)); // 버튼 안쪽 여백 최소
        setFocusPainted(false); // 포커스 테두리 제거
        setContentAreaFilled(true); // 배경색 채우기
        setOpaque(true); // 불투명 설정

        if (date != null) { // 유효한 날짜라면
            setText(dayFormat.format(date)); // 날짜를 텍스트로 표시

            addActionListener(e -> { // 버튼 클릭 시
                if (this.listener != null && this.date != null) { // 리스너와 날짜가 유효하면
                    this.listener.dateSelected(this.date);  // 선택된 날짜 콜백 호출
                }
            });
        } else {
            setEnabled(false); // 날짜가 없으면 비활성화
        }
    }

    // 선택된 날짜 강조 (배경색 변경)
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected); // 기본 동작 수행
        if (selected) { // 선택되면
            setBackground(new Color(180, 200, 255)); // 연한 파랑색 배경
        } else {
            setBackground(UIManager.getColor("Button.background")); // 기본 버튼 배경색으로 복귀
        }
    }
    public Date getDate() { // 날짜 반환
        return date;
    }
}
