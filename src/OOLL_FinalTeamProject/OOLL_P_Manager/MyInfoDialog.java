// 원장의 개인정보 변경 화면!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// MyInfoDialog 클래스: 로그인한 사용자의 개인정보를 확인하고 수정하는 팝업창입니다.
public class MyInfoDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 변수 선언 ---
    JTextField idField;    // 아이디 (수정 불가)
    JTextField nameField;  // 이름
    JTextField passField;  // 비밀번호
    JTextField phoneField; // 전화번호
    JTextField emailField; // 이메일
    JTextField addrField;  // 주소

    JButton updateBtn; // 수정 완료 버튼
    JButton cancelBtn; // 취소 버튼

    String loginId; // 현재 로그인한 사용자의 ID (수정 기준키)

    // 생성자: 팝업창 UI를 구성하고 DB에서 내 정보를 불러옵니다.
    public MyInfoDialog(JFrame parent, String title, String id) {
        super(parent, title, true); // 모달 창 설정 (부모 창 제어 막음)
        this.loginId = id;          // 전달받은 로그인 ID 저장
        setSize(400, 500);          // 창 크기
        setLocationRelativeTo(parent); // 중앙 배치

        // 1. 메인 패널 (전체 틀)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // 2. 입력 폼 패널 (2열 그리드)
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);

        // (1) 아이디: PK이므로 절대 수정 불가능하게 설정 (setEditable false)
        formPanel.add(new JLabel("아이디:"));
        idField = new JTextField();
        idField.setEditable(false);
        formPanel.add(idField);

        // (2) 이름: 개명 등의 이유로 수정 가능하게 함
        formPanel.add(new JLabel("이름:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        // (3) 비밀번호
        formPanel.add(new JLabel("비밀번호:"));
        passField = new JTextField();
        formPanel.add(passField);

        // (4) 전화번호
        formPanel.add(new JLabel("전화번호:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        // (5) 이메일
        formPanel.add(new JLabel("이메일:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        // (6) 주소
        formPanel.add(new JLabel("주소:"));
        addrField = new JTextField();
        formPanel.add(addrField);

        // 3. 하단 버튼 패널
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        updateBtn = new JButton("수정완료");
        updateBtn.setBackground(Color.WHITE);
        updateBtn.setPreferredSize(new Dimension(100, 35));
        updateBtn.addActionListener(this); // 이벤트 리스너 연결

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(this); // 이벤트 리스너 연결

        btnPanel.add(updateBtn);
        btnPanel.add(cancelBtn);

        // 패널 조립
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // ★ 창이 열리자마자 DB에서 내 정보를 조회하여 필드에 채움
        loadMyInfo();
    }

    // ★ ActionListener 구현부
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 시 그냥 닫기
        } else if (e.getSource() == updateBtn) {
            updateMyInfo(); // 수정 버튼 시 DB 업데이트 로직 실행
        }
    }

    // ★ [기능 1] 내 정보 불러오기 (SELECT)
    private void loadMyInfo() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 회원 테이블에서 내 아이디로 조회
            String sql = "SELECT * FROM member WHERE member_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId); // 로그인 ID 바인딩

            rs = pstmt.executeQuery();

            // 조회된 데이터가 있으면 텍스트 필드에 값을 채워 넣음 (setText)
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
            JOptionPane.showMessageDialog(this, "정보 로드 실패: " + e.getMessage());
        } finally {
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // ★ [기능 2] 정보 수정하기 (UPDATE)
    private void updateMyInfo() {
        // 1. 유효성 검사 (필수 항목 확인)
        if(nameField.getText().trim().isEmpty() || passField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름, 비밀번호, 전화번호는 필수입니다.");
            return;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 2. SQL 작성 (회원 정보 수정)
            // 아이디(PK)를 조건으로 나머지 정보를 업데이트함
            String sql = "UPDATE member SET name=?, password=?, phone=?, email=?, address=? WHERE member_id=?";

            pstmt = con.prepareStatement(sql);
            // 순서대로 값 대입
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, passField.getText());
            pstmt.setString(3, phoneField.getText());
            pstmt.setString(4, emailField.getText());
            pstmt.setString(5, addrField.getText());
            pstmt.setString(6, loginId); // WHERE 조건

            // 3. 실행
            int result = pstmt.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "정보가 수정되었습니다.");
                dispose(); // 성공 시 창 닫기 (ManagerMainFrame에서 자동으로 화면 갱신됨)
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