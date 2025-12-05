// 학생 - DB 데이터 처리 서비스 (개인정보, 강의목록, 수강신청 등)
package OOLL_P_Student;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class StudentService {
    // 요일 정렬 기준
    private static final List<String> DAY_ORDER = Arrays.asList("월", "화", "수", "목", "금", "토", "일");

    // member + student info
    public StudentInfo loadStudentInfo(String memberId) throws SQLException {
        String sql = "SELECT m.member_id, m.name, m.email, m.phone, m.address, s.student_no " +
                     "FROM member m LEFT JOIN student s ON m.member_id = s.member_id WHERE m.member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, memberId);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    StudentInfo info = new StudentInfo();
                    info.memberId = rs.getString("member_id");
                    info.name = rs.getString("name");
                    info.email = rs.getString("email");
                    info.phone = rs.getString("phone");
                    info.address = rs.getString("address");
                    info.studentNo = rs.getInt("student_no");
                    if (rs.wasNull()) info.studentNo = -1;
                    return info;
                }
            }
        }
        return null;
    }

    // DB에서 현재 저장된 비밀번호(학생정보 수정 다이얼로그에서 "표시만" 하기 위해)
    public String getPassword(String memberId) throws SQLException {
        String sql = "SELECT password FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, memberId);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getString("password");
            }
        }
        return null;
    }

    // 개인정보 업데이트 (비밀번호는 여기서 변경하지 않음)
    public boolean updateMemberInfo(String memberId, String name, String phone, String address) throws SQLException {
        String sql = "UPDATE member SET name = ?, phone = ?, address = ? WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, phone);
            p.setString(3, address);
            p.setString(4, memberId);
            return p.executeUpdate() > 0;
        }
    }

    // 비밀번호 변경 (현재 비밀번호 확인 포함)
    public boolean changePassword(String memberId, String currentPw, String newPw) throws SQLException {
        String q = "SELECT password FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement p = conn.prepareStatement(q)) {
            p.setString(1, memberId);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    String stored = r.getString("password");
                    if (!stored.equals(currentPw)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            String up = "UPDATE member SET password = ? WHERE member_id = ?";
            try (PreparedStatement pu = conn.prepareStatement(up)) {
                pu.setString(1, newPw);
                pu.setString(2, memberId);
                int u = pu.executeUpdate();
                return u > 0;
            }
        }
    }

    // 내 강의 로드: Java 내부에서 '요일(월..일) + 시작교시' 순으로 정렬해서 모델에 추가
    public void loadMyClass(DefaultTableModel model, int studentNo) throws SQLException {
        model.setRowCount(0);
        if (studentNo == -1) return;

        String sql =
                "SELECT l.subject_name, mb.name AS teacher_name, l.lecture_no, l.day_of_week AS weekday, l.start_period, l.end_period, l.classroom_name AS room " +
                "FROM enrollment e " +
                "JOIN lecture l ON e.lecture_no = l.lecture_no " +
                "JOIN teacher t ON l.teacher_no = t.teacher_no " +
                "JOIN member mb ON t.member_id = mb.member_id " +
                "WHERE e.student_no = ? AND e.status = '수강중'";

        List<Object[]> temp = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, studentNo);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[]{
                            rs.getString("subject_name"),
                            rs.getString("teacher_name"),
                            rs.getInt("lecture_no"),
                            rs.getString("weekday"),
                            rs.getInt("start_period") + "~" + rs.getInt("end_period"),
                            rs.getString("room")
                    };
                    temp.add(row);
                }
            }
        }

        // 정렬: 요일 우선 (DAY_ORDER), 같은 요일이면 start_period 기준
        temp.sort((a, b) -> {
            String dayA = (String) a[3];
            String dayB = (String) b[3];
            int idxA = DAY_ORDER.indexOf(dayA);
            int idxB = DAY_ORDER.indexOf(dayB);
            if (idxA == -1) idxA = 0;
            if (idxB == -1) idxB = 0;
            int cmp = Integer.compare(idxA, idxB);
            if (cmp != 0) return cmp;

            // start_period 은 문자열 "3~5" 형태의 앞부분
            String sa = a[4].toString().split("~")[0];
            String sb = b[4].toString().split("~")[0];
            try {
                int ia = Integer.parseInt(sa);
                int ib = Integer.parseInt(sb);
                return Integer.compare(ia, ib);
            } catch (NumberFormatException ex) {
                return sa.compareTo(sb);
            }
        });

        for (Object[] r : temp) model.addRow(r);
    }

    // 전체 강의 목록 (이미 수강중인 강의는 제외)
    public void loadCourseList(DefaultTableModel model, String keyword, String sortOption, int studentNo) throws SQLException {
        model.setRowCount(0);
        String base = "SELECT l.subject_name, l.enrolled_count, mb.name as teacher_name, l.lecture_no, l.day_of_week, l.start_period, l.end_period, l.classroom_name, l.capacity " +
                      "FROM lecture l JOIN teacher t ON l.teacher_no = t.teacher_no JOIN member mb ON t.member_id = mb.member_id " +
                      "WHERE (l.subject_name LIKE ? OR mb.name LIKE ?)";

        if (studentNo != -1) {
            base += " AND l.lecture_no NOT IN (SELECT lecture_no FROM enrollment WHERE student_no = ? AND status = '수강중')";
        }

        String order = "";
        if ("과목명순".equals(sortOption)) order = " ORDER BY l.subject_name ASC";
        else if ("강사명순".equals(sortOption)) order = " ORDER BY mb.name ASC";
        else if ("신청가능인원순".equals(sortOption)) order = " ORDER BY (l.capacity - l.enrolled_count) DESC";

        String sql = base + order;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            String like = "%" + (keyword == null ? "" : keyword) + "%";
            p.setString(1, like);
            p.setString(2, like);
            if (studentNo != -1) p.setInt(3, studentNo);
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
                    model.addRow(new Object[]{subj, enrolled, teacher, lectureNo, day, time, room, cap});
                }
            }
        }
    }

    // 수강신청 (트랜잭션 포함) — 기존 로직 유지
    public String attemptEnroll(int studentNo, int lectureNo) {
        if (studentNo == -1) return "학생 정보가 없습니다.";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

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
                        conn.rollback();
                        return "해당 강의를 찾을 수 없습니다.";
                    }
                }
            }

            if (enrolled >= capacity) {
                conn.rollback();
                return "[" + subj + "] 정원이 가득 찼습니다.";
            }

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

            String q3 = "SELECT l.subject_name, l.day_of_week, l.start_period, l.end_period FROM enrollment e JOIN lecture l ON e.lecture_no = l.lecture_no WHERE e.student_no = ? AND e.status = '수강중'";
            try (PreparedStatement p3 = conn.prepareStatement(q3)) {
                p3.setInt(1, studentNo);
                try (ResultSet r3 = p3.executeQuery()) {
                    while (r3.next()) {
                        String d = r3.getString("day_of_week");
                        int s = r3.getInt("start_period");
                        int ee = r3.getInt("end_period");
                        if (d.equalsIgnoreCase(day)) {
                            if (!(end < s || start > ee)) {
                                conn.rollback();
                                return "시간 충돌: 기존 수강중인 과목과 시간이 겹칩니다.";
                            }
                        }
                    }
                }
            }

            String ins = "INSERT INTO enrollment (student_no, lecture_no, status, apply_date) VALUES (?, ?, '수강중', CURDATE())";
            try (PreparedStatement pins = conn.prepareStatement(ins)) {
                pins.setInt(1, studentNo);
                pins.setInt(2, lectureNo);
                pins.executeUpdate();
            }

            String upt = "UPDATE lecture SET enrolled_count = enrolled_count + 1 WHERE lecture_no = ?";
            try (PreparedStatement pup = conn.prepareStatement(upt)) {
                pup.setInt(1, lectureNo);
                pup.executeUpdate();
            }

            conn.commit();
            return "성공";
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "수강신청 중 오류: " + ex.getMessage();
        }
    }

    // 오늘 출결 조회
    public void loadAttendanceForLecture(DefaultTableModel model, int lectureNo) throws SQLException {
        model.setRowCount(0);
        LocalDate today = LocalDate.now();
        String q = "SELECT s.student_no, mb.member_id, mb.name, a.attendance_status " +
                   "FROM enrollment e " +
                   "JOIN student s ON e.student_no = s.student_no " +
                   "JOIN member mb ON s.member_id = mb.member_id " +
                   "LEFT JOIN attendance a ON a.student_no = s.student_no AND a.lecture_no = ? AND a.att_date = ? " +
                   "WHERE e.lecture_no = ? AND e.status = '수강중'";
        try (Connection conn = DBUtil.getConnection();
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
                    model.addRow(new Object[]{sno, mid, nm, status});
                }
            }
        }
    }
}