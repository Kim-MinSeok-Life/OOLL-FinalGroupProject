// 강사 - 기존 강의 수정
package OOLL_P_Teacher;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;              // JOptionPane
import java.awt.event.ActionEvent; // ActionEvent
import java.sql.Connection;        // DB 연결
import java.sql.PreparedStatement; // PreparedStatement
import java.sql.ResultSet;         // ResultSet
import java.sql.SQLException;      // SQLException

// 기존 강의 수정 창
public class TeacherLectureEdit extends LectureFormBase {

    // 메인 화면(TeacherMain) 참조 → 수정 후 테이블 새로고침용
    private TeacherMain owner;
    // 어떤 강의를 수정하는지 식별하기 위한 lecture_no
    private int lectureNo;
    // 현재 수강인원 (정원보다 작게 변경 못 하도록 검사용)
    private int enrolledCount;

    // 생성자
    // - owner: 부모창(TeacherMain)
    // - teacherNo: 로그인한 강사번호
    // - lectureNo: 수정할 강의의 PK(lecture_no)
    TeacherLectureEdit(TeacherMain owner, int teacherNo, int lectureNo) {
        super(owner, "강의 수정", teacherNo, lectureNo); // 상위 생성자 호출
        this.owner = owner;
        this.lectureNo = lectureNo;

        submitBtn.setText("적용"); // 버튼 텍스트 "적용"

        // 다이얼로그가 뜰 때, 기존 강의 정보를 DB에서 읽어와 UI에 세팅
        loadLectureData();
    }

    // ===== 기존 강의 정보 → UI 컴포넌트에 세팅 =====
    private void loadLectureData() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConnectDB.getConnection();

            String sql =
                    "SELECT subject_name, classroom_name, day_of_week, " +
                            "       start_period, end_period, capacity, enrolled_count " +
                            "FROM lecture " +
                            "WHERE lecture_no = ?";

            ps = con.prepareStatement(sql);
            ps.setInt(1, lectureNo); // 수정할 강의 번호
            rs = ps.executeQuery();

            if (rs.next()) {
                // DB에서 읽어온 값
                String subject = rs.getString("subject_name");      // 과목명
                String room = rs.getString("classroom_name");       // 강의실
                String dbDays = rs.getString("day_of_week");        // 요일 문자열 (예: "월수금")
                int start = rs.getInt("start_period");              // 시작교시
                int end = rs.getInt("end_period");                  // 종료교시
                int capacity = rs.getInt("capacity");               // 정원
                this.enrolledCount = rs.getInt("enrolled_count");   // 현재 수강인원

                // 1) 과목명 콤보박스 선택 세팅
                int subjectIndex = 0; // 기본값: 0 ("과목을 선택하세요.")
                for (int i = 0; i < subjectName.length; i++) {
                    if (subjectName[i].equals(subject)) {
                        subjectIndex = i;
                        break;
                    }
                }
                subjectCb.setSelectedIndex(subjectIndex);

                // 2) 강의실 콤보박스 선택 세팅
                int roomIndex = 0; // 기본값: 0 ("강의실을 선택하세요.")
                for (int i = 0; i < className.length; i++) {
                    if (className[i].equals(room)) {
                        roomIndex = i;
                        break;
                    }
                }
                RoomCb.setSelectedIndex(roomIndex);

                // 3) 요일 체크박스 세팅
                //    - 일단 전체 해제 후, DB 문자열의 각 문자에 해당하는 체크박스를 찾아 체크
                for (int i = 0; i < dayCB.length; i++) {
                    dayCB[i].setSelected(false); // 초기화
                }
                if (dbDays != null) {
                    for (int i = 0; i < dbDays.length(); i++) {
                        char c = dbDays.charAt(i);          // 예: '월'
                        String dayText = String.valueOf(c);  // "월"
                        for (int j = 0; j < dayCB.length; j++) {
                            if (dayCB[j].getText().equals(dayText)) {
                                dayCB[j].setSelected(true); // 해당 요일 체크
                                break;
                            }
                        }
                    }
                }

                // 4) 시작/종료 교시 콤보박스 세팅
                startPeriodCb.setSelectedItem(start);
                endPeriodCb.setSelectedItem(end);

                // 5) 정원 텍스트필드 세팅
                EnrollmentLimit.setText(String.valueOf(capacity));

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "해당 강의 정보를 찾을 수 없습니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
                dispose(); // 강의가 없으면 창 닫기
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "강의 정보를 불러오는 중 오류가 발생했습니다.\n" + e.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
        } finally {
            ConnectDB.close(new AutoCloseable[]{rs, ps, con});
        }
    }

    // ===== 입력값 유효성 검사 오버라이드 =====
    // - 기본 검사(콤보 선택, 요일 선택, 교시, 정원 범위) + 현재 수강인원 조건까지 추가
    @Override
    protected void validateInputs() throws Exception {
        // 상위 클래스의 기본 입력 검증 먼저 수행
        super.validateInputs();

        // 추가 조건: 현재 수강인원보다 작은 정원으로는 줄일 수 없음
        String capStr = EnrollmentLimit.getText().trim();
        int newCapacity = Integer.parseInt(capStr);

        if (newCapacity < enrolledCount) {
            throw new Exception(
                    "현재 수강인원(" + enrolledCount + "명)보다 작게 정원을 설정할 수 없습니다."
            );
        }
    }

    // ===== 기존 강의를 UPDATE하는 로직 =====
    @Override
    protected void onSubmit() throws Exception {

        // ===== 1. 화면에서 수정된 값 읽어오기 =====
        String subject = (String) subjectCb.getSelectedItem(); // 과목명
        String room = (String) RoomCb.getSelectedItem();       // 강의실명

        // 요일 문자열 재구성 (예: "월수금")
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dayCB.length; i++) {
            if (dayCB[i].isSelected()) {
                sb.append(dayCB[i].getText());
            }
        }
        String days = sb.toString();

        int start = (Integer) startPeriodCb.getSelectedItem(); // 시작교시
        int end = (Integer) endPeriodCb.getSelectedItem();     // 종료교시

        String capStr = EnrollmentLimit.getText().trim();
        int capacity = Integer.parseInt(capStr);               // 정원

        // ===== 2. DB에 UPDATE =====
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConnectDB.getConnection();

            String sql =
                    "UPDATE lecture " +
                            "SET subject_name = ?, " +
                            "    classroom_name = ?, " +
                            "    day_of_week = ?, " +
                            "    start_period = ?, " +
                            "    end_period = ?, " +
                            "    capacity = ? " +
                            "WHERE lecture_no = ?";

            ps = con.prepareStatement(sql);
            ps.setString(1, subject);       // 과목명
            ps.setString(2, room);          // 강의실명
            ps.setString(3, days);          // 요일
            ps.setInt(4, start);            // 시작교시
            ps.setInt(5, end);              // 종료교시
            ps.setInt(6, capacity);         // 정원
            ps.setInt(7, lectureNo);        // 어떤 강의인지 (PK)

            int affected = ps.executeUpdate();

            if (affected <= 0) {
                throw new Exception("강의 정보 수정 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }

            // ===== 3. 성공 메시지 + 메인 강의 목록 새로고침 + 창 닫기 =====
            JOptionPane.showMessageDialog(
                    this,
                    "강의 정보가 성공적으로 수정되었습니다.",
                    "수정 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (owner != null) {
                owner.refreshLectureList(); // 수정 후 메인 테이블 다시 로딩
            }

            dispose(); // 창 닫기

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("DB 처리 중 오류가 발생했습니다.\n" + e.getMessage());
        } finally {
            ConnectDB.close(new AutoCloseable[]{ps, con});
        }
    }

    // actionPerformed 오버라이딩
    @Override
    public void actionPerformed(ActionEvent e) {
        // LectureFormBase의 actionPerformed에
        // - 취소 버튼 처리
        // - validateInputs()
        // - checkConflictsInDB()
        // - onSubmit()
        // 흐름이 이미 구현되어 있으므로 그대로 사용
        super.actionPerformed(e);
    }
}
