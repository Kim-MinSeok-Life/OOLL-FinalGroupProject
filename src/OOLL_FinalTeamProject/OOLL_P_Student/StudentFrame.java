// 학생 - 메인 프레임(상단 로고, 로그아웃 포함)
package OOLL_P_Student; // 패키지 선언

// import 선언
import javax.swing.*; // swing GUI(Graphical User Interface) 컴포넌트
import java.awt.*; // GUI를 위한 컴포넌트(Layout, Color 등 관련)

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

public class StudentFrame extends JFrame {
    private String memberId; // 로그인한 학생 아이디
    private JLabel lblUserName; // 우측 상단에 표시될 로그인한 회원명 라벨(로그아웃 버튼 왼쪽 옆 배치)

    // 생성자(로그인된 학생 아이디를 받아 학생 메인 화면 구성
    public StudentFrame(String memberId) {
        this.memberId = memberId; // 현재 로그인한 학생 아이디
        
        // 기본 프레임 설정
        setTitle("학생 페이지 - " + memberId);
        setSize(1200, 800);
        setLocation(300, 100); // 화면 시작 위치
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // 전체 레이아웃(상단&중앙)

        // 상단 패널
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(214, 230, 255)); // 배경색(연한 파란색) 지정
        top.setPreferredSize(new Dimension(1200, 60)); // 높이 설정

        // 로고 이미지 불러오기
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/logo.png"));
        // System.out.println(getClass().getResource("/img/logo.png")); // 이미지 경로 확인용 코드
        
        // 로고 이미지 크기 조정
        Image img = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        JLabel logoImgLabel = new JLabel(new ImageIcon(img));

        // 로고 텍스트 라벨
        JLabel logoText = new JLabel(" 明知 LMS (Learning Management System)");
        logoText.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        // 왼쪽 영역(이미지, 텍스트 라벨) 묶는 패널
        JPanel leftLogoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftLogoPanel.setOpaque(false); // 배경 투명 지정
        leftLogoPanel.add(logoImgLabel);
        leftLogoPanel.add(logoText);

        // top 패널의 왼쪽에 추가
        top.add(leftLogoPanel, BorderLayout.WEST);

        // 오른쪽 영역(로그인한 사용자명, 로그아웃)
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false); // 배경 투명 지정

        lblUserName = new JLabel(memberId + " 님"); // 로그인한 학생 아이디 표시

        // 로그아웃 버튼 생성
        JButton btnLogout = new JButton("로그아웃");
        btnLogout.addActionListener(e -> { // 로그아웃 버튼 클릭 시 이벤트 처리
        	// 로그아웃 확인 창 표시
            int result = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) { // 예(Yes) 클릭 시 로그인 화면으로 이동
                SwingUtilities.invokeLater(() -> new LoginFrame()); // 로그인 화면으로 이동
                dispose(); // 현재 학생 화면 닫기
            }
        });

        // 우측 헤더 패널에 요소 추가
        rightHeader.add(lblUserName);
        rightHeader.add(btnLogout);

        // top 패널의 오른쪽에 배치
        top.add(rightHeader, BorderLayout.EAST);

        // top(Header) 패널을 전체 프레임의 NORTH에 추가
        add(top, BorderLayout.NORTH);

        // 중앙 패널(StudentPanel) 추가
        StudentPanel panel = new StudentPanel(memberId);
        add(panel, BorderLayout.CENTER); // 중앙에 학생 기능 패널 추가
    }
    
}
