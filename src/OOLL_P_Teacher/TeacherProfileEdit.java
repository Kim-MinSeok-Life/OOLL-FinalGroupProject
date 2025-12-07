// 강사 - 개인정보 수정 화면
package OOLL_P_Teacher;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class TeacherProfileEdit extends JDialog implements ActionListener {

    JTextField id, name, tel_number, address, email;
    JPasswordField passwd;
    JComboBox<String> tel;
    String[] code = {"010", "070", "02", "031", "032"};
    JButton submitB, cancelB, passwdEditB;

    private TeacherMain mainOwner; // TeacherMain 창을 참조하기 위해 선언

    TeacherProfileEdit(TeacherMain owner, String title) {
        super(owner, title, true); // 메인 객체 받아서 모달 설정 및 화면 제목 받아서 쓰기
        // 모달 설정 → JDialog 창 종료 전까지는 부모 창(TeacherMain 사용 불가능)

        mainOwner = owner; // TeacherMain 창을 참조하기 위해 대입

        Container ct = getContentPane();
        ct.setLayout(new BorderLayout());
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        top.setLayout(new GridLayout(6,1));
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));

        Dimension labelSize = new Dimension(50, 25); // 레이블 길이 통일

        JPanel p1 = new JPanel(); // 아이디
        p1.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l1 = new JLabel("아이디");
        id = new JTextField(30);
        id.setEditable(false); // 수정 불가능
        id.setBackground(Color.LIGHT_GRAY); // 수정 불가능을 회색으로 표시
       l1.setPreferredSize(labelSize); // 레이블 길이 통일
        p1.add(l1); p1.add(id);

        JPanel p2 = new JPanel(); // 비밀번호
        p2.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l2 = new JLabel("비밀번호");
        passwd = new JPasswordField(18);
        passwd.setEditable(false);
        passwd.setBackground(Color.LIGHT_GRAY); // 수정 불가능을 회색으로 표시
        passwdEditB = new JButton("비밀번호 관리");
        passwdEditB.addActionListener(this); // 이벤트 연결
        l2.setPreferredSize(labelSize);
        p2.add(l2); p2.add(passwd); p2.add(passwdEditB);

        JPanel p3 = new JPanel(); // 이름
        p3.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l3 = new JLabel("이름");
        name = new JTextField(30);
        l3.setPreferredSize(labelSize);
        p3.add(l3); p3.add(name);

        JPanel p4 = new JPanel(); // 전화번호
        p4.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l4 = new JLabel("전화번호");
        tel = new JComboBox<>(code);
        tel_number = new JTextField(25);
        l4.setPreferredSize(labelSize);
        p4.add(l4); p4.add(tel); p4.add(tel_number);

        JPanel p5 = new JPanel(); // 주소
        p5.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l5 = new JLabel("이메일");
        email = new JTextField(30);
        l5.setPreferredSize(labelSize);
        p5.add(l5); p5.add(email);

        JPanel p6 = new JPanel();
        p6.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l6 = new JLabel("주소");
        address = new JTextField(30);
        l6.setPreferredSize(labelSize);
        p6.add(l6); p6.add(address);

        top.add(p1); top.add(p2); top.add(p3); top.add(p4); top.add(p5); top.add(p6);

        submitB = new JButton("적용");
        cancelB = new JButton("닫기");
        submitB.addActionListener(this); // 이벤트 연결
        cancelB.addActionListener(this); // 이벤트 연결
        bottom.add(submitB); bottom.add(cancelB);

        ct.add(top, BorderLayout.CENTER);
        ct.add(bottom, BorderLayout.SOUTH);

        // 개인정보 데이터를 가져오기 위한 코드
        id.setText(owner.getUserId());
        name.setText(owner.getName());
        email.setText(owner.getEmail());
        address.setText(owner.getAddressData());

        String phone = owner.getPhone();
        if (phone != null && phone.contains("-")){
            String[] p = phone.split("-");
            tel.setSelectedItem(p[0]);
            tel_number.setText(p[1]+"-"+p[2]);
        }
        else{
            tel_number.setText(phone);
        }

        passwd.setText(owner.getPassword());
    } // 생성자
    
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if (cmd.equals("비밀번호 관리")) {
            TeacherPasswordEdit tpwe = new TeacherPasswordEdit(this, "비밀번호 관리");
            tpwe.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            tpwe.setSize(385, 195);
            tpwe.setLocationRelativeTo(null);
            tpwe.setVisible(true);

            // TeacherMain 창을 참조하여 데이터가 연결됨.
            // 이 상태에서 비밀번호 관리 창이 닫히면 UI 갱신
            passwd.setText(mainOwner.getPassword()); 
        }

        else if (cmd.equals("적용")){
            applyProfileUpdate(); // 개인정보 수정 내용 적용 메소드 호출
        }
        else if (cmd.equals("닫기")){
            dispose();
        }
    } // actionPerformed
    
    // 개인정보 수정 내용 '적용' 메소드
    private void applyProfileUpdate(){
        // onwer는 JDialog 생성자의 super인 owenr로 getOwner()로 접근 가능
        // getOwner는 현재 Dialog를 띄운 super 창을 반환
        TeacherMain owner = (TeacherMain) getOwner();

        // 사용자가 입력 및 수정한 값 읽어오기
        String newName = name.getText();
        String newEmail = email.getText();
        String newAddress = address.getText();
        String newPhone = tel.getSelectedItem()+"-"+tel_number.getText();

        Connection con = null;  // DB 연결용
        PreparedStatement ps = null; // SQL 실행용

        try{
            con = ConnectDB.getConnection(); // DB 연결
            String sql =
                    "UPDATE member SET name=?, phone=?, email=?, address=? " +
                            "WHERE member_id=?"; // 현재 로그인한 아이디에 대해서만 수정

            ps = con.prepareStatement(sql); // SQL을 실행 준비 상태로

            ps.setString(1, newName);
            ps.setString(2, newPhone);
            ps.setString(3, newEmail);
            ps.setString(4, newAddress);
            ps.setString(5, owner.getUserId());

            int result = ps.executeUpdate(); // UPDATE 실행, 영향받은 행의 개수를 반환
            
            // 행의 개수가 1 이상이면 변경사항이 있으므로 수정
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "개인정보가 수정되었습니다.");

                // TeacherMain 화면 필드에도 갱신 하기 위해 메소드 호출
                owner.updateProfileFromEdit(newName, newPhone, newEmail, newAddress);
                dispose(); // 수정창 닫기
            }
            else { JOptionPane.showMessageDialog(this, "수정된 행이 없습니다."); }
        } // try
        catch (SQLException e){
            e.printStackTrace();
        } // catch
        finally {
            ConnectDB.close(new AutoCloseable[]{ps, con});
        }
    } // applyProfileUpdate
}
