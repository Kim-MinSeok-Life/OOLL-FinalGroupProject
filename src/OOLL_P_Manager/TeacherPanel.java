//원장의 강사관리 화면
package OOLL_P_Manager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TeacherPanel extends JPanel {
    // 폰트 설정
    private final Font sectionTitleFont = new Font("Malgun Gothic", Font.BOLD, 16);
    private final Font dataFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font buttonFont = new Font("Malgun Gothic", Font.PLAIN, 12);
    private final Font addButtonFont = new Font("Malgun Gothic", Font.BOLD, 14);
    private DefaultTableModel teacherTableModel;
    private final int PRICE_COLUMN_INDEX = 5;
    JFrame parentFrame; // 부모 프레임(주로 메인 프레임)을 저장하는 변수

    public TeacherPanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 메인 콘텐츠 패널
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 30, 15, 30));
        contentPanel.setBackground(Color.WHITE);

        // 1. 단가 공통 관리 버튼
        contentPanel.add(createPriceManagementPanel()); // 전체 강사 단가 관리 버튼 패널을 생성하여 추가
        contentPanel.add(Box.createVerticalStrut(25)); // 컴포넌트 사이에 수직 간격을 추가

        // 2. 강사 관리 콘텐츠 (JTable)
        contentPanel.add(createTeacherManagementPanel()); // 강사 목록 JTable을 포함하는 관리 패널을 생성하여 추가
        contentPanel.add(Box.createVerticalGlue()); // 남은 공간을 채우기 위해 수직 Glue를 추가

        add(contentPanel, BorderLayout.CENTER); // 메인 콘텐츠 패널을 TeacherPanel의 중앙에 배치
    }

    private JPanel createPriceManagementPanel() { // 강사 단가 공통 관리 섹션 패널을 생성하는 메서드
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // FlowLayout(왼쪽 정렬)을 사용하는 패널을 생성
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT); // 이 패널이 부모 컨테이너 내에서 왼쪽으로 정렬

        JLabel label = new JLabel("강사 단가 공통 관리: "); // 제목 레이블을 생성
        label.setFont(sectionTitleFont); // 제목 폰트를 설정
        panel.add(label); // 레이블을 패널에 추가

        JButton updatePriceButton = new JButton("단가 수정"); // '단가 수정' 버튼을 생성
        updatePriceButton.setFont(addButtonFont); // 버튼 폰트를 설정합니다.
        updatePriceButton.setBackground(new Color(255, 165, 0)); // 버튼 배경색을 오렌지색으로 설정
        updatePriceButton.setForeground(Color.WHITE); // 버튼 글자색을 흰색으로 설정
        updatePriceButton.setFocusPainted(false); // 포커스되었을 때 외곽선 표시
        updatePriceButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // 버튼에 여백을 설정
        updatePriceButton.addActionListener(e -> updateAllPrices()); // 버튼 클릭 시 모든 강사의 단가를 수정하는 메서드를 호출하도록 리스너를 추가

        panel.add(updatePriceButton); // 버튼을 패널에 추가
        return panel; // 생성된 단가 관리 패널을 반환
    }

    private JPanel createTeacherManagementPanel() { // 강사 관리 목록(JTable) 섹션 패널을 생성하는 메서드
        JPanel teacherSection = new JPanel(); // 강사 관리 섹션을 담을 패널을 생성
        teacherSection.setLayout(new BoxLayout(teacherSection, BoxLayout.Y_AXIS)); // BoxLayout을 사용하여 수직 정렬
        teacherSection.setBackground(Color.WHITE); // 배경색을 흰색으로 설정합니다.
        teacherSection.setBorder(new EmptyBorder(10, 0, 0, 0)); // 상단에 여백을 설정

        JLabel title = new JLabel("강사 관리"); // 섹션 제목 레이블을 생성
        title.setFont(sectionTitleFont); // 제목 폰트를 설정
        teacherSection.add(title); // 제목 레이블을 패널에 추가

        JLabel subtitle = new JLabel("등록된 강사의 정보를 관리합니다"); // 부제목 레이블을 생성
        subtitle.setFont(dataFont); // 부제목 폰트를 설정합니다.
        subtitle.setBorder(new EmptyBorder(0, 0, 15, 0)); // 하단에 여백을 설정
        teacherSection.add(subtitle); // 부제목 레이블을 패널에 추가

        // JTable 설정
        String[] columnNames = {"아이디", "이름", "이메일", "전화번호", "주소", "시간당 단가", "관리"}; // 테이블 열 이름들을 정의
        Object[][] initialData = loadTeacherData(); // 데이터베이스에서 초기 강사 데이터를 로드

        teacherTableModel = new DefaultTableModel(initialData, columnNames) { // 테이블 모델을 생성하고 데이터를 초기화
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        JTable table = new JTable(teacherTableModel);
        table.setFont(dataFont);
        table.setRowHeight(30);
        table.getTableHeader().setFont(buttonFont);
        table.getTableHeader().setReorderingAllowed(false);

        // 버튼 렌더러 & 에디터 설정
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JTextField(), this, table));

        JScrollPane scrollPane = new JScrollPane(table);
        // 사이즈 조정 (패널에 맞게)
        scrollPane.setPreferredSize(new Dimension(800, 400));
        teacherSection.add(scrollPane);

        return teacherSection;
    }

    // [DB] 강사 데이터 로드
    public static int salaly;
    private Object[][] loadTeacherData() { // 데이터베이스에서 강사 정보를 불러오는 메서드
        Connection conn = null; // 데이터베이스 연결 객체
        PreparedStatement pstmt = null; // SQL 실행을 위한 객체
        ResultSet rs = null; // 쿼리 실행 결과(데이터)를 담는 객체
        List<Object[]> dataList = new ArrayList<>(); // 로드된 행 데이터를 저장할 리스트

        // member 테이블과 teacher 테이블을 조인하여 역할이 '강사'인 멤버의 상세 정보를 조회하는 SQL 쿼리
        String sql = "SELECT m.member_id, m.name, m.email, m.phone, m.address, t.hourly_rate FROM member m JOIN teacher t ON m.member_id = t.member_id WHERE m.role = '강사'";

        try {
            // DB 연결 정보
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL JDBC 드라이버를 로드
            // 데이터베이스에 연결
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

            pstmt = conn.prepareStatement(sql); // SQL을 실행
            rs = pstmt.executeQuery(); // SQL 쿼리를 실행하고 결과 받기
            while (rs.next()) { // 결과 집합의 다음 행이 있을 때까지 반복
                salaly = rs.getInt("hourly_rate");
                String rate = String.format("%,d원", rs.getInt("hourly_rate")); // 시간당 단가를 콤마 형식(예: 10,000원)으로 포맷
                Object[] row = {
                        rs.getString("member_id"), // 아이디
                        rs.getString("name"), // 이름
                        rs.getString("email"), // 이메일
                        rs.getString("phone"), // 전화번호
                        rs.getString("address"), // 주소
                        rate, // 포맷된 시간당 단가
                        "수정" // '관리' 열에 들어갈 버튼 레이블
                };
                dataList.add(row); // 행 데이터를 리스트에 추가
            }
        } catch (Exception ex) { // 예외 발생 시 처리
            ex.printStackTrace(); // 예외의 스택 트레이스를 출력
        } finally { // try-catch 블록이 끝난 후 항상 실행
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(conn!=null) conn.close(); } catch(Exception ex) {} // 데이터베이스 자원(ResultSet, PreparedStatement, Connection)을 닫습니다.
        }
        return dataList.toArray(new Object[0][0]); // 리스트에 저장된 모든 행 데이터를 2차원 배열로 변환하여 반환
    }

    private void updateAllPrices() { // 모든 강사의 시간당 단가를 일괄 수정하는 메서드
        if (teacherTableModel.getRowCount() == 0) { // 테이블에 강사 데이터가 없는 경우를 확인
            JOptionPane.showMessageDialog(this, "등록된 강사 정보가 없습니다.", "경고", JOptionPane.WARNING_MESSAGE); // 경고 메시지를 표시합니다.
            return;
        }

        JTextField priceField = new JTextField(10); // 새로운 단가를 입력받을 텍스트 필드를 생성
        int result = JOptionPane.showConfirmDialog(this, // 사용자에게 입력 대화 상자를 표시
                new Object[]{"모든 강사의 새로운 시간당 단가 (숫자만 입력):", priceField}, // 대화 상자에 표시할 컴포넌트
                "단가 수정", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE); // 대화 상자의 제목, 옵션, 메시지 유형을 설정

        if (result == JOptionPane.OK_OPTION) { // 사용자가 '확인'을 누름
            String newPriceStr = priceField.getText().trim();
            // 입력값이 '숫자'로만 구성되어 있는지 정규 표현식으로 검사
            if (!Pattern.matches("^[0-9]+$", newPriceStr)) {
                JOptionPane.showMessageDialog(this, "유효한 숫자 단가(원)를 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE); // 오류 메시지를 표시합니다.
                return;
            }

            int newRate = Integer.parseInt(newPriceStr); // 입력된 단가 문자열을 정수형으로 변환
            String newPriceFormatted = String.format("%,d원", newRate); // 새로운 단가를 콤마 형식으로 포맷

            Connection conn = null; // DB 연결 객체
            PreparedStatement pstmt = null; // SQL 실행 객체
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025"); // DB 연결

                String sql = "UPDATE teacher SET hourly_rate = ?"; // teacher 테이블의 모든 hourly_rate를 수정하는 SQL 쿼리
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, newRate); // 첫 번째 ?에 새로운 단가를 설정
                int affectedRows = pstmt.executeUpdate();

                // JTable 데이터 갱신
                for (int i = 0; i < teacherTableModel.getRowCount(); i++) {
                    // 테이블 모델의 '시간당 단가' 열(PRICE_COLUMN_INDEX)의 값을 새로운 포맷된 단가로 갱신
                    teacherTableModel.setValueAt(newPriceFormatted, i, PRICE_COLUMN_INDEX);
                }
                JOptionPane.showMessageDialog(this, "총 " + affectedRows + "명의 강사 단가가 수정되었습니다."); // 성공 메시지를 표시합니다.

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try { if(pstmt!=null) pstmt.close(); if(conn!=null) conn.close(); } catch(Exception ex) {}
            }
        }
    }

    // --- 내부 클래스 (버튼 렌더러/에디터) ---
    class ButtonRenderer extends JButton implements TableCellRenderer { // JTable 셀을 버튼처럼 보이게 하는 렌더러 클래스
        public ButtonRenderer() { // 생성자
            setOpaque(true); // 버튼 배경을 불투명하게 설정
            setFont(buttonFont); // 버튼 폰트를 설정
            setBackground(Color.WHITE); // 버튼 배경색을 흰색으로 설정
            setForeground(Color.BLACK); // 버튼 글자색을 검은색으로 설정
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // 옅은 회색 테두리로 설정
        }
        @Override
        // 셀의 렌더링 컴포넌트를 반환하는 메서드
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString()); // 셀 값으로 버튼 텍스트 설정
            return this; // 현재 버튼 객체를 반환하여 렌더링
        }
    }

    class ButtonEditor extends DefaultCellEditor { // JTable 셀의 버튼을 실제로 클릭하여 편집 이벤트를 처리하는 에디터 클래스
        private JButton button;
        private String label;
        private boolean isPushed;
        private TeacherPanel panel;
        private JTable table;

        public ButtonEditor(JTextField textField, TeacherPanel panel, JTable table) { // 생성자
            super(textField);
            this.panel = panel; // TeacherPanel 참조를 저장합니다.
            this.table = table;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped()); // 버튼 클릭 시 편집을 중지하고 getCellEditorValue()를 호출
        }

        @Override
        // 셀 편집을 시작할 때 호출되어, 편집에 사용할 컴포넌트를 반환
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table; // 테이블 참조를 업데이트
            label = (value == null) ? "" : value.toString(); // 버튼 텍스트를 설정
            button.setText(label); // 버튼 컴포넌트의 텍스트를 설정
            isPushed = true; // 버튼이 눌렸음(편집이 시작됨)을 표시
            return button; // 편집 컴포넌트로 버튼을 반환
        }

        @Override
        // 편집이 중지될 때 호출되어, 최종 셀 값을 반환
        public Object getCellEditorValue() {
            if (isPushed) {
                handleAction(table); // 강사 정보 수정/삭제 로직을 처리하는 메서드를 호출
            }
            isPushed = false; // 상태 리셋
            return label; // 버튼 텍스트를 반환
        }

        private void handleAction(JTable table) { // '관리' 버튼 클릭 시 수행할 작업을 처리하는 메서드
            int row = table.getSelectedRow(); // 현재 선택된 행의 인덱스를 가져옴
            if(row < 0) return; // 유효하지 않은 행일 경우 종료

            // 수정 또는 삭제 옵션 선택
            String[] options = {"정보 수정", "강사 삭제", "취소"}; // 사용자에게 제시할 옵션 배열
            int choice = JOptionPane.showOptionDialog(panel, // 옵션 대화 상자를 표시
                    table.getValueAt(row, 1).toString() + " 강사의 정보를 어떻게 관리하시겠습니까?", // 메시지 (강사 이름을 포함)
                    "강사 관리 옵션", // 제목
                    JOptionPane.YES_NO_CANCEL_OPTION, // 옵션 유형 (예/아니오/취소와 유사)
                    JOptionPane.QUESTION_MESSAGE, // 메시지 유형
                    null, // 아이콘 (null이면 기본 아이콘 사용)
                    options, // 옵션 버튼 텍스트 배열
                    options[2]); // 기본 선택 옵션 (취소)

            if (choice == 0) { // 정보 수정 선택 (options[0])
                showEditDialog(table, row); // 강사 정보 수정 대화 상자를 띄우는 메서드를 호출
            } else if (choice == 1) { // 강사 삭제 선택 (options[1])
                handleDeleteAction(table, row); // 강사 삭제를 처리하는 메서드를 호출
            }
        }

        private void showEditDialog(JTable table, int row) { // 강사 정보 수정 대화 상자를 표시하는 메서드
            String currentId = table.getValueAt(row, 0).toString(); // 현재 선택된 강사의 아이디
            String currentName = table.getValueAt(row, 1).toString(); // 현재 이름을
            String currentEmail = table.getValueAt(row, 2).toString(); // 현재 이메일
            String currentPhone = table.getValueAt(row, 3).toString(); // 현재 전화번호
            String currentAddress = table.getValueAt(row, 4).toString(); // 현재 주소

            JTextField nameField = new JTextField(currentName); // 이름 입력 필드 (현재 이름으로 초기화)
            JTextField phoneField = new JTextField(currentPhone); // 전화번호 입력 필드
            JTextField emailField = new JTextField(currentEmail); // 이메일 입력 필드
            JTextField addressField = new JTextField(currentAddress); // 주소 입력 필드

            Object[] message = { // 대화 상자에 표시할 컴포넌트 배열
                    "아이디: " + currentId, // 아이디는 수정 불가능하도록 텍스트로만 표시
                    "이름:", nameField,
                    "이메일:", emailField,
                    "전화번호:", phoneField,
                    "주소:", addressField
            };

            int option = JOptionPane.showConfirmDialog(panel, message, "강사 정보 수정", JOptionPane.OK_CANCEL_OPTION); // 수정 대화 상자를 표시합니다.
            if (option == JOptionPane.OK_OPTION) { // '확인'
                // DB 업데이트 및 테이블 갱신을 처리하는 메서드를 호출
                updateTeacherInfo(currentId, nameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText(), row);
            }
        }
        private void handleDeleteAction(JTable table, int row) { // 강사 삭제 확인 대화 상자를 표시하는 메서드
            String teacherName = table.getValueAt(row, 1).toString(); // 삭제할 강사의 이름을 가져옴
            String teacherId = table.getValueAt(row, 0).toString(); // 삭제할 강사의 아이디를 가져옴

            int confirm = JOptionPane.showConfirmDialog(panel, // 삭제 확인 대화 상자를 표시
                    teacherName + " 강사(" + teacherId + ")를 정말로 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.", // 경고 메시지
                    "강사 삭제 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) { // '예'를 눌러 삭제를 확정할 경우
                deleteTeacher(teacherId, row); // DB에서 강사를 삭제하는 메서드를 호출
            }
        }

        // DB 업데이트: 회원 정보 (member 테이블)만 업데이트
        private void updateTeacherInfo(String id, String name, String email, String phone, String addr, int row) { // 강사 정보를 DB와 테이블에 업데이트하는 메서드입니다.
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025"); // DB 연결

                // member 테이블의 이름, 이메일, 전화번호, 주소를 member_id를 기준으로 업데이트하는 SQL 쿼리
                String sql = "UPDATE member SET name=?, email=?, phone=?, address=? WHERE member_id=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name); pstmt.setString(2, email);
                pstmt.setString(3, phone); pstmt.setString(4, addr);
                pstmt.setString(5, id);

                pstmt.executeUpdate(); // 쿼리를 실행하여 DB를 업데이트

                // 테이블 갱신
                teacherTableModel.setValueAt(name, row, 1); // 테이블 모델의 이름 열을 새 이름으로 갱신
                teacherTableModel.setValueAt(email, row, 2); // 이메일 갱신
                teacherTableModel.setValueAt(phone, row, 3); // 전화번호 갱신
                teacherTableModel.setValueAt(addr, row, 4); // 주소 갱신

                JOptionPane.showMessageDialog(panel, "강사 정보가 수정되었습니다."); // 성공 메시지를 표시

            } catch (Exception ex) { // 예외 처리
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "강사 정보 수정 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE); // 오류 메시지를 표시합니다.
            } finally {
                try { if(pstmt!=null) pstmt.close(); if(conn!=null) conn.close(); } catch(Exception ex) {} // 자원을 닫습니다.
            }
        }

        private void deleteTeacher(String id, int row) { // DB에서 강사 정보와 테이블 행을 삭제하는 메서드
            Connection conn = null; // DB 연결 객체
            PreparedStatement pstmtMember = null;
            PreparedStatement pstmtTeacher = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025"); // DB 연결

                // 외래 키 제약 조건으로 인해 teacher 테이블을 먼저 삭제해야 할 수 있다

                // 1. teacher 테이블에서 삭제
                String sqlTeacher = "DELETE FROM teacher WHERE member_id = ?"; // teacher 테이블에서 해당 member_id를 가진 행을 삭제하는 SQL 쿼리입니다.
                pstmtTeacher = conn.prepareStatement(sqlTeacher);
                pstmtTeacher.setString(1, id); // member_id 설정
                pstmtTeacher.executeUpdate();

                // 2. member 테이블에서 삭제
                String sqlMember = "DELETE FROM member WHERE member_id = ?"; // member 테이블에서 해당 member_id를 가진 행을 삭제하는 SQL 쿼리
                pstmtMember = conn.prepareStatement(sqlMember);
                pstmtMember.setString(1, id);
                pstmtMember.executeUpdate(); // 쿼리 실행

                // JTable에서 행 삭제
                teacherTableModel.removeRow(row);

                JOptionPane.showMessageDialog(panel, "강사가 성공적으로 삭제되었습니다."); // 성공 메시지를 표시

            } catch (SQLException ex) { //예외 처리
                // 외래 키 제약 조건 위반 등의 SQL 에러 처리
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "강사 삭제 중 데이터베이스 오류가 발생했습니다. 관련 테이블의 레코드를 먼저 삭제해야 할 수 있습니다.\n" + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "강사 삭제 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if(pstmtMember!=null) pstmtMember.close(); } catch(Exception ex) {}
                try { if(pstmtTeacher!=null) pstmtTeacher.close(); } catch(Exception ex) {}
                try { if(conn!=null) conn.close(); } catch(Exception ex) {}
            }
        }
    }
}