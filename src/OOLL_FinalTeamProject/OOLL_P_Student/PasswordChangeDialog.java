// 학생 - 비밀번호 변경 창
package OOLL_P_Student; // 패키지 선언

// import 선언
import javax.swing.*; // swing GUI(Graphical User Interface) 컴포넌트
import java.awt.*; // GUI를 위한 컴포넌트(Layout 등 관련)
import java.awt.event.*; // 이벤트 처리
import java.sql.SQLException; // DB 예외 처리

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

/* 비밀번호 변경 전용 다이얼로그(독립 클래스)
 * 현재 비밀번호 확인 후 변경
 * GridBagLayout을 사용해 UI 구성
*/
public class PasswordChangeDialog extends JDialog {
    public PasswordChangeDialog(Frame owner, StudentService service, String memberId) {
        super(owner, "비밀번호 변경", true); // 모달 다이얼로그 설정(true)
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,8,8,8); // 각 컴포넌트 간격(여백)
        g.fill = GridBagConstraints.HORIZONTAL; // 가로 방향 채우기

        JPasswordField currentPw = new JPasswordField(15); // 현재 비밀번호
        JPasswordField newPw = new JPasswordField(15); // 새 비밀번호
        JPasswordField confirmPw = new JPasswordField(15); // 새 비밀번호 확인

        // 현재 비밀번호 라벨 배치
        g.gridx = 0; g.gridy = 0; add(new JLabel("현재 비밀번호"), g);
        g.gridx = 1; g.gridy = 0; add(currentPw, g);

        // 새 비밀번호 라벨 배치
        g.gridx = 0; g.gridy = 1; add(new JLabel("새 비밀번호"), g);
        g.gridx = 1; g.gridy = 1; add(newPw, g);

        // 새 비밀번호 확인 라벨 배치
        g.gridx = 0; g.gridy = 2; add(new JLabel("새 비밀번호 확인"), g);
        g.gridx = 1; g.gridy = 2; add(confirmPw, g);

        // 취소&변경 버튼 배치
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("취소");
        JButton change = new JButton("변경");
        btnP.add(cancel);
        btnP.add(change);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; add(btnP, g); // 버튼 배치(버튼 패널이 2칸 차지)

        cancel.addActionListener(e -> dispose()); // 취소 버튼 실행 이벤트(다이얼로그 종료)

        change.addActionListener(e -> { // 변경 버튼 실행 이벤트 처리
            String cur = new String(currentPw.getPassword()).trim();
            String nw = new String(newPw.getPassword()).trim();
            String cf = new String(confirmPw.getPassword()).trim();

            // 입력값 유효성 검사
            if (cur.isEmpty() || nw.isEmpty() || cf.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 항목을 입력하세요.", "입력 필요", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 새 비밀번호&확인 비밀번호 불일치 검사
            if (!nw.equals(cf)) {
                JOptionPane.showMessageDialog(this, "새 비밀번호와 확인이 일치하지 않습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 새 비밀번호가 현재 비밀번호 동일 검사
            if (nw.equals(cur)) {
                JOptionPane.showMessageDialog(this, "새 비밀번호는 현재 비밀번호와 동일할 수 없습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 비밀번호 최소 길이 검사(8자 이상)
            if (nw.length() < 8) {
                JOptionPane.showMessageDialog(this, "비밀번호는 최소 8자 이상이어야 합니다.", "오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try { // DB 업데이트 처리
                boolean ok = service.changePassword(memberId, cur, nw);
                if (!ok) { // 현재 비밀번호 불일치 또는 변경 실패
                    JOptionPane.showMessageDialog(this, "현재 비밀번호가 일치하지 않거나 변경 실패", "오류", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "비밀번호가 변경되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // 성공 시 다이얼로그 닫기
                }
            } catch (SQLException ex) {
            	// DB 쿼리 수행 중 에러 발생 시
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack(); // 크기 자동 조절
        setLocationRelativeTo(owner); // 부모창(학생 페이지) 기준 중앙 표시
    }
}
