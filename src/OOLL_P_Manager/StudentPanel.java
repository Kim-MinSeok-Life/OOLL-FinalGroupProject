// 원장의 학생 관리 탭!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// StudentPanel 클래스: '학생 관리' 탭의 화면과 기능을 담당합니다.
// ActionListener를 구현하여 버튼 클릭 이벤트를 직접 처리합니다.
public class StudentPanel extends JPanel implements ActionListener {

    // --- 멤버 변수 선언 ---
    JFrame parentFrame;           // 팝업창을 띄울 때 부모가 될 프레임
    DefaultTableModel tableModel; // 테이블 데이터 관리자 (행 추가/삭제 담당)
    JTable table;                 // 화면에 보여지는 표
    JTextField searchField;       // 검색어 입력창

    // 버튼들 (이벤트 처리를 위해 멤버 변수로 선언)
    JButton searchBtn;  // 검색
    JButton editBtn;    // 수정
    JButton delBtn;     // 삭제
    JButton feeBtn;     // 수강료 설정 (추가 기능)

    // 생성자: 패널이 만들어질 때 UI를 구성합니다.
    public StudentPanel(JFrame parent) {
        this.parentFrame = parent; // 메인 프레임 주소를 받아 저장

        // 1. 패널 레이아웃 및 디자인 설정
        setLayout(new BorderLayout()); // 전체를 동서남북 구조로 설정
        setBorder(new EmptyBorder(20, 20, 20, 20)); // 테두리 여백 20px
        setBackground(Color.WHITE); // 배경 흰색

        // 2. 상단 패널 (검색창 + 버튼들)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 우측 정렬
        topPanel.setBackground(Color.WHITE);

        // (2-1) 검색 UI 구성
        topPanel.add(new JLabel("학생명/ID 검색 : "));
        searchField = new JTextField(15);
        searchField.addActionListener(this); // 엔터키 치면 검색 실행
        topPanel.add(searchField);

        searchBtn = new JButton("검색");
        searchBtn.setBackground(new Color(245, 245, 245));
        searchBtn.addActionListener(this); // 버튼 클릭 리스너 연결

        // (2-2) 관리 기능 버튼들 구성
        editBtn = new JButton("학생 정보 수정");
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(this);

        delBtn = new JButton("삭제");
        delBtn.setBackground(Color.WHITE);
        delBtn.addActionListener(this);

        // (2-3) 수강료 설정 버튼 (추가 요구사항 반영)
        feeBtn = new JButton("수강료 설정");
        feeBtn.setBackground(Color.WHITE);
        feeBtn.addActionListener(this);

        // 상단 패널에 버튼들 추가 (순서대로 배치됨)
        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(20)); // 간격 띄우기
        topPanel.add(editBtn);
        topPanel.add(delBtn);
        topPanel.add(feeBtn);

        // 3. 중앙 테이블 구성
        // 컬럼 정의: 아이디를 맨 앞으로 배치 (가독성 향상)
        // 맨 마지막 '학생번호'는 DB의 PK(Primary Key)로, 화면엔 안 보이지만 내부적으로 사용함
        String[] cols = {"아이디", "이름", "전화번호", "이메일", "주소", "학생번호"};

        // 테이블 모델 생성 (데이터 수정 불가하도록 설정)
        tableModel = new DefaultTableModel(null, cols) {
            @Override // 셀 더블클릭 수정 방지
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table); // 공통 스타일(높이, 색상 등) 적용

        // ★ 핵심: 5번째 컬럼('학생번호')을 화면(View)에서 숨김!
        // 사용자는 볼 필요 없지만, 삭제/수정할 때 이 번호가 반드시 필요하기 때문입니다.
        table.removeColumn(table.getColumnModel().getColumn(5));

        // 4. 패널에 컴포넌트 배치
        add(topPanel, BorderLayout.NORTH); // 상단에 버튼
        add(new JScrollPane(table), BorderLayout.CENTER); // 중앙에 스크롤 가능한 테이블

        // 5. 초기 데이터 로드 (전체 목록)
        refreshTable("");
    }

    // ★ ActionListener 구현: 버튼 클릭 시 실행되는 메소드
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource(); // 눌린 버튼이 무엇인지 확인

        if (source == searchBtn || source == searchField) {
            // 검색: 입력된 텍스트로 DB 다시 조회
            refreshTable(searchField.getText());

        } else if (source == editBtn) {
            // 수정: 선택된 행의 데이터를 가져와서 팝업창에 전달
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) { // 선택 안 했으면 경고
                JOptionPane.showMessageDialog(parentFrame, "수정할 학생을 선택해주세요.");
                return;
            }
            // 모델에서 데이터 꺼내기 (화면엔 안 보여도 모델엔 다 있음)
            // 인덱스 순서: 0:아이디, 1:이름, 2:전화, 3:이메일, 4:주소, 5:PK
            String id = table.getModel().getValueAt(selectedRow, 0).toString();
            String name = table.getModel().getValueAt(selectedRow, 1).toString();
            String phone = table.getModel().getValueAt(selectedRow, 2).toString();
            String email = table.getModel().getValueAt(selectedRow, 3).toString();
            String addr = table.getModel().getValueAt(selectedRow, 4).toString();

            String[] currentData = {id, name, phone, email, addr};

            // StudentDialog 팝업 띄우기 (데이터 전달)
            new StudentDialog(parentFrame, "학생 정보 수정", currentData).setVisible(true);
            refreshTable(""); // 팝업 닫히면 목록 갱신 (수정 내용 반영)

        } else if (source == delBtn) {
            // 삭제: 별도 메소드로 분리하여 처리
            deleteStudent();

        } else if (source == feeBtn) {
            // 수강료 설정: FeeDialog 팝업 띄우기
            new FeeDialog(parentFrame).setVisible(true);
        }
    }

    // ★ [핵심 로직] 학생 삭제 메소드
    // 교수님 질문 대비 포인트: "Cascade 삭제 시 트리거 미작동 문제를 해결하기 위해 자바에서 선처리함"
    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "삭제할 학생을 선택해주세요.");
            return;
        }

        // 삭제할 대상의 ID와 이름, PK 가져오기
        String memberId = table.getModel().getValueAt(selectedRow, 0).toString();
        String name = table.getModel().getValueAt(selectedRow, 1).toString();
        String studentNo = table.getModel().getValueAt(selectedRow, 5).toString(); // PK

        // 삭제 확인 팝업
        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "[" + name + "] 학생을 정말 삭제하시겠습니까?\n(회원 정보 및 수강/출결 기록이 모두 영구 삭제됩니다.)",
                "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            Statement stmt = null;
            PreparedStatement pstmt = null;
            try {
                // DB 연결
                Class.forName("com.mysql.cj.jdbc.Driver");
                String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
                con = DriverManager.getConnection(dbUrl, "root", "java2025");

                // 1. [데이터 정합성 유지]
                // 학생을 삭제하면 수강내역도 자동 삭제(Cascade)되지만, 이때 DB 트리거가 동작하지 않아
                // 강의 테이블의 '수강인원'이 줄어들지 않는 문제가 있습니다.
                // 이를 해결하기 위해, 학생 삭제 전 자바에서 먼저 수강인원을 -1 차감해줍니다.
                String updateCountSql = "UPDATE lecture " +
                        "SET enrolled_count = enrolled_count - 1 " +
                        "WHERE lecture_no IN (SELECT lecture_no FROM enrollment WHERE student_no = " + studentNo + ")";

                stmt = con.createStatement();
                stmt.executeUpdate(updateCountSql);

                // 2. [회원 삭제]
                // 'student' 테이블이 아니라 부모인 'member' 테이블에서 삭제합니다.
                // DB의 ON DELETE CASCADE 설정 덕분에 'student', 'enrollment', 'attendance' 정보가 모두 자동 삭제됩니다.
                String deleteSql = "DELETE FROM member WHERE member_id = ?";
                pstmt = con.prepareStatement(deleteSql);
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
                // 자원 해제
                try { if(stmt!=null) stmt.close(); if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
            }
        }
    }

    // [DB 조회] 학생 목록 불러오기 (검색 기능 포함)
    public void refreshTable(String keyword) {
        tableModel.setRowCount(0); // 기존 표 내용 비우기
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // ★ 핵심: 복잡한 JOIN 쿼리 대신 미리 만들어둔 뷰(view_student_info)를 사용
            String sql = "SELECT * FROM view_student_info ";

            // 검색어가 있다면 WHERE 절 추가 (이름 또는 아이디로 검색)
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql += "WHERE name LIKE '%" + keyword.trim() + "%' OR member_id LIKE '%" + keyword.trim() + "%' ";
            }

            // 최신 등록된 학생 순서로 정렬
            sql += "ORDER BY student_no DESC";

            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            // 결과셋(ResultSet)을 반복하며 테이블 모델에 추가
            while(rs.next()) {
                String name = rs.getString("name");
                String id = rs.getString("member_id");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String addr = rs.getString("address");
                int no = rs.getInt("student_no"); // PK

                // 테이블 모델에 순서대로 데이터 삽입
                // (화면에서는 컬럼 순서를 바꿨지만, 여기서는 모델 순서대로 넣음)
                tableModel.addRow(new Object[]{id, name, phone, email, addr, no});
            }
        } catch (Exception e) {
            e.printStackTrace(); // 에러 발생 시 콘솔 출력
        } finally {
            // 자원 해제
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}