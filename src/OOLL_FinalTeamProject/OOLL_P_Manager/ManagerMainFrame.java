// 여기는 원장의 메인 화면!!

package OOLL_P_Manager;

import team_project.Login;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
// Login 클래스는 같은 패키지(teamwork) 안에 있으므로 import 불필요

/**
 * [ManagerMainFrame 클래스]
 * 역할: 원장님 모드의 메인 화면(프레임)을 담당합니다.
 * 구조: 상단(정보 표시) + 중앙(탭 메뉴)으로 구성된 BorderLayout 사용.
 * 기능: DB에서 원장 정보 로드, 로그아웃, 각 관리 패널(강의/학생/강사) 연결.
 */
public class ManagerMainFrame extends JFrame implements ActionListener {

    // --- [상수 정의] 디자인 일관성을 위한 색상 및 폰트 ---
    final Color COLOR_THEME = new Color(30, 40, 70);     // 브랜드 컬러 (짙은 네이비)
    final Color COLOR_BG = new Color(245, 245, 250);     // 기본 배경색 (눈이 편한 연회색)
    final Color COLOR_WHITE = Color.WHITE;               // 흰색
    final Font FONT_TITLE = new Font("맑은 고딕", Font.BOLD, 24); // 타이틀 폰트

    // --- [멤버 변수] ---
    String loginId; // 로그인 화면에서 전달받은 원장님의 아이디(PK)를 저장

    // 상단 정보를 표시할 라벨들
    // (DB에서 조회한 데이터로 내용을 변경해야 하므로 지역변수가 아닌 멤버변수로 선언)
    JLabel idLabel, nameLabel, phoneLabel, emailLabel, addrLabel;

    // 상단 우측 버튼들 (이벤트 처리를 위해 멤버변수 선언)
    JButton editBtn, logoutBtn;

    /**
     * [생성자]
     * @param id : 로그인 성공 시 전달받은 사용자 아이디
     */
    public ManagerMainFrame(String id) {
        this.loginId = id; // 전달받은 아이디를 멤버 변수에 저장 (이후 DB 조회에 사용)

        // 1. 프레임(창) 기본 설정
        setTitle("明知 LMS - 원장 모드"); // 창 제목
        setSize(1200, 800);            // 창 크기 (가로 1200, 세로 800)
        setLocationRelativeTo(null);   // 창을 화면 정중앙에 배치
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창을 닫으면 프로그램 프로세스 종료
        setLayout(new BorderLayout()); // 전체 레이아웃: BorderLayout (북쪽, 중앙 등 구역 나눔)

        // 2. [북쪽 영역(North)] 로고와 개인정보를 담을 컨테이너 패널
        JPanel northContainer = new JPanel();
        // BoxLayout을 사용하여 패널들을 세로(Y축)로 쌓아 올림
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));

        // (2-1) 브랜드 패널 (최상단 로고 + 타이틀)
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15)); // 왼쪽 정렬, 여백 설정
        brandPanel.setBackground(COLOR_THEME); // 배경색 네이비

        // 로고 이미지 로드 (예외 처리 포함)
        JLabel logoBox;
        try {
            // 같은 패키지 내의 'image.jpg' 파일을 리소스로 불러옴
            ImageIcon icon = new ImageIcon(getClass().getResource("image.jpg"));
            // 이미지를 50x50 크기로 부드럽게(SCALE_SMOOTH) 리사이징
            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoBox = new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            // 이미지가 없거나 로드 실패 시, 텍스트로 대체하여 UI 깨짐 방지
            logoBox = new JLabel("LOGO", SwingConstants.CENTER);
            logoBox.setOpaque(true);
            logoBox.setBackground(Color.LIGHT_GRAY);
            logoBox.setForeground(Color.BLACK);
        }
        logoBox.setPreferredSize(new Dimension(50, 50)); // 로고 크기 고정

        JLabel titleLabel = new JLabel("明知 LMS (Learning Management System)");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE); // 글자색 흰색 (네이비 배경 위)

        brandPanel.add(logoBox);
        brandPanel.add(Box.createHorizontalStrut(15)); // 로고와 제목 사이 간격 띄우기
        brandPanel.add(titleLabel);

        // (2-2) 정보 패널 (원장 정보 + 버튼)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(COLOR_WHITE);
        // 테두리 설정: 하단에 얇은 선을 긋고, 안쪽으로 여백(Padding)을 줌
        infoPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(15, 30, 15, 30)
        ));

        // 정보를 2줄로 보여주기 위해 GridLayout(2행 1열) 사용
        JPanel dataGrid = new JPanel(new GridLayout(2, 1, 0, 8));
        dataGrid.setOpaque(false); // 배경 투명 (부모 패널 색상 따름)

        // 라벨 초기화 (DB 로딩 전에는 "Loading..." 표시)
        idLabel = createLabel("아이디", "Loading...");
        nameLabel = createLabel("이름", "Loading...");
        phoneLabel = createLabel("전화번호", "Loading...");
        emailLabel = createLabel("이메일", "Loading...");
        addrLabel = createLabel("주소", "Loading...");

        // 첫 번째 줄 (아이디, 이름, 전화번호)
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row1.setOpaque(false);
        row1.add(idLabel);
        row1.add(nameLabel);
        row1.add(phoneLabel);

        // 두 번째 줄 (이메일, 주소)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row2.setOpaque(false);
        row2.add(emailLabel);
        row2.add(addrLabel);

        dataGrid.add(row1);
        dataGrid.add(row2);

        // (2-3) 버튼 패널 (우측 상단)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        // 버튼 생성 및 리스너 연결
        editBtn = createStyledButton("개인정보 수정");
        editBtn.addActionListener(this); // 클릭 시 actionPerformed() 호출

        logoutBtn = createStyledButton("로그아웃");
        logoutBtn.addActionListener(this);

        btnPanel.add(editBtn);
        btnPanel.add(logoutBtn);

        // 정보 패널 조합 (중앙: 데이터, 우측: 버튼)
        infoPanel.add(dataGrid, BorderLayout.CENTER);
        infoPanel.add(btnPanel, BorderLayout.EAST);

        // 북쪽 컨테이너에 브랜드 패널과 정보 패널을 차례로 추가
        northContainer.add(brandPanel);
        northContainer.add(infoPanel);

        // 3. [중앙 영역(Center)] 탭 메뉴 구성
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        tabPane.setBackground(COLOR_BG);

        // ★ 핵심: 기능별로 분리된 Panel 클래스들을 탭에 추가하여 화면 구성
        // this(메인프레임)를 넘겨주는 이유는 팝업창 생성 시 부모 창으로 지정하기 위함
        tabPane.addTab("  강의 관리  ", new LecturePanel(this));
        tabPane.addTab("  학생 관리  ", new StudentPanel(this));
        tabPane.addTab("  강사 관리  ", new TeacherPanel(this)); // 친구가 만든 패널 통합

        // 최종적으로 프레임에 배치
        add(northContainer, BorderLayout.NORTH); // 상단 고정
        add(tabPane, BorderLayout.CENTER);       // 중앙 탭 (나머지 공간 채움)

        // 4. 화면 구성 완료 후 DB에서 원장 정보 로드
        loadMyInfo();

        setVisible(true); // 화면 표시
    }

    /**
     * [이벤트 핸들러] 버튼 클릭 시 호출되는 메서드
     * ActionListener 인터페이스의 구현부
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource(); // 어떤 버튼이 눌렸는지 확인

        if (source == editBtn) {
            // 1. 개인정보 수정 팝업 열기 (현재 ID 전달)
            MyInfoDialog dialog = new MyInfoDialog(this, "개인정보 수정", loginId);
            dialog.setVisible(true); // 모달 창이므로 닫힐 때까지 대기함

            // 2. 팝업이 닫히면(수정 후) 정보를 다시 불러와서 화면 갱신
            loadMyInfo();

        } else if (source == logoutBtn) {
            // 1. 로그아웃 확인 팝업
            int ans = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                // 2. 현재 창 닫기 (리소스 해제)
                dispose();

                // 3. 로그인 화면으로 돌아가기 (Login 클래스 호출)
                new Login().setVisible(true);
            }
        }
    }

    /**
     * [DB 연동] 로그인한 원장의 정보를 DB에서 조회하여 화면에 표시
     */
    private void loadMyInfo() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 1. JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 2. DB 연결 (URL, ID, PW)
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 3. SQL 실행 (내 ID로 회원 정보 조회)
            String sql = "SELECT * FROM member WHERE member_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId); // 바인딩 변수(?) 설정

            rs = pstmt.executeQuery();

            // 4. 결과 처리
            if (rs.next()) { // 데이터가 존재하면
                String id = rs.getString("member_id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String addr = rs.getString("address");

                // 5. 라벨 텍스트 업데이트 (HTML 포맷팅 적용)
                idLabel.setText(formatLabelText("아이디", id));
                nameLabel.setText(formatLabelText("이름", name));
                phoneLabel.setText(formatLabelText("전화번호", phone));
                emailLabel.setText(formatLabelText("이메일", email));
                addrLabel.setText(formatLabelText("주소", addr));
            }
        } catch (Exception e) {
            e.printStackTrace(); // 에러 발생 시 콘솔 출력
        } finally {
            // 6. 자원 해제 (필수)
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    // [Helper] 라벨 생성 메서드 (중복 코드 제거)
    private JLabel createLabel(String title, String value) {
        return new JLabel(formatLabelText(title, value));
    }

    // [Helper] HTML 텍스트 포맷팅 메서드 (제목은 회색, 값은 검고 진하게)
    private String formatLabelText(String title, String value) {
        return "<html><font color='#777777'>" + title + " : </font> <font size='4' color='black'><b>" + value + "</b></font></html>";
    }

    // [Helper] 버튼 생성 메서드 (스타일 통일)
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        btn.setFocusPainted(false); // 포커스 테두리 제거
        btn.setBorder(new EmptyBorder(5, 10, 5, 10)); // 테두리 대신 여백 사용
        btn.setPreferredSize(new Dimension(120, 35));

        // 마우스 호버(Mouse Over) 시 색상 변경 효과 추가
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(new Color(240, 240, 240)); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    /**
     * [Utility] 테이블 스타일 설정 메서드
     * static으로 선언하여 LecturePanel, StudentPanel 등 다른 파일에서도 공통으로 사용
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(30); // 행 높이
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(230, 240, 255)); // 선택 시 배경색 (연한 파랑)
        table.setSelectionForeground(Color.BLACK); // 선택 시 글자색 (검정 유지)
        table.setGridColor(new Color(230, 230, 230)); // 격자선 색상

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14)); // 헤더 폰트
        header.setBackground(new Color(245, 245, 250)); // 헤더 배경색
        header.setForeground(new Color(50, 50, 50));    // 헤더 글자색
        header.setPreferredSize(new Dimension(0, 35));

        // 셀 내용 가운데 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}