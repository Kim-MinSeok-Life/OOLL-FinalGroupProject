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

    // 버튼 멤버 변수
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
        searchField.addActionListener(this); // 엔터키 처리
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

        // [추가] 수강료 설정 버튼
        feeBtn = new JButton("수강료 설정");
        feeBtn.setBackground(Color.WHITE);
        feeBtn.addActionListener(this);

        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(editBtn);
        topPanel.add(delBtn);
        topPanel.add(feeBtn);

        // 컬럼 순서: 아이디 -> 이름 -> ... -> 학생번호(숨김)
        String[] cols = {"아이디", "이름", "전화번호", "이메일", "주소", "학생번호"};

        tableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table);

        // 맨 마지막 컬럼(학생번호 PK) 숨기기 (인덱스 5)
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
            // 데이터 가져오기 (컬럼 순서에 맞게 인덱스 주의)
            // 0: 아이디, 1: 이름, 2: 전화, 3: 이메일, 4: 주소
            String id = table.getModel().getValueAt(selectedRow, 0).toString();
            String name = table.getModel().getValueAt(selectedRow, 1).toString();
            String phone = table.getModel().getValueAt(selectedRow, 2).toString();
            String email = table.getModel().getValueAt(selectedRow, 3).toString();
            String addr = table.getModel().getValueAt(selectedRow, 4).toString();

            String[] currentData = {id, name, phone, email, addr};

            // 학생 수정 팝업 호출
            new StudentDialog(parentFrame, "학생 정보 수정", currentData).setVisible(true);
            refreshTable("");

        } else if (source == delBtn) {
            deleteStudent();

        } else if (source == feeBtn) {
            // 수강료 설정 팝업 호출
            new FeeDialog(parentFrame).setVisible(true);
        }
    }

    // 학생 삭제 메소드 (회원 테이블에서 삭제로 수정)
    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "삭제할 학생을 선택해주세요.");
            return;
        }

        // 0번 컬럼(아이디)와 1번 컬럼(이름) 가져오기
        String memberId = table.getModel().getValueAt(selectedRow, 0).toString();
        String name = table.getModel().getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "[" + name + "] 학생을 정말 삭제하시겠습니까?\n(회원 정보 및 수강/출결 기록이 모두 영구 삭제됩니다.)",
                "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
                con = DriverManager.getConnection(dbUrl, "root", "java2025");

                // ★ 핵심 변경: student 테이블이 아니라 'member' 테이블에서 삭제!
                // DB의 ON DELETE CASCADE 설정 덕분에 관련된 모든 정보가 자동 삭제됨
                String sql = "DELETE FROM member WHERE member_id = ?";

                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, memberId);

                int result = pstmt.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(parentFrame, "삭제되었습니다.");
                    refreshTable(""); // 목록 새로고침
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "삭제 실패 (이미 삭제된 데이터일 수 있습니다).");
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame, "삭제 중 오류 발생: " + e.getMessage());
            } finally {
                try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
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
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

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

                // 표에 넣는 순서: 아이디 -> 이름 -> ...
                tableModel.addRow(new Object[]{id, name, phone, email, addr, no});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}