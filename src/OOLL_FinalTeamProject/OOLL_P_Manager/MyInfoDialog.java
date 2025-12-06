// 원장의 개인정보 변경 화면!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// MyInfoDialog 클래스: 로그인한 원장님(본인)의 개인정보를 조회하고 수정하는 팝업창입니다.
// JDialog를 상속받아 모달 창으로 동작하며, ActionListener를 구현하여 버튼 이벤트를 처리합니다.
public class MyInfoDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 변수 선언 ---
    JTextField idField;    // 아이디 (PK이므로 수정 불가)
    JTextField nameField;  // 이름
    JTextField passField;  // 비밀번호
    JTextField phoneField; // 전화번호
    JTextField emailField; // 이메일
    JTextField addrField;  // 주소

    JButton updateBtn; // 수정 완료 버튼
    JButton cancelBtn; // 취소 버튼

    String loginId; // 수정할 대상(로그인한 사람)의 아이디를 저장할 변수

    // 생성자: 팝업창의 UI를 구성하고, DB에서 내 정보를 불러와 화면에 채웁니다.
    // parent: 부모 프레임, title: 창 제목, id: 로그인한 아이디
    public MyInfoDialog(JFrame parent, String title, String id) {
        super(parent, title, true); // true: 모달 창 (이 창을 닫기 전에는 부모 창 클릭 불가)
        this.loginId = id;          // 전달받은 아이디 저장
        setSize(400, 500);          // 창 크기 설정 (가로 400, 세로 500)
        setLocationRelativeTo(parent); // 부모 창의 정중앙에 배치

        // 1. 메인 패널 (전체 틀) 설정
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // 상하좌우 여백 20px
        mainPanel.setBackground(Color.WHITE); // 배경색 흰색

        // 2. 입력 폼 패널 (2열 그리드 레이아웃: 라벨 - 입력창)
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15)); // 가로 간격 10, 세로 간격 15
        formPanel.setBackground(Color.WHITE);

        // (1) 아이디: PK이므로 절대 수정 불가능하게 설정 (setEditable false)
        formPanel.add(new JLabel("아이디:"));
        idField = new JTextField();
        idField.setEditable(false); // 읽기 전용 설정
        formPanel.add(idField);

        // (2) 이름: 개명 등의 이유로 수정 가능하도록 설정
        formPanel.add(new JLabel("이름:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        // (3) 비밀번호: 수정 가능
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

        // 3. 하단 버튼 패널 구성
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0)); // 위쪽 간격 15px

        // 수정 완료 버튼
        updateBtn = new JButton("수정완료");
        updateBtn.setBackground(Color.WHITE);
        updateBtn.setPreferredSize(new Dimension(100, 35));
        updateBtn.addActionListener(this); // 버튼 클릭 시 actionPerformed 메소드 실행

        // 취소 버튼
        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(this); // 버튼 클릭 시 actionPerformed 메소드 실행

        btnPanel.add(updateBtn);
        btnPanel.add(cancelBtn);

        // 4. 패널 조립 (중앙에 입력폼, 남쪽에 버튼)
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel); // 다이얼로그에 메인 패널 추가

        // ★ 창이 열리자마자 DB에서 내 정보를 조회하여 필드에 채움
        loadMyInfo();
    }

    // ★ ActionListener 구현부: 버튼 클릭 이벤트 처리
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 버튼 누르면 그냥 창 닫기
        } else if (e.getSource() == updateBtn) {
            updateMyInfo(); // 수정 버튼 누르면 DB 업데이트 로직 실행
        }
    }

    // ★ [기능 1] 내 정보 불러오기 (SELECT 쿼리)
    private void loadMyInfo() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 1. DB 연결 (학교에서 배운 방식)
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            // ★ 비밀번호는 프로젝트 통일 (java2025)
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 2. SQL 작성: member 테이블에서 내 아이디로 정보 조회
            String sql = "SELECT * FROM member WHERE member_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId); // ? 에 로그인 ID 바인딩

            rs = pstmt.executeQuery(); // 쿼리 실행

            // 3. 조회된 데이터가 있으면 텍스트 필드에 값을 채워 넣음 (setText)
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
            // 4. 자원 해제 (연결 끊기)
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // ★ [기능 2] 정보 수정하기 (UPDATE 쿼리)
    private void updateMyInfo() {
        String pw = passField.getText().trim(); // 입력된 비밀번호 가져오기

        // 1. 유효성 검사 (필수 항목 확인)
        if(nameField.getText().trim().isEmpty() || pw.isEmpty() || phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름, 비밀번호, 전화번호는 필수입니다.");
            return;
        }

        // 2. ★ [추가된 보안 로직] 비밀번호 길이 검사 (8자 미만이면 경고)
        if (pw.length() < 8) {
            JOptionPane.showMessageDialog(this, "비밀번호는 8자 이상이어야 합니다.", "보안 경고", JOptionPane.WARNING_MESSAGE);
            return; // DB 저장 안 하고 여기서 멈춤!
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 3. SQL 작성: 회원 정보 수정 (UPDATE)
            // 아이디(PK)를 조건(WHERE)으로 나머지 정보들을 업데이트함
            String sql = "UPDATE member SET name=?, password=?, phone=?, email=?, address=? WHERE member_id=?";

            pstmt = con.prepareStatement(sql);
            // 입력창에 있는 값들을 순서대로 SQL 파라미터에 대입
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, pw); // 검사 통과한 비밀번호
            pstmt.setString(3, phoneField.getText());
            pstmt.setString(4, emailField.getText());
            pstmt.setString(5, addrField.getText());
            pstmt.setString(6, loginId); // WHERE 조건 (내 아이디)

            // 4. 실행
            int result = pstmt.executeUpdate(); // 수정된 행의 개수 반환

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
            // 자원 해제
            try { if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}