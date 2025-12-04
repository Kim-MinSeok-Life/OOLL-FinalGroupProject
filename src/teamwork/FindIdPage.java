package teamwork;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class FindIdPage extends JFrame {

    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 500;

    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 24);
    private final Font smallFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 16);
    private final Font resultFont = new Font("Malgun Gothic", Font.BOLD, 18);

    private JPanel cards; // CardLayout이 적용될 패널
    private CardLayout cardLayout;

    private JTextField emailField;
    private JComboBox<String> securityQuestionCombo;
    private JTextField answerField;

    public FindIdPage() {
        setTitle("아이디 찾기");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // 1단계: 입력 패널 추가
        cards.add(createInputPanel(), "INPUT");
        // 2단계: 결과 패널 추가 (초기에는 숨김)
        cards.add(createResultPanel("qwerqwer"), "RESULT"); // 예시 ID: qwerqwer

        add(cards);
        cardLayout.show(cards, "INPUT"); // 시작은 입력 화면부터

        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        int y = 0;

        JLabel titleLabel = new JLabel("아이디 찾기");
        titleLabel.setFont(titleFont);
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("본인확인을 통해 아이디를 찾으세요");
        subtitleLabel.setFont(smallFont);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);

        gbc.insets = new Insets(8, 0, 3, 0);
        JLabel emailLabel = new JLabel("이메일");
        gbc.gridy = y++;
        mainPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(emailField, gbc);

        // --- 3. 본인확인 질문 영역 ---
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

        JButton findIdButton = new JButton("아이디 찾기");
        findIdButton.setFont(buttonFont);
        findIdButton.setPreferredSize(new Dimension(300, 50));
        findIdButton.setBackground(Color.BLACK);
        findIdButton.setForeground(Color.WHITE);
        findIdButton.setFocusPainted(false);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(findIdButton, gbc);

        findIdButton.addActionListener(e -> attemptFindId());

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

    private JPanel createResultPanel(String foundId) {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridBagLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        int y = 0;

        JLabel titleLabel = new JLabel("아이디 찾기");
        titleLabel.setFont(titleFont);
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        resultPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("본인확인을 통해 아이디를 찾으세요");
        subtitleLabel.setFont(smallFont);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 30, 0);
        resultPanel.add(subtitleLabel, gbc);

        JPanel resultBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        resultBox.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        resultBox.setPreferredSize(new Dimension(300, 100));
        resultBox.setBackground(Color.WHITE);

        // 아이콘 (✔)
        JLabel checkIcon = new JLabel("⦿"); // 유니코드 원형 체크표시 대신 기호 사용
        checkIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        checkIcon.setForeground(Color.ORANGE);

        JLabel idText = new JLabel("<html>회원님의 아이디는<br><b>" + foundId + "</b><br>입니다.</html>");
        idText.setFont(resultFont);

        resultBox.add(checkIcon);
        resultBox.add(idText);

        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 50, 0);
        resultPanel.add(resultBox, gbc);

        JButton loginButton = new JButton("로그인하기");
        loginButton.setFont(buttonFont);
        loginButton.setPreferredSize(new Dimension(300, 50));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 0, 0);
        resultPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> backToLogin());

        return resultPanel;
    }

    private void attemptFindId() {
        String email = emailField.getText();
        String question = (String) securityQuestionCombo.getSelectedItem();
        String answer = answerField.getText();

        if (!email.isEmpty() && !answer.isEmpty() && securityQuestionCombo.getSelectedIndex() != 0) {
            String foundId = "qwerqwer"; //예시 DB에서 찾은 실제 ID

            cards.remove(1);
            cards.add(createResultPanel(foundId), "RESULT");

            cardLayout.show(cards, "RESULT");

        } else {
            JOptionPane.showMessageDialog(this, "모든 정보를 정확히 입력해주세요.", "인증 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToLogin() {
        new Login().setVisible(true); // Login.java의 클래스 이름 사용
        dispose();
    }

}