// 강사 - [추상클래스: 강의 개설 및 수정 클래스 상속용]
package OOLL_P_Teacher;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

// 신규 강의 개설 & 기존 강의 수정 창들에 대한 수퍼 클래스
public abstract class LectureFormBase extends JDialog implements ActionListener {

    protected int currentTeacherNo; // 로그인한 강사번호
    protected boolean editMode = false; // 수정 모드 여부
    protected int editingLectureNo = -1; // 수정 중인 강의번호

    // 데이터 값
    String[] subjectName={"과목을 선택하세요.","국어", "수학", "영어", "사회", "과학"};
    String[] className = {"강의실을 선택하세요.",
            "101호", "102호", "103호",
            "201호", "202호", "203호",
            "301호", "302호", "303호",
            "401호", "402호", "403호"
            };
        // 콤보박스는 1차원만 가능.
    String[] days = {"월", "화", "수", "목", "금", "토", "일"};
    Integer[] period = {1,2,3,4,5,6,7};

    // 공통 UI
    /// 사용자가 입력하는 값
    JComboBox<String>subjectCb; // 과목명
    JComboBox<String> RoomCb; // 강의실명
    JCheckBox[] dayCB; // 요일 [체크박스] [배열]
    JComboBox<Integer>startPeriodCb; // 시작교시
    JComboBox<Integer>endPeriodCb; // 종료교시
    JTextField EnrollmentLimit; // 정원
    JButton submitBtn, cancelBtn;

    // 신규 강의 개설용 (생성자)
    LectureFormBase(JFrame owner, String title, int teacherNo) {
        super(owner, title, true);
        this.currentTeacherNo = teacherNo; // 강사번호(강의테이블) 저장

        setSize(450, 350);
        setLocationRelativeTo(owner); // 부모 기준 중앙배치

        Container ct = getContentPane();
        ct.setLayout(new BorderLayout());
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        top.setLayout(new GridLayout(6, 1));
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JPanel p1 = new JPanel(); // 과목명
        p1.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l1 = new JLabel("과목명");
        subjectCb = new JComboBox<>(subjectName);
        p1.add(l1); p1.add(subjectCb);

        JPanel p2 = new JPanel(); // 강의실
        p2.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l2 = new JLabel("강의실");
        RoomCb = new JComboBox<>(className);
        p2.add(l2); p2.add(RoomCb);

        JPanel p3 = new JPanel(); // 요일
        p3.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l3 = new JLabel("요일");
        p3.add(l3);
        dayCB = new JCheckBox[days.length];
        for (int i = 0; i < days.length; i++) {
            dayCB[i] = new JCheckBox(days[i]); // 월~일
            p3.add(dayCB[i]);
        }

        JPanel p4 = new JPanel(); // 시작교시
        p4.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l4 = new JLabel("시작교시");
        startPeriodCb = new JComboBox<>(period);
        p4.add(l4); p4.add(startPeriodCb);

        JPanel p5 = new JPanel(); // 종료교시
        p5.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l5 = new JLabel("종료교시");
        endPeriodCb = new JComboBox<>(period);
        p5.add(l5); p5.add(endPeriodCb);

        JPanel p6 = new JPanel(); // 정원
        p6.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l6 = new JLabel("정원");
        EnrollmentLimit = new JTextField(5);
        p6.add(l6); p6.add(EnrollmentLimit);

        top.add(p1); top.add(p2); top.add(p3);
        top.add(p4); top.add(p5); top.add(p6);

        submitBtn = new JButton(); // 이름 부여는 상속 후
        // submitBtn.setText("개설"); // 추후에 서브 클래스에서
        cancelBtn = new JButton("닫기");
        submitBtn.addActionListener(this);
        cancelBtn.addActionListener(this);
        bottom.add(submitBtn); bottom.add(cancelBtn);

        ct.add(top, BorderLayout.CENTER);
        ct.add(bottom, BorderLayout.SOUTH);
    } // 생성자 끝

    // 기존 강의 수정용 (생성자)
    LectureFormBase(JFrame owner, String title, int teacherNo, int lectureNo){
        this(owner, title, teacherNo); // 신규 강의 개설용 생성자 재사용
        this.editMode = true;
        this.editingLectureNo = lectureNo; // 지금 수정 중인 강의번호 기억
    }
    
    
    
    public void actionPerformed(ActionEvent e) {
        
        // 취소 버튼 클릭 시 창닫기
        if (e.getSource() == cancelBtn) {
            dispose();
            return;
        } // if 취소버튼

        // [오버라이딩 필요] 개설 또는 적용 버튼 클릭 시
        if (e.getSource() == submitBtn) {
            try {
                validateInputs(); // 입력값 유효성 검사
                checkConflictsInDB(); // DB 충돌 검사
                onSubmit(); // 서브 클래스에서 오버라이딩
            } // try
            catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
            } // catch
        } // if 개설 또는 적용버튼
    } // actionPerformed 끝

    // 입력값 유효성 검사 메소드
    protected void validateInputs() throws Exception {
        // 콤보박스 선택 여부
        if (    subjectCb.getSelectedIndex() == 0 ||
                RoomCb.getSelectedIndex() == 0) {
            throw new Exception("선택하지 않은 항목이 존재합니다.");
            // 시작교시와 종료교시는 기본이 0번째 인덱스로 되어있으므로 설정할 필요없음
        } // if
        
        // 요일 선택 여부
        boolean anyDay = false;
        for (JCheckBox cb : dayCB) { // 체크박스 내용을 전부 검사
            if (cb.isSelected()) {
                anyDay = true; // 하나라도 선택이 됐으면
                break;
            }
        }
        if (!anyDay) throw new Exception("요일을 최소 하나 이상 선택해야 합니다.");

        // 교시 유효성 여부
        int start = (int) startPeriodCb.getSelectedItem();
        int end = (int) endPeriodCb.getSelectedItem();
        if (start > end) throw new Exception("시작교시는 종료교시보다 늦을 수 없습니다.");
        
        // 정원 조건
        ///  int형 외의 숫자 사용불가
        String capStr = EnrollmentLimit.getText().trim(); // 공백 제외하고 내용담기
        if(!capStr.matches("\\d+")) throw new Exception("정원은 숫자만 입력 가능합니다.");
        
        ///  5명 이상, 200명 이하
        int cap = Integer.parseInt(capStr);
        if (cap < 5 || cap > 200) // 5명 미만 또는 200명 초과
            throw new Exception("정원은 최소 5명, 최대 200명까지 가능합니다.");
    } // 유효성 검사 메소드 끝

    ///  DB 충돌 검사
    protected void checkConflictsInDB() throws Exception {
        String newRoom = (String) RoomCb.getSelectedItem();
        
        // 요일 문자열 만들기
        StringBuilder sb = new StringBuilder();
        // 문자열을 붙여도 깨짐을 방지하는 가변 문자열 저장 객체
        
        for (int i = 0; i < dayCB.length; i++) {
            JCheckBox cb = dayCB[i]; // "월" ~ "일"까지 순서대로 검사
            if (cb.isSelected()) // 사용자가 해당 요일을 선택할 시
                sb.append(cb.getText()); // 텍스트 이어붙임(ex: "월" → "월수" → "월수목")
        }
        String newDays = sb.toString(); // 조립된 결과를 문자열로 변환하여 저장

        /* for-each문 표현
        for (JCheckBox cb : dayCB)
            if (cb.isSelected()) sb.append(cb.getText());
        String newDays = sb.toString();
        */
        
        int newStart = (int) startPeriodCb.getSelectedItem();
        int newEnd = (int) endPeriodCb.getSelectedItem();

        // DB 연결
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC",
                "root", "java2025"
        );
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT lecture_no, teacher_no, classroom_name, day_of_week, start_period, end_period FROM lecture"
        );



        /*
        /// 현재 DB의 강의 테이블 데이터들을 rs에 전부 가져옴.
        /// rs.next()는 각 행을 한 번씩 읽으며 검사함. [반복문]
        // SELECT 실행 결과를 ResultSet으로 가져온다
        // ResultSet이 가지는 커서로 읽어오기 위해
        // 커서 위치를 행으로 이동하여 행의 여부를 boolean으로 반환
        */
        while (rs.next()) { // 조회된 모든 행을 하나씩 읽으면서 처리 [즉, 조회될 시]

            int dbLectureNo = rs.getInt("lecture_no");
            // 수정용일 때는 자기 자신을 건너뛴다
            if (editMode && dbLectureNo == editingLectureNo)
                continue;

            /// DB에서 기존 강의 정보 가져오기
            String dbRoom = rs.getString("classroom_name");
            String dbDays = rs.getString("day_of_week");
            int dbStart = rs.getInt("start_period");
            int dbEnd = rs.getInt("end_period");
            int dbTeacher = rs.getInt("teacher_no");


            /// 1. 요일 겹침 여부 확인 로직
            boolean overlapDay = false; // 요일 겹침 여부 상태: 겹칠 시 true
            for (int i = 0; i < newDays.length(); i++) {
                // 문자열을 문자 단위로 나눠서 검사하기 위함.
                // DB 요일 문자열 안에 이 문자가 존재하면 요일 겹침으로 판단
                char c = newDays.charAt(i); // 조립된 문자열의 첫번째 문자부터 끝까지 하나씩 넣고
                if (dbDays.indexOf(c) != -1){ // DB의 요일 문자열인 dbDays 안에 문자가 존재하는지
                    // String.indexOf() 는 찾으면 위치를 반환하고(배열번호 예: 1번째면 0을 반환)
                    // 찾지 못하면 -1을 반환함
                    // break에 걸리지 않는다면 겹치는 문자를 찾지 못 한 것.

                    overlapDay = true; // 겹침으로 인식되는 순간 true로 변경하고
                    break; // 검사 종료
                }
            } // for
            if (!overlapDay) continue;
            // 요일이 안 겹치면 충돌이 발생하지 않으므로 나머지 검사를 생략하고 다음 행 검사로.
            // 요일 또는 시간 중에 하나만 겹치지 않아도 충돌이 발생하지 않음.
            
            /// 2. 시간대 겹침 여부 확인 로직
            if ( !(newStart <= dbEnd && newEnd >= dbStart) )
                continue;
            // 시간이 안 겹치면 충돌이 발생하지 않으므로 나머지 검사를 생략하고 다음 행 검사로.
            // 요일 또는 시간 중에 하나만 겹치지 않아도 충돌이 발생하지 않음.

            /// 3. 요일&시간이 모두 겹치는 경우
            /// 3-1. 로그인한 본인의 강의일 경우 → 수업중이므로 불가능
            if (dbTeacher == currentTeacherNo) {
                throw new Exception(
                        "해당 시간에는 이미 다른 강의를 진행 중입니다.\n" +
                                "(" + dbDays + " " + dbStart + "~" + dbEnd + "교시)"
                );
            }

            /// 3-2. 다른 강사의 강의일 경우 → 강의실이 겹치면 불가능, 안 겹치면 가능
            if (newRoom.equals(dbRoom)) { // 강의실이 겹치면
                throw new Exception(
                        "해당 강의실은 이미 사용 중입니다.\n" +
                                "(" + dbDays + " " + dbStart + "~" + dbEnd + "교시)"
                );
            }
        } // while
        rs.close();
        stmt.close();
        con.close();
    } // checkConflictsInDB

    // [오버라이딩 필요]
    protected abstract void onSubmit() throws Exception;
    
} // LectureFormBase
