package team_project;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*; // DB 연동 필수

public class ManagerMainFrame extends JFrame {

    // --- 디자인 테마 ---
    final Color COLOR_THEME = new Color(30, 40, 70);
    final Color COLOR_BG = new Color(245, 245, 250);
    final Color COLOR_WHITE = Color.WHITE;
    final Font FONT_TITLE = new Font("맑은 고딕", Font.BOLD, 24);

    public ManagerMainFrame() {
        setTitle("明知 LMS - 원장 모드");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // =================================================================
        // [북쪽 영역] 브랜드 + 정보
        // =================================================================
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));

        // 1. 브랜드 패널
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
        brandPanel.setBackground(COLOR_THEME);

        JLabel logoBox = new JLabel("LOGO", SwingConstants.CENTER);
        logoBox.setPreferredSize(new Dimension(50, 50));
        logoBox.setOpaque(true);
        logoBox.setBackground(Color.LIGHT_GRAY);
        logoBox.setForeground(Color.BLACK);

        JLabel titleLabel = new JLabel("明知 LMS (Learning Management System)");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        brandPanel.add(logoBox);
        brandPanel.add(titleLabel);

        // 2. 정보 패널
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(COLOR_WHITE);
        infoPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(15, 30, 15, 30)
        ));

        // (1) 정보 그리드
        JPanel dataGrid = new JPanel(new GridLayout(2, 1, 0, 8));
        dataGrid.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row1.setOpaque(false);
        row1.add(createLabel("아이디", "M001"));
        row1.add(createLabel("이름", "김철수"));
        row1.add(createLabel("전화번호", "010-1234-5678"));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        row2.setOpaque(false);
        row2.add(createLabel("이메일", "admin@myongji.com"));
        row2.add(createLabel("주소", "서울시 서대문구 거북골로 34"));

        dataGrid.add(row1);
        dataGrid.add(row2);

        // (2) 버튼 그룹
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton editBtn = createStyledButton("개인정보 수정");
        JButton logoutBtn = createStyledButton("로그아웃");

        // 로그아웃
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "정말 로그아웃 하시겠습니까?",
                    "로그아웃 확인",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // 현재 창 닫기
                dispose();

                // 로그인 창 열기
                new Login().setVisible(true);
            }
        });

        btnPanel.add(editBtn);
        btnPanel.add(logoutBtn);

        infoPanel.add(dataGrid, BorderLayout.CENTER);
        infoPanel.add(btnPanel, BorderLayout.EAST);

        northContainer.add(brandPanel);
        northContainer.add(infoPanel);

        // =================================================================
        // [중앙 영역] 탭
        // =================================================================
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        tabPane.setBackground(COLOR_BG);

        tabPane.addTab("  강의 관리  ", new StyledLecturePanel(this));
        tabPane.addTab("  학생 관리  ", new StyledStudentPanel(this));

        add(northContainer, BorderLayout.NORTH);
        add(tabPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // Helper Methods
    private JLabel createLabel(String title, String value) {
        return new JLabel("<html><font color='#777777'>" + title + " : </font> <font size='4' color='black'><b>" + value + "</b></font></html>");
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        btn.setPreferredSize(new Dimension(120, 35));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(new Color(240, 240, 240)); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setSelectionForeground(Color.BLACK); // 선택 시 글자색 검정
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 250));
        header.setForeground(new Color(50, 50, 50));
        header.setPreferredSize(new Dimension(0, 35));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new ManagerMainFrame();
    }
}

// =========================================================
// [탭 1] 강의 관리 패널 (DB 조회 기능 탑재)
// =========================================================
class StyledLecturePanel extends JPanel {
    JFrame parentFrame;
    DefaultTableModel tableModel;

    public StyledLecturePanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("검색");
        searchBtn.setBackground(new Color(245, 245, 245));

        JButton addBtn = new JButton("강좌 개설");
        addBtn.setBackground(Color.WHITE);
        // 개설 버튼 누르면 팝업 열고 -> 닫히면 목록 새로고침
        addBtn.addActionListener(e -> {
            LectureDialog dialog = new LectureDialog(parentFrame, "강좌 개설");
            dialog.setVisible(true);
            refreshTable(); // ★ 창 닫히면 DB 다시 조회해서 화면 갱신
        });

        JButton editBtn = new JButton("수정");
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(e -> new LectureDialog(parentFrame, "강좌 수정").setVisible(true));

        JButton delBtn = new JButton("삭제");
        delBtn.setBackground(Color.WHITE);

        topPanel.add(new JLabel("강의명 검색 : "));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(delBtn);

        // 테이블 (데이터 없이 컬럼만 생성)
        String[] cols = {"과목명", "담당강사", "강의실", "요일", "교시", "수강/정원"};
        tableModel = new DefaultTableModel(null, cols);

        JTable table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 최초 실행 시 DB에서 목록 가져오기
        refreshTable();
    }

    // ★ DB에서 강의 목록 가져오는 메소드
    public void refreshTable() {
        tableModel.setRowCount(0); // 표 비우기

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";

            // ★ 비밀번호 java2025 적용
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            // 강의 + 강사 이름 + 수강인원 조회
            String sql = "SELECT l.subject_name, m.name, l.classroom_name, l.day_of_week, " +
                    "l.start_period, l.end_period, l.enrolled_count, l.capacity " +
                    "FROM lecture l " +
                    "LEFT JOIN teacher t ON l.teacher_no = t.teacher_no " +
                    "LEFT JOIN member m ON t.member_id = m.member_id " +
                    "ORDER BY l.lecture_no DESC"; // 최신순 정렬

            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                String sub = rs.getString("subject_name");
                String tName = rs.getString("name");
                if(tName == null) tName = "미정"; // 강사 삭제된 경우 대비
                String room = rs.getString("classroom_name");
                String day = rs.getString("day_of_week");
                String time = rs.getInt("start_period") + "-" + rs.getInt("end_period") + "교시";
                String count = rs.getInt("enrolled_count") + " / " + rs.getInt("capacity");

                tableModel.addRow(new Object[]{sub, tName, room, day, time, count});
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}

// =========================================================
// [탭 2] 학생 관리 패널
// =========================================================
class StyledStudentPanel extends JPanel {
    JFrame parentFrame;
    DefaultTableModel tableModel;

    public StyledStudentPanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("검색");
        searchBtn.setBackground(new Color(245, 245, 245));

        JButton editBtn = new JButton("학생 정보 수정");
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(e -> new StudentDialog(parentFrame, "학생 정보 수정").setVisible(true));

        JButton delBtn = new JButton("삭제");
        delBtn.setBackground(Color.WHITE);

        topPanel.add(new JLabel("학생명/ID 검색 : "));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(editBtn);
        topPanel.add(delBtn);

        String[] cols = {"이름", "아이디", "전화번호", "이메일", "주소"};
        tableModel = new DefaultTableModel(null, cols);

        JTable table = new JTable(tableModel);
        ManagerMainFrame.styleTable(table);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}

// =======================================================================
// [팝업 1] 강좌 개설용 (DB저장 + 강사불러오기 + 비밀번호 수정완료)
// =======================================================================
class LectureDialog extends JDialog {
    JComboBox<String> subjectCombo;
    JComboBox<TeacherItem> teacherCombo;
    JTextField roomField;
    JTextField startField, endField;
    JTextField capField;
    JCheckBox[] dayChecks;

    public LectureDialog(JFrame parent, String title) {
        super(parent, title, true);
        setSize(400, 450);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);

        // 1. 과목
        formPanel.add(new JLabel("과목명:"));
        subjectCombo = new JComboBox<>(new String[]{"- 선택 -", "국어","영어","수학","과학","사회"});
        subjectCombo.setBackground(Color.WHITE);
        formPanel.add(subjectCombo);

        // 2. 담당강사 (DB 로드)
        formPanel.add(new JLabel("담당강사:"));
        teacherCombo = new JComboBox<>();
        teacherCombo.addItem(new TeacherItem(0, "- 선택 -"));
        loadTeacherList(); // ★ 강사 목록 DB에서 가져오기
        teacherCombo.setBackground(Color.WHITE);
        formPanel.add(teacherCombo);

        // 3. 강의실
        formPanel.add(new JLabel("강의실:"));
        roomField = new JTextField();
        formPanel.add(roomField);

        // 4. 요일
        formPanel.add(new JLabel("요일(중복가능):"));
        JPanel dayPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        dayPanel.setBackground(Color.WHITE);
        String[] days = {"월", "화", "수", "목", "금"};
        dayChecks = new JCheckBox[5];
        for(int i=0; i<5; i++) {
            dayChecks[i] = new JCheckBox(days[i]);
            dayChecks[i].setBackground(Color.WHITE);
            dayChecks[i].setMargin(new Insets(0, 0, 0, 0));
            dayPanel.add(dayChecks[i]);
        }
        formPanel.add(dayPanel);

        // 5. 교시
        formPanel.add(new JLabel("시작교시:")); startField = new JTextField(); formPanel.add(startField);
        formPanel.add(new JLabel("종료교시:")); endField = new JTextField(); formPanel.add(endField);

        // 6. 정원
        formPanel.add(new JLabel("정원:")); capField = new JTextField(); formPanel.add(capField);

        // 버튼
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton createBtn = new JButton("개설");
        createBtn.setBackground(Color.WHITE);
        createBtn.setPreferredSize(new Dimension(80, 35));

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.addActionListener(e -> dispose());

        // --- [이벤트] 개설 버튼 클릭 (DB 저장) ---
        createBtn.addActionListener(e -> {
            try {
                validateInputs();

                String subject = (String) subjectCombo.getSelectedItem();
                TeacherItem selectedTeacher = (TeacherItem) teacherCombo.getSelectedItem();
                String room = roomField.getText();
                String startStr = startField.getText();
                String endStr = endField.getText();
                String capStr = capField.getText();

                String dayStr = "";
                for(JCheckBox box : dayChecks) if(box.isSelected()) dayStr += box.getText();

                Connection con = null;
                Statement stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";

                    // ★ 비밀번호 java2025 적용
                    con = DriverManager.getConnection(dbUrl, "root", "java2025");

                    String sql = "INSERT INTO lecture (subject_name, teacher_no, classroom_name, day_of_week, start_period, end_period, capacity) " +
                            "VALUES ('" + subject + "', " +
                            selectedTeacher.no + ", '" +
                            room + "', '" +
                            dayStr + "', " +
                            startStr + ", " +
                            endStr + ", " +
                            capStr + ")";

                    System.out.println("SQL: " + sql);

                    stmt = con.createStatement();
                    stmt.executeUpdate(sql);

                    JOptionPane.showMessageDialog(this, "강의가 개설되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
                    dispose();

                } catch (Exception err) {
                    err.printStackTrace();
                    JOptionPane.showMessageDialog(this, "DB 오류: " + err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try { if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
                }

            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(createBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void validateInputs() throws InvalidInputException {
        if (subjectCombo.getSelectedIndex() == 0) throw new InvalidInputException("과목명을 선택해주세요.");
        TeacherItem item = (TeacherItem) teacherCombo.getSelectedItem();
        if (item == null || item.no == 0) throw new InvalidInputException("담당강사를 선택해주세요.");
        if (roomField.getText().trim().isEmpty()) throw new InvalidInputException("강의실명을 입력해주세요.");
        if (startField.getText().trim().isEmpty() || endField.getText().trim().isEmpty()) throw new InvalidInputException("시작/종료 교시를 입력해주세요.");
        if (capField.getText().trim().isEmpty()) throw new InvalidInputException("정원을 입력해주세요.");

        boolean isDayChecked = false;
        for (JCheckBox box : dayChecks) {
            if (box.isSelected()) {
                isDayChecked = true;
                break;
            }
        }
        if (!isDayChecked) throw new InvalidInputException("요일을 최소 하나 이상 선택해주세요.");
    }

    // ★ DB에서 강사 목록 로드 (비번 java2025 적용)
    private void loadTeacherList() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbUrl = "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC";

            // ★ 비밀번호 java2025 적용
            con = DriverManager.getConnection(dbUrl, "root", "java2025");

            String sql = "SELECT t.teacher_no, m.name FROM teacher t JOIN member m ON t.member_id = m.member_id ORDER BY m.name";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                int no = rs.getInt("teacher_no");
                String name = rs.getString("name");
                teacherCombo.addItem(new TeacherItem(no, name));
            }
        } catch (Exception e) {
            System.out.println("강사 목록 로드 실패: " + e.getMessage());
        } finally {
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }

    private static class InvalidInputException extends Exception {
        public InvalidInputException(String message) { super(message); }
    }

    class TeacherItem {
        int no; String name;
        public TeacherItem(int no, String name) { this.no = no; this.name = name; }
        @Override public String toString() { return name; }
    }
}

// =========================================================
// [팝업 2] 학생용 다이얼로그 (400 x 350 - 최적 사이즈)
// =========================================================
class StudentDialog extends JDialog {
    public StudentDialog(JFrame parent, String title) {
        super(parent, title, true);
        setSize(400, 350);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("아이디:"));
        JTextField idField = new JTextField("park123");
        idField.setEditable(false);
        formPanel.add(idField);

        formPanel.add(new JLabel("이름:")); formPanel.add(new JTextField());
        formPanel.add(new JLabel("전화번호:")); formPanel.add(new JTextField());
        formPanel.add(new JLabel("이메일:")); formPanel.add(new JTextField());
        formPanel.add(new JLabel("주소:")); formPanel.add(new JTextField());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton saveBtn = new JButton("저장");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(80, 35));

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

}
