// 원장의 학생 기본 수강료 변경 창!!!

package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// FeeDialog 클래스: 학원 전체의 '시간당 기본 수강료'를 설정/수정하는 팝업창입니다.
// JDialog를 상속받아 모달 창으로 동작하며, ActionListener를 통해 버튼 이벤트를 처리합니다.
public class FeeDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 변수 선언 ---
    JTextField feeField; // 수강료 금액을 입력받는 텍스트 필드
    JButton saveBtn;     // 저장 버튼
    JButton cancelBtn;   // 취소 버튼

    // 생성자: 팝업창의 UI를 구성하고, DB에서 현재 저장된 수강료 정보를 불러옵니다.
    // parent: 이 팝업창을 띄운 부모 프레임 (위치 기준점)
    public FeeDialog(JFrame parent) {
        super(parent, "기본 수강료 설정", true); // true: 모달 창 (이 창을 닫기 전에는 부모 창 클릭 불가)
        setSize(350, 200);          // 창 크기 설정 (가로 350, 세로 200 - 아담한 사이즈)
        setLocationRelativeTo(parent); // 부모 창의 정중앙에 배치

        // 1. 메인 패널 설정 (전체 레이아웃 틀)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // 상하좌우 여백 설정
        mainPanel.setBackground(Color.WHITE); // 배경색 흰색

        // 2. 입력 영역 패널 (중앙 배치)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5)); // 2행 1열 그리드
        centerPanel.setBackground(Color.WHITE);

        // 라벨 추가
        centerPanel.add(new JLabel("시간(교시)당 기본 수강료:"));

        // 입력 필드 설정
        feeField = new JTextField();
        feeField.setHorizontalAlignment(JTextField.RIGHT); // 숫자는 오른쪽 정렬이 보기 좋음
        centerPanel.add(feeField);

        // 3. 버튼 영역 패널 (하단 배치)
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // 위쪽 간격 10px

        saveBtn = new JButton("저장");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.addActionListener(this); // 버튼 클릭 이벤트 연결

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.addActionListener(this); // 버튼 클릭 이벤트 연결

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        // 패널 조립
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // ★ 창이 열리자마자 DB에서 현재 설정된 금액을 불러와 입력창에 채워넣음
        loadCurrentFee();
    }

    // ★ ActionListener 구현부: 버튼 클릭 시 실행되는 메소드
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 버튼 누르면 창 닫기
        } else if (e.getSource() == saveBtn) {
            updateFee(); // 저장 버튼 누르면 DB 업데이트 로직 실행
        }
    }

    // ★ [기능 1] 현재 수강료 불러오기 (SELECT)
    // 교수님 질문 대비: "수강료 테이블은 단일 행(policy_no=1)으로 관리하여 시스템 전역 설정을 저장합니다."
    private void loadCurrentFee() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // 1. DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025"); // 비밀번호 java2025

            // 2. SQL 조회 (정책번호 1번 데이터 가져오기)
            String sql = "SELECT base_fee FROM fee_policy WHERE policy_no = 1";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                // 데이터가 있으면 입력창에 표시
                int fee = rs.getInt("base_fee");
                feeField.setText(String.valueOf(fee));
            } else {
                // 만약 데이터가 없으면 0으로 표시 (초기화가 안 된 경우 대비)
                feeField.setText("0");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "불러오기 실패: " + e.getMessage());
        } finally {
            // 자원 해제
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // ★ [기능 2] 수강료 수정하기 (UPDATE or INSERT)
    private void updateFee() {
        try {
            // 1. 입력값 검증 (예외 던지기)
            validateInput();

            // 2. DB 업데이트
            String input = feeField.getText().trim();
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
                con = DriverManager.getConnection(dbUrl, "root", "java2025");

                // 3. UPDATE 시도 (기존 1번 정책의 값을 수정)
                String sql = "UPDATE fee_policy SET base_fee = ? WHERE policy_no = 1";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(input));

                int result = pstmt.executeUpdate(); // 수정된 행 개수 반환

                if (result > 0) {
                    // 업데이트 성공 시
                    JOptionPane.showMessageDialog(this, "수강료가 변경되었습니다.");
                    dispose(); // 창 닫기
                } else {
                    // 4. [예외 처리] 만약 UPDATE가 안 됐다면? (데이터가 아예 없는 경우)
                    // -> INSERT 문으로 새로 만들어준다. (Upsert 로직 - 데이터 무결성 보장)
                    String insertSql = "INSERT INTO fee_policy (policy_no, base_fee) VALUES (1, ?)";
                    pstmt = con.prepareStatement(insertSql); // pstmt 재사용
                    pstmt.setInt(1, Integer.parseInt(input));
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "수강료가 설정되었습니다.");
                    dispose();
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
            }

        } catch (InvalidInputException ex) {
            // 5. 예외 잡아서 경고창 표시
            JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ★ [추가] 유효성 검증 메소드 (숫자 여부 체크)
    private void validateInput() throws InvalidInputException {
        String input = feeField.getText().trim();
        if (input.isEmpty()) {
            throw new InvalidInputException("금액을 입력해주세요.");
        }
        // 정규표현식: 숫자만 허용
        if (!input.matches("^[0-9]+$")) {
            throw new InvalidInputException("숫자만 입력해주세요.");
        }
    }

    // ★ [추가] 사용자 정의 예외 클래스
    private static class InvalidInputException extends Exception {
        public InvalidInputException(String message) { super(message); }
    }
}