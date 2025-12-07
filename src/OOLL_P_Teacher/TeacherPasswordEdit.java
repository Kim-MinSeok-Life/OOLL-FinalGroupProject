// 강사 - 개인정보 수정 - 비밀번호 관리 화면
package OOLL_P_Teacher;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class TeacherPasswordEdit extends JDialog implements ActionListener {

    JPasswordField passwd; // 비밀번호 입력칸
    JComboBox<String> pwHintQuestion; // 본인확인 질문 콤보박스
    JTextField pwHintAnswer; // 본인확인 답 입력칸
    JButton submitB, cancelB; // 적용, 취소 버튼

    private TeacherMain mainOwner; // 최상위 부모를 참조해둘 객체

    // 생성자 (부모: TeacherProfileEdit)
    public TeacherPasswordEdit(TeacherProfileEdit owner, String title) {
        super(owner, title, true);

        mainOwner = (TeacherMain) owner.getOwner(); // 객체 대입
        
        Container ct = getContentPane();
        ct.setLayout(new BorderLayout());
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        top.setLayout(new GridLayout(3,1));
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));

        Dimension labelSize = new Dimension(75, 25); // 레이블 길이 통일

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l1 = new JLabel("비밀번호");
        l1.setPreferredSize(labelSize);
        passwd = new JPasswordField(25);
        p1.add(l1); p1.add(passwd);

        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l2 = new JLabel("힌트 질문");
        l2.setPreferredSize(labelSize);
        pwHintQuestion = new JComboBox<>();
        pwHintQuestion.setPreferredSize(new Dimension(250, 20));
        // 본인확인 질문 안내문구
        pwHintQuestion.addItem("질문을 선택하세요");
        // 본인확인 질문 3가지 항목
        pwHintQuestion.addItem("가장 좋아하는 음식은?");
        pwHintQuestion.addItem("어머니의 성함은?");
        pwHintQuestion.addItem("출신 초등학교 이름은?");
        p2.add(l2); p2.add(pwHintQuestion);

        JPanel p3 = new JPanel();
        p3.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l3 = new JLabel("힌트 답");
        l3.setPreferredSize(labelSize);
        pwHintAnswer = new JTextField(25);
        p3.add(l3); p3.add(pwHintAnswer);

        top.add(p1); top.add(p2); top.add(p3);

        submitB = new JButton("적용");
        cancelB = new JButton("취소");
        submitB.addActionListener(this);
        cancelB.addActionListener(this);
        bottom.add(submitB); bottom.add(cancelB);

        ct.add(top, BorderLayout.CENTER);
        ct.add(bottom, BorderLayout.SOUTH);

        loadCurrentPasswordInfo(); // DB에서 읽어온 값 기반, 기존 데이터를 입력칸에 채우는 메소드 호출
    } // 생성자

    // 현재 로그인한 강사의 비밀번호/힌트질문/힌트답을 입력칸에 채워넣는 메소드
    private void loadCurrentPasswordInfo(){
        passwd.setText(mainOwner.getPassword()); // 기존 비밀번호 채우기
        String currentQuestion = mainOwner.getSecurityQuestion(); // 기존 본인확인 질문 채우기
        pwHintQuestion.setSelectedItem(currentQuestion);
        pwHintAnswer.setText(mainOwner.getSecurityAnswer()); // 기존 본인확인 답 채우기
    } // loadCurrentPasswordInfo 끝

    // 버튼 이벤트 처리
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.equals("취소")){dispose(); return; } // 창을 닫고 메소드 빠져나오기
        else if (cmd.equals("적용")) {
            // 화면에서 입력된 값 받아오기
            String newPw = passwd.getText().trim(); // 공백 제거 후 대입 [이후 검사예정]
            String newQuestion = (String) pwHintQuestion.getSelectedItem();
            String newAnswer = pwHintAnswer.getText().trim(); // 공백 제거 후 대입 [이후 검사예정]
            
            // 유효성 검사 시작
            // 비밀번호, 본인확인 답 → 공백검사
            // 본인확인 질문 → 안내 문구 제외한 질문 선택 여부 검사

            // 비밀번호 공백 검사
            if (newPw.equals("")){
                JOptionPane.showMessageDialog(
                        this,
                        "비밀번호를 입력해주세요.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
                return; // 경고창 띄운 후, 메소드만 빠져나옴.
                // 사용자는 다시 적용하려면 다시 '적용'을 클릭해야함.
            }

            // 비밀번호 길이 검사
            if (newPw.length() < 8) {
                JOptionPane.showMessageDialog(
                        this,
                        "비밀번호는 8자 이상이어야 합니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
                return; // 경고창 띄운 후, 메소드만 빠져나옴.
                // 사용자는 다시 적용하려면 다시 '적용'을 클릭해야함.
            }

            // 본인확인 질문 선택 검사
            if (pwHintQuestion.getSelectedIndex() == 0){
                JOptionPane.showMessageDialog(
                        this,
                        "힌트 질문을 선택해주세요.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
                return; // 경고창 띄운 후, 메소드만 빠져나옴.
                // 사용자는 다시 적용하려면 다시 '적용'을 클릭해야함.
            }
            
            // 본인확인 질문 공백 검사
            if (newAnswer.equals("")){
                JOptionPane.showMessageDialog(
                        this,
                        "힌트 답변을 입력해주세요.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                );
                return; // 경고창 띄운 후, 메소드만 빠져나옴.
                // 사용자는 다시 적용하려면 다시 '적용'을 클릭해야함.
            }

            // DB UPDATE 메소드
            updatePasswordInfo(newPw, newQuestion, newAnswer);

            // TeacherMain 내부 비밀번호 관련 값 갱신
            mainOwner.setPassword(newPw);
            mainOwner.setSecurityQuestion(newQuestion);
            mainOwner.setSecurityAnswer(newAnswer);

            JOptionPane.showMessageDialog(this, "적용되었습니다.");
            dispose(); // 창 닫기
        } // else if
    } // actionPerformed 끝

    // DB UPDATE 처리 메소드
    private void updatePasswordInfo(String newPw, String newQuestion, String newAnswer) {
        Connection con = null;
        PreparedStatement ps = null;

        try{
            con = ConnectDB.getConnection(); // DB연결
            // 비밀번호, 본인확인 질문 & 답 UPDATE문
            String sql =
                    "UPDATE member " +
                    "SET password = ?, security_question = ?, security_answer = ? " +
                    "WHERE member_id = ?";
            ps = con.prepareStatement(sql);

            ps.setString(1, newPw);
            ps.setString(2, newQuestion);
            ps.setString(3, newAnswer);
            ps.setString(4, mainOwner.getUserId());

            ps.executeUpdate(); // UPDATE 실행
        }
        catch (SQLException e){ e.printStackTrace(); }
        finally { ConnectDB.close(new AutoCloseable[]{ps, con});} // 자원 정리 (연결 끊기)
    }
}
