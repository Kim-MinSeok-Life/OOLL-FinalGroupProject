package OOLL_P_Login;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import OOLL_P_Manager.*;
// import OOLL_P_Student.*; // 현재 주석 처리됨
// import OOLL_P_Teacher.*; // 현재 주석 처리됨

public class Login extends JFrame {

    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 550;
    private static final int FIELD_WIDTH = 220;

    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 24);
    private final Font smallFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 16);

    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public Login() {
        // 프레임 기본 설정
        setTitle("학원 관리 시스템 로그인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 40, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // --- 로고 삽입 ---
        JLabel logoLabel = createLogoLabel("/OOLL_P_Login/logo.jpg");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(logoLabel, gbc);

        // --- 1. 제목 영역 ---
        JLabel titleLabel = new JLabel("로그인");
        titleLabel.setFont(titleFont);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("학원 관리 시스템에 로그인하세요");
        subtitleLabel.setFont(smallFont);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);

        // --- 2. 아이디 입력 영역 ---
        gbc.insets = new Insets(8, 0, 3, 0); gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel idLabel = new JLabel("아이디");
        gbc.gridy = 3;
        mainPanel.add(idLabel, gbc);

        idField = new JTextField(20);
        idField.setPreferredSize(new Dimension(FIELD_WIDTH, 40));
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(idField, gbc);

        // --- 3. 비밀번호 입력 영역 ---
        JLabel passwordLabel = new JLabel("비밀번호");
        gbc.gridy = 5; gbc.insets = new Insets(8, 0, 3, 0);
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(FIELD_WIDTH, 40));
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(passwordField, gbc);

        // --- 4. 로그인 버튼 ---
        loginButton = new JButton("로그인");
        loginButton.setFont(buttonFont);
        loginButton.setPreferredSize(new Dimension(FIELD_WIDTH, 50));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(loginButton, gbc);

        // --- 5. 회원가입 버튼/링크 ---
        JLabel signupLabel = new JLabel("회원가입");
        signupLabel.setFont(smallFont);
        signupLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(signupLabel, gbc);

        signupLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                new SignupPage().setVisible(true);
            }
        });

        // --- 6. 아이디 찾기 / 비밀번호 변경 영역 ---
        JPanel bottomLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JLabel findIdLabel = new JLabel("아이디 찾기");
        findIdLabel.setFont(smallFont);

        JLabel separator = new JLabel("|");
        separator.setFont(smallFont);

        JLabel changePwLabel = new JLabel("비밀번호 변경");
        changePwLabel.setFont(smallFont);

        findIdLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findIdLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                 new FindIdPage().setVisible(true);
            }
        });

        changePwLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePwLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                new ChangePwPage().setVisible(true);
            }
        });

        bottomLinkPanel.add(findIdLabel);
        bottomLinkPanel.add(separator);
        bottomLinkPanel.add(changePwLabel);

        gbc.gridy = 9; gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(bottomLinkPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // [DB 통합] 로그인 인증 로직
        loginButton.addActionListener(e -> attemptLogin());

        setVisible(true);
    }
    private JLabel createLogoLabel(String path) {
        try {
            // **[수정된 부분]** new File 대신 getClass().getResource()를 사용하여 리소스를 로드합니다.
            java.net.URL imageUrl = getClass().getResource(path);

            if (imageUrl == null) {
                // 이미지를 찾지 못했을 경우 예외 발생
                throw new IOException("Resource not found: " + path);
            }

            Image image = ImageIO.read(imageUrl);
            Image resizedImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(resizedImage));

        } catch (IOException e) {
            System.err.println("로고 파일을 로드하는 중 오류 발생 (파일 위치 확인): " + e.getMessage());
            // 로고 로드 실패 시 대체 텍스트 표시
            JLabel label = new JLabel("[LOGO]");
            label.setPreferredSize(new Dimension(50, 50));
            return label;
        }
    }

    private void attemptLogin() {
        String id = idField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            if (id.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력해주세요.", "인증 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            conn = DBConnect.getConnection();

            /*// 임시 DB 연결 (DBConnect 클래스가 없을 경우를 대비)
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");
            */

            String sql = "SELECT name, role FROM member WHERE member_id = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, password);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                String userName = rs.getString("name");
                String userRole = rs.getString("role");

                JOptionPane.showMessageDialog(this,
                        userName + " (" + userRole + ") 님, 환영합니다!",
                        "로그인 성공",
                        JOptionPane.INFORMATION_MESSAGE);

                switch (userRole) {
                    case "원장":
                        new ManagerMainFrame(id).setVisible(true);
                        break;
                    case "강사":
                        // new TeacherMain(id).setVisible(true);
                        break;
                    case "학생":
                        // new StudentFrame(id).setVisible(true);
                        break;
                }

                dispose();

            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 일치하지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            System.err.println("Login DB Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "데이터베이스 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.err.println("Unexpected Login Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "예상치 못한 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } finally {
            // DBConnect 클래스의 close 메서드를 사용할 수 없는 경우를 대비하여 직접 닫기
            try {
                if(rs!=null) rs.close();
                if(pstmt!=null) pstmt.close();
                if(conn!=null) conn.close();
            } catch(Exception ex) {}
            DBConnect.close(rs, pstmt, conn);
            Arrays.fill(passwordChars, '0');
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login();
        });
    }
}