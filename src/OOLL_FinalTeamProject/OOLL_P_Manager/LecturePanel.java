// 원장의 강의 관리 탭!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LecturePanel extends JPanel implements ActionListener {
    JFrame parentFrame;
    DefaultTableModel tableModel;
    JTable table;
    JTextField searchField;

    JButton searchBtn, addBtn, editBtn, delBtn;

    public LecturePanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);

        topPanel.add(new JLabel("강의명/강사명 검색 : "));
        searchField = new JTextField(15);
        searchField.addActionListener(this);
        topPanel.add(searchField);

        searchBtn = new JButton("검색");
        searchBtn.setBackground(new Color(245, 245, 245));
        searchBtn.addActionListener(this);

        addBtn = new JButton("강좌 개설");
        addBtn.setBackground(Color.WHITE);
        addBtn.addActionListener(this);

        editBtn = new JButton("수정");
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(this);

        delBtn = new JButton("삭제");
        delBtn.setBackground(Color.WHITE);
        delBtn.addActionListener(this);

        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(delBtn);

        String[] cols = {"번호", "과목명", "담당강사", "강의실", "요일", "교시", "수강/정원"};
        tableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table);
        table.removeColumn(table.getColumnModel().getColumn(0));

        // 더블 클릭 이벤트 (출결 관리)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        String lectureNo = table.getModel().getValueAt(selectedRow, 0).toString();
                        String subject = table.getModel().getValueAt(selectedRow, 1).toString();
                        String teacher = table.getModel().getValueAt(selectedRow, 2).toString();
                        new AttendanceDialog(parentFrame, subject + " (" + teacher + ") 출결 관리", lectureNo).setVisible(true);
                    }
                }
            }
        });

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == searchBtn || source == searchField) {
            refreshTable(searchField.getText());

        } else if (source == addBtn) {
            new LectureDialog(parentFrame, "강좌 개설", null).setVisible(true);
            refreshTable("");

        } else if (source == editBtn) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(parentFrame, "수정할 강좌를 선택해주세요.");
                return;
            }
            // 데이터 가져오기
            String no = table.getModel().getValueAt(selectedRow, 0).toString();
            String subject = table.getModel().getValueAt(selectedRow, 1).toString();
            String teacher = table.getModel().getValueAt(selectedRow, 2).toString();
            String room = table.getModel().getValueAt(selectedRow, 3).toString();
            String day = table.getModel().getValueAt(selectedRow, 4).toString();
            String time = table.getModel().getValueAt(selectedRow, 5).toString();

            // "5 / 20" 형태에서 분리
            String countInfo = table.getModel().getValueAt(selectedRow, 6).toString();
            String[] counts = countInfo.split(" / ");
            String currentCount = counts[0]; // 현재 수강인원 (예: 5)
            String capacity = counts[1];     // 현재 정원 (예: 20)

            // ★ [수정됨] currentCount(수강인원)도 배열에 추가해서 보냄!
            String[] currentData = {no, subject, teacher, room, day, time, capacity, currentCount};

            new LectureDialog(parentFrame, "강좌 수정", currentData).setVisible(true);
            refreshTable("");

        } else if (source == delBtn) {
            deleteLecture();
        }
    }

    private void deleteLecture() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "삭제할 강좌를 선택해주세요.");
            return;
        }
        String lectureNo = table.getModel().getValueAt(selectedRow, 0).toString();
        String subjectName = table.getModel().getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "[" + subjectName + "] 강좌를 정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            Statement stmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");
                String sql = "DELETE FROM lecture WHERE lecture_no = " + lectureNo;
                stmt = con.createStatement();
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(parentFrame, "삭제되었습니다.");
                refreshTable("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentFrame, "삭제 실패! (강사가 수업 중이거나 DB 오류)\n" + e.getMessage());
            } finally {
                try { if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
            }
        }
    }

    public void refreshTable(String keyword) {
        tableModel.setRowCount(0);
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");
            String sql = "SELECT * FROM view_lecture_info ";
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql += "WHERE subject_name LIKE '%" + keyword.trim() + "%' OR teacher_name LIKE '%" + keyword.trim() + "%' ";
            }
            sql += "ORDER BY lecture_no DESC";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                int no = rs.getInt("lecture_no");
                String sub = rs.getString("subject_name");
                String tName = rs.getString("teacher_name");
                if(tName == null) tName = "미정";
                String room = rs.getString("classroom_name");
                String day = rs.getString("day_of_week");
                String time = rs.getInt("start_period") + "-" + rs.getInt("end_period") + "교시";
                String count = rs.getInt("enrolled_count") + " / " + rs.getInt("capacity");
                tableModel.addRow(new Object[]{no, sub, tName, room, day, time, count});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}