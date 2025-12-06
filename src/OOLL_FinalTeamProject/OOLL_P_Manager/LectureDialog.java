// 원장의 강의 개설, 수정 창!!!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// LectureDialog 클래스: 강좌 개설(INSERT) 및 수정(UPDATE)을 위한 팝업창입니다.
// JDialog를 상속받아 모달 창으로 동작하며, ActionListener로 버튼 이벤트를 처리합니다.
public class LectureDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 변수 선언 ---
    JComboBox<String> subjectCombo;      // 과목 선택 (국어, 영어...)
    JComboBox<TeacherItem> teacherCombo; // 강사 선택 (이름은 보이고 값은 번호를 가진 객체)
    JComboBox<String> roomCombo;         // 강의실 선택
    JComboBox<String> startCombo;        // 시작 교시 선택
    JComboBox<String> endCombo;          // 종료 교시 선택
    JTextField capField;                 // 정원 입력창
    JCheckBox[] dayChecks;               // 요일 선택 체크박스 배열 (월~금)

    JButton actionBtn, cancelBtn;        // 실행(개설/수정) 버튼, 취소 버튼

    // --- 상태 관리 변수 ---
    String currentLectureNo = null;      // 수정 시 사용할 강의 번호(PK) 저장용
    boolean isEditMode = false;          // 현재 모드가 '수정'인지 '개설'인지 구분하는 플래그
    int currentEnrolledCount = 0;        // 수정 시, 정원을 현재 수강인원보다 적게 못 줄이게 하기 위한 변수

    // 생성자: 팝업창을 초기화하고 UI를 그립니다.
    // editData가 null이면 [개설 모드], 데이터가 있으면 [수정 모드]로 동작합니다.
    public LectureDialog(JFrame parent, String title, String[] editData) {
        super(parent, title, true); // true: 모달 창 (이 창을 닫기 전엔 뒤쪽 창 클릭 불가)
        setSize(400, 480);          // 창 크기 설정
        setLocationRelativeTo(parent); // 부모 창의 정중앙에 띄움

        // 1. 모드 판별 (개설 vs 수정)
        if (editData != null) {
            isEditMode = true;           // 수정 모드로 설정
            currentLectureNo = editData[0]; // 전달받은 데이터의 0번(PK)을 저장

            // 만약 수강인원 데이터(7번 인덱스)가 있다면 파싱해서 저장 (정원 축소 제한용)
            if(editData.length > 7) {
                currentEnrolledCount = Integer.parseInt(editData[7]);
            }
        }

        // 2. UI 패널 구성
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // 전체 여백
        mainPanel.setBackground(Color.WHITE);

        // 입력 폼 패널 (2열 그리드 레이아웃: 라벨 - 입력창)
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);

        // (1) 과목명 콤보박스
        formPanel.add(new JLabel("과목명:"));
        subjectCombo = new JComboBox<>(new String[]{"- 선택 -", "국어","영어","수학","과학","사회"});
        subjectCombo.setBackground(Color.WHITE);
        formPanel.add(subjectCombo);

        // (2) 담당강사 콤보박스 (DB에서 불러옴)
        formPanel.add(new JLabel("담당강사:"));
        teacherCombo = new JComboBox<>();
        teacherCombo.addItem(new TeacherItem(0, "- 선택 -")); // 기본값 추가
        loadTeacherList(); // ★ DB연동: 강사 목록을 DB에서 가져와 콤보박스에 채움
        teacherCombo.setBackground(Color.WHITE);
        formPanel.add(teacherCombo);

        // (3) 강의실 콤보박스 (고정 목록)
        formPanel.add(new JLabel("강의실:"));
        String[] rooms = {"- 선택 -", "101호", "102호", "103호", "201호", "202호", "203호", "301호", "302호", "303호", "401호", "402호", "403호"};
        roomCombo = new JComboBox<>(rooms);
        roomCombo.setBackground(Color.WHITE);
        formPanel.add(roomCombo);

        // (4) 요일 체크박스 (중복 선택 가능)
        formPanel.add(new JLabel("요일(중복가능):"));
        JPanel dayPanel = new JPanel(new GridLayout(1, 0, 0, 0)); // 체크박스를 한 줄에 배치
        dayPanel.setBackground(Color.WHITE);
        String[] days = {"월", "화", "수", "목", "금"};
        dayChecks = new JCheckBox[5]; // 5개의 체크박스 생성
        for(int i=0; i<5; i++) {
            dayChecks[i] = new JCheckBox(days[i]);
            dayChecks[i].setBackground(Color.WHITE);
            dayChecks[i].setMargin(new Insets(0, 0, 0, 0));
            dayPanel.add(dayChecks[i]);
        }
        formPanel.add(dayPanel);

        // (5) 시작/종료 교시 콤보박스
        String[] periods = {"- 선택 -", "1교시", "2교시", "3교시", "4교시", "5교시", "6교시", "7교시"};
        formPanel.add(new JLabel("시작교시:"));
        startCombo = new JComboBox<>(periods); startCombo.setBackground(Color.WHITE); formPanel.add(startCombo);
        formPanel.add(new JLabel("종료교시:"));
        endCombo = new JComboBox<>(periods); endCombo.setBackground(Color.WHITE); formPanel.add(endCombo);

        // (6) 정원 입력창 (숫자만 입력)
        formPanel.add(new JLabel("정원:")); capField = new JTextField("20"); formPanel.add(capField);

        // 3. [수정 모드일 경우] 기존 데이터 채워넣기
        if (isEditMode) {
            subjectCombo.setSelectedItem(editData[1]); // 과목 선택

            // 강사 이름으로 콤보박스 항목 찾아서 선택
            for (int i=0; i<teacherCombo.getItemCount(); i++) {
                if (teacherCombo.getItemAt(i).toString().equals(editData[2])) {
                    teacherCombo.setSelectedIndex(i);
                    break;
                }
            }
            roomCombo.setSelectedItem(editData[3]); // 강의실 선택

            // 요일 체크 ("월수금" 문자열에 해당 글자가 있으면 체크)
            String dayStr = editData[4];
            for(int i=0; i<5; i++) {
                if(dayStr.contains(dayChecks[i].getText())) dayChecks[i].setSelected(true);
            }

            // 시간 설정 ("1-3교시" -> 분리해서 선택)
            try {
                String[] times = editData[5].replace("교시", "").split("-");
                startCombo.setSelectedItem(times[0] + "교시");
                endCombo.setSelectedItem(times[1] + "교시");
            } catch (Exception e) {} // 파싱 에러 무시

            capField.setText(editData[6]); // 정원 채움
        }

        // 4. 하단 버튼 패널 구성
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // 모드에 따라 버튼 글씨 다르게 ("수정" or "개설")
        String btnText = isEditMode ? "수정" : "개설";
        actionBtn = new JButton(btnText);
        actionBtn.setBackground(Color.WHITE);
        actionBtn.setPreferredSize(new Dimension(80, 35));
        actionBtn.addActionListener(this); // 리스너 연결

        cancelBtn = new JButton("취소");
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.addActionListener(this); // 리스너 연결

        btnPanel.add(actionBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    // ★ ActionListener 구현: 버튼 클릭 이벤트 처리
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose(); // 취소 시 창 닫기
        } else if (e.getSource() == actionBtn) {
            // 실행(개설/수정) 버튼 클릭 시 로직
            try {
                // 1. 입력값 검증 (빈칸, 논리 오류 등 체크) -> 문제시 예외 발생
                validateInputs();

                // 2. UI 데이터 수집 및 가공
                String subject = (String) subjectCombo.getSelectedItem();
                TeacherItem selectedTeacher = (TeacherItem) teacherCombo.getSelectedItem();
                String room = (String) roomCombo.getSelectedItem();

                // "1교시" 문자열에서 "교시"를 제거하고 숫자로 변환 (DB 저장을 위해)
                int start = Integer.parseInt(((String)startCombo.getSelectedItem()).replace("교시",""));
                int end = Integer.parseInt(((String)endCombo.getSelectedItem()).replace("교시",""));
                String capStr = capField.getText();

                // 체크된 요일들을 문자열로 합침 (예: "월수금")
                String dayStr = "";
                for(JCheckBox box : dayChecks) if(box.isSelected()) dayStr += box.getText();

                // 3. ★ 중복 체크 (DB 조회)
                // 수정 모드일 경우, 자기 자신(PK)은 중복 검사에서 제외해야 함 (-1 or ID)
                int excludeId = isEditMode ? Integer.parseInt(currentLectureNo) : -1;
                // (강의실, 시간, 강사가 겹치는지 DB에서 확인 -> 겹치면 예외 던짐)
                checkDatabaseConflicts(excludeId, selectedTeacher.no, room, dayStr, start, end);

                // 4. DB 연결 및 저장 (모든 검사를 통과한 경우에만 실행됨)
                Connection con = null;
                Statement stmt = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                    String sql;
                    if (isEditMode) {
                        // 수정 모드: UPDATE 쿼리
                        sql = "UPDATE lecture SET subject_name='" + subject + "', teacher_no=" + selectedTeacher.no +
                                ", classroom_name='" + room + "', day_of_week='" + dayStr +
                                "', start_period=" + start + ", end_period=" + end + ", capacity=" + capStr +
                                " WHERE lecture_no=" + currentLectureNo;
                    } else {
                        // 개설 모드: INSERT 쿼리
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
                // validateInputs()나 checkDatabaseConflicts()에서 던진 예외를 잡아서 경고창 띄움
                JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 오류", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ★ [메소드] 입력값 유효성 검사 (문제가 있으면 예외를 던짐)
    private void validateInputs() throws InvalidInputException {
        // 콤보박스 선택 여부 확인 ("- 선택 -" 인 경우 에러)
        if (subjectCombo.getSelectedIndex() == 0) throw new InvalidInputException("과목명을 선택해주세요.");
        TeacherItem item = (TeacherItem) teacherCombo.getSelectedItem();
        if (item == null || item.no == 0) throw new InvalidInputException("담당강사를 선택해주세요.");
        if (roomCombo.getSelectedIndex() == 0) throw new InvalidInputException("강의실을 선택해주세요.");
        if (startCombo.getSelectedIndex() == 0) throw new InvalidInputException("시작 교시를 선택해주세요.");
        if (endCombo.getSelectedIndex() == 0) throw new InvalidInputException("종료 교시를 선택해주세요.");

        // 교시 논리 검사 (시작 > 종료 불가)
        int s = Integer.parseInt(((String)startCombo.getSelectedItem()).replace("교시",""));
        int e = Integer.parseInt(((String)endCombo.getSelectedItem()).replace("교시",""));
        if (s > e) throw new InvalidInputException("시작 교시가 종료 교시보다 늦을 수 없습니다.");

        // 정원 검사 (숫자 여부, 최소 인원 등)
        String capStr = capField.getText().trim();
        if (capStr.isEmpty()) throw new InvalidInputException("정원을 입력해주세요.");
        if (!capStr.matches("\\d+")) throw new InvalidInputException("정원은 숫자만 입력 가능합니다.");

        int cap = Integer.parseInt(capStr);
        if (cap < 5) throw new InvalidInputException("정원은 최소 5명이어야 합니다.");

        // ★ [중요] 수정 시, 현재 수강인원보다 정원을 적게 설정할 수 없음
        if (isEditMode && cap < currentEnrolledCount) {
            throw new InvalidInputException("정원은 현재 수강인원(" + currentEnrolledCount + "명) 보다 적을 수 없습니다.");
        }

        // 요일 체크 여부 (최소 하나는 체크해야 함)
        boolean isDayChecked = false;
        for (JCheckBox box : dayChecks) if (box.isSelected()) isDayChecked = true;
        if (!isDayChecked) throw new InvalidInputException("요일을 최소 하나 이상 선택해주세요.");
    }

    // ★ [메소드] 중복 강의 체크 (시간, 장소, 강사 충돌 확인)
    // excludeId: 수정 시 자기 자신은 검사에서 제외하기 위한 ID
    private void checkDatabaseConflicts(int excludeId, int newTeacherNo, String newRoom, String newDays, int newStart, int newEnd) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

            // 기존 모든 강의 정보 조회 (내 강의 제외)
            String sql = "SELECT lecture_no, teacher_no, classroom_name, day_of_week, start_period, end_period FROM lecture WHERE lecture_no != " + excludeId;
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                int dbTeacher = rs.getInt("teacher_no");
                String dbRoom = rs.getString("classroom_name");
                String dbDays = rs.getString("day_of_week");
                int dbStart = rs.getInt("start_period");
                int dbEnd = rs.getInt("end_period");

                // 1. 요일이 겹치는지 확인 (문자열 포함 여부)
                boolean dayOverlap = false;
                for(char c : newDays.toCharArray()) {
                    if(dbDays.indexOf(c) != -1) { dayOverlap = true; break; }
                }

                if (dayOverlap) {
                    // 2. 시간대가 겹치는지 확인 (교시 범위 비교)
                    // (새시작 <= 기존끝) AND (새끝 >= 기존시작) 공식 사용
                    if (newStart <= dbEnd && newEnd >= dbStart) {
                        // 3. 강사가 같거나, 강의실이 같으면 충돌!
                        if (newTeacherNo == dbTeacher) throw new InvalidInputException("해당 강사는 이미 수업이 있습니다.\n(" + dbDays + " " + dbStart + "-" + dbEnd + "교시)");
                        if (newRoom.equals(dbRoom)) throw new InvalidInputException("해당 강의실은 이미 사용 중입니다.\n(" + dbDays + " " + dbStart + "-" + dbEnd + "교시)");
                    }
                }
            }
        } finally { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); }
    }

    // [DB] 강사 목록 불러오기 (콤보박스 채우기용)
    private void loadTeacherList() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");
            // teacher 테이블과 member 테이블을 조인하여 강사 번호와 이름을 가져옴
            String sql = "SELECT t.teacher_no, m.name FROM teacher t JOIN member m ON t.member_id = m.member_id ORDER BY m.name";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                // TeacherItem 객체로 만들어서 콤보박스에 추가
                teacherCombo.addItem(new TeacherItem(rs.getInt("teacher_no"), rs.getString("name")));
            }
        } catch (Exception e) {}
        finally { try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {} }
    }

    // [내부 클래스] 사용자 정의 예외 (유효성 검증 실패 시 사용)
    // static으로 선언하여 외부 클래스 인스턴스 없이도 사용 가능하게 함
    private static class InvalidInputException extends Exception {
        public InvalidInputException(String message) { super(message); }
    }

    // [내부 클래스] 콤보박스용 강사 아이템
    // 화면에는 이름(name)을 보여주고, 실제 값은 번호(no)를 사용하기 위함
    class TeacherItem {
        int no; String name;
        public TeacherItem(int no, String name) { this.no = no; this.name = name; }
        // 콤보박스는 toString()의 리턴값을 화면에 표시함
        @Override public String toString() { return name; }
    }
}