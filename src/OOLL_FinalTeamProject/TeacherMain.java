package OOLL_FinalTeamProject; // 패키지 선언

// ===== import 구간 =====
import javax.swing.*; // 스윙 컴포넌트 사용
import javax.swing.table.DefaultTableModel; // 테이블 모델 사용
import javax.swing.table.TableCellRenderer; // 셀 렌더러 사용
import javax.swing.table.TableCellEditor; // 셀 에디터 사용
import java.awt.*; // 레이아웃, 색상 등
import java.awt.event.*; // 이벤트 처리
import java.util.EventObject; // 셀 에디터용 이벤트 객체
import java.util.ArrayList; // ArrayList 사용
import java.util.List; // List 인터페이스 사용
import java.util.HashMap; // HashMap 사용
import java.util.Map; // Map 인터페이스 사용

// ===== 강사 메인 화면 클래스 =====
public class TeacherMain extends JFrame { // TeacherMain 클래스 선언, JFrame 상속

    // ===== 필드 구간: 강사 정보 =====
    private String teacherId; // 로그인한 강사 아이디
    private String teacherName; // 로그인한 강사 이름
    private String teacherPhone; // 강사 연락처
    private String teacherAddress; // 강사 주소

    // ===== 필드 구간: 비밀번호/힌트 (내부 상태) =====
    private String teacherPassword; // 강사 비밀번호 (내부 보관용)
    private String teacherPwHintQuestion; // 비밀번호 힌트 질문
    private String teacherPwHintAnswer; // 비밀번호 힌트 답변

    // ===== 필드 구간: 검색/버튼/테이블 =====
    private JTextField tfSearch; // 검색어 입력 필드
    private JButton btnSearch; // 검색 버튼
    private JButton btnNewLecture; // 신규 강좌 개설 버튼
    private JTable tblLectures; // 강좌 목록 테이블
    private DefaultTableModel lectureTableModel; // 강좌 테이블 모델

    private List<LectureInfo> allLectures; // 전체 강좌 목록
    private List<LectureInfo> currentViewLectures; // 검색 결과 기준 현재 테이블에 보이는 목록

    // 상단 바 / 개인정보 카드 라벨 필드
    private JLabel lblUserName; // 최상단 우측 "이름 님" 라벨
    private JLabel lblProfileName; // 개인정보 카드 이름 라벨
    private JLabel lblProfilePhone; // 개인정보 카드 연락처 라벨
    private JLabel lblProfileAddress; // 개인정보 카드 주소 라벨

    // 강좌별(lectureTitle) → 날짜(yyyy-MM-dd) → (학생이름 → 출결 상태) 저장용 Map (메모리에서만 유지)
    private Map<String, Map<String, Map<String, String>>> attendanceMap = new HashMap<>(); // 출결 정보 Map

    // ===== 내부 강좌 정보 클래스 =====
    public static class LectureInfo { // 다른 클래스에서도 쓸 수 있게 public static 내부 클래스
        String subject; // 과목명
        int startPeriod; // 시작 교시
        int endPeriod; // 종료 교시
        String days; // 요일 문자열 (예: "월/수/금")
        String room; // 강의실

        LectureInfo(String subject, int startPeriod, int endPeriod, String days, String room) { // 생성자
            this.subject = subject; // 과목명 저장
            this.startPeriod = startPeriod; // 시작 교시 저장
            this.endPeriod = endPeriod; // 종료 교시 저장
            this.days = days; // 요일 문자열 저장
            this.room = room; // 강의실 저장
        }
    }

    // ===== 강좌 중복 체크 관련 유틸리티 =====

    // 요일 문자열 간에 하나라도 겹치는 요일이 있는지 확인 ("월/수/금" 형태)
    private boolean hasCommonDay(String days1, String days2) { // 두 요일 문자열에 공통 요일이 있는지 확인
        if (days1 == null || days1.isEmpty() || days2 == null || days2.isEmpty()) { // null 또는 빈 문자열이면
            return false; // 공통 요일 없음
        }
        String[] d1 = days1.split("/"); // 첫 번째 요일 문자열 분리
        String[] d2 = days2.split("/"); // 두 번째 요일 문자열 분리

        for (String s1 : d1) { // 첫 번째 배열 순회
            String t1 = s1.trim(); // 공백 제거
            if (t1.isEmpty()) continue; // 빈 문자열이면 건너뜀
            for (String s2 : d2) { // 두 번째 배열 순회
                if (t1.equals(s2.trim())) { // 같은 요일이 하나라도 있으면
                    return true; // 공통 요일 존재
                }
            }
        }
        return false; // 공통 요일 없음
    }

    // 교시 구간이 겹치는지 확인
    private boolean isTimeOverlap(int start1, int end1, int start2, int end2) { // 두 교시 구간이 겹치는지 확인
        return !(end1 < start2 || end2 < start1); // 한쪽이 완전히 앞이나 뒤에 있지 않으면 겹침
    }

    /**
     * 시간대 + 요일 + 강의실이 겹치는 강좌가 있는지 검사.
     */
    public boolean hasLectureConflict(String room, String days, int start, int end, LectureInfo exclude) { // 강좌 시간/요일/강의실 충돌 여부 확인
        if (room == null || days == null) return false; // 강의실이나 요일이 없으면 충돌 없음
        if (allLectures == null) return false; // 강좌 목록이 없으면 충돌 없음

        for (LectureInfo lec : allLectures) { // 모든 강좌 순회
            if (lec == exclude) continue; // 자기 자신은 건너뜀(수정 시)
            if (!room.equals(lec.room)) continue; // 강의실 다르면 패스
            if (!hasCommonDay(days, lec.days)) continue; // 요일 안 겹치면 패스
            if (!isTimeOverlap(start, end, lec.startPeriod, lec.endPeriod)) continue; // 시간 안 겹치면 패스
            return true; // 여기까지 왔으면 충돌 있음
        }
        return false; // 충돌 없음
    }

    // ===== 생성자 =====
    public TeacherMain(String teacherId, String teacherName) { // TeacherMain 생성자
        this.teacherId = teacherId; // 아이디 필드에 저장
        this.teacherName = teacherName; // 이름 필드에 저장
        this.teacherPhone = "010-1234-5678"; // 더미 연락처 값 지정
        this.teacherAddress = "서울시 서대문구 ○○"; // 더미 주소 값 지정

        this.teacherPassword = "1234"; // 더미 비밀번호 지정
        this.teacherPwHintQuestion = "기억에 남는 선생님 성함은?"; // 더미 힌트 질문 지정
        this.teacherPwHintAnswer = "홍선생"; // 더미 힌트 답 지정

        setTitle("강사 메인 화면"); // 프레임 제목 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫기 시 프로그램 종료 설정
        setSize(1200, 800); // 프레임 크기 설정
        setLocationRelativeTo(null); // 화면 중앙 배치

        initComponents(); // 컴포넌트 초기화
        initDummyLectures(); // 더미 강좌 데이터 초기화

        setVisible(true); // 프레임 보이도록 설정
    }

    // ===== 전체 컴포넌트 초기화 =====
    private void initComponents() { // 전체 UI 구성 메서드
        Container c = getContentPane(); // 컨텐트팬 가져오기
        c.setLayout(new BorderLayout()); // BorderLayout 설정
        c.setBackground(new Color(0xF5, 0xF5, 0xF5)); // 전체 배경색 연한 회색으로 설정

        JPanel topPanel = createTopPanel(); // 상단 공통 UI 패널 생성
        c.add(topPanel, BorderLayout.NORTH); // 상단에 부착

        JPanel centerPanel = createCenterPanel(); // 중앙 담당강의 패널 생성
        c.add(centerPanel, BorderLayout.CENTER); // 중앙에 부착
    }

    // ===== 상단(최상단 바 + 개인정보 카드) 패널 =====
    private JPanel createTopPanel() { // 상단 패널 생성 메서드
        JPanel wrapper = new JPanel(); // 최상단/개인정보 전체를 감싸는 패널
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS)); // 세로 배치 레이아웃 설정
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 바깥 여백 설정
        wrapper.setBackground(new Color(0xF5, 0xF5, 0xF5)); // 배경 회색 설정

        // ========== 1. 최상단 바: 로고 + 학원명 + 이름님 + 로그아웃 ==========
        JPanel headerCard = new JPanel(new BorderLayout()); // 최상단 바 카드 패널
        headerCard.setBackground(Color.WHITE); // 배경 흰색
        headerCard.setBorder(BorderFactory.createCompoundBorder( // 테두리+안쪽 여백 결합
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)), // 옅은 회색 테두리
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // 안쪽 여백
        ));

        // ---- 좌측: 로고 + 학원이름 ----
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 좌측 영역 패널
        leftHeader.setOpaque(false); // 투명 처리(부모 배경 보이도록)

        ImageIcon logoIcon = new ImageIcon("C:/My2025_after/My2025_OOLL/mj_logo.jpg"); // 로고 원본 이미지 아이콘 생성
        Image scaledLogoImage = logoIcon.getImage().getScaledInstance(80, 50, Image.SCALE_SMOOTH); // 80x50 크기로 부드럽게 스케일 조정
        ImageIcon scaledLogoIcon = new ImageIcon(scaledLogoImage); // 스케일 조정된 이미지로 새 아이콘 생성
        JLabel lblLogo = new JLabel(scaledLogoIcon); // 아이콘을 가진 로고 라벨 생성
        lblLogo.setPreferredSize(new Dimension(80, 50)); // 로고 라벨 고정 크기 설정
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER); // 수평 중앙 정렬
        lblLogo.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // 로고 라벨 테두리 회색선 추가

        JLabel lblAcademyName = new JLabel("明知 LMS"); // 학원 이름 라벨 생성
        lblAcademyName.setFont(lblAcademyName.getFont().deriveFont(Font.BOLD, 20f)); // 볼드 20pt로 설정

        leftHeader.add(lblLogo); // 좌측 헤더에 로고 라벨 추가
        leftHeader.add(Box.createHorizontalStrut(10)); // 로고와 학원명 사이 여백 추가
        leftHeader.add(lblAcademyName); // 좌측 헤더에 학원명 라벨 추가

        // ---- 우측: 이름님 + 로그아웃 ----
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 우측 영역 패널
        rightHeader.setOpaque(false); // 투명 처리

        lblUserName = new JLabel(teacherName + " 님"); // 우측 상단 이름 표시 라벨
        JButton btnLogout = new JButton("로그아웃"); // 로그아웃 버튼 생성

        btnLogout.addActionListener(e -> { // 로그아웃 버튼 클릭 리스너
            int result = JOptionPane.showConfirmDialog( // 확인 다이얼로그 표시
                    this, // 부모 컴포넌트
                    "로그아웃 하시겠습니까?", // 메시지
                    "로그아웃", // 타이틀
                    JOptionPane.YES_NO_OPTION // 예/아니오 옵션
            );
            if (result == JOptionPane.YES_OPTION) { // 예 선택 시
                dispose(); // 현재 프레임 닫기 (추후 로그인 화면 복귀 로직 필요)
            }
        });

        rightHeader.add(lblUserName); // 우측 헤더에 이름 라벨 추가
        rightHeader.add(Box.createHorizontalStrut(10)); // 이름과 버튼 사이 여백
        rightHeader.add(btnLogout); // 우측 헤더에 로그아웃 버튼 추가

        headerCard.add(leftHeader, BorderLayout.WEST); // 최상단 카드 좌측에 leftHeader 부착
        headerCard.add(rightHeader, BorderLayout.EAST); // 최상단 카드 우측에 rightHeader 부착

        // ========== 2. 개인정보 카드 ==========
        JPanel profileCard = new JPanel(new BorderLayout()); // 개인정보 카드 패널
        profileCard.setBackground(Color.WHITE); // 배경 흰색
        profileCard.setBorder(BorderFactory.createCompoundBorder( // 테두리 + 안쪽 여백
                BorderFactory.createTitledBorder("개인정보"), // "개인정보" 타이틀 테두리
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // 안쪽 여백
        ));

        // 왼쪽: 프로필 아이콘 + 텍스트들
        JPanel profileLeft = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 개인정보 왼쪽 영역 패널
        profileLeft.setOpaque(false); // 투명 처리

        JLabel lblProfileIcon = new JLabel("\uD83D\uDC64"); // 사람 아이콘 비슷한 문자 라벨
        lblProfileIcon.setFont(lblProfileIcon.getFont().deriveFont(32f)); // 아이콘 크기 크게
        lblProfileIcon.setPreferredSize(new Dimension(40, 40)); // 고정 크기 설정

        JPanel profileTextPanel = new JPanel(); // 이름/역할/연락처/주소 텍스트 패널
        profileTextPanel.setLayout(new BoxLayout(profileTextPanel, BoxLayout.Y_AXIS)); // 세로 배치
        profileTextPanel.setOpaque(false); // 투명 처리

        lblProfileName = new JLabel(teacherName); // 이름 라벨
        lblProfileName.setFont(lblProfileName.getFont().deriveFont(Font.BOLD, 18f)); // 볼드 18pt

        JLabel lblRole = new JLabel("강사"); // 역할 라벨(강사)
        lblProfilePhone = new JLabel("연락처: " + teacherPhone); // 연락처 라벨
        lblProfileAddress = new JLabel("주소: " + teacherAddress); // 주소 라벨

        profileTextPanel.add(lblProfileName); // 텍스트 패널에 이름 라벨 추가
        profileTextPanel.add(lblRole); // 텍스트 패널에 역할 라벨 추가
        profileTextPanel.add(Box.createVerticalStrut(5)); // 위/아래 여백
        profileTextPanel.add(lblProfilePhone); // 텍스트 패널에 연락처 라벨 추가
        profileTextPanel.add(lblProfileAddress); // 텍스트 패널에 주소 라벨 추가

        profileLeft.add(lblProfileIcon); // 개인정보 왼쪽 패널에 아이콘 라벨 추가
        profileLeft.add(Box.createHorizontalStrut(15)); // 아이콘과 텍스트 사이 여백
        profileLeft.add(profileTextPanel); // 개인정보 왼쪽 패널에 텍스트 패널 추가

        // 오른쪽: 개인정보 수정 버튼
        JPanel profileRight = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 개인정보 오른쪽 패널
        profileRight.setOpaque(false); // 투명 처리

        JButton btnProfile = new JButton("개인정보 수정"); // 개인정보 수정 버튼 생성
        btnProfile.addActionListener(e -> new TeacherProfileEdit( // 버튼 클릭 시
                this, // 소유자 TeacherMain
                teacherId, // 아이디
                teacherName, // 이름
                teacherPhone, // 연락처
                teacherAddress // 주소
        ));

        profileRight.add(btnProfile); // 오른쪽 패널에 버튼 추가

        profileCard.add(profileLeft, BorderLayout.WEST); // 개인정보 카드 왼쪽에 profileLeft 부착
        profileCard.add(profileRight, BorderLayout.EAST); // 개인정보 카드 오른쪽에 profileRight 부착

        // ========== wrapper에 두 카드 세로로 추가 ==========
        wrapper.add(headerCard); // 최상단 카드 추가
        wrapper.add(Box.createVerticalStrut(10)); // 위/아래 여백
        wrapper.add(profileCard); // 개인정보 카드 추가

        return wrapper; // 완성된 상단 패널 반환
    }

    // ===== 중앙(담당강의 카드: 검색 + 신규강좌 + 강좌테이블) 패널 =====
    private JPanel createCenterPanel() { // 중앙 패널 생성 메서드
        JPanel outerPanel = new JPanel(new BorderLayout()); // 회색 배경용 바깥 패널
        outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 바깥 여백 설정
        outerPanel.setBackground(new Color(0xF5, 0xF5, 0xF5)); // 배경 회색

        JPanel lectureCard = new JPanel(new BorderLayout()); // 담당강의 카드 패널
        lectureCard.setBackground(Color.WHITE); // 배경 흰색
        lectureCard.setBorder(BorderFactory.createCompoundBorder( // 테두리 + 안쪽 여백
                BorderFactory.createTitledBorder("담당강의"), // "담당강의" 타이틀
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // 안쪽 여백
        ));

        // ---- 상단: 좌측 검색, 우측 신규강좌버튼 ----
        JPanel topSearchPanel = new JPanel(new BorderLayout()); // 검색/버튼 상단 패널
        topSearchPanel.setOpaque(false); // 투명 처리

        JPanel leftSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 좌측 검색 영역 패널
        leftSearchPanel.setOpaque(false); // 투명 처리
        JLabel lblSearch = new JLabel("강좌 검색:"); // 검색 라벨
        tfSearch = new JTextField(25); // 검색어 입력 필드
        btnSearch = new JButton("검색"); // 검색 버튼

        btnSearch.addActionListener(e -> performSearch()); // 검색 버튼 클릭 시 검색 실행
        tfSearch.addActionListener(e -> performSearch()); // 텍스트 필드에서 Enter 시 검색 실행

        leftSearchPanel.add(lblSearch); // 좌측 검색 패널에 라벨 추가
        leftSearchPanel.add(tfSearch); // 좌측 검색 패널에 텍스트 필드 추가
        leftSearchPanel.add(btnSearch); // 좌측 검색 패널에 버튼 추가

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 우측 신규강좌 버튼 패널
        rightButtonPanel.setOpaque(false); // 투명 처리
        btnNewLecture = new JButton("신규 강좌 개설"); // 신규 강좌 개설 버튼

        btnNewLecture.addActionListener(e -> new TeacherLectureCreate(this)); // 버튼 클릭 시 강좌 생성 다이얼로그 호출

        rightButtonPanel.add(btnNewLecture); // 우측 버튼 패널에 버튼 추가

        topSearchPanel.add(leftSearchPanel, BorderLayout.WEST); // 상단 패널 좌측에 검색 패널 부착
        topSearchPanel.add(rightButtonPanel, BorderLayout.EAST); // 상단 패널 우측에 버튼 패널 부착

        // ---- 중단: 강좌 테이블 ----
        String[] columnNames = { // 테이블 컬럼명 배열
                "강좌명", // 0번 열: 강좌명
                "시간", // 1번 열: 시간
                "요일", // 2번 열: 요일
                "강의실", // 3번 열: 강의실
                "수정", // 4번 열: 수정 버튼
                "삭제" // 5번 열: 삭제 버튼
        };

        lectureTableModel = new DefaultTableModel(columnNames, 0) { // 강좌 테이블 모델 생성
            @Override
            public boolean isCellEditable(int row, int column) { // 셀 편집 가능 여부
                return column == 4 || column == 5; // 4,5번 열(수정/삭제)만 편집 가능
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) { // 컬럼 타입 지정
                if (columnIndex == 4 || columnIndex == 5) { // 수정/삭제 컬럼이면
                    return JButton.class; // JButton 타입으로 처리
                }
                return String.class; // 나머지는 문자열 타입
            }
        };

        tblLectures = new JTable(lectureTableModel); // JTable 생성
        tblLectures.setRowHeight(32); // 행 높이 32로 설정

        Icon editIcon = UIManager.getIcon("FileView.directoryIcon"); // 수정용 아이콘(간단히 디렉터리 아이콘 사용)
        Icon deleteIcon = UIManager.getIcon("OptionPane.errorIcon"); // 삭제용 아이콘(에러 아이콘 사용)

        tblLectures.getColumnModel().getColumn(4).setCellRenderer( // 4번 열 렌더러 설정
                new TableIconButtonRenderer("수정", editIcon) // "수정" 버튼 렌더러
        );
        tblLectures.getColumnModel().getColumn(4).setCellEditor( // 4번 열 에디터 설정
                new TableIconButtonEditor(new JCheckBox(), "수정", editIcon, this) // "수정" 버튼 에디터
        );

        tblLectures.getColumnModel().getColumn(5).setCellRenderer( // 5번 열 렌더러 설정
                new TableIconButtonRenderer("삭제", deleteIcon) // "삭제" 버튼 렌더러
        );
        tblLectures.getColumnModel().getColumn(5).setCellEditor( // 5번 열 에디터 설정
                new TableIconButtonEditor(new JCheckBox(), "삭제", deleteIcon, this) // "삭제" 버튼 에디터
        );

        tblLectures.addMouseListener(new MouseAdapter() { // 테이블 마우스 리스너 추가
            @Override
            public void mouseClicked(MouseEvent e) { // 마우스 클릭 시 호출
                int row = tblLectures.rowAtPoint(e.getPoint()); // 클릭한 행 인덱스
                int col = tblLectures.columnAtPoint(e.getPoint()); // 클릭한 열 인덱스
                if (row < 0) return; // 유효하지 않은 행이면 리턴
                if (col == 4 || col == 5) return; // 수정/삭제 컬럼 클릭이면 무시

                LectureInfo info = currentViewLectures.get(row); // 현재 뷰의 해당 강좌 정보 가져오기
                new TeacherStudentList(TeacherMain.this, info.subject); // 수강생 목록 다이얼로그 열기
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblLectures); // 테이블용 스크롤 페인 생성

        lectureCard.add(topSearchPanel, BorderLayout.NORTH); // 담당강의 카드 상단에 검색 패널 부착
        lectureCard.add(scrollPane, BorderLayout.CENTER); // 담당강의 카드 중앙에 테이블 부착

        outerPanel.add(lectureCard, BorderLayout.CENTER); // 바깥 패널 중앙에 담당강의 카드 부착

        return outerPanel; // 완성된 중앙 패널 반환
    }

    // ===== 더미 강좌 데이터 초기화 =====
    private void initDummyLectures() { // 더미 강좌 목록 생성 메서드
        allLectures = new ArrayList<>(); // 전체 강좌 리스트 생성
        currentViewLectures = new ArrayList<>(); // 현재 뷰 강좌 리스트 생성

        allLectures.add(new LectureInfo("파이썬 기초", 2, 4, "월/수/금", "203호")); // 예시 강좌 1 추가
        allLectures.add(new LectureInfo("자바 중급", 1, 3, "화/목", "201호")); // 예시 강좌 2 추가
        allLectures.add(new LectureInfo("C 언어 입문", 5, 6, "토", "301호")); // 예시 강좌 3 추가

        refreshTableWithList(allLectures); // 전체 목록 기준으로 테이블 갱신
    }

    // ===== 리스트를 기준으로 테이블 다시 채우기 =====
    private void refreshTableWithList(List<LectureInfo> list) { // 주어진 목록으로 테이블 갱신
        lectureTableModel.setRowCount(0); // 기존 테이블 행 전체 삭제
        currentViewLectures.clear(); // 현재 뷰 목록 비우기

        for (LectureInfo info : list) { // 목록 순회
            currentViewLectures.add(info); // 현재 뷰 목록에 추가
            String timeText = formatPeriodRange(info.startPeriod, info.endPeriod); // "2~4교시" 형태 문자열 생성
            Object[] row = { // 테이블 한 줄 데이터 구성
                    info.subject, // 강좌명
                    timeText, // 시간
                    info.days, // 요일
                    info.room, // 강의실
                    "수정", // 수정 버튼 텍스트
                    "삭제" // 삭제 버튼 텍스트
            };
            lectureTableModel.addRow(row); // 테이블 모델에 행 추가
        }
    }

    // ===== 교시 범위를 문자열로 포맷팅 (예: "2~4교시" 또는 "2교시") =====
    private String formatPeriodRange(int start, int end) { // 교시 범위를 문자열로 변환
        if (start == end) { // 시작과 종료가 같으면
            return start + "교시"; // "2교시" 형식 반환
        }
        return start + "~" + end + "교시"; // "2~4교시" 형식 반환
    }

    // ===== 검색 기능 =====
    private void performSearch() { // 검색 실행 메서드
        String keyword = tfSearch.getText().trim(); // 검색어 앞뒤 공백 제거

        if (keyword.isEmpty()) { // 검색어가 비어 있으면
            refreshTableWithList(allLectures); // 전체 목록으로 복원
            return; // 메서드 종료
        }

        List<LectureInfo> filtered = new ArrayList<>(); // 필터링 결과 목록
        for (LectureInfo info : allLectures) { // 전체 강좌 순회
            if (info.subject.contains(keyword)) { // 과목명에 검색어가 포함되면
                filtered.add(info); // 결과 목록에 추가
            }
        }

        refreshTableWithList(filtered); // 필터링 결과 기준으로 테이블 갱신
    }

    // ===== 강좌 신규 생성 시 TeacherMain 쪽 목록/테이블 갱신 =====
    public void addNewLecture(String subject, int start, int end, String days, String room) { // 신규 강좌 추가 메서드
        LectureInfo info = new LectureInfo(subject, start, end, days, room); // 새 강좌 정보 객체 생성
        allLectures.add(info); // 전체 강좌 목록에 추가
        performSearch(); // 현재 검색어 기준으로 다시 리스트 표시
    }

    // ===== 강좌 정보 수정 후 테이블만 다시 그리기 =====
    public void refreshLectureTable() { // 강좌 수정/삭제 후 테이블 갱신 메서드
        performSearch(); // 현재 검색어 기준으로 갱신
    }

    // ===== 수정 버튼 클릭 처리 =====
    public void handleEditLecture(int rowIndex) { // 수정 요청 처리 메서드
        if (rowIndex < 0 || rowIndex >= currentViewLectures.size()) { // 인덱스 범위 체크
            return; // 유효하지 않으면 종료
        }

        LectureInfo info = currentViewLectures.get(rowIndex); // 선택된 강좌 정보 가져오기
        new TeacherLectureEdit(this, info); // 강좌 수정 다이얼로그 열기
    }

    // ===== 삭제 버튼 클릭 처리 =====
    public void handleDeleteLecture(int rowIndex) { // 삭제 요청 처리 메서드
        if (rowIndex < 0 || rowIndex >= currentViewLectures.size()) { // 인덱스 범위 체크
            return; // 유효하지 않으면 종료
        }

        LectureInfo target = currentViewLectures.get(rowIndex); // 현재 뷰에서 삭제 대상 강좌 추출

        int result = JOptionPane.showConfirmDialog( // 삭제 확인 다이얼로그 표시
                this, // 부모 컴포넌트
                "강좌를 삭제하시겠습니까?", // 메시지
                "강좌 삭제", // 타이틀
                JOptionPane.YES_NO_OPTION // 예/아니오 옵션
        );
        if (result != JOptionPane.YES_OPTION) { // 예가 아니면
            return; // 삭제 취소
        }

        allLectures.remove(target); // 전체 목록에서 해당 강좌 삭제
        currentViewLectures.remove(target); // 현재 뷰 목록에서도 삭제

        refreshLectureTable(); // 테이블 다시 갱신
        // TODO: DB에서도 DELETE 쿼리 수행 필요
    }

    // ===== 개인정보 수정 결과를 TeacherMain에 반영 =====
    public void updateTeacherProfile(String newName, String newPhone, String newAddress) { // 개인정보 변경 반영 메서드
        this.teacherName = newName; // 이름 갱신
        this.teacherPhone = newPhone; // 연락처 갱신
        this.teacherAddress = newAddress; // 주소 갱신

        lblUserName.setText(teacherName + " 님"); // 최상단 이름 라벨 갱신

        if (lblProfileName != null) { // 개인정보 이름 라벨이 존재하면
            lblProfileName.setText(teacherName); // 이름 갱신
        }
        if (lblProfilePhone != null) { // 연락처 라벨이 존재하면
            lblProfilePhone.setText("연락처: " + teacherPhone); // 연락처 갱신
        }
        if (lblProfileAddress != null) { // 주소 라벨이 존재하면
            lblProfileAddress.setText("주소: " + teacherAddress); // 주소 갱신
        }

        System.out.println("[TeacherMain] 개인정보 갱신: 이름=" + teacherName
                + ", 연락처=" + teacherPhone
                + ", 주소=" + teacherAddress); // 콘솔 출력으로 확인
    }

    // ===== 비밀번호/힌트 변경 결과를 TeacherMain에 반영 =====
    public void updateTeacherPassword(String newPw, String hintQ, String hintA) { // 비밀번호 변경 반영 메서드
        this.teacherPassword = newPw; // 비밀번호 갱신
        this.teacherPwHintQuestion = hintQ; // 힌트 질문 갱신
        this.teacherPwHintAnswer = hintA; // 힌트 답 갱신

        System.out.println("[TeacherMain] 비밀번호/힌트 갱신"); // 콘솔 출력으로 확인
    }

    // ===== 비밀번호/힌트 현재값을 외부에서 읽을 때 사용 (필요시) =====
    public String getTeacherPassword() { // 비밀번호 getter
        return teacherPassword; // 비밀번호 반환
    }

    public String getTeacherPwHintQuestion() { // 힌트 질문 getter
        return teacherPwHintQuestion; // 힌트 질문 반환
    }

    public String getTeacherPwHintAnswer() { // 힌트 답 getter
        return teacherPwHintAnswer; // 힌트 답 반환
    }

    // ===== 출결 상태 저장/조회 (강좌별/날짜별/학생별, 메모리 상에서만 유지) =====
    public Map<String, String> getAttendanceForLecture(String lectureTitle, String dateKey) { // 특정 강좌/날짜 출결 Map 가져오기
        Map<String, Map<String, String>> lectureMap = attendanceMap.get(lectureTitle); // 강좌별 Map 조회
        if (lectureMap == null) { // 없으면
            lectureMap = new HashMap<>(); // 새 Map 생성
            attendanceMap.put(lectureTitle, lectureMap); // 최상위 Map에 저장
        }

        Map<String, String> dateMap = lectureMap.get(dateKey); // 날짜별 Map 조회
        if (dateMap == null) { // 없으면
            dateMap = new HashMap<>(); // 새 Map 생성
            lectureMap.put(dateKey, dateMap); // 강좌별 Map에 저장
        }
        return dateMap; // 날짜별 출결 Map 반환
    }

    public void updateAttendance(String lectureTitle, String dateKey, String studentName, String status) { // 출결 상태 갱신 메서드
        Map<String, String> map = getAttendanceForLecture(lectureTitle, dateKey); // 해당 강좌/날짜 출결 Map 가져오기
        map.put(studentName, status); // 학생 이름을 키로 하여 출결 상태 저장
    }

    // ===== 테이블용 아이콘 버튼 렌더러 =====
    private static class TableIconButtonRenderer extends JButton implements TableCellRenderer { // 버튼 렌더러 클래스
        public TableIconButtonRenderer(String text, Icon icon) { // 생성자
            super(text, icon); // 부모 JButton 생성자 호출
            setOpaque(true); // 불투명 설정
        }

        @Override
        public Component getTableCellRendererComponent( // 셀 렌더링 메서드
                                                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString()); // 셀 값으로 버튼 텍스트 설정

            if (isSelected) { // 선택된 셀일 경우
                setForeground(table.getSelectionForeground()); // 선택 전경색 적용
                setBackground(table.getSelectionBackground()); // 선택 배경색 적용
            } else { // 선택되지 않은 경우
                setForeground(table.getForeground()); // 기본 전경색 적용
                setBackground(UIManager.getColor("Button.background")); // 기본 버튼 배경색 적용
            }

            setBorderPainted(true); // 버튼 테두리 표시
            setContentAreaFilled(true); // 버튼 내부 채우기
            return this; // 버튼 컴포넌트 반환
        }
    }

    // ===== 테이블용 아이콘 버튼 에디터 =====
    private static class TableIconButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener { // 버튼 에디터 클래스
        private JButton button; // 실제 버튼 컴포넌트
        private String actionType; // "수정" 또는 "삭제" 구분용 문자열
        private TeacherMain main; // TeacherMain 참조
        private int currentRow; // 현재 편집 중인 행 인덱스

        public TableIconButtonEditor(JCheckBox checkBox, String actionType, Icon icon, TeacherMain main) { // 생성자
            this.actionType = actionType; // 액션 타입 저장
            this.main = main; // 메인 프레임 참조 저장
            this.button = new JButton(actionType, icon); // 텍스트와 아이콘을 가진 버튼 생성
            this.button.setOpaque(true); // 불투명 설정
            this.button.addActionListener(this); // 액션 리스너 등록
        }

        @Override
        public Component getTableCellEditorComponent( // 셀 편집용 컴포넌트 반환
                                                      JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; // 현재 행 인덱스 저장
            button.setText(value == null ? actionType : value.toString()); // 셀 값으로 버튼 텍스트 설정
            return button; // 버튼 반환
        }

        @Override
        public Object getCellEditorValue() { // 편집 결과 값 반환
            return actionType; // "수정" 또는 "삭제" 반환
        }

        @Override
        public boolean isCellEditable(EventObject e) { // 셀 편집 가능 여부
            return true; // 항상 편집 가능
        }

        @Override
        public void actionPerformed(ActionEvent e) { // 버튼 클릭 시 호출
            fireEditingStopped(); // 셀 편집 종료 이벤트 발생

            if ("수정".equals(actionType)) { // 수정 버튼인 경우
                main.handleEditLecture(currentRow); // TeacherMain의 수정 처리 호출
            } else if ("삭제".equals(actionType)) { // 삭제 버튼인 경우
                main.handleDeleteLecture(currentRow); // TeacherMain의 삭제 처리 호출
            }
        }
    }

    // ===== 테스트용 main 메서드 =====
    public static void main(String[] args) { // 프로그램 시작 지점
        SwingUtilities.invokeLater(() -> new TeacherMain("teacher01", "홍길동")); // EDT에서 TeacherMain 생성
    }
}
