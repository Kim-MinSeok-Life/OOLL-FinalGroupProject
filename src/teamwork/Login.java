package teamwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class Login extends JFrame {

    // 창 크기 상수
    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 550;
    private static final int FIELD_WIDTH = 220;

    // 폰트 설정
    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 24);
    private final Font smallFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 16);

    // 컴포넌트 선언
    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public Login() {
        // 프레임 기본 설정
        setTitle("학원 관리 시스템 로그인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null); // 화면 중앙 배치
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30)); // 내부 여백

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // --- 1. 제목 영역 ---
        JLabel titleLabel = new JLabel("로그인");
        titleLabel.setFont(titleFont);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("학원 관리 시스템에 로그인하세요");
        subtitleLabel.setFont(smallFont);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);

        // --- 2. 아이디 입력 영역 ---
        gbc.insets = new Insets(8, 0, 3, 0); gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel idLabel = new JLabel("아이디");
        gbc.gridy = 2;
        mainPanel.add(idLabel, gbc);

        idField = new JTextField(20);
        idField.setPreferredSize(new Dimension(FIELD_WIDTH, 40));
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(idField, gbc);

        // --- 3. 비밀번호 입력 영역 ---
        JLabel passwordLabel = new JLabel("비밀번호");
        gbc.gridy = 4; gbc.insets = new Insets(8, 0, 3, 0);
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(FIELD_WIDTH, 40));
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(passwordField, gbc);

        // --- 4. 로그인 버튼 ---
        loginButton = new JButton("로그인");
        loginButton.setFont(buttonFont);
        loginButton.setPreferredSize(new Dimension(FIELD_WIDTH, 50));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(loginButton, gbc);

        // --- 5. 회원가입 버튼/링크 ---
        JLabel signupLabel = new JLabel("회원가입");
        signupLabel.setFont(smallFont);
        signupLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(signupLabel, gbc);


        signupLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                new SignupPage().setVisible(true);
            }
        });

        JPanel bottomLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JLabel findIdLabel = new JLabel("아이디 찾기");
        findIdLabel.setFont(smallFont);

        JLabel separator = new JLabel("|");
        separator.setFont(smallFont);

        JLabel changePwLabel = new JLabel("비밀번호 변경");
        changePwLabel.setFont(smallFont);

        // 아이디 찾기 링크 이벤트: FindIdPage 열기
        findIdLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findIdLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                new FindIdPage().setVisible(true);
            }
        });

        // 비밀번호 변경 링크 이벤트: ChangePwPage 열기
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

        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(bottomLinkPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 이벤트 리스너 (로그인 버튼)
        loginButton.addActionListener(e -> {
            String id = idField.getText();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);

            // 보안 조치: 비밀번호 배열을 덮어쓰기
            Arrays.fill(passwordChars, '0');

            JOptionPane.showMessageDialog(this,
                    "로그인 시도\nID: " + id + "\nPW: " + password,
                    "로그인 정보",
                    JOptionPane.INFORMATION_MESSAGE);

            // TODO: 실제 DB 연결 및 인증 로직 구현 후 MainPage.java 등으로 전환
            // new TeachMain().setVisible(true);
            // dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login();
        });
    }
}