// 원장의 학생 정보 수정!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// StudentDialog 클래스: 학생 정보를 수정하는 팝업창입니다.
// JDialog를 상속받아 모달 창으로 동작하며, ActionListener를 구현하여 버튼 이벤트를 처리합니다.
public class StudentDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 변수 선언 ---
    JTextField idField;    // 아이디 (수정 불가)
    JTextField nameField;  // 이름
    JTextField phoneField; // 전화번호
    JTextField emailField; // 이메일
    JTextField addrField;  // 주소

    JButton saveBtn;   // 저장(수정 완료) 버튼
    JButton cancelBtn; // 취소 버튼

    // 생성자: 팝업창을 초기화하고 UI를 구성하며, 전달받은 학생 데이터를 화면에 채워 넣습니다.
    // parent: 부모 프레임, title: 창 제목, currentData: 수정할 학생의 정보 배열
    public StudentDialog(JFrame parent, String title, String[] currentData) {
        super(parent, title, true); // true: 모달 창 (이 창을 닫기 전엔 부모 창 클릭 불가)
        setSize(400, 350);          // 창 크기 설정
        setLocationRelativeTo(parent); // 부모 창의 정중앙에 배치

        // 1. 메인 패널 설정 (전체 레이아웃)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // 테두리 여백 20px
        mainPanel.setBackground(Color.WHITE); // 배경색 흰색

        // 2. 입력 폼 패널 설정 (2열 그리드 레이아웃: 라벨 - 입력창)
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15)); // 가로 간격 10, 세로 간격 15
        formPanel.setBackground(Color.WHITE);

        // (1) 아이디 필드 (★ 핵심: 수정 불가능하게 설정)
        formPanel.add(new JLabel("아이디:"));
        idField = new JTextField();
        idField.setEditable(false); // 사용자가 수정할 수 없도록 막음 (PK나 식별자는 보통 수정 불가)
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

        // 3. 데이터 채우기 (StudentPanel에서 넘겨받은 데이터가 있다면)
        if(currentData != null) {
            // 배열 순서: [0]아이디, [1]이름, [2]전화, [3]이메일, [4]주소
            idField.setText(currentData[0]);
            nameField.setText(currentData[1]);
            phoneField.setText(currentData[2]);
            emailField.setText(currentData[3]);
            addrField.setText(currentData[4]);
        }

        // 4. 하단 버튼 패널 구성
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0)); // 위쪽 간격 15px

        saveBtn = new JButton("저장");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(80, 35));
        saveBtn.addActionListener(this); // 리스너 연결 (저장 로직 실행)

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.addActionListener(this); // 리스너 연결 (창 닫기)

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        // 5. 패널 조립 (중앙에 폼, 남쪽에 버튼)
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    // ★ ActionListener 구현: 버튼 클릭 이벤트 처리
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 버튼 누르면 창 닫기
        } else if (e.getSource() == saveBtn) {
            // 저장 버튼 누르면 정보 수정 메소드 호출
            updateStudent();
        }
    }

    // ★ [핵심 기능] 학생 정보 수정 (DB UPDATE)
    private void updateStudent() {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            // 1. DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            // ★ 비밀번호 java2025 (프로젝트 통일)
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 2. SQL 작성 (회원 정보 수정)
            // 학생 정보는 부모 테이블인 'member'에 저장되어 있으므로 member 테이블을 수정합니다.
            String sql = "UPDATE member SET name=?, phone=?, email=?, address=? WHERE member_id=?";

            pstmt = con.prepareStatement(sql);
            // 입력창에 있는 값들을 가져와서 SQL 파라미터에 바인딩
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, phoneField.getText());
            pstmt.setString(3, emailField.getText());
            pstmt.setString(4, addrField.getText());
            pstmt.setString(5, idField.getText()); // WHERE 절 조건 (아이디)

            // 3. 실행
            int result = pstmt.executeUpdate(); // 수정된 행의 개수 반환

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "학생 정보가 수정되었습니다.");
                dispose(); // 성공하면 창 닫기 (이후 StudentPanel에서 목록 갱신됨)
            } else {
                JOptionPane.showMessageDialog(this, "수정 실패");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            // 4. 자원 해제
            try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}