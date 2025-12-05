//로그인
package teamwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
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
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

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

        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(bottomLinkPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // [DB 통합] 로그인 인증 로직
        loginButton.addActionListener(e -> attemptLogin());

        setVisible(true);
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

            // member 테이블에서 role과 name을 조회
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
                    case "강사":
                        // 성공적으로 로그인된 ID를 TeachManage 생성자에 전달합니다.
                        new TeacherManage(id).setVisible(true);
                        break;
                    case "학생":
                        JOptionPane.showMessageDialog(this, "학생 페이지는 준비 중입니다.", "안내", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "알 수 없는 사용자 역할입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                        return; // 이동 실패 시 창을 닫지 않음
                }

                dispose(); // 로그인 성공 및 이동 완료 후 현재 창 닫기

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
            DBConnect.close(rs, pstmt, conn);
            Arrays.fill(passwordChars, '0');
        }
    }

    // UI 헬퍼 메소드 (생략)
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(smallFont);
        return label;
    }

    private void createFieldRow(String labelText, JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        // ... (GBC 설정 로직)
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login();
        });
    }
}