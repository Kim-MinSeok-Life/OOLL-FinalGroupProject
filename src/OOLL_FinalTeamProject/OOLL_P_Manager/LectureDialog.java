// 원장의 강의 수정 창!!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LectureDialog extends JDialog implements ActionListener {
    JComboBox<String> subjectCombo;
    JComboBox<TeacherItem> teacherCombo;
    JComboBox<String> roomCombo;
    JComboBox<String> startCombo;
    JComboBox<String> endCombo;
    JTextField capField;
    JCheckBox[] dayChecks;

    JButton actionBtn, cancelBtn;

    String currentLectureNo = null;
    boolean isEditMode = false;

    // ★ [추가] 현재 수강인원 저장 변수
    int currentEnrolledCount = 0;

    public LectureDialog(JFrame parent, String title, String[] editData) {
        super(parent, title, true);
        setSize(400, 480);
        setLocationRelativeTo(parent);

        if (editData != null) {
            isEditMode = true;
            currentLectureNo = editData[0];
            // ★ [추가] 받아온 데이터에서 수강인원(7번째) 저장
            if(editData.length > 7) {
                currentEnrolledCount = Integer.parseInt(editData[7]);
            }
        }

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

        // 2. 담당강사
        formPanel.add(new JLabel("담당강사:"));
        teacherCombo = new JComboBox<>();
        teacherCombo.addItem(new TeacherItem(0, "- 선택 -"));
        loadTeacherList();
        teacherCombo.setBackground(Color.WHITE);
        formPanel.add(teacherCombo);

        // 3. 강의실
        formPanel.add(new JLabel("강의실:"));
        String[] rooms = {"- 선택 -", "101호", "102호", "103호", "201호", "202호", "203호", "301호", "302호", "303호", "401호", "402호", "403호"};
        roomCombo = new JComboBox<>(rooms);
        roomCombo.setBackground(Color.WHITE);
        formPanel.add(roomCombo);

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

        // 5. 시작/종료 교시
        String[] periods = {"- 선택 -", "1교시", "2교시", "3교시", "4교시", "5교시", "6교시", "7교시"};
        formPanel.add(new JLabel("시작교시:"));
        startCombo = new JComboBox<>(periods); startCombo.setBackground(Color.WHITE); formPanel.add(startCombo);
        formPanel.add(new JLabel("종료교시:"));
        endCombo = new JComboBox<>(periods); endCombo.setBackground(Color.WHITE); formPanel.add(endCombo);

        // 6. 정원
        formPanel.add(new JLabel("정원:")); capField = new JTextField("20"); formPanel.add(capField);

        // 데이터 채우기 (수정 모드)
        if (isEditMode) {
            subjectCombo.setSelectedItem(editData[1]);
            for (int i=0; i<teacherCombo.getItemCount(); i++) {
                if (teacherCombo.getItemAt(i).toString().equals(editData[2])) {
                    teacherCombo.setSelectedIndex(i);
                    break;
                }
            }
            roomCombo.setSelectedItem(editData[3]);
            String dayStr = editData[4];
            for(int i=0; i<5; i++) { if(dayStr.contains(dayChecks[i].getText())) dayChecks[i].setSelected(true); }
            try {
                String[] times = editData[5].replace("교시", "").split("-");
                startCombo.setSelectedItem(times[0] + "교시");
                endCombo.setSelectedItem(times[1] + "교시");
            } catch (Exception e) {}
            capField.setText(editData[6]);
        }

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String btnText = isEditMode ? "수정" : "개설";
        actionBtn = new JButton(btnText);
        actionBtn.setBackground(Color.WHITE);
        actionBtn.setPreferredSize(new Dimension(80, 35));
        actionBtn.addActionListener(this);

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.addActionListener(this);

        btnPanel.add(actionBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose();
        } else if (e.getSource() == actionBtn) {
            try {
                validateInputs();

                String subject = (String) subjectCombo.getSelectedItem();
                TeacherItem selectedTeacher = (TeacherItem) teacherCombo.getSelectedItem();
                String room = (String) roomCombo.getSelectedItem();
                int start = Integer.parseInt(((String)startCombo.getSelectedItem()).replace("교시",""));
                int end = Integer.parseInt(((String)endCombo.getSelectedItem()).replace("교시",""));
                String capStr = capField.getText();
                String dayStr = "";
                for(JCheckBox box : dayChecks) if(box.isSelected()) dayStr += box.getText();

                int excludeId = isEditMode ? Integer.parseInt(currentLectureNo) : -1;
                checkDatabaseConflicts(excludeId, selectedTeacher.no, room, dayStr, start, end);

                Connection con = null;
                Statement stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                    String sql;
                    if (isEditMode) {
                        sql = "UPDATE lecture SET subject_name='" + subject + "', teacher_no=" + selectedTeacher.no +
                                ", classroom_name='" + room + "', day_of_week='" + dayStr +
                                "', start_period=" + start + ", end_period=" + end + ", capacity=" + capStr +
                                " WHERE lecture_no=" + currentLectureNo;
                    } else {
                        sql = "INSERT INTO lecture (subject_name, teacher_no, classroom_name, day_of_week, start_period, end_period, capacity) " +
                                "VALUES ('" + subject + "', " + selectedTeacher.no + ", '" + room + "', '" + dayStr + "', " + start + ", " + end + ", " + capStr + ")";
                    }

                    stmt = con.createStatement();
                    stmt.executeUpdate(sql);

                    String msg = isEditMode ? "수정되었습니다." : "개설되었습니다.";
                    JOptionPane.showMessageDialog(this, msg, "성공", JOptionPane.INFORMATION_MESSAGE);
                    dispose();

                } catch (Exception err) {
                    err.printStackTrace();
                    JOptionPane.showMessageDialog(this, "DB 오류: " + err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try { if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
                }

            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void validateInputs() throws InvalidInputException {
        if (subjectCombo.getSelectedIndex() == 0) throw new InvalidInputException("과목명을 선택해주세요.");
        TeacherItem item = (TeacherItem) teacherCombo.getSelectedItem();
        if (item == null || item.no == 0) throw new InvalidInputException("담당강사를 선택해주세요.");
        if (roomCombo.getSelectedIndex() == 0) throw new InvalidInputException("강의실을 선택해주세요.");
        if (startCombo.getSelectedIndex() == 0) throw new InvalidInputException("시작 교시를 선택해주세요.");
        if (endCombo.getSelectedIndex() == 0) throw new InvalidInputException("종료 교시를 선택해주세요.");

        int s = Integer.parseInt(((String)startCombo.getSelectedItem()).replace("교시",""));
        int e = Integer.parseInt(((String)endCombo.getSelectedItem()).replace("교시",""));
        if (s > e) throw new InvalidInputException("시작 교시가 종료 교시보다 늦을 수 없습니다.");

        String capStr = capField.getText().trim();
        if (capStr.isEmpty()) throw new InvalidInputException("정원을 입력해주세요.");
        // 숫자만 입력했는지 확인
        if (!capStr.matches("\\d+")) throw new InvalidInputException("정원은 숫자만 입력 가능합니다.");

        int cap = Integer.parseInt(capStr);

        // ★ [핵심 1] 최소 5명 체크
        if (cap < 5) throw new InvalidInputException("정원은 최소 5명이어야 합니다.");

        // ★ [핵심 2] (수정 시) 정원이 수강인원보다 적은지 체크
        if (isEditMode && cap < currentEnrolledCount) {
            throw new InvalidInputException("정원은 현재 수강인원(" + currentEnrolledCount + "명) 보다 적을 수 없습니다.");
        }

        boolean isDayChecked = false;
        for (JCheckBox box : dayChecks) if (box.isSelected()) isDayChecked = true;
        if (!isDayChecked) throw new InvalidInputException("요일을 최소 하나 이상 선택해주세요.");
    }

    private void checkDatabaseConflicts(int excludeId, int newTeacherNo, String newRoom, String newDays, int newStart, int newEnd) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");
            String sql = "SELECT lecture_no, teacher_no, classroom_name, day_of_week, start_period, end_period FROM lecture WHERE lecture_no != " + excludeId;
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                int dbTeacher = rs.getInt("teacher_no");
                String dbRoom = rs.getString("classroom_name");
                String dbDays = rs.getString("day_of_week");
                int dbStart = rs.getInt("start_period");
                int dbEnd = rs.getInt("end_period");

                boolean dayOverlap = false;
                for(char c : newDays.toCharArray()) {
                    if(dbDays.indexOf(c) != -1) { dayOverlap = true; break; }
                }

                if (dayOverlap) {
                    if (newStart <= dbEnd && newEnd >= dbStart) {
                        if (newTeacherNo == dbTeacher) throw new InvalidInputException("해당 강사는 이미 수업이 있습니다.\n(" + dbDays + " " + dbStart + "-" + dbEnd + "교시)");
                        if (newRoom.equals(dbRoom)) throw new InvalidInputException("해당 강의실은 이미 사용 중입니다.\n(" + dbDays + " " + dbStart + "-" + dbEnd + "교시)");
                    }
                }
            }
        } finally { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); }
    }

    private void loadTeacherList() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");
            String sql = "SELECT t.teacher_no, m.name FROM teacher t JOIN member m ON t.member_id = m.member_id ORDER BY m.name";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                teacherCombo.addItem(new TeacherItem(rs.getInt("teacher_no"), rs.getString("name")));
            }
        } catch (Exception e) {}
        finally { try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {} }
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