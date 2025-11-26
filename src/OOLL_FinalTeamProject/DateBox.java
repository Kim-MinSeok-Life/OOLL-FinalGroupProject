package OOLL_FinalTeamProject;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// 달력 안에서 하루를 나타내는 버튼
public class DateBox extends JButton {

    private Date date;
    private CalendarPanel.DateSelectionListener listener;
    private SimpleDateFormat dayFormat = new SimpleDateFormat("d");

    public DateBox(Date date, CalendarPanel.DateSelectionListener listener) {
        this.date = date;
        this.listener = listener;

        setMargin(new Insets(1, 1, 1, 1));
        setFocusPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);

        if (date != null) {
            setText(dayFormat.format(date));

            addActionListener(e -> {
                if (this.listener != null && this.date != null) {
                    this.listener.dateSelected(this.date); // 클릭 시 선택 콜백
                }
            });
        } else {
            setEnabled(false);
        }
    }

    // 선택된 날짜 강조 (배경색 변경)
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            setBackground(new Color(180, 200, 255)); // 연한 파랑
        } else {
            setBackground(UIManager.getColor("Button.background"));
        }
    }

    public Date getDate() {
        return date;
    }
}
