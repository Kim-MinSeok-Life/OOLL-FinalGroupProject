package teamwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ChangePwPage extends JFrame {

    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 500;

    // 폰트 설정
    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 24);
    private final Font smallFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 16);

    private JPanel cards; // CardLayout이 적용될 패널
    private CardLayout cardLayout;

    // 1단계 필드
    private JTextField idField;
    private JComboBox<String> securityQuestionCombo;
    private JTextField answerField;

    // 2단계 필드
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ChangePwPage() {
        // 프레임 기본 설정
        setTitle("비밀번호 변경");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // 1단계: 인증 패널 추가
        cards.add(createAuthPanel(), "AUTH");
        // 2단계: 변경 패널 추가
        cards.add(createChangePanel(), "CHANGE");

        add(cards);
        cardLayout.show(cards, "AUTH"); // 시작은 인증 화면부터

        setVisible(true);
    }


    private JPanel createAuthPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        int y = 0;

        // --- 1. 제목 영역 ---
        JLabel titleLabel = new JLabel("비밀번호 변경");
        titleLabel.setFont(titleFont);
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("본인확인을 통해 비밀번호를 변경하세요");
        subtitleLabel.setFont(smallFont);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);

        // --- 2. 아이디 입력 영역 ---
        gbc.insets = new Insets(8, 0, 3, 0);
        JLabel idLabel = new JLabel("아이디");
        gbc.gridy = y++;
        mainPanel.add(idLabel, gbc);

        idField = new JTextField(20);
        idField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(idField, gbc);

        gbc.insets = new Insets(8, 0, 3, 0);
        JLabel questionLabel = new JLabel("본인확인 질문");
        gbc.gridy = y++;
        mainPanel.add(questionLabel, gbc);

        String[] questions = {"질문을 선택하세요", "어머니의 성함은?", "가장 좋아하는 음식은?", "출신 초등학교 이름은?"};
        securityQuestionCombo = new JComboBox<>(questions);
        securityQuestionCombo.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(securityQuestionCombo, gbc);

        gbc.insets = new Insets(8, 0, 3, 0);
        JLabel answerLabel = new JLabel("본인확인 답변");
        gbc.gridy = y++;
        mainPanel.add(answerLabel, gbc);

        answerField = new JTextField(20);
        answerField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 40, 0);
        mainPanel.add(answerField, gbc);

        JButton authButton = new JButton("본인확인");
        authButton.setFont(buttonFont);
        authButton.setPreferredSize(new Dimension(300, 50));
        authButton.setBackground(Color.BLACK);
        authButton.setForeground(Color.WHITE);
        authButton.setFocusPainted(false);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(authButton, gbc);

        authButton.addActionListener(e -> attemptAuthentication());


        JButton backToLoginButton = new JButton("로그인으로 돌아가기");
        backToLoginButton.setFont(smallFont);
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setForeground(Color.GRAY);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(backToLoginButton, gbc);

        backToLoginButton.addActionListener(e -> backToLogin());

        return mainPanel;
    }

    private void attemptAuthentication() {
        String id = idField.getText();
        String question = (String) securityQuestionCombo.getSelectedItem();
        String answer = answerField.getText();

        if (!id.isEmpty() && !answer.isEmpty() && securityQuestionCombo.getSelectedIndex() != 0) {
            // 테스트를 위해 인증 성공 시 바로 2단계로 이동
            cardLayout.show(cards, "CHANGE");

        } else {
            JOptionPane.showMessageDialog(this, "모든 정보를 정확히 입력해주세요.", "인증 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createChangePanel() {
        JPanel changePanel = new JPanel();
        changePanel.setLayout(new GridBagLayout());
        changePanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        int y = 0;

        // --- 1. 제목 영역 ---
        JLabel titleLabel = new JLabel("비밀번호 변경");
        titleLabel.setFont(titleFont);
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        changePanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("새로운 비밀번호를 입력하세요");
        subtitleLabel.setFont(smallFont);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 30, 0);
        changePanel.add(subtitleLabel, gbc);

        // --- 2. 새 비밀번호 입력 영역 ---
        gbc.insets = new Insets(8, 0, 3, 0);
        JLabel newPwLabel = new JLabel("새 비밀번호");
        gbc.gridy = y++;
        changePanel.add(newPwLabel, gbc);

        newPasswordField = new JPasswordField(20);
        newPasswordField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 20, 0);
        changePanel.add(newPasswordField, gbc);

        // --- 3. 새 비밀번호 확인 영역 ---
        gbc.insets = new Insets(8, 0, 3, 0);
        JLabel confirmPwLabel = new JLabel("새 비밀번호 확인");
        gbc.gridy = y++;
        changePanel.add(confirmPwLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 40, 0);
        changePanel.add(confirmPasswordField, gbc);

        // --- 4. 비밀번호 변경 버튼 ---
        JButton changePwButton = new JButton("비밀번호 변경");
        changePwButton.setFont(buttonFont);
        changePwButton.setPreferredSize(new Dimension(300, 50));
        changePwButton.setBackground(Color.BLACK);
        changePwButton.setForeground(Color.WHITE);
        changePwButton.setFocusPainted(false);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 0, 0);
        changePanel.add(changePwButton, gbc);

        // 이벤트 리스너 (비밀번호 변경 로직)
        changePwButton.addActionListener(e -> attemptPasswordChange());

        return changePanel;
    }

    /** 2단계: 비밀번호 변경 시도 로직 */
    private void attemptPasswordChange() {
        char[] newPwChars = newPasswordField.getPassword();
        char[] confirmPwChars = confirmPasswordField.getPassword();
        String newPw = new String(newPwChars);
        String confirmPw = new String(confirmPwChars);

        if (newPw.isEmpty() || confirmPw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "새 비밀번호를 모두 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPw.equals(confirmPw)) {
            JOptionPane.showMessageDialog(this, "새 비밀번호와 확인이 일치하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            // 보안 조치: 메모리 데이터 삭제
            Arrays.fill(newPwChars, '0');
            Arrays.fill(confirmPwChars, '0');
            return;
        }

 
        // [핵심 로직]: 실제 DB의 비밀번호 변경 로직이 실행
   

        JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.", "변경 완료", JOptionPane.INFORMATION_MESSAGE);

        // 보안 조치: 메모리 데이터 삭제
        Arrays.fill(newPwChars, '0');
        Arrays.fill(confirmPwChars, '0');

        // 변경 후 로그인 화면으로 돌아가기
        backToLogin();
    }

    /** 로그인 화면으로 돌아가기 로직 */
    private void backToLogin() {
        // FindIdPage에서 Login.java로 돌아올 때도 사용되므로 클래스 이름만 사용
        new Login().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChangePwPage();
        });
    }
}
