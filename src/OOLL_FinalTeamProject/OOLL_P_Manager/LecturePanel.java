// 원장의 강의 관리 탭!!
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// LecturePanel 클래스: 원장님 페이지의 '강의 관리' 탭 화면을 담당하는 패널입니다.
// ActionListener를 구현(implements)하여 버튼 클릭 이벤트를 직접 처리합니다.
public class LecturePanel extends JPanel implements ActionListener {

    // --- 멤버 변수 선언 ---
    JFrame parentFrame;          // 부모 프레임 (팝업창을 띄울 때 부모로 지정하기 위해 필요)
    DefaultTableModel tableModel; // 테이블의 데이터(행/열)를 관리하는 모델 객체
    JTable table;                // 화면에 보이는 표 컴포넌트
    JTextField searchField;      // 검색어를 입력받는 텍스트 필드

    // 버튼 객체들 (이벤트 처리를 위해 멤버 변수로 선언)
    JButton searchBtn;  // 검색 버튼
    JButton addBtn;     // 강좌 개설 버튼
    JButton editBtn;    // 강좌 수정 버튼
    JButton delBtn;     // 강좌 삭제 버튼
    JButton refreshBtn; // 새로고침 버튼

    // 생성자: 패널이 생성될 때 UI를 구성하고 초기 데이터를 불러옵니다.
    public LecturePanel(JFrame parent) {
        this.parentFrame = parent; // 부모 프레임(ManagerMainFrame)을 받아와서 저장해둡니다.

        // 1. 패널 기본 설정
        setLayout(new BorderLayout()); // 동/서/남/북/중앙 레이아웃 사용
        setBorder(new EmptyBorder(20, 20, 20, 20)); // 패널 테두리에 20px씩 여백을 줌 (답답하지 않게)
        setBackground(Color.WHITE); // 배경색을 흰색으로 설정

        // 2. 상단 패널 (검색창과 버튼들이 들어갈 곳) 구성
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 오른쪽 정렬로 배치
        topPanel.setBackground(Color.WHITE); // 상단 패널도 배경 흰색

        // (2-1) 검색 기능 UI
        topPanel.add(new JLabel("강의명/강사명 검색 : ")); // 라벨 추가
        searchField = new JTextField(15); // 15글자 정도 크기의 입력창 생성
        searchField.addActionListener(this); // 엔터키를 쳐도 검색되도록 리스너 연결
        topPanel.add(searchField);

        searchBtn = new JButton("검색");
        searchBtn.setBackground(new Color(245, 245, 245)); // 버튼 배경 살짝 회색
        searchBtn.addActionListener(this); // 클릭 시 actionPerformed 실행

        // (2-2) 새로고침 버튼
        refreshBtn = new JButton("새로고침");
        refreshBtn.setBackground(new Color(240, 240, 255)); // 포인트 컬러 (연한 파랑)
        refreshBtn.addActionListener(this);

        // (2-3) 관리 기능 버튼들
        addBtn = new JButton("강좌 개설");
        addBtn.setBackground(Color.WHITE);
        addBtn.addActionListener(this);

        editBtn = new JButton("수정");
        editBtn.setBackground(Color.WHITE);
        editBtn.addActionListener(this);

        delBtn = new JButton("삭제");
        delBtn.setBackground(Color.WHITE);
        delBtn.addActionListener(this);

        // 상단 패널에 버튼들을 순서대로 추가 (중간에 간격 Box.createHorizontalStrut 추가)
        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(5)); // 검색과 새로고침 사이 5px 간격
        topPanel.add(refreshBtn);
        topPanel.add(Box.createHorizontalStrut(20)); // 기능 버튼들과 20px 간격 벌리기
        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(delBtn);

        // 3. 중앙 테이블 (강의 목록) 구성
        // 테이블 컬럼 이름 정의 (맨 앞 '번호'는 DB의 PK인 lecture_no를 담을 곳입니다)
        String[] cols = {"번호", "과목명", "담당강사", "강의실", "요일", "교시", "수강/정원"};

        // 테이블 모델 생성 (데이터는 일단 null로 비워둠)
        tableModel = new DefaultTableModel(null, cols) {
            @Override // 사용자가 표 내용을 더블클릭해서 직접 수정하지 못하도록 막음
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel); // 모델을 기반으로 JTable 생성
        ManagerMainFrame.styleTable(table); // 메인 프레임에 있는 스타일 함수(폰트, 높이 등) 적용

        // ★ 핵심: 0번째 컬럼('번호')을 화면에서 숨기기!
        // (사용자에게는 안 보이지만, 프로그램 내부적으로 수정/삭제할 때 PK를 꺼내 쓰기 위함)
        table.removeColumn(table.getColumnModel().getColumn(0));

        // 테이블 더블 클릭 이벤트 추가 (출결 관리 기능)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 마우스 왼쪽 버튼을 빠르게 두 번(2) 클릭했을 때
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow(); // 클릭한 행 번호 가져오기
                    if (selectedRow != -1) { // 선택된 행이 있다면
                        // 모델에서 숨겨진 강의번호(PK)와 강의명, 강사명 가져오기
                        // 주의: 화면엔 없어도 모델에는 0번에 '번호'가 있습니다.
                        String lectureNo = table.getModel().getValueAt(selectedRow, 0).toString();
                        String subject = table.getModel().getValueAt(selectedRow, 1).toString();
                        String teacher = table.getModel().getValueAt(selectedRow, 2).toString();

                        // 출결 관리 팝업창(AttendanceDialog) 열기
                        new AttendanceDialog(parentFrame, subject + " (" + teacher + ") 출결 관리", lectureNo).setVisible(true);
                    }
                }
            }
        });

        // 4. 패널 배치
        add(topPanel, BorderLayout.NORTH); // 상단에 버튼 패널 배치
        add(new JScrollPane(table), BorderLayout.CENTER); // 중앙에 스크롤 가능한 테이블 배치

        // 5. 초기 데이터 로드
        refreshTable(""); // 검색어 없이 전체 목록 조회 실행
    }

    // ★ ActionListener 구현부: 버튼 클릭 시 실행되는 메소드
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource(); // 어떤 버튼이 눌렸는지 확인

        if (source == searchBtn || source == searchField) {
            // 검색 버튼 누르거나 검색창에서 엔터 -> 입력된 검색어로 테이블 갱신
            refreshTable(searchField.getText());

        } else if (source == refreshBtn) {
            // 새로고침 버튼 -> 검색어 지우고 전체 목록 다시 불러오기
            searchField.setText("");
            refreshTable("");

        } else if (source == addBtn) {
            // 강좌 개설 버튼 -> LectureDialog 팝업 띄우기 (데이터는 null 전달)
            new LectureDialog(parentFrame, "강좌 개설", null).setVisible(true);
            refreshTable(""); // 팝업이 닫히면(작업 완료 후) 목록 새로고침

        } else if (source == editBtn) {
            // 수정 버튼 -> 선택된 행의 데이터를 가져와서 팝업에 채워주기
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) { // 선택 안 했으면 경고
                JOptionPane.showMessageDialog(parentFrame, "수정할 강좌를 선택해주세요.");
                return;
            }
            // 선택된 행의 데이터 추출 (숨겨진 0번 컬럼부터 순서대로)
            String no = table.getModel().getValueAt(selectedRow, 0).toString(); // PK
            String subject = table.getModel().getValueAt(selectedRow, 1).toString();
            String teacher = table.getModel().getValueAt(selectedRow, 2).toString();
            String room = table.getModel().getValueAt(selectedRow, 3).toString();
            String day = table.getModel().getValueAt(selectedRow, 4).toString();
            String time = table.getModel().getValueAt(selectedRow, 5).toString();

            // "5 / 20" 문자열에서 현재인원과 정원 분리
            String countInfo = table.getModel().getValueAt(selectedRow, 6).toString();
            String[] counts = countInfo.split(" / ");
            String currentCount = counts[0];
            String capacity = counts[1];

            // 데이터를 배열에 담아서 팝업창 생성자로 전달
            String[] currentData = {no, subject, teacher, room, day, time, capacity, currentCount};

            new LectureDialog(parentFrame, "강좌 수정", currentData).setVisible(true);
            refreshTable(""); // 수정 완료 후 목록 갱신

        } else if (source == delBtn) {
            // 삭제 버튼 -> 삭제 로직 메소드 호출
            deleteLecture();
        }
    }

    // [기능] 강의 삭제 메소드
    private void deleteLecture() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "삭제할 강좌를 선택해주세요.");
            return;
        }
        // 삭제할 강의 번호(PK)와 이름 가져오기
        String lectureNo = table.getModel().getValueAt(selectedRow, 0).toString();
        String subjectName = table.getModel().getValueAt(selectedRow, 1).toString();

        // 삭제 확인 팝업 띄우기 (실수 방지)
        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "[" + subjectName + "] 강좌를 정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            Statement stmt = null;
            try {
                // DB 연결
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                // DELETE 쿼리 실행 (lecture 테이블에서 해당 번호 삭제)
                // * ON DELETE CASCADE 설정 덕분에 수강생 기록도 같이 삭제됨 (혹은 RESTRICT라면 막힘)
                String sql = "DELETE FROM lecture WHERE lecture_no = " + lectureNo;

                stmt = con.createStatement();
                stmt.executeUpdate(sql);

                JOptionPane.showMessageDialog(parentFrame, "삭제되었습니다.");
                refreshTable(""); // 삭제 후 목록 갱신
            } catch (Exception e) {
                // 예외 발생 시 (예: DB 연결 실패, 또는 삭제 제약조건 위반 등)
                JOptionPane.showMessageDialog(parentFrame, "삭제 실패! (오류: " + e.getMessage() + ")");
            } finally {
                // 자원 해제
                try { if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
            }
        }
    }

    // [기능] 강의 목록 조회 메소드 (검색 기능 포함)
    public void refreshTable(String keyword) {
        // 기존 테이블 데이터 싹 비우기 (새로 채우기 위해)
        tableModel.setRowCount(0);

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

            // ★ 뷰(view_lecture_info) 사용: 복잡한 조인 쿼리 없이 뷰를 테이블처럼 조회함
            String sql = "SELECT * FROM view_lecture_info ";

            // 검색어가 있으면 WHERE 절 추가 (강의명 또는 강사명에 포함된 경우)
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql += "WHERE subject_name LIKE '%" + keyword.trim() + "%' OR teacher_name LIKE '%" + keyword.trim() + "%' ";
            }

            // 최신 강의가 위로 오도록 정렬
            sql += "ORDER BY lecture_no DESC";

            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            // 결과셋(ResultSet)을 한 줄씩 읽어서 테이블 모델에 추가
            while(rs.next()) {
                int no = rs.getInt("lecture_no");
                String sub = rs.getString("subject_name");
                String tName = rs.getString("teacher_name");
                if(tName == null) tName = "미정"; // 강사가 삭제된 경우 대비
                String room = rs.getString("classroom_name");
                String day = rs.getString("day_of_week");
                String time = rs.getInt("start_period") + "-" + rs.getInt("end_period") + "교시";

                // 뷰에서 계산된 수강인원과 정원을 합쳐서 보여줌 (예: "3 / 20")
                String count = rs.getInt("enrolled_count") + " / " + rs.getInt("capacity");

                // 모델에 데이터 추가 (PK 포함 7개 컬럼)
                tableModel.addRow(new Object[]{no, sub, tName, room, day, time, count});
            }
        } catch (Exception e) {
            e.printStackTrace(); // 에러 로그 출력
        } finally {
            // 자원 해제
            try { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); if(con!=null) con.close(); } catch(Exception ex) {}
        }
    }
}