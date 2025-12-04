package teamwork;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

public class SignupPage extends JFrame {

    // 창 크기 상수 (Login.java와 통일)
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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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
        createFieldRow("아이디", idField);

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
        // [수정] 직책 목록 통일
        String[] jobTitles = {"직책을 선택하세요", "원장", "강사", "학생"};
        JComboBox<String> jobTitleCombo = new JComboBox<>(jobTitles);
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
        textLabel.setFont(linkFont);

        JLabel loginLink = new JLabel("로그인");
        loginLink.setFont(linkFont);
        loginLink.setForeground(Color.BLUE);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

    /**회원가입 예외 처리 로직 */
    private void handleSignup(ActionEvent e) {
        String id = idField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        char[] confirmPwChars = confirmPasswordField.getPassword();
        String password = new String(passwordChars);
        String confirmPw = new String(confirmPwChars);
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        try {
            // 1. 필수 필드 누락 검사
            if (id.isEmpty() || password.isEmpty() || confirmPw.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                throw new IllegalArgumentException("필수 정보를 모두 입력해주세요.");
            }

            // 2. 비밀번호 일치 검사
            if (!password.equals(confirmPw)) {
                throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }

            // 3. 비밀번호 형식 및 길이 검사 (최소 8자, 영문/숫자/특수문자 포함)
            if (password.length() < 8 || !Pattern.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,}$", password)) {
                throw new IllegalArgumentException("비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.");
            }

            // 4. 아이디 중복 검사 (DB 연동 시 시스템 예외 처리)
            if (isIdDuplicate(id)) {
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }

            // 5. 이메일 형식 및 중복 검사
            if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,4}$", email)) {
                throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
            }
            if (isEmailDuplicate(email)) {
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }

            // 6. 전화번호 형식 검사
            if (!Pattern.matches("^010-\\d{4}-\\d{4}$", phone)) {
                throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다. (예: 010-1234-5678)");
            }


            // --- 모든 검증 통과 시 ---

            // [DB 저장 로직 실행]

            JOptionPane.showMessageDialog(this,
                    "회원가입 요청이 접수되었습니다.\nID: " + id,
                    "가입 완료",
                    JOptionPane.INFORMATION_MESSAGE);

            // 가입 완료 후 로그인 페이지로 이동
            new Login().setVisible(true);
            dispose();

        } catch (IllegalArgumentException ex) {
            // 사용자 입력 오류 (유효성 검사 실패)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            // 시스템 오류 (DB 연결 실패 등)
            JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage(), "시스템 오류", JOptionPane.ERROR_MESSAGE);
        } finally {
            // 비밀번호 정보는 항상 메모리에서 삭제 (보안 조치)
            Arrays.fill(passwordChars, '0');
            Arrays.fill(confirmPwChars, '0');
        }
    }

    //ID 중복 확인 (실제로는 DB 조회 필요)
    private boolean isIdDuplicate(String id) {
        return id.equalsIgnoreCase("testuser");
    }

    //이메일 중복 확인 (실제로는 DB 조회 필요)
    private boolean isEmailDuplicate(String email) {
        return email.equalsIgnoreCase("test@example.com");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SignupPage();
        });
    }
}