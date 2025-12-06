// 원장의 학생 기본 수강료 변경 창!!!

package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// FeeDialog 클래스: 학원 전체의 '시간당 기본 수강료'를 설정하는 팝업창입니다.
// JDialog를 상속받고 ActionListener를 구현하여 이벤트를 처리합니다.
public class FeeDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 변수 선언 ---
    JTextField feeField; // 금액 입력 필드
    JButton saveBtn;     // 저장 버튼
    JButton cancelBtn;   // 취소 버튼

    // 생성자: UI를 구성하고 현재 저장된 수강료를 DB에서 불러옵니다.
    public FeeDialog(JFrame parent) {
        super(parent, "기본 수강료 설정", true); // 모달 창 설정
        setSize(350, 200);          // 창 크기 (아담하게)
        setLocationRelativeTo(parent); // 부모 창 중앙에 배치

        // 1. 메인 패널 (전체 틀)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // 안쪽 여백
        mainPanel.setBackground(Color.WHITE);

        // 2. 입력 영역 (중앙)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5)); // 2행 1열
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(new JLabel("시간(교시)당 기본 수강료:"));

        feeField = new JTextField();
        feeField.setHorizontalAlignment(JTextField.RIGHT); // 숫자는 오른쪽 정렬이 국룰!
        centerPanel.add(feeField);

        // 3. 버튼 영역 (하단)
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        saveBtn = new JButton("저장");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.addActionListener(this); // 리스너 연결

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.addActionListener(this); // 리스너 연결

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        // 패널 조립
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // ★ 창이 열리자마자 DB에서 현재 설정된 금액을 불러옴
        loadCurrentFee();
    }

    // ★ ActionListener 구현: 버튼 클릭 이벤트 처리
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 시 닫기
        } else if (e.getSource() == saveBtn) {
            updateFee(); // 저장 시 DB 업데이트 로직 실행
        }
    }

    // ★ [기능 1] 현재 수강료 불러오기 (SELECT)
    private void loadCurrentFee() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 수강료 정책 테이블(fee_policy)에서 1번 정책을 조회
            // (우리는 정책번호 1번만 쓰기로 DB 설계 때 약속함)
            String sql = "SELECT base_fee FROM fee_policy WHERE policy_no = 1";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                int fee = rs.getInt("base_fee");
                feeField.setText(String.valueOf(fee)); // 가져온 금액을 입력창에 표시
            } else {
                // 만약 데이터가 없으면 0으로 표시 (보통 초기화 SQL 덕분에 있을 것임)
                feeField.setText("0");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "불러오기 실패: " + e.getMessage());
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // ★ [기능 2] 수강료 수정하기 (UPDATE or INSERT)
    private void updateFee() {
        String input = feeField.getText().trim();

        // 1. 유효성 검사: 숫자만 입력했는지 확인 (정규표현식)
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

            // 2. UPDATE 시도 (기존 1번 정책의 값을 수정)
            String sql = "UPDATE fee_policy SET base_fee = ? WHERE policy_no = 1";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(input));

            int result = pstmt.executeUpdate(); // 수정된 행 개수 반환

            if (result > 0) {
                // 업데이트 성공
                JOptionPane.showMessageDialog(this, "수강료가 변경되었습니다.");
                dispose(); // 창 닫기
            } else {
                // 3. [예외 처리] 만약 UPDATE가 안 됐다면? (데이터가 아예 없는 경우)
                // -> INSERT 문으로 새로 만들어준다. (Upsert 로직)
                String insertSql = "INSERT INTO fee_policy (policy_no, base_fee) VALUES (1, ?)";
                pstmt = con.prepareStatement(insertSql); // pstmt 재사용
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