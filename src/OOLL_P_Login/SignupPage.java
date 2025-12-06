package OOLL_P_Login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.regex.Pattern;
//import OOLL_P_Student.*;
//import OOLL_P_Teacher.*;
import OOLL_P_Manager.*;

public class SignupPage extends JFrame {

    // 창 크기 상수
    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 550;
    private static final int FIELD_WIDTH = 220;
    private static final int PANEL_PADDING = 30;

    // 폰트 설정
    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 24);
    private final Font smallFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 16);
    private final Font linkFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font fieldLabelFont = new Font("Malgun Gothic", Font.PLAIN, 12);

    // 컴포넌트 선언
    private JTextField idField, nameField, addressField, phoneField, emailField, answerField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> jobTitleCombo, securityQuestionCombo;

    private GridBagConstraints gbc;
    private JPanel mainPanel;
    private int y = 0;

    public SignupPage() {
        // 프레임 기본 설정
        setTitle("학원 관리 시스템 회원가입");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // [초기화] GBC와 메인 패널 초기화 (createFieldRow 사용 전 필수)
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;


        // --- 1. 제목 영역 ---
        JLabel titleLabel = new JLabel("회원가입");
        titleLabel.setFont(titleFont);
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("학원 관리 시스템에 가입하세요");
        subtitleLabel.setFont(fieldLabelFont);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(subtitleLabel, gbc);

        // --- 2. 입력 필드 그룹 배치 ---

        idField = new JTextField(20);
        createFieldRow("아이디", idField); // gbc, mainPanel 사용 시작

        passwordField = new JPasswordField(20);
        createFieldRow("비밀번호", passwordField);

        confirmPasswordField = new JPasswordField(20);
        createFieldRow("비밀번호 확인", confirmPasswordField);

        nameField = new JTextField(20);
        createFieldRow("이름", nameField);

        addressField = new JTextField(20);
        createFieldRow("주소", addressField);

        phoneField = new JTextField("010-0000-0000", 20);
        createFieldRow("전화번호", phoneField);

        emailField = new JTextField(20);
        createFieldRow("이메일", emailField);

        // --- 3. 직책 (콤보박스) ---
        mainPanel.add(createLabel("직책"), gbc, y++);
        String[] jobTitles = {"직책을 선택하세요", "원장", "강사", "학생"};
        jobTitleCombo = new JComboBox<>(jobTitles);
        jobTitleCombo.setPreferredSize(new Dimension(FIELD_WIDTH, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(jobTitleCombo, gbc);

        // --- 4. 보안 확인 질문 (콤보박스) ---
        mainPanel.add(createLabel("본인확인 질문"), gbc, y++);
        String[] questions = {"질문을 선택하세요", "어머니의 성함은?", "가장 좋아하는 음식은?", "출신 초등학교 이름은?"};
        securityQuestionCombo = new JComboBox<>(questions);
        securityQuestionCombo.setPreferredSize(new Dimension(FIELD_WIDTH, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(securityQuestionCombo, gbc);

        // 본인확인 답변
        answerField = new JTextField(20);
        createFieldRow("본인확인 답변", answerField);
        gbc.insets = new Insets(8, 0, 30, 0);


        // --- 5. 가입하기 버튼 ---
        JButton signupButton = new JButton("가입하기");
        signupButton.setFont(buttonFont);
        signupButton.setPreferredSize(new Dimension(FIELD_WIDTH, 50));
        signupButton.setBackground(Color.BLACK);
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(signupButton, gbc);

        // --- 6. 이미 계정이 있으신가요? 로그인 링크 ---
        JPanel loginLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        JLabel textLabel = new JLabel("이미 계정이 있으신가요?");
        JLabel loginLink = new JLabel("로그인");

        loginLinkPanel.add(textLabel);
        loginLinkPanel.add(loginLink);

        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(loginLinkPanel, gbc);

        scrollPane.setViewportView(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // 이벤트 리스너 추가
        signupButton.addActionListener(this::handleSignup);
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Login().setVisible(true);
                dispose();
            }
        });

        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(fieldLabelFont);
        return label;
    }

    private void createFieldRow(String labelText, JComponent field) {
        gbc.insets = new Insets(8, 0, 3, 0);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = y++;
        mainPanel.add(createLabel(labelText), gbc);

        field.setPreferredSize(new Dimension(FIELD_WIDTH, 40));
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(field, gbc);
    }

    /** 회원가입 예외 처리 및 DB 저장 로직 */
    private void handleSignup(ActionEvent e) {
        String id = idField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        char[] confirmPwChars = confirmPasswordField.getPassword();
        String password = new String(passwordChars);
        String confirmPw = new String(confirmPwChars);
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String question = (String) securityQuestionCombo.getSelectedItem();
        String answer = answerField.getText().trim();
        String role = (String) jobTitleCombo.getSelectedItem();

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 1. 필수 필드 및 형식 검사
            if (id.isEmpty() || password.isEmpty() || confirmPw.isEmpty() || name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty() || question.equals("질문을 선택하세요") || answer.isEmpty() || role.equals("직책을 선택하세요")) {
                throw new IllegalArgumentException("필수 정보를 모두 입력하고 직책을 선택해주세요.");
            }
            if (!password.equals(confirmPw)) {
                throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }

            // 비밀번호는 최소 8자 이상만 검사
            if (password.length() < 8) {
                throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
            }

            if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,4}$", email)) {
                throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
            }
            if (!Pattern.matches("^010-\\d{4}-\\d{4}$", phone)) {
                throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다. (예: 010-1234-5678)");
            }

            // 2. DB 연결 및 트랜잭션 설정
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            // 3. 아이디 중복 검사 (DB 조회)
            if (isIdDuplicate(conn, id)) {
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }

            // 4. 회원 정보 INSERT (member 테이블)
            String memberSql = "INSERT INTO member (member_id, password, name, address, phone, email, security_question, security_answer, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(memberSql);
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            pstmt.setString(3, name);
            pstmt.setString(4, address);
            pstmt.setString(5, phone);
            pstmt.setString(6, email);
            pstmt.setString(7, question);
            pstmt.setString(8, answer);
            pstmt.setString(9, role);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // 5. 역할별 추가 테이블 INSERT
                insertRoleSpecificData(conn, id, role);

                conn.commit(); // 트랜잭션 커밋

                JOptionPane.showMessageDialog(this,
                        role + " (" + id + ") 님, 가입이 완료되었습니다.",
                        "가입 완료",
                        JOptionPane.INFORMATION_MESSAGE);

                new Login().setVisible(true);
                dispose();
            } else {
                throw new SQLException("회원 가입에 실패했습니다. DB 오류.");
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
            if (conn != null) try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
        } catch (SQLException ex) {
            System.err.println("DB Error: " + ex.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            JOptionPane.showMessageDialog(this, "데이터베이스 오류가 발생했습니다. (" + ex.getMessage().split("\n")[0] + ")", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.err.println("Unexpected Error: " + ex.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            JOptionPane.showMessageDialog(this, "예상치 못한 오류가 발생했습니다.", "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBConnect.close(pstmt, conn);
            Arrays.fill(passwordChars, '0');
            Arrays.fill(confirmPwChars, '0');
        }
    }

    /** DB에서 ID 중복을 확인하는 메서드 */
    private boolean isIdDuplicate(Connection conn, String id) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT member_id FROM member WHERE member_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            return rs.next();
        } finally {
            DBConnect.close(rs, pstmt);
        }
    }

    /** 역할에 따라 강사, 학생, 원장 테이블에 추가 데이터를 삽입하는 메서드 */
    private void insertRoleSpecificData(Connection conn, String memberId, String role) throws SQLException {
        String sql = "";
        PreparedStatement pstmt = null;

        try {
            switch (role) {
                case "원장":
                    sql = "INSERT INTO manager (member_id) VALUES (?)";
                    break;
                case "강사":
                    sql = "INSERT INTO teacher (member_id, hourly_rate) VALUES (?, 30000)";
                    break;
                case "학생":
                    sql = "INSERT INTO student (member_id) VALUES (?)";
                    break;
                default:
                    return;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();

        } finally {
            DBConnect.close(pstmt);
        }
    }
}