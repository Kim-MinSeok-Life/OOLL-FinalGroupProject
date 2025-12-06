// 강사 - 기존 강의 수정
package OOLL_P_Teacher;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TeacherLectureEdit extends JDialog implements ActionListener {


    TeacherLectureEdit(TeacherMain owner, String title) {
        super(owner, title, true);
    }

    public void actionPerformed(ActionEvent e) {}
}
