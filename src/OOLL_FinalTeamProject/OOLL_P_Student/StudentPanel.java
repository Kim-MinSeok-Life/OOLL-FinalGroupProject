// 학생 - 메인 화면 패널 (개인정보, 내 강의, 수강신청)
package OOLL_P_Student; // 패키지 선언

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

// import 선언
import javax.swing.*; // swing GUI(Graphical User Interface) 컴포넌트
import javax.swing.table.*; // swing GUI 컴포넌트(JTable, TableModel 관련)
import java.awt.*; // GUI를 위한 컴포넌트(Layout, Color, Dimension 등 관련)
import java.awt.event.*; // 이벤트 처리
import java.sql.SQLException; // DB 예외 처리

import teamTest.Student;
//import OOLL_P_Student.StudentInfo;
//import OOLL_P_Student.StudentFrame;

// 로그인한 학생의 메인화면을 구성하는 패널(개인정보 표시, 내 강의 조회, 수강신청 화면 구성)
public class StudentPanel extends JPanel {
    // [상단] 개인정보 텍스트 필드(아이디, 이름, 이메일, 전화번호, 주소)
    private JTextField tfId, tfName, tfEmail, tfPhone, tfAddress;

    /* [중단&하단] 탭 영역
     * 탭 컴포넌트(나의 강의 목록 테이블, 전체 강의 목록 테이블),
     * 테이블 모델(나의 강의 목록, 전체 강의 목록),
     * 검색 기능(검색어 입력 필드, 정렬 기준 선택[과목명|강사명|정원])
    */
    private JTable tableMyClass, tableCourseList; // 현재 수강중인 강의(tableMyClass), 전체 강의(tableCourseList) 보여주는 JTable
    private DefaultTableModel modelMyClass, modelCourse; // tableMyClass(modelMyClass), tableCourseList(modelCourse)의 데이터 모델
    private JTextField searchField; // 수강신청 탭의 검색어 입력용 JTextField
    private JComboBox<String> sortBox; // 수강신청 탭의 정렬 옵션 선태용 JComboBox

    // 로그인한 회원 정보 저장 변수(로그인한 학생 식별 정보)
    private final String currentMemberId; // 로그인한 회원 아이디
    private int currentStudentNo = -1; // DB 조회 전 임시 초기값(기본값)

    // StudentService 객체(DB에서 학생 정보, 강의 목록, 출결 데이터를 가져오는 서비스 클래스)
    private StudentService service = new StudentService(); // DB 관련 처리

    // 생성자(로그인한 회원 아이디 받아와서 화면 구성)
    public StudentPanel(String memberId) {
        this.currentMemberId = memberId; // 로그인한 회원 아이디 저장
        setLayout(new BorderLayout()); // 페널 기본 레이아웃 지정
        setBackground(Color.WHITE); // 배경색 지정

        // center 영역 구성(개인정보, 탭[내 강의, 수강신청]부분)
        JPanel center = new JPanel();
        center.setLayout(new FlowLayout(FlowLayout.LEFT, 60, 20)); // center 영역 레이아웃 지정(여백)
        add(center, BorderLayout.CENTER); // 레이아웃 지정

        // 개인정보 영역 구성(학생아이디, 이름, 이메일, 연락처, 주소 정보와 정보수정 버튼 표시)
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // 5행 2열, 간격 10 레이아웃 지정
        infoPanel.setBorder(BorderFactory.createTitledBorder("개인정보")); // 테두리 제목 지정
        infoPanel.setPreferredSize(new Dimension(1100, 180)); // 패널 크기 지정
        infoPanel.setBackground(Color.WHITE); // 배경색 지정

        // 개인정보 입력창 생성(수정 불가)
        tfId = new JTextField(15); // 아이디 표시
        tfName = new JTextField(15); // 이름 표시
        tfEmail = new JTextField(15); // 이메일 표시
        tfPhone = new JTextField(15); // 전화번호 표시
        tfAddress = new JTextField(30); // 주소 표시

        // 화면상에서 바로 수정 못하도록 설정(정보 수정 버튼을 통해 수정 가능)
        tfId.setEditable(false);
        tfName.setEditable(false);
        tfEmail.setEditable(false);
        tfPhone.setEditable(false);
        tfAddress.setEditable(false);

        // 정보수정 버튼(StudentEditDialog 호출)
        JButton editBtn = new JButton("정보수정"); // 버튼
        editBtn.addActionListener(e -> { // 버튼 클릭 시 이벤트 처리
            // 새로 분리된 dialog 호출
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this); // 현재 패널이 포함된 최상위 윈도우(Frame)를 부모로 지정
            StudentEditDialog dlg = new StudentEditDialog(frame, service, currentMemberId); // 생성자에 Frame, service 객체, memberId 전달
            dlg.setVisible(true); // Modal 다이얼로그 표시
            if (dlg.isSaved()) reloadAllData(); // 저장 후 화면 갱신
        });

        // 주소 필드, 정보 수정 버튼 같은 줄 배치
        JPanel addressPanel = new JPanel(new BorderLayout(5, 0));
        addressPanel.add(tfAddress, BorderLayout.CENTER); // 주소 필드 배치
        JPanel addressBtnPanel = new JPanel(new GridLayout(1, 1, 5, 0)); // 주소 레이아웃 지정
        addressBtnPanel.add(editBtn); // 버튼 배치
        addressPanel.add(addressBtnPanel, BorderLayout.EAST); // 버튼 레이아웃 지정

        // 개인정보 라벨, 입력창 배치
        infoPanel.add(new JLabel("학생아이디"));
        infoPanel.add(tfId);
        infoPanel.add(new JLabel("이름"));
        infoPanel.add(tfName);
        infoPanel.add(new JLabel("이메일"));
        infoPanel.add(tfEmail);
        infoPanel.add(new JLabel("연락처"));
        infoPanel.add(tfPhone);
        infoPanel.add(new JLabel("주소"));
        infoPanel.add(addressPanel); // 정보 수정 버튼과 함께 배치

        // 탭 패널(내 강의/수강신청) 생성
        JTabbedPane tabPane = new JTabbedPane(); // JTabbedPane: 탭 형태로 구성하여 하나의 창에 표시할 수 있게 해주는 컴포넌트
        tabPane.setPreferredSize(new Dimension(1100, 500)); // 탭 전체 크기 지정

        // 내 강의 탭(학생이 현재 수강중인 강의)
        JPanel myClassPanel = new JPanel(new BorderLayout());
        String[] myClassHeader = {"강의번호", "과목명", "담당강사", "요일", "시간", "강의실", "현재 정원"}; // 학생이 필요한 정보만 표시(강의번호 숨기기)
        modelMyClass = new DefaultTableModel(myClassHeader, 0) {
            public boolean isCellEditable(int r, int c) { return false; } // 수정 불가
        };
        tableMyClass = new JTable(modelMyClass);
        
        // 강의번호 컬럼 숨기기(0번째인 강의번호의 셀의 너비와 간격, 높이, 글자 보이지 않도록 정렬)
        tableMyClass.getColumnModel().getColumn(0).setMinWidth(0);
        tableMyClass.getColumnModel().getColumn(0).setMaxWidth(0);
        tableMyClass.getColumnModel().getColumn(0).setWidth(0);
        tableMyClass.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        myClassPanel.add(new JScrollPane(tableMyClass), BorderLayout.CENTER); // 화면보다 크기가 큰 컴포넌트일 시 스크롤을 통해 표시

        // 내 강의 목록 클릭 시 해당 수강생의 출결 팝업 열기
        tableMyClass.addMouseListener(new MouseAdapter() { // 마우스 클릭시 이벤트 리스너 등록
            public void mouseClicked(MouseEvent e) { // 마우스 클릭 시 이벤트 처리
            	if(e.getClickCount() == 2) { // 더블 클릭 확인
            		int r = tableMyClass.getSelectedRow(); // 클릭한 행(r) 인덱스 가져오기
                    if (r == -1) return; // 아무 행 선택되지 않을 때
                    int lectureNo = Integer.parseInt(tableMyClass.getValueAt(r, 0).toString()); // 숨겨둔 0번째 강의 번호 가져오기
                    String lectureName = tableMyClass.getValueAt(r, 1).toString(); // 1번째 강의명 가져오기
                    openAttendanceDialog(lectureNo, lectureName); // 선택한 강의 번호와 이름을 전달하여 출결/수강생 조회 다이얼로그 열기
            	}
            }
        });
        
        // 강의 삭제
        JButton deleteBtn = new JButton("강의 삭제"); // 버튼
        deleteBtn.addActionListener(e -> { // 버튼 클릭 시 이벤트 처리
            int r = tableMyClass.getSelectedRow(); // 클릭한 행(r) 인덱스 가져오기
            if(r == -1) return; // 아무 행 선택되지 않을 때
            String lectureName = tableMyClass.getValueAt(r, 1).toString(); 
            int lectureNo = Integer.parseInt(tableMyClass.getValueAt(r, 0).toString());
            // 사용자에게 삭제 확인 받기위한 다이얼로그 열기
            int result = JOptionPane.showConfirmDialog(this, lectureName + " 강의를 정말로 삭제하시겠습니까?", "강의 삭제", JOptionPane.YES_NO_OPTION);
            if(result == JOptionPane.YES_OPTION){
                try { // 삭제 성공 시
                	service.deleteLecture(currentStudentNo, lectureNo);
                    reloadAllData(); // 삭제 후 화면 갱신
                } catch(SQLException ex){
                	// DB 쿼리 수행 중 에러 발생 시
                    JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage());
                }
            }
        });
        JPanel southMyClassPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southMyClassPanel.add(deleteBtn);
        myClassPanel.add(southMyClassPanel, BorderLayout.SOUTH);
        
        tabPane.addTab("내 강의", myClassPanel);

        // 수강신청 탭(전체 강의 목록, 검색/정렬)
        JPanel coursePanel = new JPanel(new BorderLayout());
        
        // [상단(수강신청 탭 기준)]검색, 정렬 패널
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 레이아웃 지정
        searchPanel.setBackground(Color.WHITE); // 배경색
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("검색"); // 검색 버튼
        String[] sortOpt = {"과목명순", "강사명순", "신청가능인원순"};
        sortBox = new JComboBox<>(sortOpt);
        
        // 검색&정렬 배치
        searchPanel.add(new JLabel("검색: "));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(new JLabel("정렬: "));
        searchPanel.add(sortBox);

        // 전체 강의 목록 테이블
        String[] courseHeader = {"과목명", "현재인원", "담당강사", "강의번호", "요일", "시간", "강의실", "정원"};
        modelCourse = new DefaultTableModel(courseHeader, 0) {
            public boolean isCellEditable(int r, int c) { return false; } // 수정 불가
        };
        tableCourseList = new JTable(modelCourse);
        
        // 검색/목록 배치
        coursePanel.add(searchPanel, BorderLayout.NORTH);
        coursePanel.add(new JScrollPane(tableCourseList), BorderLayout.CENTER);

        // 탭에 패널 추가
        tabPane.addTab("내 강의", myClassPanel);
        tabPane.addTab("수강신청", coursePanel);

        // center 영역에 개인정보, 탭 추가
        center.add(infoPanel);
        center.add(tabPane);

        // 검색&정렬 이벤트
        searchBtn.addActionListener(e -> reloadCourseList()); // 검색 버튼 클릭 시 목록 갱신
        sortBox.addActionListener(e -> reloadCourseList()); // 정렬 옵션 변경 시 목록 갱신

        // 수강신청 버튼
        JButton enrollBtn = new JButton("수강신청");
        enrollBtn.addActionListener(e -> { // 버튼 클릭 시 이벤트 리스너 등록
            int r = tableCourseList.getSelectedRow(); // 강의 테이블에서 선택된 행(r) 인덱스 가져오기
            if (r == -1) { // 선택된 행이 없을 시 안내 메세지
                JOptionPane.showMessageDialog(this, "수강할 강의를 선택하세요.");
                return;
            }
            int lectureNo = Integer.parseInt(tableCourseList.getValueAt(r, 3).toString()); // 선태한 행의 강의 번호(인덱스 3) 가져오기
            String res = service.attemptEnroll(currentStudentNo, lectureNo); // StudentService에서 수강신청 처리
            if ("성공".equals(res)) { // 수강신청 성공 시
                JOptionPane.showMessageDialog(this, "수강신청이 완료되었습니다.");
            } else {
            	// 수강신청 실패 시
                JOptionPane.showMessageDialog(this, res);
            }
            reloadAllData(); // 수강신청 완료 후 내 강의 및 수강신청 탭 화면 갱신
        });
        JPanel southEnrollPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 수강신청 버튼을 우측 정렬 패널에 추가
        southEnrollPanel.add(enrollBtn);
        coursePanel.add(southEnrollPanel, BorderLayout.SOUTH);

        // 초기 초기 데이터 로드(SwingUtilities(유틸리티 메서드) 사용하여 데이터 요청
        SwingUtilities.invokeLater(() -> reloadAllData());
    }

    /* 전체 데이터 다시 로드
     * StudentService -> 로그인된 학생의 개인정보, 강의 목록 화면 반영
     * currentMemberId로 StudentInfo 조회(조회 성공 시 tf* 필드에 값 채움)
     * 내 강의 목록 갱신
     * 전체 강의 목록 갱신
    */
    private void reloadAllData() {
        try { // 로그인한 개인정보 조회 성공 시
            StudentInfo info = service.loadStudentInfo(currentMemberId); // 로그인한 회원 전체 개인정보 데이터 조회
            if (info != null) {
            	// 조회된 정보를 반영
                tfId.setText(info.memberId);
                tfName.setText(info.name);
                tfEmail.setText(info.email);
                tfPhone.setText(info.phone);
                tfAddress.setText(info.address);
                currentStudentNo = info.studentNo; // 수강신청에서 사용됨
            } else {
            	// 로그인한 개인정보 조회 실패 시
                JOptionPane.showMessageDialog(this, "회원정보가 없습니다: " + currentMemberId);
            }
            service.loadMyClass(modelMyClass, currentStudentNo); // 내 강의 목록 불러오기
            reloadCourseList(); // 전체 강의 목록 불러오기
        } catch (SQLException ex) {
        	// DB 쿼리 수행 중 에러 발생 시
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터 로드 오류: " + ex.getMessage());
        }
    }

    // 수강신청 탭(검색&정렬 반영 후 목록 반영) : 전체 강의 목록 갱신
    private void reloadCourseList() {
        try { // 목록 갱신 성공 시
            service.loadCourseList(modelCourse, searchField.getText().trim(), (String) sortBox.getSelectedItem(), currentStudentNo);
        } catch (SQLException ex) {
        	// DB 쿼리 수행 중 에러 발생 시
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "강의목록 로드 오류: " + ex.getMessage());
        }
    }

    // 출결&수강생 조회 다이얼로그
    private void openAttendanceDialog(int lectureNo, String lectureName) {
    	// 모달 다이얼로그 생성
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "수강생 / 출결 - " + lectureName + " (" + lectureNo + ")", true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        String[] cols = {"학생번호", "학생아이디", "학생이름", "출결(오늘)"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { // 테이블 모델 생성
            public boolean isCellEditable(int r, int c) { return false; } // 수정 불가
        };
        JTable t = new JTable(m);
        styleTableCenter(t); // 가운데 정렬

        try { // 출결 조회 성공 시
        	// StudentService에서 출결 데이터 불러옴
            service.loadAttendanceForLecture(m, lectureNo);
        } catch (SQLException ex) {
        	// DB 쿼리 수행 중 에러 발생 시
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "출결 조회 오류: " + ex.getMessage());
        }

        dlg.add(new JScrollPane(t), BorderLayout.CENTER);

        // 하단 버튼
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("닫기"); // 닫기 버튼
        JButton refreshBtn = new JButton("갱신"); // 갱신 버튼
        closeBtn.addActionListener(e -> dlg.dispose()); // 닫기 버튼 실행 이벤트(다이얼로그 종료)
        refreshBtn.addActionListener(e -> { // 버튼 클릭 시 이벤트 처리
            dlg.dispose(); // 해당 프레임 종료(나머지 프레임 살아있음)
            openAttendanceDialog(lectureNo, lectureName); // 갱신(새로 열기)
        });
        bottom.add(refreshBtn);
        bottom.add(closeBtn);
        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setModal(true);
        dlg.setVisible(true);
    }

    // 테이블(JTable) 가운데 정렬 스타일 설정
    private void styleTableCenter(JTable tbl) {
        tbl.setRowHeight(26); // 테이블 높이 지정
        // 테이블 헤더의 기본 렌더러 가져와서 텍스트 가운데 정렬
        ((DefaultTableCellRenderer)tbl.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); // JTable로 각 셀을 표현하기 위한 표준 클래스
        center.setHorizontalAlignment(JLabel.CENTER);
        // 모든 컬럼에 렌더러 적용()
        for (int i = 0; i < tbl.getColumnCount(); i++) tbl.getColumnModel().getColumn(i).setCellRenderer(center);
    }
    
    // main
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(() -> { // 모든 swing 관련 코드가 EDT(Event Dispatch Thread)에서 안전하게 실행되도록 사용
    		// 학생 페이지 실행 
    		StudentFrame win = new StudentFrame("testUser");
            win.setTitle("학생 페이지");
            win.setSize(1200, 800);
            win.setLocation(300, 100);
            win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 애플리케이션 완전히 종료
            win.setVisible(true); // 생성된 GUI 컴포넌트 출력
        });
    }
}
