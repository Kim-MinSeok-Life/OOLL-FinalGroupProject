//package teamPro;
package OOLL_FinalTeamProject;

import javax.swing.*;
import java.awt.*;

public class StudentFrame extends JFrame {
    private String memberId;
    private JLabel lblUserName;

    public StudentFrame(String memberId) {
        this.memberId = memberId;
        setTitle("학생 페이지 - " + memberId);
        setSize(1200, 800);
        setLocation(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 패널
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(214, 230, 255));
        top.setPreferredSize(new Dimension(1200, 60));

        // 로고 이미지 넣기
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/logo.png"));
        // System.out.println(getClass().getResource("/images/logo.png")); // 이미지 확인용 코드 
        Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        JLabel logoImgLabel = new JLabel(new ImageIcon(img));

        // 로고 텍스트
        JLabel logoText = new JLabel(" 明知 LMS (Learning Management System)");
        logoText.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        // 왼쪽 로고 묶음
        JPanel leftLogoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftLogoPanel.setOpaque(false);
        leftLogoPanel.add(logoImgLabel);
        leftLogoPanel.add(logoText);

        top.add(leftLogoPanel, BorderLayout.WEST);

        // 오른쪽: 사용자명 + 로그아웃
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false);

        lblUserName = new JLabel(memberId + " 님");

        JButton btnLogout = new JButton("로그아웃");
        btnLogout.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(() -> new LoginFrame());
                dispose();
            }
        });

        rightHeader.add(lblUserName);
        rightHeader.add(btnLogout);

        top.add(rightHeader, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // 중앙 StudentPanel 추가
        StudentPanel panel = new StudentPanel(memberId);
        add(panel, BorderLayout.CENTER);
    }
}
