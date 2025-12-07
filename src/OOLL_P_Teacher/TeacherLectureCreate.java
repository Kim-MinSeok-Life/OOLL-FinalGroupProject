// 강사 - 신규 강의 개설
package OOLL_P_Teacher;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;              // JOptionPane 등
import java.sql.Connection;        // DB 연결
import java.sql.PreparedStatement; // PreparedStatement
import java.sql.SQLException;      // 예외 처리

// 신규 강의 개설 창
public class TeacherLectureCreate extends LectureFormBase {

    // 메인 화면(TeacherMain)을 다시 새로고침하기 위해 참조로 가지고 있음
    private TeacherMain owner; // 강사 메인 화면

    // 생성자
    // - owner: 부모창(강사 메인)
    // - teacherNo: 로그인한 강사번호 (lecture.teacher_no에 들어갈 값)
    TeacherLectureCreate(TeacherMain owner, int teacherNo) {
        super(owner, "신규 강의 개설", teacherNo); // 상위 생성자 호출 (모달 다이얼로그)
        this.owner = owner;                       // 나중에 새로고침용으로 사용
        submitBtn.setText("개설");               // 버튼 텍스트 "개설"
    }

    // 실제 DB에 INSERT를 수행하는 로직
    @Override
    protected void onSubmit() throws Exception {

        // ===== 1. 화면에서 값 읽어오기 =====
        // 과목명
        String subject = (String) subjectCb.getSelectedItem();
        // 강의실명
        String room = (String) RoomCb.getSelectedItem();

        // 요일 문자열(예: "월수금") 구성
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dayCB.length; i++) {
            if (dayCB[i].isSelected()) {       // 체크된 요일만
                sb.append(dayCB[i].getText()); // "월", "화" ...
            }
        }
        String days = sb.toString();            // 최종 요일 문자열

        // 시작/종료 교시
        int start = (Integer) startPeriodCb.getSelectedItem();
        int end = (Integer) endPeriodCb.getSelectedItem();

        // 정원 (문자열 → int)
        String capStr = EnrollmentLimit.getText().trim();
        int capacity = Integer.parseInt(capStr); // validateInputs에서 숫자 여부는 이미 검사됨

        // ===== 2. DB에 INSERT =====
        Connection con = null;
        PreparedStatement ps = null;

        try {
            // 공용 DB 연결 유틸 사용 (TeacherMain에서 쓰던 것과 동일)
            con = ConnectDB.getConnection();

            // lecture 테이블에 새 강의 INSERT
            // - lecture_no : AUTO_INCREMENT 가정
            // - enrolled_count : 0명으로 시작
            String sql =
                    "INSERT INTO lecture " +
                            " (teacher_no, subject_name, classroom_name, day_of_week, " +
                            "  start_period, end_period, capacity, enrolled_count) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";

            ps = con.prepareStatement(sql);
            ps.setInt(1, currentTeacherNo); // 로그인한 강사번호
            ps.setString(2, subject);       // 과목명
            ps.setString(3, room);          // 강의실명
            ps.setString(4, days);          // 요일 문자열
            ps.setInt(5, start);            // 시작교시
            ps.setInt(6, end);              // 종료교시
            ps.setInt(7, capacity);         // 정원

            int affected = ps.executeUpdate(); // INSERT 실행

            if (affected <= 0) {
                // 삽입된 행이 없으면 예외 처리
                throw new Exception("강의 개설 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }

            // ===== 3. 성공 메시지 + 메인 화면 강의 목록 새로고침 + 창 닫기 =====
            JOptionPane.showMessageDialog(
                    this,
                    "강의가 성공적으로 개설되었습니다.",
                    "개설 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // 메인 화면의 강의 목록 다시 로딩
            if (owner != null) {
                owner.refreshLectureList(); // ★ TeacherMain에 추가해야 하는 메소드 (아래 참고)
            }

            dispose(); // 창 닫기

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("DB 처리 중 오류가 발생했습니다.\n" + e.getMessage());
        } finally {
            // 자원 정리
            ConnectDB.close(new AutoCloseable[]{ps, con});
        }
    }
}
