package teamwork;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SignupPage extends JFrame {

    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 550;
    private static final int FIELD_WIDTH = 220; // 컴포넌트 너비 조정

    // 폰트 설정
    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 24);
    private final Font fieldLabelFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 16);
    private final Font linkFont = new Font("Malgun Gothic", Font.PLAIN, 12);

    private JTextField idField, nameField, addressField, phoneField, emailField, answerField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> jobTitleCombo, securityQuestionCombo;
    private JButton signupButton;

    private GridBagConstraints gbc;
    private JPanel mainPanel;
    private int y = 0;

    public SignupPage() {
        // 프레임 기본 설정
        setTitle("학원 관리 시스템 회원가입");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT); // 창 크기 고정
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        // 내부 여백 조정
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        //제목
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

        //입력 필드
        // 아이디
        idField = new JTextField(20);
        createFieldRow("아이디", idField);

        // 비밀번호
        passwordField = new JPasswordField(20);
        createFieldRow("비밀번호", passwordField);

        // 비밀번호 확인
        confirmPasswordField = new JPasswordField(20);
        createFieldRow("비밀번호 확인", confirmPasswordField);

        // 이름
        nameField = new JTextField(20);
        createFieldRow("이름", nameField);

        // 주소
        addressField = new JTextField(20);
        createFieldRow("주소", addressField);

        // 전화번호
        phoneField = new JTextField("010-0000-0000", 20);
        createFieldRow("전화번호", phoneField);

        // 이메일
        emailField = new JTextField(20);
        createFieldRow("이메일", emailField);

        //직책 (콤보박스)
        mainPanel.add(createLabel("직책"), gbc, y++);
        String[] jobTitles = {"직책을 선택하세요", "원장", "강사","학생"};
        jobTitleCombo = new JComboBox<>(jobTitles);
        jobTitleCombo.setPreferredSize(new Dimension(FIELD_WIDTH, 40)); // 너비 조정
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(jobTitleCombo, gbc);

        //본인 확인 질문 (콤보박스)
        mainPanel.add(createLabel("본인확인 질문"), gbc, y++);
        String[] questions = {"질문을 선택하세요", "어머니의 성함은?", "가장 좋아하는 음식은?", "출신 초등학교 이름은?"};
        securityQuestionCombo = new JComboBox<>(questions);
        securityQuestionCombo.setPreferredSize(new Dimension(FIELD_WIDTH, 40)); // 너비 조정
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(securityQuestionCombo, gbc);

        // 본인확인 답변
        answerField = new JTextField(20);
        createFieldRow("본인확인 답변", answerField);
        gbc.insets = new Insets(8, 0, 30, 0);


        //가입 버튼
        signupButton = new JButton("가입하기");
        signupButton.setFont(buttonFont);
        signupButton.setPreferredSize(new Dimension(FIELD_WIDTH, 50)); // 너비 조정
        signupButton.setBackground(Color.BLACK);
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(signupButton, gbc);

        //이미 계정이 있으신가요?
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

        //이벤트 리스너
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

        field.setPreferredSize(new Dimension(FIELD_WIDTH, 40)); // 너비 조정
        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(field, gbc);
    }

    private void handleSignup(ActionEvent e) {
        String password = new String(passwordField.getPassword());
        String confirmPw = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPw)) {
            JOptionPane.showMessageDialog(this, "비밀번호와 비밀번호 확인이 일치하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "회원가입 완료",
                "가입 완료",
                JOptionPane.INFORMATION_MESSAGE);

        Arrays.fill(passwordField.getPassword(), '0');
        Arrays.fill(confirmPasswordField.getPassword(), '0');

        new Login().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SignupPage();
        });
    }
}