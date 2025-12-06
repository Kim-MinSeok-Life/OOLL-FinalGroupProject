// 원장의 개인정보 변경 화면!!
package OOLL_P_Manager;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MyInfoDialog extends JDialog implements ActionListener {
    // 필드
    JTextField idField, nameField, passField, phoneField, emailField, addrField;
    JButton updateBtn, cancelBtn; // 버튼 멤버 변수

    String loginId;

    public MyInfoDialog(JFrame parent, String title, String id) {
        super(parent, title, true);
        this.loginId = id;
        setSize(400, 500);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("아이디:"));
        idField = new JTextField();
        idField.setEditable(false);
        formPanel.add(idField);

        formPanel.add(new JLabel("이름:"));
        nameField = new JTextField();
        // 이름 수정 가능
        formPanel.add(nameField);

        formPanel.add(new JLabel("비밀번호:"));
        passField = new JTextField();
        formPanel.add(passField);

        formPanel.add(new JLabel("전화번호:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        formPanel.add(new JLabel("이메일:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("주소:"));
        addrField = new JTextField();
        formPanel.add(addrField);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        updateBtn = new JButton("수정완료");
        updateBtn.setBackground(Color.WHITE);
        updateBtn.setPreferredSize(new Dimension(100, 35));
        updateBtn.addActionListener(this); // 리스너 연결

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(this); // 리스너 연결

        btnPanel.add(updateBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        loadMyInfo();
    }

    // ★ 이벤트 처리 메소드 (여기로 통합)
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose();
        } else if (e.getSource() == updateBtn) {
            updateMyInfo();
        }
    }

    private void loadMyInfo() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            String sql = "SELECT * FROM member WHERE member_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                idField.setText(rs.getString("member_id"));
                nameField.setText(rs.getString("name"));
                passField.setText(rs.getString("password"));
                phoneField.setText(rs.getString("phone"));
                emailField.setText(rs.getString("email"));
                addrField.setText(rs.getString("address"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "정보 로드 실패");
        } finally {
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    private void updateMyInfo() {
        if(nameField.getText().trim().isEmpty() || passField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름, 비밀번호, 전화번호는 필수입니다.");
            return;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            String sql = "UPDATE member SET name=?, password=?, phone=?, email=?, address=? WHERE member_id=?";

            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, passField.getText());
            pstmt.setString(3, phoneField.getText());
            pstmt.setString(4, emailField.getText());
            pstmt.setString(5, addrField.getText());
            pstmt.setString(6, loginId);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "정보가 수정되었습니다.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "수정 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage());
        } finally {
            try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}