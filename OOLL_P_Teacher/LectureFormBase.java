// 강사 - [추상클래스: 강의 개설 및 수정 클래스 상속용]
package OOLL_P_Teacher;

import OOLL_P_Student.*;
import OOLL_P_Teacher.*;
import OOLL_P_Login.*;
import OOLL_P_Manager.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// 신규 강의 개설 & 기존 강의 수정 창들에 대한 수퍼 클래스
public abstract class LectureFormBase extends JDialog {

    // 데이터 값 ()
    String[] subjectName;
    int[] startPeriod;
    int[] endPeriod;

    // 공통 UI
    /// 처음 0, 학생이 수강신청 시 자동 증가하는 값
    JTextField Enrollment; // 수강인원

    /// 사용자가 입력하는 값
    JComboBox<String>subjectCb; // 과목명
    JComboBox<String> RoomCb; // 강의실명
    JTextField EnrollmentLimit; // 정원
    JCheckBox[] dayCB; // 요일 [체크박스] [배열]
    JComboBox<String>startPeriodCb; // 시작교시
    JComboBox<String>endPeriodCb; // 종료교시

    int[] Period = {1,2,3,4,5,6,7};





    ///  하단
    JButton saveBtn, closeBtn; // 저장, 닫기 버튼

    LectureFormBase(TeacherMain owner, String title) {
        super(owner, title, true);
        
    }

}
