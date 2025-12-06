// TeacherStudentList.java
package OOLL_P_Teacher; // TeacherMain, ConnectDB와 같은 패키지로 맞춤

import javax.swing.*; // 스윙 컴포넌트
import javax.swing.table.DefaultTableModel; // 테이블 모델
import javax.swing.table.TableCellRenderer; // 셀 렌더러
import javax.swing.table.TableCellEditor; // 셀 에디터
import java.awt.*; // 레이아웃, Color
import java.awt.event.*; // 이벤트
import java.util.EventObject; // 셀 에디터 이벤트
import java.util.Date; // 날짜 타입
import java.text.SimpleDateFormat; // 날짜 포맷
import java.sql.*; // JDBC
import java.util.ArrayList; // ArrayList
import java.util.List; // List

// ===== 수강생 목록 + 출결 관리 다이얼로그 =====
public class TeacherStudentList extends JDialog { // JDialog 상속

    private JTable tblStudents; // 수강생 테이블
    private DefaultTableModel studentTableModel; // 테이블 모델
    private JSpinner dateSpinner; // 날짜 선택 스피너
    private CalendarPanel calendarPanel; // 월 단위 달력 패널

    private int lectureNo; // 현재 강의 번호 (lecture_no)
    private SimpleDateFormat dateFormat = // yyyy-MM-dd 형식 날짜 포맷
            new SimpleDateFormat("yyyy-MM-dd");

    private List<Integer> studentNoList = // JTable 행 인덱스 → student_no 매핑 리스트
            new ArrayList<>();

    // ===== 생성자 =====
    public TeacherStudentList(TeacherMain owner, String title, int lectureNo) { // TeacherMain에서 호출
        super(owner, title, true); // 모달 다이얼로그 설정
        this.lectureNo = lectureNo; // 강의번호 저장

        initComponents(); // UI 구성
        reloadStudentsForCurrentDate(); // 현재 날짜 기준 수강생/출결 데이터 로드
        // 크기/위치/visible은 TeacherMain에서 setSize/setLocation/setVisible 호출
    }

    // ===== UI 구성 =====
    private void initComponents() {                     // UI 구성 메소드
        Container c = getContentPane();                 // 컨텐트팬 가져오기
        c.setLayout(new BorderLayout());                // BorderLayout 사용

        // ---- 상단: 날짜 + 달력 영역 ----
        JPanel topPanel = new JPanel(new BorderLayout()); // 상단 전체 패널

        // 상단 위쪽: 날짜 라벨 + 스피너
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 날짜 영역 패널
        JLabel lblDate = new JLabel("날짜:");            // "날짜:" 라벨

        SpinnerDateModel dateModel =                    // 스피너에 사용할 날짜 모델
                new SpinnerDateModel(
                        new Date(),                     // 기본값: 오늘
                        null,                           // 최소값 제한 없음
                        null,                           // 최대값 제한 없음
                        java.util.Calendar.DAY_OF_MONTH // 일 단위 증감
                );
        dateSpinner = new JSpinner(dateModel);          // JSpinner 생성
        JSpinner.DateEditor editor =                    // 날짜 표시 형식 지정
                new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);                  // 스피너에 에디터 설정

        datePanel.add(lblDate);                         // 라벨 추가
        datePanel.add(dateSpinner);                     // 스피너 추가

        // 가운데: 실제 달력 패널
        calendarPanel = new CalendarPanel(              // CalendarPanel 생성
                (Date) dateSpinner.getValue(),          // 초기 날짜: 스피너 값
                selectedDate -> {                       // 날짜 선택 콜백 (람다)
                    dateSpinner.setValue(selectedDate); // 달력에서 날짜 클릭 시 스피너 값 변경
                });

        // 스피너 값 변경 시: 테이블 재로딩 + 달력 동기화
        dateSpinner.addChangeListener(e -> {             // 스피너 값이 바뀔 때
            reloadStudentsForCurrentDate();             // 현재 날짜 기준 수강생/출결 로드
            calendarPanel.setDisplayDate(               // 달력도 현재 스피너 날짜 기준으로 다시 그림
                    (Date) dateSpinner.getValue()
            );
        });

        topPanel.add(datePanel, BorderLayout.NORTH);    // 날짜 패널을 상단에 배치
        topPanel.add(calendarPanel, BorderLayout.CENTER); // 달력 패널을 중앙에 배치

        c.add(topPanel, BorderLayout.NORTH);            // 상단 영역으로 추가

        // ---- 중앙: 수강생 테이블 ----
        String[] columnNames = {                        // 컬럼명 배열
                "학생 이름",                            // 0번: 학생 이름
                "출결 상태",                            // 1번: 현재 출결 상태
                "출석",                                // 2번: 출석 버튼
                "지각",                                // 3번: 지각 버튼
                "결석"                                 // 4번: 결석 버튼
        };

        studentTableModel = new DefaultTableModel(columnNames, 0) { // 테이블 모델 생성
            @Override
            public boolean isCellEditable(int row, int column) {   // 셀 편집 가능 여부
                return column == 2 || column == 3 || column == 4;  // 버튼 3개 컬럼만 편집 가능
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {      // 각 컬럼 타입 지정
                if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4) {
                    return JButton.class;                          // 버튼 타입
                }
                return String.class;                               // 나머지는 문자열
            }
        };

        tblStudents = new JTable(studentTableModel);               // JTable 생성
        tblStudents.setRowHeight(28);                              // 행 높이 설정

        // 출석/지각/결석 버튼용 렌더러/에디터 설정
        tblStudents.getColumnModel().getColumn(2).setCellRenderer(
                new AttendanceButtonRenderer("출석"));             // 2번 컬럼: 출석 렌더러
        tblStudents.getColumnModel().getColumn(2).setCellEditor(
                new AttendanceButtonEditor(new JCheckBox(), "출석", this)); // 2번 컬럼: 출석 에디터

        tblStudents.getColumnModel().getColumn(3).setCellRenderer(
                new AttendanceButtonRenderer("지각"));             // 3번 컬럼: 지각 렌더러
        tblStudents.getColumnModel().getColumn(3).setCellEditor(
                new AttendanceButtonEditor(new JCheckBox(), "지각", this)); // 3번 컬럼: 지각 에디터

        tblStudents.getColumnModel().getColumn(4).setCellRenderer(
                new AttendanceButtonRenderer("결석"));             // 4번 컬럼: 결석 렌더러
        tblStudents.getColumnModel().getColumn(4).setCellEditor(
                new AttendanceButtonEditor(new JCheckBox(), "결석", this)); // 4번 컬럼: 결석 에디터

        JScrollPane scrollPane = new JScrollPane(tblStudents);     // 스크롤 가능하도록 감싸기
        c.add(scrollPane, BorderLayout.CENTER);                    // 중앙에 테이블 배치
    }

    // ===== 현재 선택된 날짜 문자열(yyyy-MM-dd) =====
    private String getSelectedDateKey() {                          // 스피너의 현재 날짜를 yyyy-MM-dd로 반환
        Date d = (Date) dateSpinner.getValue();                    // 스피너에서 Date 꺼내기
        return dateFormat.format(d);                               // 포맷 적용 후 문자열 반환
    }

    // ===== 선택된 날짜 기준으로 테이블 다시 로드 =====
    private void reloadStudentsForCurrentDate() {                  // 현재 날짜 기준 수강생/출결 로드
        studentTableModel.setRowCount(0);                          // 기존 행들 모두 삭제
        studentNoList.clear();                                     // student_no 리스트 초기화

        String dateKey = getSelectedDateKey();                     // yyyy-MM-dd 형식 날짜 문자열
        loadStudentsFromDB(dateKey);                               // DB에서 해당 날짜 수강생/출결 로드
    }

    // ===== DB에서 수강생 목록 + 출결 상태 로드 =====
    private void loadStudentsFromDB(String dateKey) {              // dateKey: "yyyy-MM-dd"
        Connection con = null;                                     // DB 연결 객체
        PreparedStatement ps = null;                               // PreparedStatement
        ResultSet rs = null;                                       // ResultSet

        try {
            con = ConnectDB.getConnection();                       // DB 연결

            // enrollment + student + member + attendance 조인
            String sql =
                    "SELECT s.student_no, m.name AS student_name, " +
                            "       COALESCE(a.attendance_status, '결석') AS attendance_status " +
                            "FROM enrollment e " +
                            "JOIN student s ON e.student_no = s.student_no " +
                            "JOIN member m ON s.member_id = m.member_id " +
                            "LEFT JOIN attendance a " +
                            "  ON a.lecture_no = e.lecture_no " +
                            " AND a.student_no = e.student_no " +
                            " AND a.att_date = ? " +
                            "WHERE e.lecture_no = ? " +
                            "ORDER BY m.name";

            ps = con.prepareStatement(sql);                        // 쿼리 준비
            ps.setString(1, dateKey);                              // 1번 파라미터: 날짜(yyyy-MM-dd)
            ps.setInt(2, lectureNo);                               // 2번 파라미터: lecture_no

            rs = ps.executeQuery();                                // 쿼리 실행

            while (rs.next()) {                                    // 결과 한 줄씩 처리
                int studentNo = rs.getInt("student_no");           // 학생 번호
                String studentName = rs.getString("student_name"); // 학생 이름
                String status = rs.getString("attendance_status"); // 출결 상태

                studentNoList.add(studentNo);                      // 인덱스 → student_no 매핑 저장

                Object[] row = {                                   // JTable 한 행 구성
                        studentName,                               // 학생 이름
                        status,                                    // 출결 상태
                        "출석",                                    // 출석 버튼 텍스트
                        "지각",                                    // 지각 버튼 텍스트
                        "결석"                                     // 결석 버튼 텍스트
                };
                studentTableModel.addRow(row);                     // 테이블 모델에 행 추가
            }

        } catch (SQLException e) {
            e.printStackTrace();                                   // 콘솔에 에러 출력
            JOptionPane.showMessageDialog(                         // 사용자에게 에러 안내
                    this,
                    "수강생/출결 정보 조회 중 오류가 발생했습니다.\n" + e.getMessage(),
                    "DB 오류",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            ConnectDB.close(new AutoCloseable[]{rs, ps, con});     // 자원 정리
        }
    }

    // ===== 출결 상태 업데이트 메서드 =====
    public void updateAttendance(int rowIndex, String status) {    // 클릭한 행(rowIndex)의 출결을 status로 변경
        if (rowIndex < 0 || rowIndex >= studentTableModel.getRowCount()) { // 인덱스 검증
            return;                                                // 유효하지 않으면 종료
        }
        if (rowIndex >= studentNoList.size()) {                    // studentNoList 범위 체크
            return;                                                // 유효하지 않으면 종료
        }

        String dateKey = getSelectedDateKey();                     // 현재 선택 날짜 (yyyy-MM-dd)
        String studentName = (String) studentTableModel.getValueAt(rowIndex, 0); // 학생 이름
        int studentNo = studentNoList.get(rowIndex);               // 학생 번호

        Connection con = null;                                     // DB 연결
        PreparedStatement ps = null;                               // PreparedStatement

        try {
            con = ConnectDB.getConnection();                       // DB 연결

            // attendance 테이블에 출결 기록 INSERT/UPDATE
            String sql =
                    "INSERT INTO attendance (lecture_no, student_no, att_date, attendance_status) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE attendance_status = VALUES(attendance_status)";

            ps = con.prepareStatement(sql);                        // 쿼리 준비
            ps.setInt(1, lectureNo);                               // 1: lecture_no
            ps.setInt(2, studentNo);                               // 2: student_no
            ps.setString(3, dateKey);                              // 3: att_date (yyyy-MM-dd)
            ps.setString(4, status);                               // 4: attendance_status ("출석"/"지각"/"결석")

            ps.executeUpdate();                                    // 실행

            studentTableModel.setValueAt(status, rowIndex, 1);     // 테이블 상 출결 상태 컬럼 갱신

            System.out.println("[출결 변경] lecture_no=" + lectureNo +
                    ", date=" + dateKey +
                    ", student_no=" + studentNo +
                    ", student_name=" + studentName +
                    ", status=" + status);                         // 로그 출력

        } catch (SQLException e) {
            e.printStackTrace();                                   // 콘솔에 에러 출력
            JOptionPane.showMessageDialog(                         // 사용자에게 에러 안내
                    this,
                    "출결 저장 중 오류가 발생했습니다.\n" + e.getMessage(),
                    "DB 오류",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            ConnectDB.close(new AutoCloseable[]{ps, con});         // 자원 정리
        }
    }

    // ===== 출석/지각/결석 버튼 렌더러 =====
    private static class AttendanceButtonRenderer extends JButton implements TableCellRenderer { // 버튼 렌더러
        public AttendanceButtonRenderer(String text) {             // 생성자
            super(text);                                          // 버튼 텍스트 설정
            setOpaque(true);                                      // 불투명 설정
        }

        @Override
        public Component getTableCellRendererComponent(           // 셀 렌더링
                                                                  JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            setText(value == null ? "" : value.toString());       // 셀 값으로 텍스트 설정

            if (isSelected) {                                     // 선택된 셀이면
                setForeground(table.getSelectionForeground());    // 선택 전경색
                setBackground(table.getSelectionBackground());    // 선택 배경색
            } else {
                setForeground(table.getForeground());             // 기본 전경색
                setBackground(UIManager.getColor("Button.background")); // 기본 버튼 배경색
            }

            return this;                                          // 버튼 컴포넌트 반환
        }
    }

    // ===== 출석/지각/결석 버튼 에디터 =====
    private static class AttendanceButtonEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {          // 셀 에디터 + ActionListener

        private JButton button;                                   // 실제 버튼
        private String actionType;                                // "출석"/"지각"/"결석"
        private TeacherStudentList parent;                        // 부모 다이얼로그
        private int currentRow;                                   // 현재 행 인덱스

        public AttendanceButtonEditor(JCheckBox checkBox, String actionType, TeacherStudentList parent) {
            this.actionType = actionType;                         // 액션 타입 저장
            this.parent = parent;                                 // 부모 다이얼로그 저장
            this.button = new JButton(actionType);                // 버튼 생성
            this.button.setOpaque(true);                          // 불투명 설정
            this.button.addActionListener(this);                  // 액션 리스너 등록
        }

        @Override
        public Component getTableCellEditorComponent(             // 편집용 컴포넌트 반환
                                                                  JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;                                     // 현재 행 저장
            button.setText(value == null ? actionType : value.toString()); // 버튼 텍스트 설정
            return button;                                        // 버튼 반환
        }

        @Override
        public Object getCellEditorValue() {                      // 편집 결과 값
            return actionType;                                    // 액션 타입 그대로 반환
        }

        @Override
        public boolean isCellEditable(EventObject e) {            // 항상 편집 가능
            return true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {              // 버튼 클릭 시
            fireEditingStopped();                                 // 셀 편집 종료 알림
            parent.updateAttendance(currentRow, actionType);      // 부모 다이얼로그에 출결 업데이트 요청
        }
    }
}
