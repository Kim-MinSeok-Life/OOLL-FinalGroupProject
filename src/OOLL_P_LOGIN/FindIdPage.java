//ID 찾기
package OOLL_P_LOGIN;

import javax.swing.*;
import java.awt.*;
import java.sql.*; // DB 관련 패키지 import


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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // 1단계: 입력 패널 추가
        cards.add(createInputPanel(), "INPUT");
        // 2단계: 결과 패널 추가
        cards.add(createResultPanel("ID_PLACEHOLDER"), "RESULT");

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

        // 아이콘 (⦿)
        JLabel checkIcon = new JLabel("⦿");
        checkIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        checkIcon.setForeground(Color.ORANGE);

        // [수정] ID가 표시될 레이블에 이름 부여
        JLabel idText = new JLabel("<html>회원님의 아이디는<br><b>" + foundId + "</b><br>입니다.</html>");
        idText.setFont(resultFont);
        idText.setName("foundIdLabel"); // 결과 ID를 업데이트하기 위한 이름

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

    /**아이디 찾기 시도 로직 */
    private void attemptFindId() {
        String email = emailField.getText().trim();
        String question = (String) securityQuestionCombo.getSelectedItem();
        String answer = answerField.getText().trim();

        if (email.isEmpty() || answer.isEmpty() || securityQuestionCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "모든 정보를 정확히 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String foundId = null;

        try {
            conn = DBConnect.getConnection();

            // member 테이블에서 이메일, 질문, 답변이 일치하는 ID를 조회
            String sql = "SELECT member_id FROM member WHERE email = ? AND security_question = ? AND security_answer = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, question);
            pstmt.setString(3, answer);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                foundId = rs.getString("member_id");
            }

            if (foundId != null) {
                // 성공 시 결과 화면으로 전환

                // 기존 결과 패널을 제거하고 새 ID로 새 패널을 추가한 후 전환
                cards.remove(1);
                cards.add(createResultPanel(foundId), "RESULT");

                cardLayout.show(cards, "RESULT");

            } else {
                JOptionPane.showMessageDialog(this, "일치하는 회원 정보가 없습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            System.err.println("DB Error during ID search: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "데이터베이스 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBConnect.close(rs, pstmt, conn);
        }
    }

    private void backToLogin() {
        new Login().setVisible(true); // Login.java의 클래스 이름 사용
        dispose();
    }
}