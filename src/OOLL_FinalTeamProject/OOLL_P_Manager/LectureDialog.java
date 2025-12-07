// 원장의 강의 개설, 수정 창!!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * [LectureDialog 클래스]
 * 역할: 신규 강좌를 개설(INSERT)하거나 기존 강좌 정보를 수정(UPDATE)하는 팝업창.
 * 특징:
 * 1. 월~일 요일 선택 기능 (체크박스).
 * 2. 강의실/시간/강사 중복 여부를 DB에서 조회하여 방지하는 로직 포함.
 * 3. 입력값 유효성 검사 (정원, 시간 순서 등) 적용.
 */
public class LectureDialog extends JDialog implements ActionListener {

    // --- [UI 컴포넌트 선언] ---
    JComboBox<String> subjectCombo;      // 과목 선택 (국어, 영어 등)
    JComboBox<TeacherItem> teacherCombo; // 강사 선택 (TeacherItem 객체 사용)
    JComboBox<String> roomCombo;         // 강의실 선택
    JComboBox<String> startCombo;        // 시작 교시
    JComboBox<String> endCombo;          // 종료 교시
    JTextField capField;                 // 수강 정원 입력
    JCheckBox[] dayChecks;               // 요일 선택 체크박스 배열 (월~일)

    JButton actionBtn, cancelBtn;        // 등록/수정 버튼, 취소 버튼

    // --- [데이터 관리 변수] ---
    String currentLectureNo = null;      // 수정 모드일 때, 대상 강의의 PK(번호)
    boolean isEditMode = false;          // 현재 창이 '수정' 모드인지 '개설' 모드인지 판별
    int currentEnrolledCount = 0;        // (수정 시) 현재 수강 중인 인원수 (정원 축소 제한용)

    /**
     * [생성자]
     * @param parent   : 부모 프레임
     * @param title    : 창 제목
     * @param editData : 수정할 데이터 배열 (null이면 개설 모드)
     */
    public LectureDialog(JFrame parent, String title, String[] editData) {
        super(parent, title, true); // true: 모달(Modal) 창 설정 (이 창을 닫기 전엔 부모 창 제어 불가)

        // ★ [UI 설정] 요일 체크박스가 7개(월~일)로 늘어남에 따라 가로 폭을 400 -> 480으로 확장
        setSize(480, 480);
        setLocationRelativeTo(parent); // 화면 정중앙 배치

        // 1. 모드 판별 (데이터가 넘어왔으면 수정 모드)
        if (editData != null) {
            isEditMode = true;
            currentLectureNo = editData[0]; // PK 저장

            // 현재 수강 인원 파싱 (정원 유효성 검사 때 사용)
            if(editData.length > 7) {
                currentEnrolledCount = Integer.parseInt(editData[7]);
            }
        }

        // 2. 메인 패널 구성 (전체 레이아웃)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // 여백 설정
        mainPanel.setBackground(Color.WHITE);

        // 3. 입력 폼 패널 (2열 그리드)
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);

        // (1) 과목명
        formPanel.add(new JLabel("과목명:"));
        subjectCombo = new JComboBox<>(new String[]{"- 선택 -", "국어","영어","수학","과학","사회"});
        subjectCombo.setBackground(Color.WHITE);
        formPanel.add(subjectCombo);

        // (2) 담당강사 (DB에서 목록 로드)
        formPanel.add(new JLabel("담당강사:"));
        teacherCombo = new JComboBox<>();
        teacherCombo.addItem(new TeacherItem(0, "- 선택 -"));
        loadTeacherList(); // ★ DB 연결하여 강사 목록 가져오기
        teacherCombo.setBackground(Color.WHITE);
        formPanel.add(teacherCombo);

        // (3) 강의실
        formPanel.add(new JLabel("강의실:"));
        String[] rooms = {"- 선택 -", "101호", "102호", "103호", "201호", "202호", "203호", "301호", "302호", "303호", "401호", "402호", "403호"};
        roomCombo = new JComboBox<>(rooms);
        roomCombo.setBackground(Color.WHITE);
        formPanel.add(roomCombo);

        // (4) ★ [요일 체크박스] 월~일 7개 생성
        formPanel.add(new JLabel("요일(중복가능):"));
        JPanel dayPanel = new JPanel(new GridLayout(1, 0, 0, 0)); // 한 줄에 배치
        dayPanel.setBackground(Color.WHITE);

        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        dayChecks = new JCheckBox[7]; // 배열 크기 7 할당

        for(int i=0; i<7; i++) {
            dayChecks[i] = new JCheckBox(days[i]);
            dayChecks[i].setBackground(Color.WHITE);
            dayChecks[i].setMargin(new Insets(0, 0, 0, 0));
            dayPanel.add(dayChecks[i]);
        }
        formPanel.add(dayPanel);

        // (5) 교시 선택
        String[] periods = {"- 선택 -", "1교시", "2교시", "3교시", "4교시", "5교시", "6교시", "7교시"};
        formPanel.add(new JLabel("시작교시:"));
        startCombo = new JComboBox<>(periods); startCombo.setBackground(Color.WHITE); formPanel.add(startCombo);
        formPanel.add(new JLabel("종료교시:"));
        endCombo = new JComboBox<>(periods); endCombo.setBackground(Color.WHITE); formPanel.add(endCombo);

        // (6) 정원 입력
        formPanel.add(new JLabel("정원:")); capField = new JTextField("20"); formPanel.add(capField);

        // 4. [수정 모드일 경우] 기존 데이터로 UI 채우기 (Pre-fill)
        if (isEditMode) {
            subjectCombo.setSelectedItem(editData[1]); // 과목

            // 강사 이름 매칭하여 선택
            for (int i=0; i<teacherCombo.getItemCount(); i++) {
                if (teacherCombo.getItemAt(i).toString().equals(editData[2])) {
                    teacherCombo.setSelectedIndex(i);
                    break;
                }
            }
            roomCombo.setSelectedItem(editData[3]); // 강의실

            // ★ 요일 체크박스 복원 (DB 문자열 "월수" -> 월, 수 체크)
            String dayStr = editData[4];
            for(int i=0; i<7; i++) {
                if(dayStr.contains(dayChecks[i].getText())) dayChecks[i].setSelected(true);
            }

            // 교시 복원 ("1-3교시" -> 1교시, 3교시)
            try {
                String[] times = editData[5].replace("교시", "").split("-");
                startCombo.setSelectedItem(times[0] + "교시");
                endCombo.setSelectedItem(times[1] + "교시");
            } catch (Exception e) {}

            capField.setText(editData[6]); // 정원
        }

        // 5. 하단 버튼 패널
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

        // 패널 조립
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    // ★ ActionListener 구현부
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 시 닫기
        } else if (e.getSource() == actionBtn) {
            try {
                // 1. [유효성 검사] 입력값이 올바른지 체크 (문제 시 예외 발생)
                validateInputs();

                // 2. [데이터 수집] UI에서 값 가져오기
                String subject = (String) subjectCombo.getSelectedItem();
                TeacherItem selectedTeacher = (TeacherItem) teacherCombo.getSelectedItem();
                String room = (String) roomCombo.getSelectedItem();

                // "1교시" -> 1 (숫자 변환)
                int start = Integer.parseInt(((String)startCombo.getSelectedItem()).replace("교시",""));
                int end = Integer.parseInt(((String)endCombo.getSelectedItem()).replace("교시",""));
                String capStr = capField.getText();

                // 요일 문자열 생성 (체크된 것들만 합침. 예: "토일")
                String dayStr = "";
                for(JCheckBox box : dayChecks) if(box.isSelected()) dayStr += box.getText();

                // 3. [중복 체크] DB에서 시간/장소/강사 충돌 확인
                int excludeId = isEditMode ? Integer.parseInt(currentLectureNo) : -1;
                checkDatabaseConflicts(excludeId, selectedTeacher.no, room, dayStr, start, end);

                // 4. [DB 저장] 모든 검사를 통과하면 실행
                Connection con = null;
                Statement stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                    String sql;
                    if (isEditMode) {
                        // 수정 (UPDATE)
                        sql = "UPDATE lecture SET subject_name='" + subject + "', teacher_no=" + selectedTeacher.no +
                                ", classroom_name='" + room + "', day_of_week='" + dayStr +
                                "', start_period=" + start + ", end_period=" + end + ", capacity=" + capStr +
                                " WHERE lecture_no=" + currentLectureNo;
                    } else {
                        // 개설 (INSERT)
                        sql = "INSERT INTO lecture (subject_name, teacher_no, classroom_name, day_of_week, start_period, end_period, capacity) " +
                                "VALUES ('" + subject + "', " + selectedTeacher.no + ", '" + room + "', '" + dayStr + "', " + start + ", " + end + ", " + capStr + ")";
                    }

                    stmt = con.createStatement();
                    stmt.executeUpdate(sql);

                    String msg = isEditMode ? "수정되었습니다." : "개설되었습니다.";
                    JOptionPane.showMessageDialog(this, msg, "성공", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // 성공 시 창 닫기

                } catch (Exception err) {
                    err.printStackTrace();
                    JOptionPane.showMessageDialog(this, "DB 오류: " + err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try { if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
                }

            } catch (InvalidInputException ex) {
                // 검증 실패 시 경고창 표시
                JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ★ [유효성 검사 메소드] (throws Exception 패턴 적용)
    private void validateInputs() throws InvalidInputException {
        // 필수 선택 체크
        if (subjectCombo.getSelectedIndex() == 0) throw new InvalidInputException("과목명을 선택해주세요.");
        TeacherItem item = (TeacherItem) teacherCombo.getSelectedItem();
        if (item == null || item.no == 0) throw new InvalidInputException("담당강사를 선택해주세요.");
        if (roomCombo.getSelectedIndex() == 0) throw new InvalidInputException("강의실을 선택해주세요.");
        if (startCombo.getSelectedIndex() == 0) throw new InvalidInputException("시작 교시를 선택해주세요.");
        if (endCombo.getSelectedIndex() == 0) throw new InvalidInputException("종료 교시를 선택해주세요.");

        // 시간 논리 체크
        int s = Integer.parseInt(((String)startCombo.getSelectedItem()).replace("교시",""));
        int e = Integer.parseInt(((String)endCombo.getSelectedItem()).replace("교시",""));
        if (s > e) throw new InvalidInputException("시작 교시가 종료 교시보다 늦을 수 없습니다.");

        // 정원 숫자 및 최소값 체크
        String capStr = capField.getText().trim();
        if (capStr.isEmpty()) throw new InvalidInputException("정원을 입력해주세요.");
        if (!capStr.matches("\\d+")) throw new InvalidInputException("정원은 숫자만 입력 가능합니다.");

        int cap = Integer.parseInt(capStr);
        if (cap < 5) throw new InvalidInputException("정원은 최소 5명이어야 합니다.");

        // 수정 시 현재 수강인원보다 적게 줄일 수 없음
        if (isEditMode && cap < currentEnrolledCount) {
            throw new InvalidInputException("정원은 현재 수강인원(" + currentEnrolledCount + "명) 보다 적을 수 없습니다.");
        }

        // 요일 최소 1개 선택 체크
        boolean isDayChecked = false;
        for (JCheckBox box : dayChecks) if (box.isSelected()) isDayChecked = true;
        if (!isDayChecked) throw new InvalidInputException("요일을 최소 하나 이상 선택해주세요.");
    }

    // ★ [중복 강의 체크 로직]
    // DB에 있는 다른 강의들과 시간/장소/강사가 겹치는지 전수 조사
    private void checkDatabaseConflicts(int excludeId, int newTeacherNo, String newRoom, String newDays, int newStart, int newEnd) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

            // 내 강의(excludeId)를 제외한 모든 강의 조회
            String sql = "SELECT lecture_no, teacher_no, classroom_name, day_of_week, start_period, end_period FROM lecture WHERE lecture_no != " + excludeId;
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                int dbTeacher = rs.getInt("teacher_no");
                String dbRoom = rs.getString("classroom_name");
                String dbDays = rs.getString("day_of_week");
                int dbStart = rs.getInt("start_period");
                int dbEnd = rs.getInt("end_period");

                // 1. 요일 중복 여부 확인 (문자열 포함 관계)
                boolean dayOverlap = false;
                for(char c : newDays.toCharArray()) {
                    if(dbDays.indexOf(c) != -1) { dayOverlap = true; break; }
                }

                if (dayOverlap) {
                    // 2. 교시 중복 여부 확인 (범위 교차 검사)
                    // 공식: (새시작 <= 기존끝) AND (새끝 >= 기존시작)
                    if (newStart <= dbEnd && newEnd >= dbStart) {
                        // 3. 강사 중복 또는 강의실 중복 체크
                        if (newTeacherNo == dbTeacher) throw new InvalidInputException("해당 강사는 이미 수업이 있습니다.\n(" + dbDays + " " + dbStart + "-" + dbEnd + "교시)");
                        if (newRoom.equals(dbRoom)) throw new InvalidInputException("해당 강의실은 이미 사용 중입니다.\n(" + dbDays + " " + dbStart + "-" + dbEnd + "교시)");
                    }
                }
            }
        } finally { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); }
    }

    // [DB] 강사 목록 불러오기 (콤보박스용)
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

    // [예외 클래스] 사용자 정의 예외
    private static class InvalidInputException extends Exception {
        public InvalidInputException(String message) { super(message); }
    }

    // [헬퍼 클래스] 콤보박스 아이템 (보이는 건 이름, 값은 번호)
    class TeacherItem {
        int no; String name;
        public TeacherItem(int no, String name) { this.no = no; this.name = name; }
        @Override public String toString() { return name; }
    }
}