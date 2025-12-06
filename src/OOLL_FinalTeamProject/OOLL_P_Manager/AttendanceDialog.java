// 원장의 출결 관리 창!!!!

package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// AttendanceDialog 클래스: 특정 강의의 날짜별 출석부를 관리하는 팝업창입니다.
// JDialog를 상속받아 모달 창으로 동작하며, ActionListener로 버튼 이벤트를 처리합니다.
public class AttendanceDialog extends JDialog implements ActionListener {

    // --- 멤버 변수 선언 ---
    String lectureNo; // 현재 어떤 강의의 출석부인지 식별하기 위한 강의번호(PK)

    JTextField dateField; // 날짜 입력창 (YYYY-MM-DD)

    DefaultTableModel tableModel; // 테이블 데이터 관리자
    JTable table;                 // 화면에 보이는 표

    // 버튼들
    JButton loadBtn;     // 조회/초기화 버튼
    JButton presentBtn;  // 출석 처리 버튼
    JButton lateBtn;     // 지각 처리 버튼
    JButton absentBtn;   // 결석 처리 버튼
    JButton closeBtn;    // 닫기 버튼

    // 생성자: 팝업창 UI를 구성하고 초기 데이터를 로드합니다.
    // parent: 부모 프레임, title: 창 제목, lectureNo: 클릭된 강의의 번호
    public AttendanceDialog(JFrame parent, String title, String lectureNo) {
        super(parent, title, true); // true: 모달 창 (뒤쪽 창 클릭 방지)
        this.lectureNo = lectureNo; // 전달받은 강의 번호 저장 (DB 조회 시 사용)
        setSize(600, 500);          // 창 크기 설정 (가로 600, 세로 500)
        setLocationRelativeTo(parent); // 부모 창 중앙에 배치

        // 메인 패널 설정
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // 전체 여백 20px
        mainPanel.setBackground(Color.WHITE); // 배경 흰색

        // 1. [상단] 날짜 선택 및 조회 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 왼쪽 정렬
        topPanel.setBackground(Color.WHITE);

        topPanel.add(new JLabel("날짜(YYYY-MM-DD): "));

        // 오늘 날짜를 기본값으로 자동 설정 (사용자 편의성)
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        dateField = new JTextField(today, 10);
        topPanel.add(dateField);

        loadBtn = new JButton("조회 / 초기화");
        loadBtn.setBackground(new Color(240, 240, 240)); // 버튼 색상 (연회색)
        loadBtn.addActionListener(this); // 이벤트 연결
        topPanel.add(loadBtn);

        // 2. [중앙] 수강생 목록 테이블
        // 컬럼 구성: 0번 '학생번호'는 DB 업데이트용 PK (화면엔 숨길 예정)
        String[] cols = {"학생번호", "이름", "아이디", "연락처", "출결상태"};

        // 테이블 모델 생성 (내용 수정 불가)
        tableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table); // 공통 스타일 적용 (높이, 폰트 등)

        // ★ 핵심: 0번 컬럼(학생번호)을 화면(View)에서 숨김!
        // (업데이트 쿼리 날릴 때 WHERE 조건절에 쓰기 위함)
        table.removeColumn(table.getColumnModel().getColumn(0));

        JScrollPane scroll = new JScrollPane(table); // 스크롤바 추가

        // 3. [하단] 상태 변경 버튼들 (출석, 지각, 결석)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // 중앙 정렬
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // 위쪽 간격

        // 버튼 생성 도우미 메서드 사용 (코드 중복 제거)
        presentBtn = createStatusButton("출석", Color.WHITE);
        lateBtn = createStatusButton("지각", Color.WHITE);
        absentBtn = createStatusButton("결석", Color.WHITE);

        closeBtn = new JButton("닫기");
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setPreferredSize(new Dimension(80, 40));
        closeBtn.addActionListener(e -> dispose()); // 창 닫기

        // 패널에 버튼 추가
        btnPanel.add(presentBtn);
        btnPanel.add(lateBtn);
        btnPanel.add(absentBtn);
        btnPanel.add(Box.createHorizontalStrut(20)); // 기능 버튼과 닫기 버튼 사이 간격
        btnPanel.add(closeBtn);

        // 최종 조립
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // ★ 창이 열리자마자 오늘 날짜 기준으로 자동 조회 실행
        loadAttendance();
    }

    // 버튼 생성 도우미 메서드
    private JButton createStatusButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(80, 40));
        btn.addActionListener(this);
        return btn;
    }

    // ★ ActionListener 구현부: 버튼 클릭 이벤트 처리
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource(); // 어떤 버튼이 눌렸는지 확인

        if (src == loadBtn) {
            loadAttendance(); // 조회 및 초기화 실행
        } else if (src == presentBtn) {
            updateStatus("출석"); // '출석' 상태로 변경
        } else if (src == lateBtn) {
            updateStatus("지각"); // '지각' 상태로 변경
        } else if (src == absentBtn) {
            updateStatus("결석"); // '결석' 상태로 변경
        }
    }

    // ★ [핵심 기능 1] 출결 데이터 조회 및 초기화 (DB 프로시저 사용)
    // 교수님 질문 대비: "데이터가 없는 날짜는 어떻게 처리하나요?" -> "프로시저가 자동으로 생성해줍니다."
    private void loadAttendance() {
        String date = dateField.getText().trim();

        // 1. 날짜 입력값 검증 (빈칸 확인)
        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "날짜를 입력하세요.");
            return;
        }

        // 2. 정규표현식(Regex)을 이용한 날짜 형식 검사 (yyyy-MM-dd)
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            JOptionPane.showMessageDialog(this, "날짜를 xxxx(년)-xx(월)-xx(일) 형식으로 입력해 주세요.\n예: 2025-12-05", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. 실제 존재하는 날짜인지 논리적 검사 (예: 2월 30일 방지)
        try {
            java.time.LocalDate.parse(date);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "존재하지 않는 날짜입니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0); // 기존 테이블 내용 비우기
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 4. [프로시저 호출] proc_init_attendance
            // 역할: 해당 날짜에 출결 데이터가 없으면, 수강생 전체를 '결석' 상태로 일괄 생성(INSERT)함.
            // 이렇게 하면 자바에서 일일이 INSERT 할 필요가 없어서 성능이 좋음.
            String procSql = "CALL proc_init_attendance(" + lectureNo + ", '" + date + "')";
            stmt = con.createStatement();
            stmt.execute(procSql); // 프로시저 실행
            stmt.close(); // 닫고 다시 엶 (다음 쿼리 실행 위해)

            // 5. [조회 쿼리] 출결 테이블(attendance) + 학생 정보 JOIN
            // attendance(a), student(s), member(m) 3개 테이블 조인
            String selectSql = "SELECT s.student_no, m.name, m.member_id, m.phone, a.attendance_status " +
                    "FROM attendance a " +
                    "JOIN student s ON a.student_no = s.student_no " +
                    "JOIN member m ON s.member_id = m.member_id " +
                    "WHERE a.lecture_no = " + lectureNo + " AND a.att_date = '" + date + "' " +
                    "ORDER BY m.name";

            stmt = con.createStatement();
            rs = stmt.executeQuery(selectSql);

            while(rs.next()) {
                String sNo = rs.getString("student_no"); // PK
                String name = rs.getString("name");
                String id = rs.getString("member_id");
                String phone = rs.getString("phone");
                String status = rs.getString("attendance_status");

                // 모델에 데이터 추가 (0번: PK, 1~4: 화면 표시 데이터)
                tableModel.addRow(new Object[]{sNo, name, id, phone, status});
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage());
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // ★ [핵심 기능 2] 출결 상태 변경 (DB UPDATE)
    private void updateStatus(String newStatus) {
        // 선택된 행이 있는지 확인
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "상태를 변경할 학생을 선택해주세요.");
            return;
        }

        // 1. 모델에서 숨겨진 학생번호(PK) 가져오기
        // (화면엔 없지만 모델의 0번 인덱스에 저장되어 있음)
        String sNo = table.getModel().getValueAt(selectedRow, 0).toString();
        String date = dateField.getText().trim(); // 날짜

        Connection con = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 2. UPDATE 쿼리 실행
            // 조건: 학생번호 AND 강의번호 AND 날짜 가 일치하는 행을 수정
            String sql = "UPDATE attendance SET attendance_status = '" + newStatus + "' " +
                    "WHERE student_no = " + sNo + " AND lecture_no = " + lectureNo + " AND att_date = '" + date + "'";

            stmt = con.createStatement();
            int result = stmt.executeUpdate(sql);

            if (result > 0) {
                // 3. [UI 갱신] DB 업데이트 성공 시, 화면 값도 바로 바꿔줌
                // 전체 새로고침(refresh)하지 않고 해당 셀 값만 바꿔서 효율적임
                // 4번 컬럼이 '출결상태' 표시용
                table.getModel().setValueAt(newStatus, selectedRow, 4);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "변경 실패: " + e.getMessage());
        } finally {
            try { if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}