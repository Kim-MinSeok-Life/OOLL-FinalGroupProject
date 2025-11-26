package OOLL_FinalTeamProject; // 패키지 선언

// ===== import 구간 =====
import javax.swing.*; // 스윙 컴포넌트
import javax.swing.table.DefaultTableModel; // 테이블 모델
import javax.swing.table.TableCellRenderer; // 셀 렌더러
import javax.swing.table.TableCellEditor; // 셀 에디터
import java.awt.*; // 레이아웃
import java.awt.event.*; // 이벤트
import java.util.EventObject; // 셀 에디터 이벤트
import java.util.Date; // 날짜 타입
import java.util.Map;  // 출결 상태 Map
import java.text.SimpleDateFormat; // 날짜 포맷

// ===== 수강생 목록 + 출결 관리 다이얼로그 =====
public class TeacherStudentList extends JDialog { // JDialog 상속

    private JTable tblStudents; // 수강생 테이블
    private DefaultTableModel studentTableModel; // 테이블 모델
    private JSpinner dateSpinner; // 날짜 선택 스피너
    private CalendarPanel calendarPanel; // 월 달력 패널

    private TeacherMain main;      // 부모 메인 프레임
    private String lectureTitle;   // 현재 강좌 제목

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 키 포맷

    // ===== 생성자 =====
    public TeacherStudentList(Frame owner, String lectureTitle) { // 생성자
        super(owner, "수강생 목록 - " + lectureTitle, true); // 타이틀, 모달 설정
        this.lectureTitle = lectureTitle;

        if (owner instanceof TeacherMain) {
            this.main = (TeacherMain) owner;
        } else {
            throw new IllegalArgumentException("owner must be TeacherMain");
        }

        setSize(800, 600); // 달력 때문에 높이 조금 키움
        setLocationRelativeTo(owner); // 부모 기준 중앙 정렬

        initComponents(); // UI 구성
        initDummyStudents(); // 현재 선택된 날짜 기준 수강생 데이터 로드

        setVisible(true); // 다이얼로그 표시
    }

    // ===== UI 구성 =====
    private void initComponents() { // UI 구성 메서드
        Container c = getContentPane(); // 컨텐트팬 가져오기
        c.setLayout(new BorderLayout()); // BorderLayout 설정

        // ---- 상단: 날짜 + 달력 영역 ----
        JPanel topPanel = new JPanel(new BorderLayout()); // 상단 전체 패널

        // 상단 위쪽: 날짜 라벨 + 스피너 (모델 역할)
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblDate = new JLabel("날짜:");

        SpinnerDateModel dateModel =
                new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH); // 오늘 기준
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);

        datePanel.add(lblDate);
        datePanel.add(dateSpinner);

        // 가운데: 실제 달력 패널
        calendarPanel = new CalendarPanel((Date) dateSpinner.getValue(), selectedDate -> {
            // 달력에서 날짜 클릭 시 -> 스피너 값 변경 -> 출결 테이블/달력 다시 로드
            dateSpinner.setValue(selectedDate);
        });

        // 스피너 값이 바뀔 때마다 현재 날짜의 출결 다시 로드 + 달력 월/선택날짜 동기화
        dateSpinner.addChangeListener(e -> {
            reloadStudentsForCurrentDate(); // 테이블 데이터 재로딩
            calendarPanel.setDisplayDate((Date) dateSpinner.getValue()); // 달력도 동기화
        });

        topPanel.add(datePanel, BorderLayout.NORTH);
        topPanel.add(calendarPanel, BorderLayout.CENTER);

        c.add(topPanel, BorderLayout.NORTH); // 상단에 부착

        // ---- 중앙: 수강생 테이블 ----
        String[] columnNames = { // 컬럼명 배열
                "학생 이름", // 0
                "출결 상태", // 1
                "출석",      // 2
                "지각",      // 3
                "결석"       // 4
        };

        studentTableModel = new DefaultTableModel(columnNames, 0) { // 테이블 모델 생성
            @Override
            public boolean isCellEditable(int row, int column) { // 셀 편집 가능 여부
                return column == 2 || column == 3 || column == 4; // 출석/지각/결석 버튼만 편집 가능
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) { // 컬럼 타입
                if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4) { // 버튼 컬럼
                    return JButton.class; // 버튼 타입
                }
                return String.class; // 나머지는 문자열
            }
        };

        tblStudents = new JTable(studentTableModel); // 테이블 생성
        tblStudents.setRowHeight(28); // 행 높이 설정

        // 출석/지각/결석 버튼용 렌더러/에디터 설정
        tblStudents.getColumnModel().getColumn(2).setCellRenderer(
                new AttendanceButtonRenderer("출석")); // 출석 버튼 렌더러
        tblStudents.getColumnModel().getColumn(2).setCellEditor(
                new AttendanceButtonEditor(new JCheckBox(), "출석", this)); // 출석 버튼 에디터

        tblStudents.getColumnModel().getColumn(3).setCellRenderer(
                new AttendanceButtonRenderer("지각")); // 지각 버튼 렌더러
        tblStudents.getColumnModel().getColumn(3).setCellEditor(
                new AttendanceButtonEditor(new JCheckBox(), "지각", this)); // 지각 버튼 에디터

        tblStudents.getColumnModel().getColumn(4).setCellRenderer(
                new AttendanceButtonRenderer("결석")); // 결석 버튼 렌더러
        tblStudents.getColumnModel().getColumn(4).setCellEditor(
                new AttendanceButtonEditor(new JCheckBox(), "결석", this)); // 결석 버튼 에디터

        JScrollPane scrollPane = new JScrollPane(tblStudents); // 스크롤페인 생성
        c.add(scrollPane, BorderLayout.CENTER); // 중앙에 테이블 부착
    }

    // ===== 현재 선택된 날짜 문자열(yyyy-MM-dd) =====
    private String getSelectedDateKey() {
        Date d = (Date) dateSpinner.getValue();
        return dateFormat.format(d);
    }

    // ===== 선택된 날짜 기준으로 테이블 다시 로드 =====
    private void reloadStudentsForCurrentDate() {
        studentTableModel.setRowCount(0); // 기존 행 전부 삭제
        initDummyStudents();              // 현재 날짜 기준 다시 채우기
    }

    // ===== 수강생 데이터 로드 (출결 상태 포함) =====
    private void initDummyStudents() { // 수강생 데이터 생성 메서드
        String dateKey = getSelectedDateKey();

        // 메인에서 해당 강좌 + 날짜의 출결 상태 Map 가져오기 (없으면 내부에서 새로 생성됨)
        Map<String, String> lectureAttendance = main.getAttendanceForLecture(lectureTitle, dateKey);

        // 기본 출결 상태는 "결석"이고, 이전에 저장된 값이 있으면 그걸 사용
        addStudentRow("홍길동", lectureAttendance.getOrDefault("홍길동", "결석"));
        addStudentRow("김학생", lectureAttendance.getOrDefault("김학생", "결석"));
        addStudentRow("가가가", lectureAttendance.getOrDefault("가가가", "결석"));
        addStudentRow("나나나", lectureAttendance.getOrDefault("나나나", "결석"));
        addStudentRow("다다다", lectureAttendance.getOrDefault("다다다", "결석"));
        addStudentRow("라라라", lectureAttendance.getOrDefault("라라라", "결석"));
        addStudentRow("마마마", lectureAttendance.getOrDefault("마마마", "결석"));
        addStudentRow("바바바", lectureAttendance.getOrDefault("바바바", "결석"));
        addStudentRow("사사사", lectureAttendance.getOrDefault("사사사", "결석"));
        addStudentRow("아아아", lectureAttendance.getOrDefault("아아아", "결석"));
        addStudentRow("자자자", lectureAttendance.getOrDefault("자자자", "결석"));
        addStudentRow("차차차", lectureAttendance.getOrDefault("차차차", "결석"));
        addStudentRow("카카카", lectureAttendance.getOrDefault("카카카", "결석"));
    }

    // ===== 수강생 행 추가 =====
    private void addStudentRow(String name, String status) { // 한 명의 수강생 행 추가
        Object[] row = { // 행 데이터
                name, // 학생 이름
                status, // 출결 상태
                "출석", // 출석 버튼 텍스트
                "지각", // 지각 버튼 텍스트
                "결석"  // 결석 버튼 텍스트
        };
        studentTableModel.addRow(row); // 테이블 모델에 행 추가
    }

    // ===== 출결 상태 업데이트 메서드 =====
    public void updateAttendance(int rowIndex, String status) { // 출결 상태 변경 메서드
        if (rowIndex < 0 || rowIndex >= studentTableModel.getRowCount()) { // 인덱스 범위 체크
            return; // 유효하지 않으면 종료
        }

        // 현재 날짜 키
        String dateKey = getSelectedDateKey();

        // 학생 이름 가져오기
        String studentName = (String) studentTableModel.getValueAt(rowIndex, 0);

        // 테이블 표시 갱신
        studentTableModel.setValueAt(status, rowIndex, 1); // 1번 컬럼("출결 상태") 값 변경

        // 메인 쪽 출결 상태에도 저장 (강좌 + 날짜 + 학생 기준)
        main.updateAttendance(lectureTitle, dateKey, studentName, status);

        // TODO: 선택된 날짜(dateSpinner) 기준으로 DB 출결 기록 UPDATE 필요
        System.out.println("[출결 변경] lecture=" + lectureTitle
                + ", date=" + dateKey
                + ", student=" + studentName + ", 상태=" + status); // 콘솔 출력
    }

    // ===== 출석/지각/결석 버튼 렌더러 =====
    private static class AttendanceButtonRenderer extends JButton implements TableCellRenderer { // 렌더러 클래스
        public AttendanceButtonRenderer(String text) { // 생성자
            super(text); // 버튼 텍스트 설정
            setOpaque(true); // 불투명 설정
        }

        @Override
        public Component getTableCellRendererComponent( // 셀 렌더링 메서드
                                                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            setText(value == null ? "" : value.toString()); // 셀 값으로 텍스트 설정

            if (isSelected) { // 선택된 셀이면
                setForeground(table.getSelectionForeground()); // 선택 전경색
                setBackground(table.getSelectionBackground()); // 선택 배경색
            } else { // 선택 안 된 셀이면
                setForeground(table.getForeground()); // 기본 전경색
                setBackground(UIManager.getColor("Button.background")); // 기본 버튼 배경색
            }

            return this; // 버튼 반환
        }
    }

    // ===== 출석/지각/결석 버튼 에디터 =====
    private static class AttendanceButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener { // 에디터 클래스

        private JButton button; // 실제 버튼
        private String actionType; // "출석", "지각", "결석"
        private TeacherStudentList parent; // 부모 다이얼로그
        private int currentRow; // 현재 행 인덱스

        public AttendanceButtonEditor(JCheckBox checkBox, String actionType, TeacherStudentList parent) { // 생성자
            this.actionType = actionType; // 액션 타입 저장
            this.parent = parent; // 부모 다이얼로그 저장
            this.button = new JButton(actionType); // 버튼 생성
            this.button.setOpaque(true); // 불투명 설정
            this.button.addActionListener(this); // 액션 리스너 등록
        }

        @Override
        public Component getTableCellEditorComponent( // 셀 편집용 컴포넌트
                                                      JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; // 현재 행 인덱스 저장
            button.setText(value == null ? actionType : value.toString()); // 텍스트 설정
            return button; // 버튼 반환
        }

        @Override
        public Object getCellEditorValue() { // 편집 결과 값 반환
            return actionType; // 액션 타입 반환
        }

        @Override
        public boolean isCellEditable(EventObject e) { // 셀 편집 가능 여부
            return true; // 항상 편집 가능
        }

        @Override
        public void actionPerformed(ActionEvent e) { // 버튼 클릭 시 호출
            fireEditingStopped(); // 셀 편집 종료 이벤트 발생
            parent.updateAttendance(currentRow, actionType); // 부모 다이얼로그에 출결 상태 변경 요청
        }
    }
}
