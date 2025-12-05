// 학생 - 개인정보 수정 창
package OOLL_FinalTeamProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

// 개인정보 수정 전용 다이얼로그 (독립 클래스)
// 비밀번호 변경은 오직 PasswordChangeDialog를 통해서만 수행
public class StudentEditDialog extends JDialog {
    private boolean saved = false;

    // 기간(교시) 입력에서 사용할 상수
    public static final String[] Period = {
            "1교시(09:00 ~ 09:50)",
            "2교시(10:00 ~ 10:50)",
            "3교시(11:00 ~ 11:50)",
            "4교시(13:00 ~ 13:50)",
            "5교시(14:00 ~ 14:50)",
            "6교시(15:00 ~ 15:50)",
            "7교시(16:00 ~ 16:50)",
            "8교시(17:00 ~ 17:50)",
            "9교시(19:00 ~ 19:50)",
            "10교시(20:00 ~ 20:50)",
            "11교시(21:00 ~ 21:50)",
            "12교시(22:00 ~ 22:50)"
    };

    public StudentEditDialog(Frame owner, StudentService service, String memberId) {
        super(owner, "개인정보 수정", true);
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,8,8,8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(15);
        idField.setEditable(false);
        JPasswordField currentPwField = new JPasswordField(15);
        currentPwField.setEditable(false); // 요청: 현재 필드는 보여주기만, 편집 금지
        JTextField nameField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField addressField = new JTextField(25);

        // load initial data
        try {
            StudentInfo info = service.loadStudentInfo(memberId);
            if (info != null) {
                idField.setText(info.memberId);
                nameField.setText(info.name);
                phoneField.setText(info.phone);
                addressField.setText(info.address);
            }
            String pw = service.getPassword(memberId);
            if (pw != null) currentPwField.setText(pw);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터 로드 오류: " + ex.getMessage());
        }

        g.gridx = 0; g.gridy = 0; add(new JLabel("아이디"), g);
        g.gridx = 1; g.gridy = 0; add(idField, g);

        g.gridx = 0; g.gridy = 1; add(new JLabel("현재 비밀번호"), g);
        JPanel pwP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pwP.add(currentPwField);
        JButton changePwBtn = new JButton("비밀번호 변경");
        changePwBtn.addActionListener(e -> {
            PasswordChangeDialog pd = new PasswordChangeDialog(owner, service, memberId);
            pd.setVisible(true);
        });
        pwP.add(changePwBtn);
        g.gridx = 1; g.gridy = 1; add(pwP, g);

        g.gridx = 0; g.gridy = 2; add(new JLabel("이름"), g);
        g.gridx = 1; g.gridy = 2; add(nameField, g);

        g.gridx = 0; g.gridy = 3; add(new JLabel("연락처"), g);
        g.gridx = 1; g.gridy = 3; add(phoneField, g);

        g.gridx = 0; g.gridy = 4; add(new JLabel("주소"), g);
        g.gridx = 1; g.gridy = 4; add(addressField, g);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("취소");
        JButton save = new JButton("저장");
        btnP.add(cancel);
        btnP.add(save);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2; add(btnP, g);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newAddr = addressField.getText().trim();
            if (newName.isEmpty() || newPhone.isEmpty() || newAddr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이름, 연락처, 주소는 필수 입력입니다.", "입력 필요", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!Utils.isValidPhone(newPhone)) {
                JOptionPane.showMessageDialog(this, "유효한 전화번호를 입력하세요.", "전화번호 형식 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "정말 수정하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                boolean ok = service.updateMemberInfo(memberId, newName, newPhone, newAddr);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "정보가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
                    saved = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "수정 실패", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }
}
