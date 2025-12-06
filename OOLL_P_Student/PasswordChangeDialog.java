// 학생 - 비밀번호 변경 창
package OOLL_P_Student;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

/**
 * 비밀번호 변경 전용 다이얼로그 (독립 클래스)
 * - 현재 비밀번호 확인 후 변경
 */
public class PasswordChangeDialog extends JDialog {
    public PasswordChangeDialog(Frame owner, StudentService service, String memberId) {
        super(owner, "비밀번호 변경", true);
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,8,8,8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JPasswordField currentPw = new JPasswordField(15);
        JPasswordField newPw = new JPasswordField(15);
        JPasswordField confirmPw = new JPasswordField(15);

        g.gridx = 0; g.gridy = 0; add(new JLabel("현재 비밀번호"), g);
        g.gridx = 1; g.gridy = 0; add(currentPw, g);

        g.gridx = 0; g.gridy = 1; add(new JLabel("새 비밀번호"), g);
        g.gridx = 1; g.gridy = 1; add(newPw, g);

        g.gridx = 0; g.gridy = 2; add(new JLabel("새 비밀번호 확인"), g);
        g.gridx = 1; g.gridy = 2; add(confirmPw, g);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("취소");
        JButton change = new JButton("변경");
        btnP.add(cancel);
        btnP.add(change);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; add(btnP, g);

        cancel.addActionListener(e -> dispose());

        change.addActionListener(e -> {
            String cur = new String(currentPw.getPassword()).trim();
            String nw = new String(newPw.getPassword()).trim();
            String cf = new String(confirmPw.getPassword()).trim();

            if (cur.isEmpty() || nw.isEmpty() || cf.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 항목을 입력하세요.", "입력 필요", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!nw.equals(cf)) {
                JOptionPane.showMessageDialog(this, "새 비밀번호와 확인이 일치하지 않습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean ok = service.changePassword(memberId, cur, nw);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "현재 비밀번호가 일치하지 않거나 변경 실패", "오류", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "비밀번호가 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }
}