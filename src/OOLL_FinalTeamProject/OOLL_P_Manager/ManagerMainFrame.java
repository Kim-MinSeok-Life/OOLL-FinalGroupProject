// 여기는 원장의 메인 화면!!

package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManagerMainFrame extends JFrame implements ActionListener {

    final Color COLOR_THEME = new Color(30, 40, 70);
    final Color COLOR_BG = new Color(245, 245, 250);
    final Color COLOR_WHITE = Color.WHITE;
    final Font FONT_TITLE = new Font("맑은 고딕", Font.BOLD, 24);

    String loginId;
    JLabel idLabel, nameLabel, phoneLabel, emailLabel, addrLabel;

    JButton editBtn, logoutBtn;

    public ManagerMainFrame() {
        this("admin");
    }

    public ManagerMainFrame(String id) {
        this.loginId = id;

        setTitle("明知 LMS - 원장 모드");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // [북쪽] 브랜드 + 정보
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));

        // 1. 브랜드 패널
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
        brandPanel.setBackground(COLOR_THEME);

        // ★ [수정됨] 파일명: image.jpg 적용!
        JLabel logoBox;
        try {
            // 같은 패키지에 있는 image.jpg 파일을 불러옴
            ImageIcon icon = new ImageIcon(getClass().getResource("image.jpg"));
            // 이미지 크기를 50x50으로 부드럽게 조절
            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoBox = new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            // 이미지가 없거나 에러나면 글자로 대체
            System.out.println("이미지 로드 실패: " + e.getMessage());
            logoBox = new JLabel("LOGO", SwingConstants.CENTER);
            logoBox.setOpaque(true);
            logoBox.setBackground(Color.LIGHT_GRAY);
            logoBox.setForeground(Color.BLACK);
        }
        logoBox.setPreferredSize(new Dimension(50, 50));

        JLabel titleLabel = new JLabel("明知 LMS (Learning Management System)");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        brandPanel.add(logoBox);
        brandPanel.add(Box.createHorizontalStrut(15));
        brandPanel.add(titleLabel);

        // 2. 정보 패널
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(COLOR_WHITE);
        infoPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(15, 30, 15, 30)
        ));

        JPanel dataGrid = new JPanel(new GridLayout(2, 1, 0, 8));
        dataGrid.setOpaque(false);

        idLabel = createLabel("아이디", "Loading...");
        nameLabel = createLabel("이름", "Loading...");
        phoneLabel = createLabel("전화번호", "Loading...");
        emailLabel = createLabel("이메일", "Loading...");
        addrLabel = createLabel("주소", "Loading...");

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row1.setOpaque(false);
        row1.add(idLabel); row1.add(nameLabel); row1.add(phoneLabel);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row2.setOpaque(false);
        row2.add(emailLabel); row2.add(addrLabel);

        dataGrid.add(row1);
        dataGrid.add(row2);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        editBtn = createStyledButton("개인정보 수정");
        editBtn.addActionListener(this);

        logoutBtn = createStyledButton("로그아웃");
        logoutBtn.addActionListener(this);

        btnPanel.add(editBtn);
        btnPanel.add(logoutBtn);

        infoPanel.add(dataGrid, BorderLayout.CENTER);
        infoPanel.add(btnPanel, BorderLayout.EAST);

        northContainer.add(brandPanel);
        northContainer.add(infoPanel);

        // [중앙] 탭
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        tabPane.setBackground(COLOR_BG);

        tabPane.addTab("  강의 관리  ", new LecturePanel(this));
        tabPane.addTab("  학생 관리  ", new StudentPanel(this));

        // 능이가 만든 강사 관리 탭 추가!
        tabPane.addTab("  강사 관리  ", new TeacherPanel(this));

        add(northContainer, BorderLayout.NORTH);
        add(tabPane, BorderLayout.CENTER);

        loadMyInfo();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == editBtn) {
            MyInfoDialog dialog = new MyInfoDialog(this, "개인정보 수정", loginId);
            dialog.setVisible(true);
            loadMyInfo();

        } else if (source == logoutBtn) {
            int ans = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                dispose();
                // new Login().setVisible(true); // 친구 코드와 합칠 때 주석 해제
            }
        }
    }

    private void loadMyInfo() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            String sql = "SELECT * FROM member WHERE member_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                idLabel.setText(formatLabelText("아이디", rs.getString("member_id")));
                nameLabel.setText(formatLabelText("이름", rs.getString("name")));
                phoneLabel.setText(formatLabelText("전화번호", rs.getString("phone")));
                emailLabel.setText(formatLabelText("이메일", rs.getString("email")));
                addrLabel.setText(formatLabelText("주소", rs.getString("address")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    private JLabel createLabel(String title, String value) {
        return new JLabel(formatLabelText(title, value));
    }

    private String formatLabelText(String title, String value) {
        return "<html><font color='#777777'>" + title + " : </font> <font size='4' color='black'><b>" + value + "</b></font></html>";
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        btn.setPreferredSize(new Dimension(120, 35));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(new Color(240, 240, 240)); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 250));
        header.setForeground(new Color(50, 50, 50));
        header.setPreferredSize(new Dimension(0, 35));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new ManagerMainFrame("admin");
    }
}