// TeacherMain.java
package OOLL_P_Teacher; // 패키지 선언

import OOLL_P_Student.*; // 학생 패키지 import (필요시)
import OOLL_P_Teacher.*; // 동일 패키지 내 다른 클래스 import (필요시)
import OOLL_P_Login.*;   // 로그인 패키지 import (Login 클래스 사용)
import OOLL_P_Manager.*; // 원장 패키지 import (필요시)

import java.awt.*;           // 레이아웃, Color 등 AWT 관련 클래스 import
import java.awt.event.*;     // ActionListener, MouseListener 등 이벤트 관련 클래스 import
import java.sql.*;           // JDBC 사용할 때 필요한 java.sql 패키지 import
import javax.swing.*;        // 스윙 컴포넌트 사용
import javax.swing.table.DefaultTableModel; // JTable용 테이블 모델 사용
import java.util.*;          // List, ArrayList 등 사용

import javax.swing.table.TableCellRenderer; // 셀 렌더러 인터페이스 사용

public class TeacherMain extends JFrame implements ActionListener { // JFrame 상속 + 버튼 액션 처리 위해 ActionListener 구현

    // ======== 선언부 ========
    // ==== 데이터 값 =====
    private String userId;              // 아이디 [데이터값]
    private String password;           // 비밀번호 [데이터값]
    private String name;               // 이름 [데이터값]
    private String addressData;        // 주소 [데이터값]
    private String phone;              // 전화번호 [데이터값]
    private String email;              // 이메일 [데이터값]
    private String security_question;  // 본인확인 질문 힌트 [데이터값]
    private String security_answer;    // 본인확인 질문 답 [데이터값]
    private String role;               // 역할,직책(원장/강사/학생)
    private int teacherNo;             // 강사번호 [DB]

    // 강의번호를 행 인덱스별로 기억하는 리스트
    private java.util.List<Integer> lectureNoList = new ArrayList<>(); // JTable 행 → lecture_no 매핑을 위한 리스트

    // ===== JPanel ======
    // 1-1. 최상단&상단 (로고 및 개인정보)
    private JPanel topPanel; // 로고 및 개인정보 [최상단&상단] 전체 패널

    // 1-2. 중단&하단 (담당강의)
    private JPanel bottomPanel;   // 담당강의 [중단&하단] 전체 패널
    private JPanel lecturePanel;  // 중단&하단 전체 패널 카드(흰바탕, "담당강의" 카드)

    // 2-1. 최상단 (로고, 학원이름, 사용자 이름, 로그아웃)
    private JPanel headerPanel;       // 최상단 전체 패널 카드(흰바탕)
    private JPanel headerLeftPanel;   // 최상단 좌측 패널(로고, 학원이름)
    private JPanel headerRightPanel;  // 최상단 우측 패널(사용자 이름, 로그아웃 버튼)

    // 2-2. 상단 (개인정보 내역, 개인정보 수정)
    private JPanel profilePanel;      // 상단 전체 패널 카드(흰바탕)
    private JPanel profileLeftPanel;  // 상단 좌측 패널(개인정보 내역)
    private JPanel profileRightPanel; // 상단 우측 패널(개인정보 수정 버튼)

    // 3-1. 중단 (검색창, 검색버튼, 강의개설)
    private JPanel searchPanel;       // 중단 전체 패널
    private JPanel searchLeftPanel;   // 중단 좌측 패널(검색창, 검색 버튼)
    private JPanel searchRightPanel;  // 중단 우측 패널(강의 개설 버튼)

    // 3-2. 하단 (강좌목록, 강좌수정 및 삭제, 강좌별 출결관리)
    private JPanel tablePanel;        // 하단 전체 패널 카드 (필드만 유지, 실제로는 사용 안 함)

    // ===== [최상단 : 좌측 - 로고, 학원이름 / 우측 - 사용자 이름, 로그아웃 버튼] =====
    // 로고 [최상단 좌측]
    private ImageIcon logoIcon;        // 로고의 원본 이미지 아이콘
    private Image scaledLogoImage;     // 80x50 크기로 조정된 이미지
    private ImageIcon scaledLogoIcon;  // 조정된 이미지를 다시 아이콘으로
    private JLabel logo;               // 학원 로고 이미지 레이블

    private JLabel AcademyName;        // 학원 이름 [최상단 좌측]
    private JLabel HeaderUserName;     // 사용자 이름 [최상단 우측]
    private JButton logoutBtn;         // 로그아웃 버튼 [최상단 우측]

    // ===== [상단 : 개인정보 및 개인정보 수정 버튼] ======
    // 사용자 사진 [상단 좌측]
    private ImageIcon userIcon;        // 사용자의 원본 이미지 아이콘
    private Image scaleduserImage;     // 조정된 이미지
    private ImageIcon scaleduserIcon;  // 조정된 이미지를 다시 아이콘으로
    private JLabel userImage;          // 사용자 이미지 레이블

    // 이름/직업/연락처/이메일/주소 레이블 [상단 좌측]
    private JLabel userName;       // 이름 레이블
    private JLabel careerName;     // 직업(모드) 레이블
    private JLabel phoneNumber;    // 연락처 레이블
    private JLabel eMail;          // 이메일 레이블
    private JLabel address;        // 주소 레이블

    // 개인정보 수정 버튼 [상단 우측]
    private JButton profileEditBtn; // 개인정보 수정 버튼

    // ===== [중앙 : 담당강의 목록 및 신규 강좌 개설 버튼] =====
    // 검색 설명 레이블 [중단 위쪽 좌측]
    private JLabel searchLabel;       // "강의 검색:" 레이블
    // 검색창 [중단 위쪽 좌측]
    private JTextField searchField;   // 검색어 입력 필드
    // 검색 버튼 [중단 위쪽 좌측]
    private JButton searchBtn;        // 검색 버튼

    // 신규 강좌 개설 버튼 [중단 위쪽 우측]
    private JButton addLectureBtn;    // 신규 강좌 개설 버튼

    // ===== 강좌 테이블용 필드 =====
    private JTable lectureTable;             // 하단 강좌 목록 테이블
    private DefaultTableModel lectureModel;  // 테이블 모델

    // ===== 생성자 =====
    public TeacherMain(String id) {                        // 생성자, 로그인 아이디를 매개변수로 받음
        this.userId = id;                                  // 아이디 필드에 저장
        loadTeacherInfo(id);                               // DB에서 강사 정보 불러오기

        setTitle("강사 메인화면");                         // 창 제목 설정
        setSize(1200, 800);                               // 창 크기 설정
        setLocationRelativeTo(null);                      // 화면 중앙에 위치
        setVisible(true);                                 // 프레임 보이기
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // 종료 시 프로세스 종료

        Container ct = getContentPane();                  // 컨텐트팬 얻기
        ct.setBackground(new Color(0xF5, 0xF5, 0xF5));    // 전체 배경색 연한 회색
        ct.setLayout(new BorderLayout());                 // BorderLayout 사용

        // ==== 1-1. 최상단&상단 (로고 및 개인정보) ====
        topPanel = new JPanel();                          // 최상단&상단 전체를 감싸는 패널
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // 위→아래로 header, profile 두 개 쌓기
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 바깥 여백
        topPanel.setBackground(new Color(0xF5, 0xF5, 0xF5)); // 배경색 설정

        // ==== 1-2. 중단&하단 (담당강의) ====
        bottomPanel = new JPanel();                       // 담당강의 영역 전체 패널
        bottomPanel.setLayout(new BorderLayout());        // 내부에 lecturePanel 하나 채울 예정
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 바깥 여백
        bottomPanel.setBackground(new Color(0xF5, 0xF5, 0xF5)); // 배경 회색

        lecturePanel = new JPanel(new BorderLayout());    // "담당강의" 카드 패널
        lecturePanel.setBackground(Color.WHITE);          // 카드 배경 흰색
        lecturePanel.setBorder(                          // 테두리 + 안쪽 여백
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("담당강의"),      // 타이틀
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)  // 안쪽 여백
                )
        );

        // ==== 2-1. 최상단 (로고, 학원이름, 사용자 이름, 로그아웃) ====
        headerPanel = new JPanel(new BorderLayout());     // 최상단 카드 전체 패널
        headerPanel.setBackground(Color.WHITE);           // 흰색 배경
        headerPanel.setBorder(                            // 테두리 + 안쪽 여백
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)), // 옅은 회색 테두리
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)              // 안쪽 여백
                )
        );

        headerLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));  // 좌측(로고/학원이름)
        headerLeftPanel.setOpaque(false);                               // 배경은 부모 패널 따라감

        headerRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 우측(이름/로그아웃)
        headerRightPanel.setOpaque(false);                               // 배경은 부모 패널 따라감

        // --- 로고 이미지 설정 ---
        logoIcon = new ImageIcon("C:/My2025_after/My2025_OOLL_FinalProject/mj_logo.jpg"); // 로고 원본 이미지 아이콘
        scaledLogoImage = logoIcon.getImage().getScaledInstance(80, 50, Image.SCALE_SMOOTH); // 80x50으로 스케일 조정
        scaledLogoIcon = new ImageIcon(scaledLogoImage);  // 스케일된 이미지를 새 아이콘으로
        logo = new JLabel(scaledLogoIcon);                // 아이콘을 가진 JLabel 생성
        logo.setPreferredSize(new Dimension(80, 50));     // 고정 크기 부여
        logo.setHorizontalAlignment(SwingConstants.CENTER); // 수평 중앙 정렬
        logo.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // 로고 주변 테두리

        // --- 학원 이름 라벨 ---
        AcademyName = new JLabel("明知 LMS");             // 학원 이름 텍스트
        AcademyName.setFont(AcademyName.getFont().deriveFont(Font.BOLD, 20)); // 굵게 20pt

        // --- 사용자 이름 / 로그아웃 버튼 ---
        HeaderUserName = new JLabel(name + " 님");        // 상단 우측 사용자 이름
        logoutBtn = new JButton("로그아웃");              // 로그아웃 버튼

        // --- 최상단 좌우 패널에 컴포넌트 배치 ---
        headerLeftPanel.add(logo);                        // 좌측 패널에 로고 추가
        headerLeftPanel.add(Box.createHorizontalStrut(10)); // 로고-학원명 사이 간격
        headerLeftPanel.add(AcademyName);                 // 좌측 패널에 학원명 추가

        headerRightPanel.add(HeaderUserName);             // 우측 패널에 사용자 이름 추가
        headerRightPanel.add(Box.createHorizontalStrut(10)); // 이름-버튼 사이 간격
        headerRightPanel.add(logoutBtn);                  // 우측 패널에 로그아웃 버튼 추가

        headerPanel.add(headerLeftPanel, BorderLayout.WEST);  // header 좌측 영역에 부착
        headerPanel.add(headerRightPanel, BorderLayout.EAST); // header 우측 영역에 부착

        // ==== 2-2. 상단 (개인정보 내역, 개인정보 수정) ====
        profilePanel = new JPanel(new BorderLayout());    // 개인정보 카드 전체 패널
        profilePanel.setBackground(Color.WHITE);          // 카드 배경 흰색
        profilePanel.setBorder(                          // 테두리 + 안쪽 여백
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("개인정보"),        // 타이틀
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)    // 안쪽 여백
                )
        );

        profileLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 좌측(사진+텍스트)
        profileLeftPanel.setOpaque(false);                               // 배경은 부모 패널 따라감

        profileRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 우측(개인정보 수정 버튼)
        profileRightPanel.setOpaque(false);                               // 배경은 부모 패널 따라감

        // --- 프로필 이미지 ---
        userIcon = new ImageIcon("C:/My2025_after/My2025_OOLL_FinalProject/mj_logo.jpg"); // 임시로 같은 이미지 사용
        scaleduserImage = userIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH); // 80x80 크기로 조정
        scaleduserIcon = new ImageIcon(scaleduserImage);  // 조정된 이미지로 새 아이콘
        userImage = new JLabel(scaleduserIcon);           // 사용자 이미지 레이블
        userImage.setPreferredSize(new Dimension(80, 80)); // 고정 크기 지정

        // --- 프로필 텍스트들 ---
        userName = new JLabel(name);                      // 이름
        userName.setFont(userName.getFont().deriveFont(Font.BOLD, 18)); // 굵게 18pt
        careerName = new JLabel("강사");                  // 직업(모드)
        phoneNumber = new JLabel("전화번호: " + phone);   // 연락처
        eMail = new JLabel("이메일: " + email);           // 이메일
        address = new JLabel("주소: " + addressData);     // 주소

        // --- 프로필 텍스트를 세로로 쌓을 패널 ---
        JPanel profileTextPanel = new JPanel();           // 이름/직업/연락처/이메일/주소를 세로로 쌓을 패널
        profileTextPanel.setLayout(new BoxLayout(profileTextPanel, BoxLayout.Y_AXIS)); // 세로 방향 배치
        profileTextPanel.setOpaque(false);                // 배경은 부모 패널 따라감

        profileTextPanel.add(userName);                   // 이름
        profileTextPanel.add(careerName);                 // 직업
        profileTextPanel.add(Box.createVerticalStrut(5)); // 간격
        profileTextPanel.add(phoneNumber);                // 연락처
        profileTextPanel.add(eMail);                      // 이메일
        profileTextPanel.add(address);                    // 주소

        profileLeftPanel.add(userImage);                  // 좌측에 이미지
        profileLeftPanel.add(Box.createHorizontalStrut(15)); // 이미지-텍스트 사이 간격
        profileLeftPanel.add(profileTextPanel);           // 텍스트 묶음

        profileEditBtn = new JButton("개인정보 수정");    // 개인정보 수정 버튼
        profileRightPanel.add(profileEditBtn);            // 우측 패널에 버튼 추가

        profilePanel.add(profileLeftPanel, BorderLayout.WEST); // 좌측에 프로필 내용
        profilePanel.add(profileRightPanel, BorderLayout.EAST); // 우측에 버튼

        // ==== 3-1. 중단 (검색창, 검색버튼, 강의개설) ====
        searchPanel = new JPanel(new BorderLayout());     // 검색/신규강좌 전체 패널
        searchPanel.setOpaque(false);                     // lecturePanel 흰 배경 보이게

        searchLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));  // 좌측(검색)
        searchLeftPanel.setOpaque(false);

        searchRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 우측(신규 강의)
        searchRightPanel.setOpaque(false);

        searchLabel = new JLabel("강의 검색:");           // 검색 라벨
        searchField = new JTextField(25);                // 검색어 입력 필드
        searchBtn = new JButton("검색");                  // 검색 버튼

        searchLeftPanel.add(searchLabel);                // 좌측 패널에 라벨 추가
        searchLeftPanel.add(searchField);                // 좌측 패널에 텍스트필드 추가
        searchLeftPanel.add(searchBtn);                  // 좌측 패널에 버튼 추가

        addLectureBtn = new JButton("신규 강의 개설");    // 신규 강의 개설 버튼
        searchRightPanel.add(addLectureBtn);             // 우측 패널에 버튼 추가

        searchPanel.add(searchLeftPanel, BorderLayout.WEST); // 검색영역은 WEST
        searchPanel.add(searchRightPanel, BorderLayout.EAST); // 신규 강의 버튼은 EAST

        // ==== 3-2. 하단 (강좌목록 테이블) ====
        String[] columnNames = {                         // 컬럼 이름 정의
                "강좌명", "시간", "요일", "강의실", "정원", "수강인원", "수정", "삭제"
        };

        lectureModel = new DefaultTableModel(columnNames, 0) { // 테이블 모델 생성
            @Override
            public boolean isCellEditable(int row, int column) { // 셀 편집 가능 여부
                return false;                                    // 모든 셀 직접 편집 불가
            }
        };

        lectureTable = new JTable(lectureModel);          // JTable 생성
        lectureTable.setRowHeight(32);                    // 행 높이 설정

        lectureTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonLookRenderer()); // 6열 "수정" 버튼처럼 렌더링
        lectureTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonLookRenderer()); // 7열 "삭제" 버튼처럼 렌더링

        lectureTable.addMouseListener(new LectureTableClickListener()); // 테이블 클릭 리스너 등록

        loadLectureList();                                // ★ DB에서 강의 목록 로딩

        JScrollPane tableScrollPane = new JScrollPane(lectureTable); // 테이블을 스크롤로 감싸기

        // ==== topPanel / bottomPanel에 카드들 붙이기 ====
        topPanel.add(headerPanel);                       // 최상단 카드
        topPanel.add(Box.createVerticalStrut(10));       // 간격
        topPanel.add(profilePanel);                      // 개인정보 카드

        lecturePanel.add(searchPanel, BorderLayout.NORTH);   // 담당강의 카드 상단: 검색 영역
        lecturePanel.add(tableScrollPane, BorderLayout.CENTER); // 담당강의 카드 중앙: 테이블

        bottomPanel.add(lecturePanel, BorderLayout.CENTER);  // 담당강의 카드 하나 채우기

        ct.add(topPanel, BorderLayout.NORTH);               // 프레임 NORTH에 상단 두 카드
        ct.add(bottomPanel, BorderLayout.CENTER);           // 프레임 CENTER에 담당강의 카드

        // ==== 이벤트 연결 ====
        logoutBtn.addActionListener(this);                  // 로그아웃 버튼 액션 연결
        profileEditBtn.addActionListener(this);             // 개인정보 수정 버튼 액션 연결
        addLectureBtn.addActionListener(this);              // 신규 강의 개설 버튼 액션 연결
        searchBtn.addActionListener(this);                  // 검색 버튼 액션 연결

    } // 생성자 끝

    // ====== JTable 셀을 버튼처럼 보이게 하는 렌더러 ======
    class ButtonLookRenderer extends JButton implements TableCellRenderer { // JButton 상속 + TableCellRenderer 구현

        public ButtonLookRenderer() {                      // 렌더러용 생성자
            super();                                      // JButton 기본 생성자 호출
            setOpaque(true);                              // 배경색 보이도록
            setFocusPainted(false);                       // 포커스 점선 제거
            setBorder(BorderFactory.createLineBorder(Color.GRAY)); // 회색 테두리
        }

        @Override
        public Component getTableCellRendererComponent(   // 셀 그릴 때마다 호출
                                                          JTable table,
                                                          Object value,
                                                          boolean isSelected,
                                                          boolean hasFocus,
                                                          int row,
                                                          int column) {

            setText(value == null ? "" : value.toString()); // "수정"/"삭제" 텍스트 넣기

            if (isSelected) { // 선택된 셀이면
                setBackground(table.getSelectionBackground()); // 선택 배경색
                setForeground(table.getSelectionForeground()); // 선택 글자색
            } else {
                setBackground(UIManager.getColor("Button.background")); // 기본 버튼 배경색
                setForeground(table.getForeground());                   // 기본 글자색
            }
            return this;                                  // 버튼 컴포넌트 반환
        }
    }

    // ====== 강좌 테이블 클릭 리스너 ======
    class LectureTableClickListener extends MouseAdapter { // MouseAdapter 상속

        @Override
        public void mouseClicked(MouseEvent e) {          // 마우스 클릭 시 호출

            int row = lectureTable.getSelectedRow();      // 클릭한 행
            int col = lectureTable.getSelectedColumn();   // 클릭한 열

            if (row < 0) return;                          // 행이 선택 안됐으면 리턴

            // 0~5 열 → 수강생 목록 띄우기
            if (col >= 0 && col <= 5) {
                if (row >= 0 && row < lectureNoList.size()) { // 인덱스 범위 확인
                    int lectureNo = lectureNoList.get(row);    // 해당 행의 lecture_no 가져오기

                    TeacherStudentList tsl =                  // 수강생 목록 다이얼로그 생성
                            new TeacherStudentList(TeacherMain.this, "수강생 목록", lectureNo);
                    tsl.setSize(800, 600);                    // 크기 지정
                    tsl.setLocationRelativeTo(TeacherMain.this); // 현재 창 기준 중앙
                    tsl.setVisible(true);                     // 표시
                }
            }
            // 6번 열 → 수정 창 열기 (DB 연동은 TeacherLectureEdit 쪽에서 구현)
            else if (col == 6) {
                TeacherLectureEdit tle =                      // 기존 강의 수정 다이얼로그
                        new TeacherLectureEdit(TeacherMain.this, "기존 강의 수정");
                tle.setSize(500, 400);                       // 크기
                tle.setLocationRelativeTo(TeacherMain.this);  // 중앙
                tle.setVisible(true);                        // 표시
            }
            // 7번 열 → 삭제 (UI뿐 아니라 DB까지 삭제)
            else if (col == 7) {

                if (row >= 0 && row < lectureNoList.size()) { // 유효한 행인지 확인
                    int lectureNo = lectureNoList.get(row);    // 삭제 대상 lecture_no

                    int result = JOptionPane.showConfirmDialog( // 삭제 확인 다이얼로그
                            TeacherMain.this,
                            "정말 삭제하시겠습니까?\n(해당 강의 및 연관된 수강/출결 정보가 영향을 받을 수 있습니다.)",
                            "삭제 확인",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (result == JOptionPane.YES_OPTION) {    // YES 선택 시
                        boolean success = deleteLectureFromDB(lectureNo); // DB에서 삭제 시도

                        if (success) {                         // DB 삭제 성공 시
                            ((DefaultTableModel) lectureTable.getModel()).removeRow(row); // 테이블 행 제거
                            lectureNoList.remove(row);         // lectureNoList에서도 제거
                            JOptionPane.showMessageDialog(     // 사용자에게 안내
                                    TeacherMain.this,
                                    "강의가 삭제되었습니다.",
                                    "삭제 완료",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {                               // 실패 시
                            JOptionPane.showMessageDialog(
                                    TeacherMain.this,
                                    "강의 삭제 중 오류가 발생했습니다.\n" +
                                            "이미 수강중인 학생/출결 데이터 등의 FK 제약을 확인하세요.",
                                    "삭제 실패",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
            }
        }
    }

    // ====== 강의 삭제를 실제 DB에 반영하는 메소드 ======
    private boolean deleteLectureFromDB(int lectureNo) {  // lecture_no 기준으로 강의 삭제
        Connection con = null;                            // DB 연결 객체
        PreparedStatement ps = null;                      // PreparedStatement

        try {
            con = ConnectDB.getConnection();              // DB 연결

            String sql =
                    "DELETE FROM lecture " +
                            "WHERE lecture_no = ?";       // 강의 테이블에서 lecture_no로 삭제

            ps = con.prepareStatement(sql);               // 쿼리 준비
            ps.setInt(1, lectureNo);                      // 1번 파라미터에 lectureNo 바인딩

            int affected = ps.executeUpdate();            // 실행 후 영향받은 행 수 반환
            System.out.println("[강의 삭제] lecture_no=" + lectureNo + ", affected=" + affected); // 로그 출력

            return affected > 0;                          // 1개 이상 삭제되면 true
        } catch (SQLException e) {
            e.printStackTrace();                          // 오류 스택 출력
            return false;                                 // 실패로 처리
        } finally {
            ConnectDB.close(new AutoCloseable[]{ps, con}); // 자원 정리
        }
    }

    // ====== DB에서 강사 정보 한 번에 읽어오는 메소드 ======
    private void loadTeacherInfo(String id) {             // 강사 정보 로딩
        Connection con = null;                            // DB 연결 객체
        PreparedStatement ps = null;                      // SQL 실행 객체
        ResultSet rs = null;                              // 결과 집합

        try {
            con = ConnectDB.getConnection();              // DB 연결

            String sql =
                    "SELECT m.password, m.name, m.address, m.phone, m.email, " +
                            "       m.security_question, m.security_answer, " +
                            "       t.teacher_no, t.hourly_rate " +
                            "FROM member m " +
                            "JOIN teacher t ON m.member_id = t.member_id " +
                            "WHERE m.member_id = ?";       // 로그인 아이디로 강사 정보 가져오기

            ps = con.prepareStatement(sql);               // 쿼리 준비
            ps.setString(1, id);                          // 1번 파라미터에 member_id 바인딩
            rs = ps.executeQuery();                       // SELECT 실행

            if (rs.next()) {                              // 결과가 존재하면
                this.name = rs.getString("name");         // 이름
                this.addressData = rs.getString("address"); // 주소
                this.phone = rs.getString("phone");       // 전화번호
                this.email = rs.getString("email");       // 이메일
                this.security_question = rs.getString("security_question"); // 힌트 질문
                this.security_answer = rs.getString("security_answer");     // 힌트 답
                this.password = rs.getString("password"); // 비밀번호
                this.role = "강사";                       // 현재 화면은 강사
                this.teacherNo = rs.getInt("teacher_no"); // 강사번호
            } else {
                System.err.println("강사 정보 조회 실패: 해당 아이디 없음"); // 로그 출력
            }
        } catch (SQLException e) {
            e.printStackTrace();                          // 오류 출력
        } finally {
            ConnectDB.close(new AutoCloseable[]{rs, ps, con}); // 자원 정리
        }
    }

    // ===== 개인정보 수정 후 상단 프로필 갱신용 메소드 =====
    public void updateProfileFromEdit(String newName, String newPhone, String newEmail, String newAddress) {
        name = newName;                                   // 필드 값 갱신
        phone = newPhone;
        email = newEmail;
        addressData = newAddress;

        userName.setText(newName);                        // 화면 레이블 갱신
        phoneNumber.setText("전화번호: " + newPhone);
        eMail.setText("이메일: " + newEmail);
        address.setText("주소: " + newAddress);

        HeaderUserName.setText(newName + " 님");          // 최상단 레이블도 갱신
    }

    // ===== 강사가 담당하는 강의 목록 로드 =====
    private void loadLectureList() {
        Connection con = null;                            // DB 연결
        PreparedStatement ps = null;                      // SQL 실행
        ResultSet rs = null;                              // 결과 집합

        lectureNoList.clear();                            // lectureNoList 초기화
        lectureModel.setRowCount(0);                      // 테이블 데이터 초기화

        try {
            con = ConnectDB.getConnection();              // DB 연결

            String sql =
                    "SELECT lecture_no, subject_name, time_text, day_of_week, classroom_name, " +
                            "capacity, enrolled_count " +
                            "FROM view_teacher_my_lectures " + // 뷰에서
                            "WHERE teacher_no = ?";            // 현재 강사번호 기준 조회

            ps = con.prepareStatement(sql);               // 쿼리 준비
            ps.setInt(1, teacherNo);                      // 강사번호 바인딩
            rs = ps.executeQuery();                       // 실행

            lectureModel.setRowCount(0);                  // 혹시 모르니 다시 초기화

            while (rs.next()) {                           // 결과 한 줄씩 읽기
                int lectureNo = rs.getInt("lecture_no");  // lecture_no
                lectureNoList.add(lectureNo);             // 리스트에 보관

                Object[] row = {                          // 한 행 데이터 구성
                        rs.getString("subject_name"),     // 강좌명
                        rs.getString("time_text"),        // 시간
                        rs.getString("day_of_week"),      // 요일
                        rs.getString("classroom_name"),   // 강의실
                        rs.getInt("capacity"),            // 정원
                        rs.getInt("enrolled_count"),      // 수강인원
                        "수정",                           // 수정 버튼용 텍스트
                        "삭제"                            // 삭제 버튼용 텍스트
                };
                lectureModel.addRow(row);                 // 테이블에 행 추가
            }

        } catch (SQLException e) {
            e.printStackTrace();                          // 오류 출력
        } finally {
            ConnectDB.close(new AutoCloseable[]{rs, ps, con}); // 자원 정리
        }
    }

    // ===== Getter 메소드 =====
    public String getUserId() { return userId; }          // 아이디 반환
    public String getPassword() { return password; }      // 비밀번호 반환
    public String getName() { return name; }              // 이름 반환
    public String getPhone() { return phone; }            // 전화번호 반환
    public String getEmail() { return email; }            // 이메일 반환
    public String getAddressData() { return addressData; }// 주소 반환
    public String getSecurityQuestion() { return security_question; } // 힌트 질문 반환
    public String getSecurityAnswer() { return security_answer; }     // 힌트 답 반환
    public String getRole() { return role; }              // 역할 반환
    public int getTeacherNo() { return teacherNo; }       // 강사번호 반환

    // ===== Setter 메소드 =====
    public void setPassword(String pw) { password = pw; } // 비밀번호 세터
    public void setSecurityQuestion(String q) { security_question = q; } // 질문 세터
    public void setSecurityAnswer(String a) { security_answer = a; }     // 답 세터

    // ===== 버튼 액션 처리 =====
    @Override
    public void actionPerformed(ActionEvent ae) {         // 버튼 클릭 시 호출
        String cmd = ae.getActionCommand();               // 버튼의 텍스트 가져오기

        if (cmd.equals("로그아웃")) {                     // 로그아웃 버튼
            new Login();                                 // 로그인 화면 다시 띄움
            this.dispose();                              // 현재 TeacherMain 닫기
        } else if (cmd.equals("개인정보 수정")) {          // 개인정보 수정
            TeacherProfileEdit tpe =
                    new TeacherProfileEdit(this, "개인정보 수정"); // 다이얼로그 생성
            tpe.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 닫으면 이 다이얼로그만 닫기
            tpe.setSize(500, 400);                       // 크기
            tpe.setLocationRelativeTo(this);             // 중앙
            tpe.setVisible(true);                        // 표시
        } else if (cmd.equals("신규 강의 개설")) {          // 신규 강의 개설
            TeacherLectureCreate tlc =
                    new TeacherLectureCreate(this, "신규 강의 개설"); // 다이얼로그 생성
            tlc.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 닫기 설정
            tlc.setSize(500, 400);                       // 크기
            tlc.setLocationRelativeTo(this);             // 중앙
            tlc.setVisible(true);                        // 표시
        } else if (cmd.equals("검색")) {                  // 검색 버튼
            // 검색 로직은 나중에 채워도 됨
            JOptionPane.showMessageDialog(
                    this,
                    "검색 기능은 나중에 WHERE 조건만 추가하면 됨.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
