package teamwork;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TeachMain extends JFrame {

    private static final int FRAME_WIDTH = 850;
    private static final int FRAME_HEIGHT = 700;

    // 폰트 설정
    private final Font titleFont = new Font("Malgun Gothic", Font.BOLD, 20);
    private final Font sectionTitleFont = new Font("Malgun Gothic", Font.BOLD, 16);
    private final Font tabFont = new Font("Malgun Gothic", Font.BOLD, 14);
    private final Font dataFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font addButtonFont = new Font("Malgun Gothic", Font.BOLD, 14);

    // [필드] 테이블 모델
    private DefaultTableModel teacherTableModel;
    private final int PRICE_COLUMN_INDEX = 5;

    // [필드] 원장 정보
    private String managerId = "qwerqwer";
    private String managerName = "남궁현";
    private String managerJob = "원장";
    private String managerPhone = "01012364567";
    private String managerEmail = "qwer1234@naver.com";
    private String managerAddress = "경기도 수원";
    private String managerJoinDate = "2025. 11. 8.";


    public TeachMain() {
        // 프레임 기본 설정
        setTitle("학원 관리 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. 상단 헤더 (로그아웃 버튼 포함) ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. 메인 콘텐츠 패널 ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // 수직 배치
        contentPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        // 2-1. 단가 공통 관리 버튼
        contentPanel.add(createPriceManagementPanel());
        contentPanel.add(Box.createVerticalStrut(25));

        // 2-2. 원장 정보 섹션
        contentPanel.add(createManagerInfoPanel());
        contentPanel.add(Box.createVerticalStrut(25)); // 세로 여백

        // 2-3. 탭 메뉴 (강의/강사/학생 관리)
        JPanel tabPanel = createTabMenuPanel();
        contentPanel.add(tabPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // 2-4. 강사 관리 콘텐츠 (JTable)
        contentPanel.add(createTeacherManagementPanel());
        contentPanel.add(Box.createVerticalGlue());

        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // --- 헬퍼 메소드 ---

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("학원관리 시스템");
        title.setFont(titleFont);
        panel.add(title, BorderLayout.WEST);

        JButton logoutButton = new JButton("로그아웃 →");
        logoutButton.setFont(dataFont);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setForeground(Color.GRAY);

        // [수정] 로그아웃 버튼 클릭 시 Login 창으로 이동
        logoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "로그아웃 되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            new Login().setVisible(true); // Login 창 열기
            dispose(); // 현재 TeachMain 창 닫기
        });
        panel.add(logoutButton, BorderLayout.EAST);

        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(FRAME_WIDTH, 1));
        separator.setBackground(Color.LIGHT_GRAY);
        panel.add(separator, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createManagerInfoPanel() {
        JPanel infoSection = new JPanel();
        infoSection.setLayout(new BorderLayout());
        infoSection.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        infoSection.setBackground(Color.WHITE);
        infoSection.setMinimumSize(new Dimension(FRAME_WIDTH - 60, 150));

        JPanel dataContainer = new JPanel();
        dataContainer.setLayout(new BoxLayout(dataContainer, BoxLayout.Y_AXIS));
        dataContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        dataContainer.setBackground(Color.WHITE);

        // 1. 제목 영역
        JLabel title = new JLabel("👤 원장 정보");
        title.setFont(sectionTitleFont);
        dataContainer.add(title);

        JLabel subtitle = new JLabel("로그인한 원장의 정보입니다");
        subtitle.setFont(dataFont);
        subtitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        dataContainer.add(subtitle);

        // 2. 데이터 표시 영역 (GridBagLayout)
        JPanel dataPanel = new JPanel(new GridBagLayout());
        dataPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // 데이터 배열 (필드 사용)
        String[] labels = {"아이디", "이름", "직책", "전화번호", "이메일", "주소", "가입일"};
        String[] data = {managerId, managerName, managerJob, managerPhone, managerEmail, managerAddress, managerJoinDate};

        int cols = 3;
        for (int i = 0; i < data.length; i++) {
            gbc.gridx = (i % cols) * 2;
            gbc.gridy = i / cols;

            JLabel label = new JLabel(labels[i]);
            label.setFont(dataFont);
            label.setForeground(Color.GRAY);
            dataPanel.add(label, gbc);

            gbc.gridx = (i % cols) * 2 + 1;
            gbc.weightx = 1.0;

            JLabel value = new JLabel(data[i]);
            value.setFont(dataFont);
            value.setName("manager_" + labels[i].toLowerCase().replace(" ", ""));
            dataPanel.add(value, gbc);
            gbc.weightx = 0;
        }

        dataContainer.add(dataPanel);
        infoSection.add(dataContainer, BorderLayout.CENTER);

        // 3. 수정 버튼 영역 (우측 상단)
        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        editPanel.setBackground(Color.WHITE);

        JButton editButton = new JButton("정보 수정");
        editButton.setFont(dataFont);
        editButton.setBackground(Color.LIGHT_GRAY);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> editManagerInfo());

        editPanel.add(editButton);
        infoSection.add(editPanel, BorderLayout.EAST);

        return infoSection;
    }

    /** 원장 정보 수정 팝업 및 로직 */
    private void editManagerInfo() {
        // 입력 필드 생성 및 현재 값 설정
        JTextField nameField = new JTextField(managerName, 15);
        JTextField phoneField = new JTextField(managerPhone, 15);
        JTextField emailField = new JTextField(managerEmail, 15);
        JTextField addressField = new JTextField(managerAddress, 15);

        // 아이디와 직책은 수정 불가
        JLabel idLabel = new JLabel(managerId);
        JLabel jobLabel = new JLabel(managerJob);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 아이디 (수정 불가)
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("아이디:"), gbc);
        gbc.gridx = 1; inputPanel.add(idLabel, gbc);

        // 이름
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("이름:"), gbc);
        gbc.gridx = 1; inputPanel.add(nameField, gbc);

        // 직책 (수정 불가)
        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("직책:"), gbc);
        gbc.gridx = 1; inputPanel.add(jobLabel, gbc);

        // 전화번호
        gbc.gridx = 0; gbc.gridy = 3; inputPanel.add(new JLabel("전화번호:"), gbc);
        gbc.gridx = 1; inputPanel.add(phoneField, gbc);

        // 이메일
        gbc.gridx = 0; gbc.gridy = 4; inputPanel.add(new JLabel("이메일:"), gbc);
        gbc.gridx = 1; inputPanel.add(emailField, gbc);

        // 주소
        gbc.gridx = 0; gbc.gridy = 5; inputPanel.add(new JLabel("주소:"), gbc);
        gbc.gridx = 1; inputPanel.add(addressField, gbc);


        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "원장 정보 수정", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newAddress = addressField.getText().trim();

            try {
                // 🚨 [예외 처리] 필수 필드 및 형식 검사
                if (newName.isEmpty()) {
                    throw new IllegalArgumentException("이름은 필수 입력 항목입니다.");
                }
                if (!Pattern.matches("^010-\\d{4}-\\d{4}$", newPhone)) {
                    throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다. (예: 010-1234-5678)");
                }
                if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,4}$", newEmail)) {
                    throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
                }

                // [업데이트] 필드 값 업데이트
                managerName = newName;
                managerPhone = newPhone;
                managerEmail = newEmail;
                managerAddress = newAddress;

                // [화면 갱신]
                revalidate();
                repaint();

                JOptionPane.showMessageDialog(this, "원장 정보가 수정되었습니다.", "수정 완료", JOptionPane.INFORMATION_MESSAGE);

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                // 실제 DB 오류 등 시스템 예외 처리
                JOptionPane.showMessageDialog(this, "정보 수정 중 서버 오류 발생: " + ex.getMessage(), "시스템 오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private JPanel createPriceManagementPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("강사 단가 공통 관리: ");
        label.setFont(sectionTitleFont);
        panel.add(label);

        JButton updatePriceButton = new JButton("단가 일괄 수정");
        updatePriceButton.setFont(addButtonFont);
        updatePriceButton.setBackground(new Color(255, 165, 0));
        updatePriceButton.setForeground(Color.WHITE);
        updatePriceButton.setFocusPainted(false);
        updatePriceButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        updatePriceButton.addActionListener(e -> updateAllPrices());

        // [추가] 단가 일괄 수정 버튼 마우스 오버 효과
        Color originalColor = new Color(255, 165, 0); // 주황색
        Color hoverColor = new Color(255, 185, 50); // 밝은 주황색

        updatePriceButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                updatePriceButton.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updatePriceButton.setBackground(originalColor);
            }
        });

        panel.add(updatePriceButton);

        return panel;
    }

    private JPanel createTabMenuPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setMaximumSize(new Dimension(FRAME_WIDTH, 40));
        panel.setBackground(Color.WHITE);

        JButton lectureTab = createTabButton("📖 강의 관리");
        JButton teacherTab = createTabButton("👨‍🏫 강사 관리");
        JButton studentTab = createTabButton("🎓 학생 관리");

        teacherTab.setBackground(new Color(230, 230, 230));
        teacherTab.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));

        panel.add(lectureTab);
        panel.add(teacherTab);
        panel.add(studentTab);

        return panel;
    }

    private JButton createTabButton(String text) {
        JButton button = new JButton(text);
        button.setFont(tabFont);
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createTeacherManagementPanel() {
        JPanel teacherSection = new JPanel();
        teacherSection.setLayout(new BoxLayout(teacherSection, BoxLayout.Y_AXIS));
        teacherSection.setBackground(Color.WHITE);
        teacherSection.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel title = new JLabel("강사 관리");
        title.setFont(sectionTitleFont);
        teacherSection.add(title);

        JLabel subtitle = new JLabel("등록된 강사의 정보를 관리합니다");
        subtitle.setFont(dataFont);
        subtitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        teacherSection.add(subtitle);

        // JTable 설정
        String[] columnNames = {"아이디", "이름", "이메일", "전화번호", "주소", "시간당 단가", "관리"};

        Object[][] initialData = {
                {"teacher01", "김수학", "kim.math@academy.com", "010-1111-2222", "서울시 강남구", "35,000원", "수정"},
                {"teacher02", "이영아", "lee.english@academy.com", "010-3333-4444", "서울시 서초구", "40,000원", "수정"},
                {"teacher03", "박과학", "park.science@academy.com", "010-5555-6666", "서울시 송파구", "38,000원", "수정"}
        };

        teacherTableModel = new DefaultTableModel(initialData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // '관리' 열만 수정 가능
            }
        };
        JTable table = new JTable(teacherTableModel);
        table.setFont(dataFont);
        table.setRowHeight(30);

        table.getTableHeader().setFont(buttonFont);
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());

        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JTextField(), this));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(FRAME_WIDTH - 60, 200));
        teacherSection.add(scrollPane);

        return teacherSection;
    }

    private void updateAllPrices() {
        if (teacherTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "등록된 강사 정보가 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField priceField = new JTextField(10);

        int result = JOptionPane.showConfirmDialog(this,
                new Object[]{"모든 강사의 새로운 시간당 단가 (숫자만 입력):", priceField},
                "단가 일괄 수정", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPriceStr = priceField.getText().trim();

            if (!Pattern.matches("^[0-9]+$", newPriceStr)) {
                JOptionPane.showMessageDialog(this, "유효한 숫자 단가(원)를 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newPriceFormatted = newPriceStr + "원";

            for (int i = 0; i < teacherTableModel.getRowCount(); i++) {
                teacherTableModel.setValueAt(newPriceFormatted, i, PRICE_COLUMN_INDEX);
            }

            JOptionPane.showMessageDialog(this,
                    "총 " + teacherTableModel.getRowCount() + "명의 강사 단가가 " + newPriceFormatted + "으로 일괄 수정되었습니다.",
                    "수정 완료", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    class ButtonRenderer extends DefaultCellEditor implements TableCellRenderer {
        private final JButton button;

        public ButtonRenderer() {
            super(new JTextField());
            button = new JButton("수정");
            button.setOpaque(true);
            button.setFont(buttonFont);
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            // [추가] 마우스 이벤트 처리 (Hover Effect)
            button.addMouseListener(new MouseAdapter() {
                private final Color originalColor = Color.WHITE;
                private final Color hoverColor = new Color(240, 240, 240); // 마우스 오버 시 밝은 회색

                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(originalColor);
                }
            });

            button.setPreferredSize(new Dimension(60, 20));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            button.setText((value == null) ? "" : value.toString());
            // 선택된 상태일 때 배경색이 유지되도록 설정
            if (isSelected) {
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setBackground(Color.WHITE);
            }
            return button;
        }
    }

    /* 버튼 에디터 */
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private TeachMain outerFrame;
        private JTable currentTable;

        public ButtonEditor(JTextField textField, TeachMain frame) {
            super(textField);
            this.outerFrame = frame;
            button = new JButton();
            button.setOpaque(true);

            // [수정] 버튼 클릭 시 이벤트 처리: fireEditingStopped() 호출 없이 바로 액션 실행
            button.addActionListener(e -> {
                // 편집이 끝났음을 알리기 전에 데이터를 저장해야 합니다.
                // 그러나 데이터가 변경되지 않으므로, 바로 액션 실행.
                if (currentTable != null) {
                    handleEditAction(currentTable);
                }
                // *필수* 에디터가 끝났음을 알립니다. (이게 없으면 다음 셀 선택 시 문제가 생길 수 있습니다.)
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.currentTable = table;

            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(Color.WHITE); // 기본 배경
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setFont(buttonFont);
            button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            button.setPreferredSize(new Dimension(60, 20));
            isPushed = true;
            return button;
        }

        private void handleEditAction(JTable table) {
            // 현재 편집 중인 행의 인덱스를 가져옵니다.
            int selectedRow = table.getEditingRow();

            if (selectedRow >= 0) {
                String currentId = table.getValueAt(selectedRow, 0).toString();
                String currentName = table.getValueAt(selectedRow, 1).toString();
                String currentEmail = table.getValueAt(selectedRow, 2).toString();
                String currentPhone = table.getValueAt(selectedRow, 3).toString();
                String currentAddress = table.getValueAt(selectedRow, 4).toString();

                JTextField nameField = new JTextField(currentName, 15);
                JTextField emailField = new JTextField(currentEmail, 15);
                JTextField phoneField = new JTextField(currentPhone, 15);
                JTextField addressField = new JTextField(currentAddress, 15);

                JLabel idLabel = new JLabel(currentId);

                JPanel inputPanel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(5, 5, 5, 5);

                gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("아이디:"), gbc);
                gbc.gridx = 1; inputPanel.add(idLabel, gbc);

                gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("이름:"), gbc);
                gbc.gridx = 1; inputPanel.add(nameField, gbc);

                gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("이메일:"), gbc);
                gbc.gridx = 1; inputPanel.add(emailField, gbc);

                gbc.gridx = 0; gbc.gridy = 3; inputPanel.add(new JLabel("전화번호:"), gbc);
                gbc.gridx = 1; inputPanel.add(phoneField, gbc);

                gbc.gridx = 0; gbc.gridy = 4; inputPanel.add(new JLabel("주소:"), gbc);
                gbc.gridx = 1; inputPanel.add(addressField, gbc);


                int result = JOptionPane.showConfirmDialog(outerFrame, inputPanel,
                        currentName + " 강사 정보 수정", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {

                    String newName = nameField.getText().trim();
                    String newEmail = emailField.getText().trim();
                    String newPhone = phoneField.getText().trim();
                    String newAddress = addressField.getText().trim();

                    try {
                        // 🚨 [예외 처리] 강사 정보 형식 검사
                        if (newName.isEmpty()) {
                            throw new IllegalArgumentException("이름은 필수 입력 항목입니다.");
                        }
                        if (!Pattern.matches("^010-\\d{4}-\\d{4}$", newPhone)) {
                            throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다.");
                        }
                        if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,4}$", newEmail)) {
                            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
                        }

                        // **[데이터 저장 로직]** 변경된 내용을 테이블 모델에 반영
                        outerFrame.teacherTableModel.setValueAt(newName, selectedRow, 1);
                        outerFrame.teacherTableModel.setValueAt(newEmail, selectedRow, 2);
                        outerFrame.teacherTableModel.setValueAt(newPhone, selectedRow, 3);
                        outerFrame.teacherTableModel.setValueAt(newAddress, selectedRow, 4);

                        table.repaint();
                        JOptionPane.showMessageDialog(outerFrame, "강사 정보가 수정되었습니다.", "수정 완료", JOptionPane.INFORMATION_MESSAGE);

                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(outerFrame, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(outerFrame, "정보 수정 중 시스템 오류 발생: " + ex.getMessage(), "시스템 오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(outerFrame, "수정할 강사를 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            }
        }


        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TeachMain();
        });
    }
}