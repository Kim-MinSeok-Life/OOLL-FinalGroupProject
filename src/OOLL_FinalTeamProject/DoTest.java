package OOLL_FinalTeamProject;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
 
class LoginTest extends JFrame implements ActionListener {
    private JLabel title;
    private JPanel header;
    private JLabel textId;
    private JLabel textPw;
    private JTextField inputId;
    private JPasswordField inputPw;
    private JButton login_btn;
    private JButton cancel_btn;
    private JLabel result;
    private JLabel subTitle;

    private ImageIcon logo;
    private JLabel imageLabel;
    private Image scaled;

    LoginTest(String t) {
        super(t);

        // header 구성
        header = new JPanel();
        header.setLayout(null);
        header.setBounds(0, 0, 1000, 120);
        header.setBackground(new Color(210, 230, 255)); // 살짝 진한 파스텔톤

        // header 제목
        title = new JLabel("明知 LMS", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 50));
        title.setBounds(0, 30, 1000, 60);
        header.add(title);

        // 로고 이미지
        logo = new ImageIcon("C:/My2025_after/My2025_OOLL/mj_logo.jpg");
        scaled = logo.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        imageLabel = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
        imageLabel.setBounds(0, 105, 1000, 300);

        // 로고 옆 소제목
        subTitle = new JLabel("明知 Learning Management System", SwingConstants.CENTER);
        subTitle.setFont(new Font("맑은 고딕", Font.BOLD, 35));
        subTitle.setForeground(new Color(60, 60, 60));
        subTitle.setBounds(0, 390, 1000, 60);


        // 기본 컴포넌트
        textId = new JLabel("아이디: ");
        textPw = new JLabel("비밀번호: ");
        inputId = new JTextField("");
        inputPw = new JPasswordField("");
        login_btn = new JButton("Login");
        cancel_btn = new JButton("Cancel");
        result = new JLabel("");

        // 배경
        Container ct = getContentPane(); // 컴포넌트들을 창에 출력
        ct.setLayout(null); // 임의로 배치할 것임을 암시.
        ct.setBackground(new Color(235, 245, 255));

        // 위치 조정
 /*     textId.setBounds(300, 500, 100, 30);
        inputId.setBounds(400, 500, 250, 30);
        textPw.setBounds(300, 540, 100, 30);
        inputPw.setBounds(400, 540, 250, 30);
        login_btn.setBounds(400, 590, 100, 40);
        cancel_btn.setBounds(550, 590, 100, 40);*/

        textId.setBounds(330, 490, 80, 30);     // 💬 아이디 라벨 왼쪽
        inputId.setBounds(400, 500, 250, 30);   // 💬 입력창 오른쪽
        textPw.setBounds(330, 540, 80, 30);     // 💬 비밀번호 라벨 왼쪽
        inputPw.setBounds(400, 540, 250, 30);   // 💬 입력창 오른쪽
        login_btn.setBounds(400, 590, 100, 40); // 💬 로그인 버튼 중앙 왼쪽
        cancel_btn.setBounds(520, 590, 100, 40); // 💬 취소 버튼 중앙 오른쪽
        result.setBounds(350, 650, 400, 30);
        result.setForeground(Color.RED);

        textId.setSize(100, 50);
        // 버튼 리스너 연결
        login_btn.addActionListener(this);
        cancel_btn.addActionListener(this);

        // 화면에 추가
        ct.add(header);
        ct.add(imageLabel);
        ct.add(subTitle);
        ct.add(textId);
        ct.add(inputId);
        ct.add(textPw);
        ct.add(inputPw);
        ct.add(login_btn);
        ct.add(cancel_btn);
        ct.add(result);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == login_btn) {
            if (inputId.getText().equals("") || inputPw.getText().equals("")) {
                result.setText("아이디 또는 비밀번호를 입력하지 않으셨습니다!");
            }
            else
                result.setText("로그인 시도 중..");
        } // 겉 if
    else if(e.getSource()==cancel_btn) {
        inputId.setText("");
        inputPw.setText("");
        result.setText("");
    } // 겉 else if
}
}
public class DoTest {
    public static void main(String[] args) {
        LoginTest test = new LoginTest("Login");
        // test.setTitle("Login");
        test.setSize(385,195);
        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        test.setVisible(true);
    }

}



// git 때문에 수정해봄

// git rule 실험용으로 한줄추가