package teamwork;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Login extends JFrame {

    // 창 크기 상수
    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 550;

    // 폰트 설정
    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 24);
    private final Font smallFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 16);

    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public Login() {
        // 프레임
        setTitle("학원 관리 시스템 로그인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 40, 50, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        //제목
        JLabel titleLabel = new JLabel("로그인");
        titleLabel.setFont(titleFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("학원 관리 시스템에 로그인하세요");
        subtitleLabel.setFont(smallFont);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);

        //아이디 입력 영역
        gbc.insets = new Insets(8, 0, 3, 0);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel idLabel = new JLabel("아이디");
        gbc.gridy = 2;
        mainPanel.add(idLabel, gbc);

        idField = new JTextField(20);
        idField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(idField, gbc);

        //비밀번호 입력 영역
        JLabel passwordLabel = new JLabel("비밀번호");
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 0, 3, 0);
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(passwordField, gbc);

        // 로그인 버튼
        loginButton = new JButton("로그인");
        loginButton.setFont(buttonFont);
        loginButton.setPreferredSize(new Dimension(300, 50));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(loginButton, gbc);

        //회원가입 버튼
        JLabel signupLabel = new JLabel("회원가입");
        signupLabel.setFont(smallFont);
        signupLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(signupLabel, gbc);

        // 회원가입 레이블 클릭 시 회원가입 열기
        signupLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 로그인 창은 숨기거나 닫기
                setVisible(false);
                // 회원가입 창을 새로 띄움
                new SignupPage().setVisible(true);
            }
        });


        //아이디 찾기, 비밀번호 변경 영역
        JPanel bottomLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JLabel findIdLabel = new JLabel("아이디 찾기");
        findIdLabel.setFont(smallFont);

        JLabel separator = new JLabel("|");
        separator.setFont(smallFont);

        JLabel changePwLabel = new JLabel("비밀번호 변경");
        changePwLabel.setFont(smallFont);

        findIdLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePwLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bottomLinkPanel.add(findIdLabel);
        bottomLinkPanel.add(separator);
        bottomLinkPanel.add(changePwLabel);

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(bottomLinkPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 이벤트 리스너 추가 (로그인)
        loginButton.addActionListener(e -> {
            String id = idField.getText();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
            Arrays.fill(passwordChars, '0');

            JOptionPane.showMessageDialog(this,
                    "로그인 시도\nID: " + id + "\nPW: " + password,
                    "로그인 정보",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login();
        });
    }
}