package project_test;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// 학생 기본 수강료 변경!!!

public class FeeDialog extends JDialog implements ActionListener {
    JTextField feeField;
    JButton saveBtn, cancelBtn;

    public FeeDialog(JFrame parent) {
        super(parent, "기본 수강료 설정", true);
        setSize(350, 200); // 아담한 사이즈
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // 입력 영역
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(new JLabel("시간(교시)당 기본 수강료:"));
        feeField = new JTextField();
        feeField.setHorizontalAlignment(JTextField.RIGHT); // 숫자니까 오른쪽 정렬
        centerPanel.add(feeField);

        // 버튼 영역
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        saveBtn = new JButton("저장");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.addActionListener(this);

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.addActionListener(this);

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // 창 열릴 때 현재 금액 불러오기
        loadCurrentFee();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose();
        } else if (e.getSource() == saveBtn) {
            updateFee();
        }
    }

    // [DB] 현재 수강료 불러오기 (SELECT)
    private void loadCurrentFee() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 정책번호 1번 데이터 가져오기
            String sql = "SELECT base_fee FROM fee_policy WHERE policy_no = 1";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                int fee = rs.getInt("base_fee");
                feeField.setText(String.valueOf(fee));
            } else {
                // 데이터가 없을 경우 (예외 상황)
                feeField.setText("0");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "불러오기 실패: " + e.getMessage());
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // [DB] 수강료 수정하기 (UPDATE)
    private void updateFee() {
        String input = feeField.getText().trim();

        // 숫자만 입력했는지 검사
        if (!input.matches("^[0-9]+$")) {
            JOptionPane.showMessageDialog(this, "숫자만 입력해주세요.");
            return;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 정책번호 1번 업데이트
            String sql = "UPDATE fee_policy SET base_fee = ? WHERE policy_no = 1";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(input));

            int result = pstmt.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "수강료가 변경되었습니다.");
                dispose();
            } else {
                // 혹시 데이터가 없어서 업데이트가 안 된 경우 -> INSERT 시도
                String insertSql = "INSERT INTO fee_policy (policy_no, base_fee) VALUES (1, ?)";
                pstmt = con.prepareStatement(insertSql);
                pstmt.setInt(1, Integer.parseInt(input));
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "수강료가 설정되었습니다.");
                dispose();
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage());
        } finally {
            try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}
