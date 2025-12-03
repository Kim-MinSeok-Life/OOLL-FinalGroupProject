//package teamPro;
 package OOLL_FinalTeamProject;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.*;

public class Student extends JFrame {
    // 개인정보 필드
    JTextField tfId, tfName, tfEmail, tfPhone, tfAddress;

    // 탭 컴포넌트
    JTable tableMyClass, tableCourseList;
    DefaultTableModel modelMyClass, modelCourse;
    JTextField searchField;
    JComboBox<String> sortBox;

    // test
    private final String currentMemberId = "stu01";
    private int currentStudentNo = -1;
    
    public Student() {
        Container ct = getContentPane();
        ct.setLayout(new BorderLayout());
        ct.setBackground(Color.WHITE);

        JPanel top = new JPanel();
        JPanel center = new JPanel();

        ct.add(top, BorderLayout.NORTH);
        ct.add(center, BorderLayout.CENTER);

        // top 영역
        top.setLayout(new BorderLayout());
        top.setPreferredSize(new Dimension(1200, 80));
        top.setBackground(new Color(214, 230, 255));

        // logo 부분
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        logoPanel.setBackground(new Color(214, 230, 255));
        
        // 수정해야됨
        ImageIcon icon = new ImageIcon("logo.png");
        Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(img));
        
        JLabel logo = new JLabel("明知 LMS (Learning Management System)");
        logo.setFont(new Font("맑은 고딕", Font.BOLD, 20)); // 폰트 설정

        // 로그아웃
        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "로그아웃 처리(샘플)");
            dispose();
        });

        top.add(logo, BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);

        // center 영역
        center.setLayout(new FlowLayout(FlowLayout.LEFT, 60, 20));

        // 개인정보 영역
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("개인정보"));
        infoPanel.setPreferredSize(new Dimension(1100, 180));
        infoPanel.setBackground(Color.WHITE);

        tfId = new JTextField(15);
        tfName = new JTextField(15);
        tfEmail = new JTextField(15);
        tfPhone = new JTextField(15);
        tfAddress = new JTextField(30);

        tfId.setEditable(false);
        tfName.setEditable(false);
        tfEmail.setEditable(false);
        tfPhone.setEditable(false);
        tfAddress.setEditable(false);

        // 정보수정 버튼 -> 실제 수정 다이얼로그 실행
        JButton editBtn = new JButton("정보수정");
        editBtn.addActionListener(e -> openEditDialog());

        // 주소 칸에 들어갈 패널: 텍스트필드 + 버튼 (이 패널을 infoPanel의 셀에 넣음)
        JPanel addressPanel = new JPanel(new BorderLayout(5, 0));
        addressPanel.add(tfAddress, BorderLayout.CENTER);
        addressPanel.add(editBtn, BorderLayout.EAST);

        // 개인정보 추가
        infoPanel.add(new JLabel("학생아이디"));
        infoPanel.add(tfId);
        infoPanel.add(new JLabel("이름"));
        infoPanel.add(tfName);
        infoPanel.add(new JLabel("이메일"));
        infoPanel.add(tfEmail);
        infoPanel.add(new JLabel("연락처"));
        infoPanel.add(tfPhone);
        infoPanel.add(new JLabel("주소"));
        infoPanel.add(addressPanel);

        // 탭
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setPreferredSize(new Dimension(1100, 500));

        // 내 강의 탭 (table)
        JPanel myClassPanel = new JPanel(new BorderLayout());
        String[] myClassHeader = {"과목명", "담당강사", "강의번호", "요일", "시간", "강의실"};
        modelMyClass = new DefaultTableModel(myClassHeader, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMyClass = new JTable(modelMyClass);
        myClassPanel.add(new JScrollPane(tableMyClass), BorderLayout.CENTER);

        // 강의행 클릭하면 해당 강좌의 수강생/출결 조회 다이얼로그 열기
        tableMyClass.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = tableMyClass.getSelectedRow();
                if (r == -1) return;
                int lectureNo = Integer.parseInt(tableMyClass.getValueAt(r, 2).toString());
                String lectureName = tableMyClass.getValueAt(r, 0).toString();
                openAttendanceDialog(lectureNo, lectureName);
            }
        });

        // 수강신청 탭
        JPanel coursePanel = new JPanel(new BorderLayout());

        // 수강신청 검색&정렬 부분
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("검색");
        String[] sortOpt = {"과목명순", "강사명순", "신청가능인원순"};
        sortBox = new JComboBox<>(sortOpt);

        searchPanel.add(new JLabel("검색: "));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(new JLabel("정렬: "));
        searchPanel.add(sortBox);

        // 수강신청 테이블
        String[] courseHeader = {"과목명", "현재인원", "담당강사", "강의번호", "요일", "시간", "강의실", "정원"};
        modelCourse = new DefaultTableModel(courseHeader, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCourseList = new JTable(modelCourse);

        coursePanel.add(searchPanel, BorderLayout.NORTH);
        coursePanel.add(new JScrollPane(tableCourseList), BorderLayout.CENTER);

        // 탭 부분
        tabPane.addTab("내 강의", myClassPanel);
        tabPane.addTab("수강신청", coursePanel);

        // center에 추가
        center.add(infoPanel);
        center.add(tabPane);

        // 이벤트: 검색/정렬 버튼 동작
        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            String sort = (String) sortBox.getSelectedItem();
            loadCourseList(kw, sort);
        });
        sortBox.addActionListener(e -> {
            String kw = searchField.getText().trim();
            loadCourseList(kw, (String) sortBox.getSelectedItem());
        });

        // 선택 강의 수강신청(간단 버튼) — UI에 옵션으로 배치
        JButton enrollBtn = new JButton("수강신청");
        enrollBtn.addActionListener(e -> {
            int r = tableCourseList.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "수강할 강의를 선택하세요.");
                return;
            }
            int lectureNo = Integer.parseInt(tableCourseList.getValueAt(r, 3).toString());
            attemptEnroll(lectureNo);
        });
        JPanel southEnrollPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southEnrollPanel.add(enrollBtn);
        coursePanel.add(southEnrollPanel, BorderLayout.SOUTH);

        // 초기 데이터 로드: 학생정보, 내 강의, 전체 강의 목록
        SwingUtilities.invokeLater(() -> {
            loadStudentInfo(currentMemberId); // member 테이블에서 개인정보 + student_no 구해옴
            loadMyClass();
            loadCourseList("", "과목명순");
        });
    }

    // ---------- DB 연결 ----------
    private Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // DB 정보: academy_lms, 계정 root, pw 7907
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "7907");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 연결 실패:\n" + e.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
        return conn;
    }

    // ---------- 개인정보 + student_no 얻어오기 ----------
    private void loadStudentInfo(String memberId) {
        String sql = "SELECT m.member_id, m.name, m.email, m.phone, m.address, s.student_no " +
                     "FROM member m LEFT JOIN student s ON m.member_id = s.member_id WHERE m.member_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, memberId);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    tfId.setText(rs.getString("member_id"));
                    tfName.setText(rs.getString("name"));
                    tfEmail.setText(rs.getString("email"));
                    tfPhone.setText(rs.getString("phone"));
                    tfAddress.setText(rs.getString("address"));
                    currentStudentNo = rs.getInt("student_no");
                    if (rs.wasNull()) currentStudentNo = -1;
                } else {
                    JOptionPane.showMessageDialog(this, "회원정보가 없습니다: " + memberId);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ---------- 내 강의 로드 ----------
    private void loadMyClass() {
        modelMyClass.setRowCount(0);
        if (currentStudentNo == -1) return;
        String sql = "SELECT l.subject_name, m.name as teacher_name, l.lecture_no, l.day_of_week, CONCAT(l.start_period,'~',l.end_period) as time, l.classroom_name " +
                     "FROM enrollment e JOIN lecture l ON e.lecture_no = l.lecture_no " +
                     "JOIN teacher t ON l.teacher_no = t.teacher_no JOIN member m ON t.member_id = m.member_id " +
                     "WHERE e.student_no = ? AND e.status = '수강중'";
        try (Connection conn = getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, currentStudentNo);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    modelMyClass.addRow(new Object[]{
                            rs.getString("subject_name"),
                            rs.getString("teacher_name"),
                            rs.getInt("lecture_no"),
                            rs.getString("day_of_week"),
                            rs.getString("time"),
                            rs.getString("classroom_name")
                    });
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ---------- 수강신청 목록 로드 (검색 + 정렬) ----------
    private void loadCourseList(String keyword, String sortOption) {
        modelCourse.setRowCount(0);
        String base = "SELECT l.subject_name, l.enrolled_count, m.name as teacher_name, l.lecture_no, l.day_of_week, l.start_period, l.end_period, l.classroom_name, l.capacity " +
                      "FROM lecture l JOIN teacher t ON l.teacher_no = t.teacher_no JOIN member m ON t.member_id = m.member_id " +
                      "WHERE (l.subject_name LIKE ? OR m.name LIKE ?)";
        String order = "";
        if ("과목명순".equals(sortOption)) order = " ORDER BY l.subject_name ASC";
        else if ("강사명순".equals(sortOption)) order = " ORDER BY m.name ASC";
        else if ("신청가능인원순".equals(sortOption)) order = " ORDER BY (l.capacity - l.enrolled_count) DESC";
        String sql = base + order;

        try (Connection conn = getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            String like = "%" + (keyword == null ? "" : keyword) + "%";
            p.setString(1, like);
            p.setString(2, like);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String subj = rs.getString("subject_name");
                    int enrolled = rs.getInt("enrolled_count");
                    String teacher = rs.getString("teacher_name");
                    int lectureNo = rs.getInt("lecture_no");
                    String day = rs.getString("day_of_week");
                    int s = rs.getInt("start_period");
                    int e = rs.getInt("end_period");
                    String time = s + "~" + e;
                    String room = rs.getString("classroom_name");
                    int cap = rs.getInt("capacity");
                    modelCourse.addRow(new Object[]{subj, enrolled, teacher, lectureNo, day, time, room, cap});
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ---------- 수강신청 시도 (정원/중복/시간 충돌 확인 포함) ----------
    private void attemptEnroll(int lectureNo) {
        if (currentStudentNo == -1) {
            JOptionPane.showMessageDialog(this, "학생 정보가 없습니다. 먼저 회원-학생 연계를 확인하세요.");
            return;
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1) 중복 확인
            String q1 = "SELECT COUNT(*) FROM enrollment WHERE student_no = ? AND lecture_no = ?";
            try (PreparedStatement p1 = conn.prepareStatement(q1)) {
                p1.setInt(1, currentStudentNo);
                p1.setInt(2, lectureNo);
                try (ResultSet r1 = p1.executeQuery()) {
                    if (r1.next() && r1.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "이미 해당 강의를 수강중입니다.");
                        conn.rollback();
                        return;
                    }
                }
            }
            // 2) 정원/현재인원 확인
            String q2 = "SELECT capacity, enrolled_count, day_of_week, start_period, end_period, subject_name FROM lecture WHERE lecture_no = ? FOR UPDATE";
            int capacity = 0, enrolled = 0, start = 0, end = 0;
            String day = null, subj = null;
            try (PreparedStatement p2 = conn.prepareStatement(q2)) {
                p2.setInt(1, lectureNo);
                try (ResultSet r2 = p2.executeQuery()) {
                    if (r2.next()) {
                        capacity = r2.getInt("capacity");
                        enrolled = r2.getInt("enrolled_count");
                        day = r2.getString("day_of_week");
                        start = r2.getInt("start_period");
                        end = r2.getInt("end_period");
                        subj = r2.getString("subject_name");
                    } else {
                        JOptionPane.showMessageDialog(this, "해당 강의를 찾을 수 없습니다.");
                        conn.rollback();
                        return;
                    }
                }
            }
            if (enrolled >= capacity) {
                JOptionPane.showMessageDialog(this, "[" + subj + "] 강의의 정원이 가득 찼습니다.");
                conn.rollback();
                return;
            }
            // 3) 시간 충돌 검사
            String q3 = "SELECT l.subject_name, l.day_of_week, l.start_period, l.end_period FROM enrollment e JOIN lecture l ON e.lecture_no = l.lecture_no WHERE e.student_no = ? AND e.status = '수강중'";
            try (PreparedStatement p3 = conn.prepareStatement(q3)) {
                p3.setInt(1, currentStudentNo);
                try (ResultSet r3 = p3.executeQuery()) {
                    while (r3.next()) {
                        String d = r3.getString("day_of_week");
                        int s = r3.getInt("start_period");
                        int ee = r3.getInt("end_period");
                        if (d.equalsIgnoreCase(day)) {
                            if (!(end < s || start > ee)) {
                                JOptionPane.showMessageDialog(this, "시간 충돌: 기존 수강중인 과목과 시간이 겹칩니다.");
                                conn.rollback();
                                return;
                            }
                        }
                    }
                }
            }
            // 4) 실제 등록
            String ins = "INSERT INTO enrollment (student_no, lecture_no, status, apply_date) VALUES (?, ?, '수강중', CURDATE())";
            try (PreparedStatement pins = conn.prepareStatement(ins)) {
                pins.setInt(1, currentStudentNo);
                pins.setInt(2, lectureNo);
                pins.executeUpdate();
            }
            // 5) 강의의 enrolled_count 증가
            String upt = "UPDATE lecture SET enrolled_count = enrolled_count + 1 WHERE lecture_no = ?";
            try (PreparedStatement pup = conn.prepareStatement(upt)) {
                pup.setInt(1, lectureNo);
                pup.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "수강신청이 완료되었습니다.");
            loadCourseList(searchField.getText().trim(), (String) sortBox.getSelectedItem());
            loadMyClass();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "수강신청 중 오류 발생: " + ex.getMessage());
        }
    }

    // 특정 강좌 클릭 시: 수강생 목록 + (오늘)출결 조회 다이얼로그
    private void openAttendanceDialog(int lectureNo, String lectureName) {
        // 학생 페이지에서는 출력(읽기)만 하도록 구현 (원장/강사가 DB를 수정하면 여기서 반영됨)
        JDialog dlg = new JDialog(this, "수강생 / 출결 - " + lectureName + " (" + lectureNo + ")", true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        String[] cols = {"학생번호", "학생아이디", "학생이름", "출결(오늘)"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        styleTableCenter(t);

        // 오늘 날짜 출결(원장/강사가 업데이트한 값) 가져오기
        LocalDate today = LocalDate.now();
        String q = "SELECT s.student_no, mb.member_id, mb.name, a.attendance_status " +
                   "FROM enrollment e " +
                   "JOIN student s ON e.student_no = s.student_no " +
                   "JOIN member mb ON s.member_id = mb.member_id " +
                   "LEFT JOIN attendance a ON a.student_no = s.student_no AND a.lecture_no = ? AND a.att_date = ? " +
                   "WHERE e.lecture_no = ? AND e.status = '수강중'";

        try (Connection conn = getConnection();
             PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, lectureNo);
            p.setDate(2, Date.valueOf(today));
            p.setInt(3, lectureNo);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    int sno = rs.getInt("student_no");
                    String mid = rs.getString("member_id");
                    String nm = rs.getString("name");
                    String status = rs.getString("attendance_status");
                    if (status == null) status = "미처리";
                    m.addRow(new Object[]{sno, mid, nm, status});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "출결 조회 오류: " + ex.getMessage());
        }

        dlg.add(new JScrollPane(t), BorderLayout.CENTER);

        // 하단: 안내버튼만 배치 (학생 페이지는 수정 권한 없음)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("닫기");
        JButton refreshBtn = new JButton("갱신");

        closeBtn.addActionListener(e -> dlg.dispose());
        refreshBtn.addActionListener(e -> {
            dlg.dispose();
            openAttendanceDialog(lectureNo, lectureName);
        });

        bottom.add(refreshBtn);
        bottom.add(closeBtn);
        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void styleTableCenter(JTable tbl) {
        tbl.setRowHeight(26);
        ((DefaultTableCellRenderer)tbl.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tbl.getColumnCount(); i++) tbl.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    // 개인정보 수정 다이얼로그
    private void openEditDialog() {
        JDialog dlg = new JDialog(this, "개인정보 수정", true);
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // fields: id(readonly), password, name, phone, address
        JTextField idField = new JTextField(tfId.getText(), 15);
        idField.setEditable(false);
        JPasswordField pwField = new JPasswordField(15);
        JTextField nameField = new JTextField(tfName.getText(), 15);
        JTextField phoneField = new JTextField(tfPhone.getText(), 15);
        JTextField addressField = new JTextField(tfAddress.getText(), 25);

        g.gridx = 0; g.gridy = 0; dlg.add(new JLabel("아이디"), g);
        g.gridx = 1; g.gridy = 0; dlg.add(idField, g);

        g.gridx = 0; g.gridy = 1; dlg.add(new JLabel("비밀번호"), g);
        g.gridx = 1; g.gridy = 1; dlg.add(pwField, g);

        g.gridx = 0; g.gridy = 2; dlg.add(new JLabel("이름"), g);
        g.gridx = 1; g.gridy = 2; dlg.add(nameField, g);

        g.gridx = 0; g.gridy = 3; dlg.add(new JLabel("연락처"), g);
        g.gridx = 1; g.gridy = 3; dlg.add(phoneField, g);

        g.gridx = 0; g.gridy = 4; dlg.add(new JLabel("주소"), g);
        g.gridx = 1; g.gridy = 4; dlg.add(addressField, g);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("취소");
        JButton saveBtn = new JButton("저장");
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 2; dlg.add(btnPanel, g);

        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String newPw = new String(pwField.getPassword()).trim();
            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newAddr = addressField.getText().trim();

            if (newPw.isEmpty() || newName.isEmpty() || newPhone.isEmpty() || newAddr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "모든 항목을 입력하세요.", "입력 필요", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dlg, "정말 수정하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // DB 업데이트 (member 테이블에 저장)
            String sql = "UPDATE member SET password=?, name=?, phone=?, address=? WHERE member_id=?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newPw);
                pstmt.setString(2, newName);
                pstmt.setString(3, newPhone);
                pstmt.setString(4, newAddr);
                pstmt.setString(5, idField.getText());
                int updated = pstmt.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(dlg, "정보가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
                    // 화면 갱신
                    loadStudentInfo(idField.getText());
                    dlg.dispose();
                } else {
                    JOptionPane.showMessageDialog(dlg, "수정 실패", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "DB 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setVisible(true);
    }

    // main
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(() -> {
    		Student win = new Student();
            win.setTitle("학생 페이지");
            win.setSize(1200, 800);
            win.setLocation(300, 100);
            win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            win.setVisible(true);
        });
    }
}
