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

    JFrame parentFrame; // 부모 프레임 (팝업 띄울 때 사용)

    public TeacherPanel(JFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); // 배경색 통일

        // 메인 콘텐츠 패널
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 30, 15, 30));
        contentPanel.setBackground(Color.WHITE);

        // 1. 단가 공통 관리 버튼
        contentPanel.add(createPriceManagementPanel());
        contentPanel.add(Box.createVerticalStrut(25));

        // 2. 강사 관리 콘텐츠 (JTable)
        contentPanel.add(createTeacherManagementPanel());
        contentPanel.add(Box.createVerticalGlue());

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createPriceManagementPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("강사 단가 공통 관리: ");
        label.setFont(sectionTitleFont);
        panel.add(label);

        JButton updatePriceButton = new JButton("단가 수정");
        updatePriceButton.setFont(addButtonFont);
        updatePriceButton.setBackground(new Color(255, 165, 0));
        updatePriceButton.setForeground(Color.WHITE);
        updatePriceButton.setFocusPainted(false);
        updatePriceButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        updatePriceButton.addActionListener(e -> updateAllPrices());

        panel.add(updatePriceButton);
        return panel;
    }

    private JPanel createTeacherManagementPanel() {
        JPanel teacherSection = new JPanel();
        teacherSection.setLayout(new BoxLayout(teacherSection, BoxLayout.Y_AXIS));
        teacherSection.setBackground(Color.WHITE);
        teacherSection.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel title = new JLabel("강사 관리");
        title.setFont(sectionTitleFont);
        teacherSection.add(title);

        JLabel subtitle = new JLabel("등록된 강사의 정보를 관리합니다");
        subtitle.setFont(dataFont);
        subtitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        teacherSection.add(subtitle);

        // JTable 설정
        String[] columnNames = {"아이디", "이름", "이메일", "전화번호", "주소", "시간당 단가", "관리"};
        Object[][] initialData = loadTeacherData();

        teacherTableModel = new DefaultTableModel(initialData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // '관리' 열만 수정 가능
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
    private Object[][] loadTeacherData() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Object[]> dataList = new ArrayList<>();

        String sql = "SELECT m.member_id, m.name, m.email, m.phone, m.address, t.hourly_rate FROM member m JOIN teacher t ON m.member_id = t.member_id WHERE m.role = '강사'";

        try {
            // DB 연결 정보
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String rate = String.format("%,d원", rs.getInt("hourly_rate"));
                Object[] row = {
                        rs.getString("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rate,
                        "수정" // 버튼 레이블
                };
                dataList.add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); if(conn!=null) conn.close(); } catch(Exception ex) {}
        }
        return dataList.toArray(new Object[0][0]);
    }

    private void updateAllPrices() {
        if (teacherTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "등록된 강사 정보가 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField priceField = new JTextField(10);
        int result = JOptionPane.showConfirmDialog(this,
                new Object[]{"모든 강사의 새로운 시간당 단가 (숫자만 입력):", priceField},
                "단가 수정", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPriceStr = priceField.getText().trim();
            if (!Pattern.matches("^[0-9]+$", newPriceStr)) {
                JOptionPane.showMessageDialog(this, "유효한 숫자 단가(원)를 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newRate = Integer.parseInt(newPriceStr);
            String newPriceFormatted = String.format("%,d원", newRate);

            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                String sql = "UPDATE teacher SET hourly_rate = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, newRate);
                int affectedRows = pstmt.executeUpdate();

                for (int i = 0; i < teacherTableModel.getRowCount(); i++) {
                    teacherTableModel.setValueAt(newPriceFormatted, i, PRICE_COLUMN_INDEX);
                }
                JOptionPane.showMessageDialog(this, "총 " + affectedRows + "명의 강사 단가가 수정되었습니다.");

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try { if(pstmt!=null) pstmt.close(); if(conn!=null) conn.close(); } catch(Exception ex) {}
            }
        }
    }

    // --- 내부 클래스 (버튼 렌더러/에디터) ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(buttonFont);
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private TeacherPanel panel;
        private JTable table;

        public ButtonEditor(JTextField textField, TeacherPanel panel, JTable table) {
            super(textField);
            this.panel = panel;
            this.table = table;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 수정 로직 실행
                handleAction(table);
            }
            isPushed = false;
            return label;
        }

        private void handleAction(JTable table) {
            int row = table.getSelectedRow();
            if(row < 0) return;

            // 수정 또는 삭제 옵션 선택
            String[] options = {"정보 수정", "강사 삭제", "취소"};
            int choice = JOptionPane.showOptionDialog(panel,
                    table.getValueAt(row, 1).toString() + " 강사의 정보를 어떻게 관리하시겠습니까?",
                    "강사 관리 옵션",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]); // 취소를 기본값으로

            if (choice == 0) { // 정보 수정 선택
                showEditDialog(table, row);
            } else if (choice == 1) { // 강사 삭제 선택
                handleDeleteAction(table, row);
            }
        }

        private void showEditDialog(JTable table, int row) {
            String currentId = table.getValueAt(row, 0).toString();
            String currentName = table.getValueAt(row, 1).toString();
            String currentEmail = table.getValueAt(row, 2).toString();
            String currentPhone = table.getValueAt(row, 3).toString();
            String currentAddress = table.getValueAt(row, 4).toString();

            JTextField nameField = new JTextField(currentName);
            JTextField phoneField = new JTextField(currentPhone);
            JTextField emailField = new JTextField(currentEmail);
            JTextField addressField = new JTextField(currentAddress);

            Object[] message = {
                    "아이디: " + currentId,
                    "이름:", nameField,
                    "이메일:", emailField,
                    "전화번호:", phoneField,
                    "주소:", addressField
            };

            int option = JOptionPane.showConfirmDialog(panel, message, "강사 정보 수정", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                updateTeacherInfo(currentId, nameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText(), row);
            }
        }

        // **새로 추가된 삭제 기능**
        private void handleDeleteAction(JTable table, int row) {
            String teacherName = table.getValueAt(row, 1).toString();
            String teacherId = table.getValueAt(row, 0).toString();

            int confirm = JOptionPane.showConfirmDialog(panel,
                    teacherName + " 강사(" + teacherId + ")를 정말로 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.",
                    "강사 삭제 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                deleteTeacher(teacherId, row);
            }
        }

        // DB 업데이트: 회원 정보 (member 테이블)만 업데이트
        private void updateTeacherInfo(String id, String name, String email, String phone, String addr, int row) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                String sql = "UPDATE member SET name=?, email=?, phone=?, address=? WHERE member_id=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name); pstmt.setString(2, email);
                pstmt.setString(3, phone); pstmt.setString(4, addr);
                pstmt.setString(5, id);

                pstmt.executeUpdate();

                // 테이블 갱신
                teacherTableModel.setValueAt(name, row, 1);
                teacherTableModel.setValueAt(email, row, 2);
                teacherTableModel.setValueAt(phone, row, 3);
                teacherTableModel.setValueAt(addr, row, 4);

                JOptionPane.showMessageDialog(panel, "강사 정보가 수정되었습니다.");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "강사 정보 수정 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if(pstmt!=null) pstmt.close(); if(conn!=null) conn.close(); } catch(Exception ex) {}
            }
        }

        // **DB 삭제 메서드 (새로 추가)**
        private void deleteTeacher(String id, int row) {
            Connection conn = null;
            PreparedStatement pstmtMember = null;
            PreparedStatement pstmtTeacher = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/academy_lms?serverTimezone=UTC", "root", "java2025");

                // 외래 키 제약 조건으로 인해 teacher 테이블을 먼저 삭제해야 할 수 있습니다.
                // 트랜잭션을 적용하면 더 안전하지만, 여기서는 간단하게 쿼리 두 개를 실행합니다.

                // 1. teacher 테이블에서 삭제 (member_id가 외래 키로 참조될 가능성이 높음)
                String sqlTeacher = "DELETE FROM teacher WHERE member_id = ?";
                pstmtTeacher = conn.prepareStatement(sqlTeacher);
                pstmtTeacher.setString(1, id);
                pstmtTeacher.executeUpdate();

                // 2. member 테이블에서 삭제
                String sqlMember = "DELETE FROM member WHERE member_id = ?";
                pstmtMember = conn.prepareStatement(sqlMember);
                pstmtMember.setString(1, id);
                pstmtMember.executeUpdate();

                // JTable에서 행 삭제
                teacherTableModel.removeRow(row);

                JOptionPane.showMessageDialog(panel, "강사가 성공적으로 삭제되었습니다.");

            } catch (SQLException ex) {
                // SQL 에러 처리 (예: 외래 키 제약 조건 위반 등)
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