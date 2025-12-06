// 원장의 학생 정보 수정!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// StudentDialog 클래스: 원장이 학생의 개인정보를 수정하는 팝업창입니다.
// JDialog를 상속받아 모달 창으로 만들고, ActionListener로 버튼 이벤트를 처리합니다.
public class StudentDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 변수 선언 ---
    JTextField idField;    // 아이디 (수정 불가 - 식별자)
    JTextField nameField;  // 이름
    JTextField phoneField; // 전화번호
    JTextField emailField; // 이메일
    JTextField addrField;  // 주소

    JButton saveBtn;   // 저장 버튼
    JButton cancelBtn; // 취소 버튼

    // 생성자: 팝업창 UI를 그리고, 넘겨받은 학생 데이터(currentData)를 화면에 채워줍니다.
    // parent: 부모 프레임, title: 창 제목, currentData: 선택된 학생의 정보 배열 [아이디, 이름, 전화, 이메일, 주소]
    public StudentDialog(JFrame parent, String title, String[] currentData) {
        super(parent, title, true); // true: 모달 창 (부모 창 클릭 방지)
        setSize(400, 350);          // 창 크기 설정 (스크롤 없이 딱 맞는 크기)
        setLocationRelativeTo(parent); // 부모 창 중앙에 배치

        // 1. 메인 패널 설정
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // 여백 20px
        mainPanel.setBackground(Color.WHITE); // 배경 흰색

        // 2. 입력 폼 패널 (2열 그리드: 라벨 - 입력창)
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15)); // 가로 간격 10, 세로 간격 15
        formPanel.setBackground(Color.WHITE);

        // (1) 아이디 필드 (PK이므로 수정 불가)
        formPanel.add(new JLabel("아이디:"));
        idField = new JTextField();
        idField.setEditable(false); // ★ 읽기 전용 설정 (회색 처리됨)
        formPanel.add(idField);

        // (2) 나머지 정보 필드 (수정 가능)
        formPanel.add(new JLabel("이름:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("전화번호:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        formPanel.add(new JLabel("이메일:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("주소:"));
        addrField = new JTextField();
        formPanel.add(addrField);

        // 3. 데이터 채우기 (StudentPanel에서 선택한 행의 정보를 미리 채워둠)
        if(currentData != null) {
            idField.setText(currentData[0]);   // 아이디
            nameField.setText(currentData[1]); // 이름
            phoneField.setText(currentData[2]); // 전화번호
            emailField.setText(currentData[3]); // 이메일
            addrField.setText(currentData[4]);  // 주소
        }

        // 4. 하단 버튼 패널
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0)); // 위쪽 간격

        // 저장 버튼
        saveBtn = new JButton("저장");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(80, 35));
        saveBtn.addActionListener(this); // 이벤트 연결

        // 취소 버튼
        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.addActionListener(this); // 이벤트 연결

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        // 패널 조립
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    // ★ ActionListener 구현부: 버튼 클릭 이벤트 처리
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 시 창 닫기
        } else if (e.getSource() == saveBtn) {
            // 저장 버튼 클릭 시 업데이트 로직 실행
            updateStudent();
        }
    }

    // ★ [핵심 로직] 학생 정보 수정 (DB UPDATE)
    // 예외 처리 패턴(validate -> try-catch) 적용됨
    private void updateStudent() {
        try {
            // 1. 유효성 검사 수행 (빈칸 체크 등) -> 문제시 예외 던짐
            validateInputs();

            // 2. 검사 통과 시 DB 연결 및 수정
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                // DB 드라이버 로드
                Class.forName("com.mysql.cj.jdbc.Driver");
                // DB 연결 (비밀번호 java2025)
                String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
                con = DriverManager.getConnection(dbUrl, "root", "java2025");

                // 3. SQL 작성: 학생 정보는 부모 테이블인 'member'에 있으므로 member를 수정함
                String sql = "UPDATE member SET name=?, phone=?, email=?, address=? WHERE member_id=?";

                pstmt = con.prepareStatement(sql);
                // 입력창 값 바인딩
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, phoneField.getText());
                pstmt.setString(3, emailField.getText());
                pstmt.setString(4, addrField.getText());
                pstmt.setString(5, idField.getText()); // WHERE 조건 (아이디)

                // 4. 실행
                int result = pstmt.executeUpdate(); // 수정된 행 개수 반환

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "학생 정보가 수정되었습니다.");
                    dispose(); // 성공 시 창 닫기 (목록 자동 갱신됨)
                } else {
                    throw new Exception("수정 실패 (대상 학생을 찾을 수 없습니다)");
                }

            } catch (Exception ex) {
                ex.printStackTrace(); // 콘솔에 에러 출력
                JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            } finally {
                // 자원 해제
                try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
            }

        } catch (InvalidInputException ex) {
            // 5. 유효성 검사 예외 처리 (경고창)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ★ [검증 메소드] 입력값 검사 (문제 발생 시 예외 전파)
    private void validateInputs() throws InvalidInputException {
        if (nameField.getText().trim().isEmpty()) {
            throw new InvalidInputException("이름을 입력해주세요.");
        }
        if (phoneField.getText().trim().isEmpty()) {
            throw new InvalidInputException("전화번호를 입력해주세요.");
        }
        if (emailField.getText().trim().isEmpty()) {
            throw new InvalidInputException("이메일을 입력해주세요.");
        }
        if (addrField.getText().trim().isEmpty()) {
            throw new InvalidInputException("주소를 입력해주세요.");
        }
    }

    // ★ [사용자 정의 예외] 검증 실패 상황을 명확히 표현
    private static class InvalidInputException extends Exception {
        public InvalidInputException(String message) { super(message); }
    }
}