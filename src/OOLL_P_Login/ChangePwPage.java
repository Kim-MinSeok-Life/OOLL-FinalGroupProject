//비번받기
package OOLL_P_Login;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.sql.*; // DB 관련 패키지 import
import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Manager.*;


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

    // DB 업데이트 시 사용하기 위해, 인증 성공한 ID를 저장할 필드
    private String authenticatedId;

    public ChangePwPage() {
        // 프레임 기본 설정
        setTitle("비밀번호 변경");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    /** 1단계: DB를 통한 본인 인증 로직 */
    private void attemptAuthentication() {
        String id = idField.getText().trim();
        String question = (String) securityQuestionCombo.getSelectedItem();
        String answer = answerField.getText().trim();

        if (id.isEmpty() || answer.isEmpty() || securityQuestionCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "모든 정보를 정확히 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnect.getConnection();

            // member 테이블에서 ID, 질문, 답변이 일치하는 레코드를 조회
            String sql = "SELECT member_id FROM member WHERE member_id = ? AND security_question = ? AND security_answer = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, question);
            pstmt.setString(3, answer);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 인증 성공
                authenticatedId = id; // 인증된 ID 저장
                cardLayout.show(cards, "CHANGE"); // 2단계로 이동
            } else {
                JOptionPane.showMessageDialog(this, "인증 정보가 일치하지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            System.err.println("DB Error during authentication: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "데이터베이스 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBConnect.close(rs, pstmt, conn);
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

    /** 2단계: DB를 통한 비밀번호 업데이트 로직 */
    private void attemptPasswordChange() {
        char[] newPwChars = newPasswordField.getPassword();
        char[] confirmPwChars = confirmPasswordField.getPassword();
        String newPw = new String(newPwChars);
        String confirmPw = new String(confirmPwChars);

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            if (authenticatedId == null || authenticatedId.isEmpty()) {
                throw new IllegalStateException("인증된 사용자 ID가 없습니다. 1단계 인증을 다시 해주세요.");
            }
            if (newPw.isEmpty() || confirmPw.isEmpty()) {
                throw new IllegalArgumentException("새 비밀번호를 모두 입력해주세요.");
            }
            if (!newPw.equals(confirmPw)) {
                throw new IllegalArgumentException("새 비밀번호와 확인이 일치하지 않습니다.");
            }

            //비밀번호 길이 제약 (8자 이상)
            if (newPw.length() < 8) {
                throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
            }

            // DB 연결 및 업데이트
            conn = DBConnect.getConnection();
            String sql = "UPDATE member SET password = ? WHERE member_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPw);
            pstmt.setString(2, authenticatedId);

            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.", "변경 완료", JOptionPane.INFORMATION_MESSAGE);
                backToLogin();
            } else {
                throw new SQLException("비밀번호 변경에 실패했습니다. (DB에 사용자가 없음)");
            }

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            System.err.println("DB Error during password update: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "데이터베이스 오류로 비밀번호 변경에 실패했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBConnect.close(pstmt, conn);
            Arrays.fill(newPwChars, '0');
            Arrays.fill(confirmPwChars, '0');
        }
    }

    private void backToLogin() {
        new Login().setVisible(true); // Login.java의 클래스 이름 사용
        dispose();
    }
}