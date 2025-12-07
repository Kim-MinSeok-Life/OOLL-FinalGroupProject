// 학생 - DB 데이터 처리 서비스 (개인정보, 강의목록, 수강신청 등)
package OOLL_P_Student; // 패키지 선언

// import 선언
import java.sql.*; // JDBC 관련 클래스(Connection, PreparedStatement, ResultSet 등)
import java.sql.Date; // SQL DATE 타입 사용
import java.time.LocalDate; // 오늘 날짜 조회용
import java.util.*; // List, Arrays 등
import javax.swing.table.DefaultTableModel; // JTable 모델
//패키지별 클래스 전체 가져오기
import OOLL_P_Student.*; // 학생 기능 관련 패키지 불러오기
import OOLL_P_Teacher.*; // 강사 기능 관련 패키지 불러오기
import OOLL_P_Login.*; // 로그인 기능 관련 패키지 불러오기
import OOLL_P_Manager.*; // 관리자 기능 관련 패키지 불러오기

public class StudentService {
    // 요일 정렬 기준(월~일)
    private static final List<String> DAY_ORDER = Arrays.asList("월", "화", "수", "목", "금", "토", "일");

    // 회원 + 학생 정보 조회
    public StudentInfo loadStudentInfo(String memberId) throws SQLException {
    	// 회원과 학생 테이블 JOIN하여 회원 정보와 학생 번호 조회
        String sql = "SELECT m.member_id, m.name, m.email, m.phone, m.address, s.student_no " +
                     "FROM member m LEFT JOIN student s ON m.member_id = s.member_id WHERE m.member_id = ?";
        try (Connection conn = DBUtil.getConnection(); // DB 연결
             PreparedStatement p = conn.prepareStatement(sql)) { // SQL 준비
            p.setString(1, memberId); // 회원아이디 연결
            try (ResultSet rs = p.executeQuery()) { // 쿼리 실행
                if (rs.next()) { // 결과 존재 시
                    StudentInfo info = new StudentInfo(); // StudentInfo 객체 생성
                    info.memberId = rs.getString("member_id"); // 아이디
                    info.name = rs.getString("name"); // 이름
                    info.email = rs.getString("email"); // 이메일
                    info.phone = rs.getString("phone"); // 연락처
                    info.address = rs.getString("address"); // 주소
                    info.studentNo = rs.getInt("student_no"); // 학생 번호
                    if (rs.wasNull()) info.studentNo = -1; // 학생 번호 없으면 -1
                    return info; // 조회된 정보 반환
                }
            }
        }
        return null; // 회원 정보 없음(null 반환)
    }

    // DB에서 현재 비밀번호 조회(학생정보 수정 다이얼로그 표시용)
    public String getPassword(String memberId) throws SQLException {
        String sql = "SELECT password FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection(); // DB 연결
             PreparedStatement p = conn.prepareStatement(sql)) { // SQL 준비
            p.setString(1, memberId); // 회원아이디 연결
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getString("password"); // 비밀번호 반환
            }
        }
        return null; // 비밀번호 없음(null 반환)
    }

    // 개인정보 업데이트(비밀번호는 여기서 변경하지 않음)
    public boolean updateMemberInfo(String memberId, String name, String phone, String address, String email) throws SQLException {
        String sql = "UPDATE member SET name = ?, phone = ?, address = ?, email = ? WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection(); // DB 연결
             PreparedStatement p = conn.prepareStatement(sql)) { // SQL 준비
        	p.setString(1, name); // 이름
            p.setString(2, phone); // 연락처
            p.setString(3, address); // 주소
            p.setString(4, email); // 이메일
            p.setString(5, memberId); // 회원 아이디
            return p.executeUpdate() > 0; // 수정 성공 시 true 반환
        }
    }

    // 비밀번호 변경(현재 비밀번호 확인 후 업데이트)
    public boolean changePassword(String memberId, String currentPw, String newPw) throws SQLException {
    	// DB에서 회원 비밀번호 조회
        String q = "SELECT password FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection(); // DB 연결
             PreparedStatement p = conn.prepareStatement(q)) { // SQL 준비
            p.setString(1, memberId);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) { // 회원 존재 시
                    String stored = r.getString("password"); // DB에 저장된 비밀번호
                    if (!stored.equals(currentPw)) { // 현재 비밀번호 불일치
                        return false; // 변경 불가
                    }
                } else {
                    return false; // 회원 없음
                }
            }
            // 비밀번호 업데이트
            String up = "UPDATE member SET password = ? WHERE member_id = ?";
            try (PreparedStatement pu = conn.prepareStatement(up)) { // SQL 준비
                pu.setString(1, newPw); // 새 비밀번호
                pu.setString(2, memberId); // 회원 아이디
                int u = pu.executeUpdate();
                return u > 0; // 변경 성공 여부 반환
            }
        }
    }

    // 내 강의 조회 및 모델에 추가 (요일+시작교시 기준 정렬)
    public void loadMyClass(DefaultTableModel model, int studentNo) throws SQLException {
        model.setRowCount(0); // 기존 데이터 초기화
        if (studentNo == -1) return; // 학생 정보 없으면 종료

        // 수강중인 강의 조회(강의, 강사, 요일, 교시, 강의실, 현재 인원)
        String sql =
        	    "SELECT l.lecture_no, l.subject_name, mb.name AS teacher_name, " +
        	    "l.day_of_week AS weekday, l.start_period, l.end_period, " +
        	    "l.classroom_name AS room, l.enrolled_count AS current_count " +
        	    "FROM enrollment e " +
        	    "JOIN lecture l ON e.lecture_no = l.lecture_no " +
        	    "JOIN teacher t ON l.teacher_no = t.teacher_no " +
        	    "JOIN member mb ON t.member_id = mb.member_id " +
        	    "WHERE e.student_no = ? AND e.status = '수강중'";

        List<Object[]> temp = new ArrayList<>(); // 임시 저장 리스트
        try (Connection conn = DBUtil.getConnection(); // DB 연결
             PreparedStatement p = conn.prepareStatement(sql)) { // SQL 준비
            p.setInt(1, studentNo); // studentNo 연결
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) { // 결과 
                	int start = rs.getInt("start_period"); // 시작 교시
                	int end = rs.getInt("end_period"); // 종료 교시
                	String period = Utils.getPeriodTitle(start) + " ~ " + Utils.getPeriodTitle(end); // 시간 문자열
                	Object[] row = new Object[]{
                			 rs.getInt("lecture_no"),		// 강의 번호
                             rs.getString("subject_name"),  // 과목명
                             rs.getString("teacher_name"),  // 강사명
                             rs.getString("weekday"),    	// 요일
                             period, 						// 시간
                             rs.getString("room"),          // 강의실
                             rs.getInt("current_count")     // 현재 정원
                     };
                    temp.add(row); // 임시 리스트에 추가
                }
            }
        }

        // 정렬: 요일 우선(DAY_ORDER), 동일 요일이면 시작 교시(start_period) 기준 오름차순
        temp.sort((a, b) -> {
            String dayA = (String) a[3];
            String dayB = (String) b[3];
            int idxA = DAY_ORDER.indexOf(dayA);
            int idxB = DAY_ORDER.indexOf(dayB);
            if (idxA == -1) idxA = 0;
            if (idxB == -1) idxB = 0;
            int cmp = Integer.compare(idxA, idxB);
            if (cmp != 0) return cmp;

            // 시작 교시 비교("3~5" 형태의 앞부분)
            String sa = a[4].toString().split("~")[0];
            String sb = b[4].toString().split("~")[0];
            try {
                int ia = Integer.parseInt(sa);
                int ib = Integer.parseInt(sb);
                return Integer.compare(ia, ib);
            } catch (NumberFormatException ex) {
                return sa.compareTo(sb); // 문자열 비교
            }
        });
        // 정렬 후 JTable 모델에 추가
        for (Object[] r : temp) model.addRow(r);
    }

    // 전체 강의 목록 조회(이미 수강중인 강의는 제외)
    public void loadCourseList(DefaultTableModel model, String keyword, String sortOption, int studentNo) throws SQLException {
        model.setRowCount(0); // 모델 초기화
        String base = "SELECT l.subject_name, l.enrolled_count, mb.name as teacher_name, l.lecture_no, l.day_of_week, l.start_period, l.end_period, l.classroom_name, l.capacity " +
                      "FROM lecture l JOIN teacher t ON l.teacher_no = t.teacher_no JOIN member mb ON t.member_id = mb.member_id " +
                      "WHERE (l.subject_name LIKE ? OR mb.name LIKE ?)";

        // 수강중인 강의 제외
        if (studentNo != -1) {
            base += " AND l.lecture_no NOT IN (SELECT lecture_no FROM enrollment WHERE student_no = ? AND status = '수강중')";
        }

        // 정렬 옵션 처리
        String order = "";
        if ("과목명순".equals(sortOption)) order = " ORDER BY l.subject_name ASC";
        else if ("강사명순".equals(sortOption)) order = " ORDER BY mb.name ASC";
        else if ("신청가능인원순".equals(sortOption)) order = " ORDER BY (l.capacity - l.enrolled_count) DESC";

        String sql = base + order;

        try (Connection conn = DBUtil.getConnection(); // DB 연결
             PreparedStatement p = conn.prepareStatement(sql)) { // SQL 준비
            String like = "%" + (keyword == null ? "" : keyword) + "%";
            p.setString(1, like); // 과목명 검색
            p.setString(2, like); // 강사명 검색
            if (studentNo != -1) p.setInt(3, studentNo); // 수강중인 강의 제외
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String subj = rs.getString("subject_name"); // 강의명
                    int enrolled = rs.getInt("enrolled_count"); // 현재 인원
                    String teacher = rs.getString("teacher_name"); // 강사명
                    int lectureNo = rs.getInt("lecture_no"); // 강의 번호
                    String day = rs.getString("day_of_week"); // 요일
                    int s = rs.getInt("start_period"); // 시작 시간
                    int e = rs.getInt("end_period"); // 종료 시간
                    String time = s + "~" + e; // 시간 문자열
                    String room = rs.getString("classroom_name"); // 강의실
                    int cap = rs.getInt("capacity"); // 정원
                    model.addRow(new Object[]{subj, enrolled, teacher, lectureNo, day, time, room, cap}); // 모델에 추가
                }
            }
        }
    }

    // 수강신청(트랜잭션 포함)
    public String attemptEnroll(int studentNo, int lectureNo) {
        if (studentNo == -1) return "학생 정보가 없습니다."; // 학생 정보 없음
        try (Connection conn = DBUtil.getConnection()) { // DB 연결
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 이미 수강중인지 확인
            String q1 = "SELECT COUNT(*) FROM enrollment WHERE student_no = ? AND lecture_no = ?";
            try (PreparedStatement p1 = conn.prepareStatement(q1)) {
                p1.setInt(1, studentNo);
                p1.setInt(2, lectureNo);
                try (ResultSet r1 = p1.executeQuery()) {
                    if (r1.next() && r1.getInt(1) > 0) {
                        conn.rollback();
                        return "이미 해당 강의를 수강중입니다.";
                    }
                }
            }

            // 강의 정보 조회 및 FOR UPDATE(동시성 방지)
            String q2 = "SELECT capacity, enrolled_count, day_of_week, start_period, end_period, subject_name FROM lecture WHERE lecture_no = ? FOR UPDATE";
            int capacity = 0, enrolled = 0, start = 0, end = 0;
            String day = null, subj = null;
            try (PreparedStatement p2 = conn.prepareStatement(q2)) {
                p2.setInt(1, lectureNo);
                try (ResultSet r2 = p2.executeQuery()) {
                    if (r2.next()) {
                        capacity = r2.getInt("capacity");			// 강의 정원
                        enrolled = r2.getInt("enrolled_count");		// 현재 수강 인원
                        day = r2.getString("day_of_week");			// 요일
                        start = r2.getInt("start_period");			// 시작 교시
                        end = r2.getInt("end_period");				// 종료 교시
                        subj = r2.getString("subject_name");		// 과목명
                    } else {
                        conn.rollback();
                        return "해당 강의를 찾을 수 없습니다.";
                    }
                }
            }

            // 정원 확인
            if (enrolled >= capacity) {
                conn.rollback();
                return "[" + subj + "] 정원이 가득 찼습니다.";
            }

            // 같은 과목 수강 여부 확인
            String qSameSubject = "SELECT COUNT(*) FROM enrollment e JOIN lecture l ON e.lecture_no = l.lecture_no WHERE e.student_no = ? AND l.subject_name = ? AND e.status = '수강중'";
            try (PreparedStatement ps = conn.prepareStatement(qSameSubject)) {
                ps.setInt(1, studentNo);
                ps.setString(2, subj);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        return "이미 같은 과목(" + subj + ")을 수강중입니다.";
                    }
                }
            }

            // 시간 겹침 확인
            String q3 = "SELECT l.subject_name, l.day_of_week, l.start_period, l.end_period FROM enrollment e JOIN lecture l ON e.lecture_no = l.lecture_no WHERE e.student_no = ? AND e.status = '수강중'";
            try (PreparedStatement p3 = conn.prepareStatement(q3)) {
                p3.setInt(1, studentNo);
                try (ResultSet r3 = p3.executeQuery()) {
                    while (r3.next()) {
                        String d = r3.getString("day_of_week");		// 기존 강의 요일 
                        int s = r3.getInt("start_period");			// 시작 교시
                        int ee = r3.getInt("end_period");			// 종료 교시
                        if (d.equalsIgnoreCase(day)) { // 동일 요일이면
                            if (!(end < s || start > ee)) { // 시간 충돌 체크
                                conn.rollback();
                                return "시간 충돌: 기존 수강중인 과목과 시간이 겹칩니다.";
                            }
                        }
                    }
                }
            }

            // 수강신청(INSERT)
            String ins = "INSERT INTO enrollment (student_no, lecture_no, status, apply_date) VALUES (?, ?, '수강중', CURDATE())";
            try (PreparedStatement pins = conn.prepareStatement(ins)) {
                pins.setInt(1, studentNo);
                pins.setInt(2, lectureNo);
                pins.executeUpdate();
            }

            // 강의 인원 증가(UPDATE)
            String upt = "UPDATE lecture SET enrolled_count = enrolled_count + 1 WHERE lecture_no = ?";
            try (PreparedStatement pup = conn.prepareStatement(upt)) {
                pup.setInt(1, lectureNo);
                pup.executeUpdate();
            }

            conn.commit(); // 트랜잭션 커밋
            return "성공";
        } catch (SQLException ex) {
        	// DB 쿼리 수행 중 에러 발생 시
            ex.printStackTrace(); // 오류 로그 출력
            return "수강신청 중 오류: " + ex.getMessage();
        }
    }

    // 오늘 출결 조회
    public void loadAttendanceForLecture(DefaultTableModel model, int lectureNo) throws SQLException {
        model.setRowCount(0); // 초기화
        LocalDate today = LocalDate.now(); // 오늘 날짜
        String q = "SELECT s.student_no, mb.member_id, mb.name, a.attendance_status " +
                   "FROM enrollment e " +
                   "JOIN student s ON e.student_no = s.student_no " +
                   "JOIN member mb ON s.member_id = mb.member_id " +
                   "LEFT JOIN attendance a ON a.student_no = s.student_no AND a.lecture_no = ? AND a.att_date = ? " +
                   "WHERE e.lecture_no = ? AND e.status = '수강중'";
        try (Connection conn = DBUtil.getConnection(); // DB 연결
             PreparedStatement p = conn.prepareStatement(q)) { // SQL 준비
            p.setInt(1, lectureNo);
            p.setDate(2, Date.valueOf(today));
            p.setInt(3, lectureNo);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    int sno = rs.getInt("student_no"); // 학생 번호
                    String mid = rs.getString("member_id"); // 회원 아이디
                    String nm = rs.getString("name"); // 이름
                    String status = rs.getString("attendance_status"); // 출결 상태
                    if (status == null) status = "미처리"; // 출결 미등록 시
                    model.addRow(new Object[]{sno, mid, nm, status}); // JTable 모델에 추가
                }
            }
        }
    }
    
    // 강의 삭제(수강 취소)
    public boolean deleteLecture(int studentNo, int lectureNo) throws SQLException {
        if (studentNo == -1) return false; // 학생 정보 없음

        String deleteEnrollment = "DELETE FROM enrollment WHERE student_no = ? AND lecture_no = ?";
        String updateLecture = 
            "UPDATE lecture SET enrolled_count = GREATEST(enrolled_count - 1, 0) WHERE lecture_no = ?";

        try (Connection conn = DBUtil.getConnection()) { // DB 연결
            conn.setAutoCommit(false); // 트랜잭션 시작
            try (PreparedStatement pDel = conn.prepareStatement(deleteEnrollment);
                 PreparedStatement pUpd = conn.prepareStatement(updateLecture)) { // SQL 준비

                // 수강 기록 삭제
                pDel.setInt(1, studentNo);
                pDel.setInt(2, lectureNo);
                int deleted = pDel.executeUpdate();

                // 삭제 실패 시 롤백
                if (deleted == 0) {
                    conn.rollback();
                    return false;
                }

                // 현재 강의 인원 감소(최소 0 유지)
                pUpd.setInt(1, lectureNo);
                pUpd.executeUpdate();

                conn.commit(); // 트랜잭션 커밋
                return true;
            } catch (SQLException ex) {
            	// DB 쿼리 수행 중 에러 발생 시
                conn.rollback(); // 롤백
                throw ex; // 예외 다시 던짐
            }
        }
    }
}