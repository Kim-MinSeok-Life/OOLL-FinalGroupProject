package OOLL_P_Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField tfId;
    private JPasswordField pfPw;

    public LoginFrame() {
        setTitle("로그인");
        setSize(360, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; add(new JLabel("아이디"), g);
        tfId = new JTextField(15);
        g.gridx = 1; add(tfId, g);

        g.gridx = 0; g.gridy = 1; add(new JLabel("비밀번호"), g);
        pfPw = new JPasswordField(15);
        g.gridx = 1; add(pfPw, g);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogin = new JButton("로그인");
        JButton btnExit = new JButton("종료");
        btnP.add(btnLogin);
        btnP.add(btnExit);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; add(btnP, g);

        btnExit.addActionListener(e -> System.exit(0));
        btnLogin.addActionListener(e -> doLogin());

        setVisible(true);
    }

    private void doLogin() {
        String id = tfId.getText().trim();
        String pw = new String(pfPw.getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디/비밀번호를 입력하세요.");
            return;
        }
        String sql = "SELECT password, role FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, id);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password");
                    String role = rs.getString("role");
                    if (stored.equals(pw)) {
                        JOptionPane.showMessageDialog(this, "로그인 성공: " + id);
                        SwingUtilities.invokeLater(() -> {
                            StudentFrame sf = new StudentFrame(id);
                            sf.setVisible(true);
                        });
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "비밀번호가 틀렸습니다.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "회원 정보를 찾을 수 없습니다.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "로그인 중 오류: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
