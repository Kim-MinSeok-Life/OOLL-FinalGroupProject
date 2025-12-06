// 원장의 학생 정보 수정!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentDialog extends JDialog implements ActionListener {
    JTextField idField, nameField, phoneField, emailField, addrField;
    JButton saveBtn, cancelBtn;

    public StudentDialog(JFrame parent, String title, String[] currentData) {
        super(parent, title, true);
        setSize(400, 350);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);

        // 1. 아이디 (수정 불가)
        formPanel.add(new JLabel("아이디:"));
        idField = new JTextField();
        idField.setEditable(false);
        formPanel.add(idField);

        // 2. 나머지 정보
        formPanel.add(new JLabel("이름:")); nameField = new JTextField(); formPanel.add(nameField);
        formPanel.add(new JLabel("전화번호:")); phoneField = new JTextField(); formPanel.add(phoneField);
        formPanel.add(new JLabel("이메일:")); emailField = new JTextField(); formPanel.add(emailField);
        formPanel.add(new JLabel("주소:")); addrField = new JTextField(); formPanel.add(addrField);

        // 데이터 채우기 (전달받은 값)
        if(currentData != null) {
            idField.setText(currentData[0]);
            nameField.setText(currentData[1]);
            phoneField.setText(currentData[2]);
            emailField.setText(currentData[3]);
            addrField.setText(currentData[4]);
        }

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        saveBtn = new JButton("저장");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(80, 35));
        saveBtn.addActionListener(this);

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.addActionListener(this);

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose();
        } else if (e.getSource() == saveBtn) {
            // 학생 정보 수정 로직
            updateStudent();
        }
    }

    private void updateStudent() {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // member 테이블 정보만 수정하면 됨 (학생 정보는 member 테이블에 있으니까)
            String sql = "UPDATE member SET name=?, phone=?, email=?, address=? WHERE member_id=?";

            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, phoneField.getText());
            pstmt.setString(3, emailField.getText());
            pstmt.setString(4, addrField.getText());
            pstmt.setString(5, idField.getText());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "학생 정보가 수정되었습니다.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "수정 실패");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}