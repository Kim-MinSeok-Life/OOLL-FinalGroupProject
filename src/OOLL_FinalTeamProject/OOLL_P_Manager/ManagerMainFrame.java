// 여기는 원장의 메인 화면!!

package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*; // DB 연동을 위한 SQL 패키지 임포트

// 메인 프레임 클래스: JFrame을 상속받아 창을 만들고, ActionListener를 구현해 버튼 이벤트를 처리함
public class ManagerMainFrame extends JFrame implements ActionListener {

    // 디자인의 통일성을 위해 색상과 폰트를 상수로 정의 (유지보수 용이)
    final Color COLOR_THEME = new Color(30, 40, 70);     // 메인 테마색 (네이비)
    final Color COLOR_BG = new Color(245, 245, 250);     // 배경색 (연한 회색)
    final Color COLOR_WHITE = Color.WHITE;               // 기본 흰색
    final Font FONT_TITLE = new Font("맑은 고딕", Font.BOLD, 24); // 제목용 폰트

    String loginId; // 로그인한 원장님의 아이디를 저장할 변수

    // 상단에 정보를 표시할 라벨들 (DB에서 값을 가져와서 내용을 바꿔야 하므로 멤버 변수로 선언)
    JLabel idLabel, nameLabel, phoneLabel, emailLabel, addrLabel;

    // 상단 버튼들 (이벤트 처리를 위해 멤버 변수로 선언)
    JButton editBtn, logoutBtn;

    // 기본 생성자: 테스트용으로 아이디 없이 실행할 때 'admin'으로 간주
    public ManagerMainFrame() {
        this("admin");
    }

    // 실제 생성자: 로그인 화면에서 넘어온 아이디(id)를 받음
    public ManagerMainFrame(String id) {
        this.loginId = id; // 전달받은 아이디를 멤버 변수에 저장

        // 1. 프레임(창) 기본 설정
        setTitle("明知 LMS - 원장 모드"); // 창 제목 설정
        setSize(1200, 800);            // 창 크기 설정 (가로 1200, 세로 800)
        setLocationRelativeTo(null);   // 창을 화면 정중앙에 띄움
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창을 닫으면 프로그램 프로세스도 종료
        setLayout(new BorderLayout()); // 동서남북 레이아웃 관리자 사용

        // 2. [북쪽 영역] 로고와 정보 패널을 담을 컨테이너
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS)); // 세로 방향으로 배치

        // (2-1) 브랜드 패널 (로고 + 프로그램 제목)
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15)); // 왼쪽 정렬
        brandPanel.setBackground(COLOR_THEME); // 배경색을 테마색(네이비)으로 설정

        JLabel logoBox;
        try {
            // 같은 패키지 내의 'image.jpg' 이미지를 불러옴
            ImageIcon icon = new ImageIcon(getClass().getResource("image.jpg"));
            // 이미지를 50x50 크기로 부드럽게(SCALE_SMOOTH) 조절
            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoBox = new JLabel(new ImageIcon(img)); // 라벨에 이미지 삽입
        } catch (Exception e) {
            // 이미지가 없거나 오류 발생 시 대체 텍스트 표시 (예외 처리)
            logoBox = new JLabel("LOGO", SwingConstants.CENTER);
            logoBox.setOpaque(true); // 배경색 적용을 위해 불투명하게 설정
            logoBox.setBackground(Color.LIGHT_GRAY);
            logoBox.setForeground(Color.BLACK);
        }
        logoBox.setPreferredSize(new Dimension(50, 50)); // 로고 영역 크기 고정

        JLabel titleLabel = new JLabel("明知 LMS (Learning Management System)"); // 제목 텍스트
        titleLabel.setFont(FONT_TITLE); // 폰트 적용
        titleLabel.setForeground(Color.WHITE); // 글자색 흰색

        brandPanel.add(logoBox); // 패널에 로고 추가
        brandPanel.add(titleLabel); // 패널에 제목 추가

        // (2-2) 정보 패널 (원장님 개인정보 표시 영역)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(COLOR_WHITE);
        // 테두리 설정: 아래쪽에만 얇은 선 + 안쪽 여백(Padding)
        infoPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(15, 30, 15, 30)
        ));

        // 정보를 2줄로 보여주기 위해 Grid 레이아웃 사용 (2행 1열)
        JPanel dataGrid = new JPanel(new GridLayout(2, 1, 0, 8));
        dataGrid.setOpaque(false); // 배경 투명하게 (부모 배경색 따름)

        // 라벨 초기화 (아직 DB 연동 전이므로 Loading 표시)
        idLabel = createLabel("아이디", "Loading...");
        nameLabel = createLabel("이름", "Loading...");
        phoneLabel = createLabel("전화번호", "Loading...");
        emailLabel = createLabel("이메일", "Loading...");
        addrLabel = createLabel("주소", "Loading...");

        // 첫 번째 줄 패널
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row1.setOpaque(false);
        row1.add(idLabel); row1.add(nameLabel); row1.add(phoneLabel);

        // 두 번째 줄 패널
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row2.setOpaque(false);
        row2.add(emailLabel); row2.add(addrLabel);

        dataGrid.add(row1); // 그리드에 첫째 줄 추가
        dataGrid.add(row2); // 그리드에 둘째 줄 추가

        // (2-3) 버튼 패널 (우측 상단 버튼들)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        editBtn = createStyledButton("개인정보 수정"); // 스타일이 적용된 버튼 생성
        editBtn.addActionListener(this); // 버튼 클릭 시 이 클래스의 actionPerformed 호출

        logoutBtn = createStyledButton("로그아웃");
        logoutBtn.addActionListener(this);

        btnPanel.add(editBtn);
        btnPanel.add(logoutBtn);

        // 정보 패널의 중앙엔 데이터, 동쪽(오른쪽)엔 버튼 배치
        infoPanel.add(dataGrid, BorderLayout.CENTER);
        infoPanel.add(btnPanel, BorderLayout.EAST);

        // 북쪽 컨테이너에 차곡차곡 쌓기
        northContainer.add(brandPanel);
        northContainer.add(infoPanel);

        // 3. [중앙 영역] 탭 메뉴 구성
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        tabPane.setBackground(COLOR_BG);

        // ★ 핵심: 기능별로 분리한 패널 클래스들을 탭에 추가
        // this(MainFrame)를 넘겨주는 이유는 팝업창을 띄울 때 부모 창으로 쓰기 위함
        tabPane.addTab("  강의 관리  ", new LecturePanel(this));
        tabPane.addTab("  학생 관리  ", new StudentPanel(this));
        tabPane.addTab("  강사 관리  ", new TeacherPanel(this)); // 친구가 만든 패널 통합

        // 최종적으로 프레임에 배치
        add(northContainer, BorderLayout.NORTH); // 북쪽에 상단 영역
        add(tabPane, BorderLayout.CENTER);       // 중앙에 탭 영역

        // 4. 화면 구성이 끝난 후 DB에서 원장 정보 불러오기
        loadMyInfo();

        setVisible(true); // 창을 화면에 표시
    }

    // ★ 버튼 클릭 이벤트 처리 메소드 (ActionListener 구현)
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource(); // 어떤 버튼이 눌렸는지 확인

        if (source == editBtn) {
            // 개인정보 수정 팝업 열기 (현재 ID를 전달)
            MyInfoDialog dialog = new MyInfoDialog(this, "개인정보 수정", loginId);
            dialog.setVisible(true);
            // 팝업이 닫히면(수정 완료 후) 정보를 다시 불러와서 화면 갱신
            loadMyInfo();

        } else if (source == logoutBtn) {
            // 로그아웃 확인 메시지
            int ans = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                dispose(); // 현재 원장 화면 닫기 (메모리 해제)
                // Login 클래스를 새로 생성해서 로그인 화면 띄우기 (친구 패키지)
                // new Login().setVisible(true);
            }
        }
    }

    // [DB 연동] 내 정보 불러오기
    private void loadMyInfo() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
            // DB 연결 (비밀번호 java2025)
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // SQL: member 테이블에서 내 ID에 해당하는 정보 조회
            String sql = "SELECT * FROM member WHERE member_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId); // ? 에 ID 값 바인딩

            rs = pstmt.executeQuery(); // 쿼리 실행 및 결과 받기
            if (rs.next()) { // 데이터가 존재하면
                // DB에서 컬럼 값 꺼내기
                String id = rs.getString("member_id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String addr = rs.getString("address");

                // 화면 라벨 텍스트 갱신 (HTML 포맷팅 사용)
                idLabel.setText(formatLabelText("아이디", id));
                nameLabel.setText(formatLabelText("이름", name));
                phoneLabel.setText(formatLabelText("전화번호", phone));
                emailLabel.setText(formatLabelText("이메일", email));
                addrLabel.setText(formatLabelText("주소", addr));
            }
        } catch (Exception e) {
            e.printStackTrace(); // 에러 발생 시 콘솔에 출력
        } finally {
            // 자원 해제 (DB 연결 끊기)
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // 라벨 생성 도우미 (중복 코드 제거)
    private JLabel createLabel(String title, String value) {
        return new JLabel(formatLabelText(title, value));
    }

    // 라벨 텍스트를 HTML로 꾸며주는 도우미 (제목은 회색, 값은 검고 진하게)
    private String formatLabelText(String title, String value) {
        return "<html><font color='#777777'>" + title + " : </font> <font size='4' color='black'><b>" + value + "</b></font></html>";
    }

    // 버튼 생성 도우미 (스타일 통일)
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        btn.setFocusPainted(false); // 포커스 테두리 제거
        btn.setBorder(new EmptyBorder(5, 10, 5, 10)); // 테두리 대신 여백 주기
        btn.setPreferredSize(new Dimension(120, 35)); // 버튼 크기 고정

        // 마우스 올렸을 때 배경색 살짝 변하게 하는 효과
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(new Color(240, 240, 240)); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    // ★ [static] 테이블 스타일 설정 (다른 패널에서도 공통으로 사용하기 위해 static 선언)
    public static void styleTable(JTable table) {
        table.setRowHeight(30); // 행 높이 늘리기
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14)); // 폰트 설정
        table.setSelectionBackground(new Color(230, 240, 255)); // 선택 시 배경색
        table.setSelectionForeground(Color.BLACK); // 선택 시 글자색 검정 유지
        table.setGridColor(new Color(230, 230, 230)); // 격자선 색상

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14)); // 헤더 폰트
        header.setBackground(new Color(245, 245, 250)); // 헤더 배경색
        header.setForeground(new Color(50, 50, 50)); // 헤더 글자색
        header.setPreferredSize(new Dimension(0, 35)); // 헤더 높이

        // 셀 내용을 가운데 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // 메인 메소드 (테스트 실행용)
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new ManagerMainFrame("admin"); // 테스트용 계정 실행
    }
}