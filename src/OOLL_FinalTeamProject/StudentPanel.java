// 학생 - 메인 화면 패널 (개인정보, 내 강의, 수강신청)
package OOLL_FinalTeamProject;

import javax.swing.*;
import javax.swing.table.*;

import teamTest.Student;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class StudentPanel extends JPanel {
    // 개인정보 필드
    private JTextField tfId, tfName, tfEmail, tfPhone, tfAddress;

    // 탭 컴포넌트
    private JTable tableMyClass, tableCourseList;
    private DefaultTableModel modelMyClass, modelCourse;
    private JTextField searchField;
    private JComboBox<String> sortBox;

    private final String currentMemberId;
    private int currentStudentNo = -1;

    private StudentService service = new StudentService();

    public StudentPanel(String memberId) {
        this.currentMemberId = memberId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel top = new JPanel();
        JPanel center = new JPanel();

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // top 영역 간단
        top.setLayout(new BorderLayout());
        top.setPreferredSize(new Dimension(1200, 50));
        top.setBackground(new Color(214, 230, 255));

        JLabel logo = new JLabel("明知 LMS (Learning Management System)");
        logo.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        top.add(logo, BorderLayout.WEST);

        center.setLayout(new FlowLayout(FlowLayout.LEFT, 60, 20));

        // 개인정보 영역
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 10));
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

        JButton editBtn = new JButton("정보수정");
        editBtn.addActionListener(e -> {
            // 새로 분리된 dialog 호출 (옵션 A)
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            StudentEditDialog dlg = new StudentEditDialog(frame, service, currentMemberId);
            dlg.setVisible(true);
            if (dlg.isSaved()) reloadAllData();
        });

        JPanel addressPanel = new JPanel(new BorderLayout(5, 0));
        addressPanel.add(tfAddress, BorderLayout.CENTER);
        JPanel addressBtnPanel = new JPanel(new GridLayout(1, 1, 5, 0));
        addressBtnPanel.add(editBtn);
        addressPanel.add(addressBtnPanel, BorderLayout.EAST);

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

        // 내 강의 탭
        JPanel myClassPanel = new JPanel(new BorderLayout());
        String[] myClassHeader = {"과목명", "담당강사", "강의번호", "요일", "시간", "강의실"};
        modelMyClass = new DefaultTableModel(myClassHeader, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMyClass = new JTable(modelMyClass);
        myClassPanel.add(new JScrollPane(tableMyClass), BorderLayout.CENTER);

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

        String[] courseHeader = {"과목명", "현재인원", "담당강사", "강의번호", "요일", "시간", "강의실", "정원"};
        modelCourse = new DefaultTableModel(courseHeader, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCourseList = new JTable(modelCourse);
        coursePanel.add(searchPanel, BorderLayout.NORTH);
        coursePanel.add(new JScrollPane(tableCourseList), BorderLayout.CENTER);

        tabPane.addTab("내 강의", myClassPanel);
        tabPane.addTab("수강신청", coursePanel);

        center.add(infoPanel);
        center.add(tabPane);

        // 이벤트
        searchBtn.addActionListener(e -> reloadCourseList());
        sortBox.addActionListener(e -> reloadCourseList());

        JButton enrollBtn = new JButton("수강신청");
        enrollBtn.addActionListener(e -> {
            int r = tableCourseList.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "수강할 강의를 선택하세요.");
                return;
            }
            int lectureNo = Integer.parseInt(tableCourseList.getValueAt(r, 3).toString());
            String res = service.attemptEnroll(currentStudentNo, lectureNo);
            if ("성공".equals(res)) {
                JOptionPane.showMessageDialog(this, "수강신청이 완료되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, res);
            }
            reloadAllData();
        });
        JPanel southEnrollPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southEnrollPanel.add(enrollBtn);
        coursePanel.add(southEnrollPanel, BorderLayout.SOUTH);

        // 초기 로드
        SwingUtilities.invokeLater(() -> reloadAllData());
    }

    private void reloadAllData() {
        try {
            StudentInfo info = service.loadStudentInfo(currentMemberId);
            if (info != null) {
                tfId.setText(info.memberId);
                tfName.setText(info.name);
                tfEmail.setText(info.email);
                tfPhone.setText(info.phone);
                tfAddress.setText(info.address);
                currentStudentNo = info.studentNo;
            } else {
                JOptionPane.showMessageDialog(this, "회원정보가 없습니다: " + currentMemberId);
            }
            service.loadMyClass(modelMyClass, currentStudentNo);
            reloadCourseList();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터 로드 오류: " + ex.getMessage());
        }
    }

    private void reloadCourseList() {
        try {
            service.loadCourseList(modelCourse, searchField.getText().trim(), (String) sortBox.getSelectedItem(), currentStudentNo);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "강의목록 로드 오류: " + ex.getMessage());
        }
    }

    private void openAttendanceDialog(int lectureNo, String lectureName) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "수강생 / 출결 - " + lectureName + " (" + lectureNo + ")", true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        String[] cols = {"학생번호", "학생아이디", "학생이름", "출결(오늘)"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        styleTableCenter(t);

        try {
            service.loadAttendanceForLecture(m, lectureNo);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "출결 조회 오류: " + ex.getMessage());
        }

        dlg.add(new JScrollPane(t), BorderLayout.CENTER);

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

        dlg.setModal(true);
        dlg.setVisible(true);
    }

    private void styleTableCenter(JTable tbl) {
        tbl.setRowHeight(26);
        ((DefaultTableCellRenderer)tbl.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tbl.getColumnCount(); i++) tbl.getColumnModel().getColumn(i).setCellRenderer(center);
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
