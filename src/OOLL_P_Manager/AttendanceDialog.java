// 원장의 출결 관리 창!!!!

package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AttendanceDialog extends JDialog implements ActionListener {
    String lectureNo; // 현재 강의 번호
    JTextField dateField;
    DefaultTableModel tableModel;
    JTable table;
    JButton loadBtn, presentBtn, lateBtn, absentBtn, closeBtn;

    public AttendanceDialog(JFrame parent, String title, String lectureNo) {
        super(parent, title, true);
        this.lectureNo = lectureNo;
        setSize(600, 500); // 넉넉하게
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // 1. 상단 날짜 선택 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        topPanel.add(new JLabel("날짜(YYYY-MM-DD): "));

        // 오늘 날짜 자동 입력
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        dateField = new JTextField(today, 10);
        topPanel.add(dateField);

        loadBtn = new JButton("조회");
        loadBtn.setBackground(new Color(240, 240, 240));
        loadBtn.addActionListener(this);
        topPanel.add(loadBtn);

        // 2. 중앙 학생 목록 테이블
        String[] cols = {"학생번호", "이름", "아이디", "연락처", "출결상태"};
        tableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table); // 스타일 적용

        // 학생번호 숨기기 (업데이트용 PK)
        table.removeColumn(table.getColumnModel().getColumn(0));

        JScrollPane scroll = new JScrollPane(table);

        // 3. 하단 상태 변경 버튼들
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        presentBtn = createStatusButton("출석", Color.WHITE);
        lateBtn = createStatusButton("지각", Color.WHITE);
        absentBtn = createStatusButton("결석", Color.WHITE);

        closeBtn = new JButton("닫기");
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setPreferredSize(new Dimension(80, 40));
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(presentBtn);
        btnPanel.add(lateBtn);
        btnPanel.add(absentBtn);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(closeBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 창 열리자마자 오늘 날짜로 자동 조회
        loadAttendance();
    }

    private JButton createStatusButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(80, 40));
        btn.addActionListener(this);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == loadBtn) {
            loadAttendance();
        } else if (src == presentBtn) {
            updateStatus("출석");
        } else if (src == lateBtn) {
            updateStatus("지각");
        } else if (src == absentBtn) {
            updateStatus("결석");
        }
    }

    // ★ [DB] 출결 데이터 조회 (날짜 검사 포함)
    private void loadAttendance() {
        String date = dateField.getText().trim();

        // 1. 빈칸 검사
        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "날짜를 입력하세요.");
            return;
        }

        // 2. ★ 날짜 형식 검사 (yyyy-MM-dd)
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            JOptionPane.showMessageDialog(this, "날짜를 xxxx(년)-xx(월)-xx(일) 형식으로 입력해 주세요.\n예: 2025-12-05", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. 실제 날짜 유효성 검사 (옵션)
        try {
            java.time.LocalDate.parse(date);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "존재하지 않는 날짜입니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 1. 프로시저 호출 (데이터 없으면 '결석'으로 초기화)
            String procSql = "CALL proc_init_attendance(" + lectureNo + ", '" + date + "')";
            stmt = con.createStatement();
            stmt.execute(procSql);
            stmt.close();

            // 2. 데이터 조회
            String selectSql = "SELECT s.student_no, m.name, m.member_id, m.phone, a.attendance_status " +
                    "FROM attendance a " +
                    "JOIN student s ON a.student_no = s.student_no " +
                    "JOIN member m ON s.member_id = m.member_id " +
                    "WHERE a.lecture_no = " + lectureNo + " AND a.att_date = '" + date + "' " +
                    "ORDER BY m.name";

            stmt = con.createStatement();
            rs = stmt.executeQuery(selectSql);

            while(rs.next()) {
                String sNo = rs.getString("student_no");
                String name = rs.getString("name");
                String id = rs.getString("member_id");
                String phone = rs.getString("phone");
                String status = rs.getString("attendance_status");

                // 화면에 보이는 순서는 [이름, 아이디, 연락처, 상태]이지만
                // 모델에는 [학생번호(PK)]가 0번에 숨어있음
                tableModel.addRow(new Object[]{sNo, name, id, phone, status});
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage());
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // ★ [DB] 출결 상태 변경 (UPDATE)
    private void updateStatus(String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "상태를 변경할 학생을 선택해주세요.");
            return;
        }

        // 숨겨진 학생번호(PK) 가져오기
        String sNo = table.getModel().getValueAt(selectedRow, 0).toString();
        String date = dateField.getText().trim();

        Connection con = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            String sql = "UPDATE attendance SET attendance_status = '" + newStatus + "' " +
                    "WHERE student_no = " + sNo + " AND lecture_no = " + lectureNo + " AND att_date = '" + date + "'";

            stmt = con.createStatement();
            int result = stmt.executeUpdate(sql);

            if (result > 0) {
                // 화면 갱신 (모델 업데이트)
                table.getModel().setValueAt(newStatus, selectedRow, 4);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "변경 실패: " + e.getMessage());
        } finally {
            try { if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}