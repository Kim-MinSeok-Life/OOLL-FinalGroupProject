// 원장의 학생 관리 탭!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentPanel extends JPanel implements ActionListener {
    JFrame parentFrame;
    DefaultTableModel tableModel;
    JTable table;
    JTextField searchField;

    // ★ feeBtn 추가됨
    JButton searchBtn, editBtn, delBtn, feeBtn;

    public StudentPanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);

        topPanel.add(new JLabel("학생명/ID 검색 : "));
        searchField = new JTextField(15);
        searchField.addActionListener(this);
        topPanel.add(searchField);

        searchBtn = new JButton("검색");
        searchBtn.setBackground(new Color(245, 245, 245));
        searchBtn.addActionListener(this);

        editBtn = new JButton("학생 정보 수정");
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(this);

        delBtn = new JButton("삭제");
        delBtn.setBackground(Color.WHITE);
        delBtn.addActionListener(this);

        // ★ [추가] 수강료 설정 버튼
        feeBtn = new JButton("수강료 설정");
        feeBtn.setBackground(Color.WHITE);
        feeBtn.addActionListener(this);

        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(editBtn);
        topPanel.add(delBtn);
        topPanel.add(feeBtn); // 패널에 추가

        // ★ [수정됨] 컬럼 순서 변경: 아이디 -> 이름
        String[] cols = {"아이디", "이름", "전화번호", "이메일", "주소", "학생번호"};

        tableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table);

        table.removeColumn(table.getColumnModel().getColumn(5));

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == searchBtn || source == searchField) {
            refreshTable(searchField.getText());

        } else if (source == editBtn) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(parentFrame, "수정할 학생을 선택해주세요.");
                return;
            }
            String id = table.getModel().getValueAt(selectedRow, 0).toString();
            String name = table.getModel().getValueAt(selectedRow, 1).toString();
            String phone = table.getModel().getValueAt(selectedRow, 2).toString();
            String email = table.getModel().getValueAt(selectedRow, 3).toString();
            String addr = table.getModel().getValueAt(selectedRow, 4).toString();

            String[] currentData = {id, name, phone, email, addr};

            new StudentDialog(parentFrame, "학생 정보 수정", currentData).setVisible(true);
            refreshTable("");

        } else if (source == delBtn) {
            deleteStudent();

        } else if (source == feeBtn) {
            // ★ [추가] 수강료 설정 팝업 열기
            new FeeDialog(parentFrame).setVisible(true);
        }
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "삭제할 학생을 선택해주세요.");
            return;
        }

        String name = table.getModel().getValueAt(selectedRow, 1).toString();
        String studentNo = table.getModel().getValueAt(selectedRow, 5).toString();

        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "[" + name + "] 학생을 삭제하시겠습니까?\n(수강 내역 및 출결 기록이 모두 삭제됩니다.)",
                "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            Statement stmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                String sql = "DELETE FROM student WHERE student_no = " + studentNo;

                stmt = con.createStatement();
                stmt.executeUpdate(sql);

                JOptionPane.showMessageDialog(parentFrame, "삭제되었습니다.");
                refreshTable("");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentFrame, "삭제 실패: " + e.getMessage());
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

            String sql = "SELECT * FROM view_student_info ";
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql += "WHERE name LIKE '%" + keyword.trim() + "%' OR member_id LIKE '%" + keyword.trim() + "%' ";
            }
            sql += "ORDER BY student_no DESC";

            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                String name = rs.getString("name");
                String id = rs.getString("member_id");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String addr = rs.getString("address");
                int no = rs.getInt("student_no");

                tableModel.addRow(new Object[]{id, name, phone, email, addr, no});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}