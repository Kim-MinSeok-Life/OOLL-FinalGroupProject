package OOLL_FinalTeamProject; // 패키지 선언

// ===== import 구간 =====
import javax.swing.*; // 스윙 컴포넌트
import java.awt.*; // 레이아웃

// ===== 신규 강좌 개설 다이얼로그 =====
public class TeacherLectureCreate extends LectureFormBase { // LectureFormBase 상속

    private JButton btnSave; // 저장 버튼
    private JButton btnClose; // 닫기 버튼

    private TeacherMain main; // TeacherMain 참조

    // ===== 생성자 =====
    public TeacherLectureCreate(TeacherMain owner) { // 생성자
        super(owner, "신규 강좌 개설"); // 수퍼 생성자 호출 (타이틀 포함)
        this.main = owner; // TeacherMain 참조 저장
        initButtons(); // 하단 버튼 구성
        setVisible(true); // 다이얼로그 표시
    }

    // ===== 하단 버튼 구성 =====
    private void initButtons() { // 버튼 구성 메서드
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 하단 버튼 패널 생성
        btnSave = new JButton("저장"); // 저장 버튼 생성
        btnClose = new JButton("닫기"); // 닫기 버튼 생성

        btnSave.addActionListener(e -> onSave()); // 저장 버튼 클릭 시 onSave 호출
        btnClose.addActionListener(e -> dispose()); // 닫기 버튼 클릭 시 다이얼로그 닫기

        bottomPanel.add(btnSave); // 패널에 저장 버튼 추가
        bottomPanel.add(btnClose); // 패널에 닫기 버튼 추가

        getContentPane().add(bottomPanel, BorderLayout.SOUTH); // 다이얼로그 하단에 버튼 패널 부착
    }

    // ===== 저장 로직 구현 (강좌 생성) =====
    @Override
    protected void onSave() { // 추상 메서드 구현
        // 1) 시작/종료 교시 검증
        adjustPeriodIfInvalid(); // 시작/종료 교시 검증 및 자동 조정

        // 2) 현재 입력값 읽기
        String subject = (String) cbSubject.getSelectedItem(); // 선택된 과목명 가져오기
        int start = getSelectedStartPeriod(); // 선택된 시작 교시 숫자
        int end = getSelectedEndPeriod(); // 선택된 종료 교시 숫자
        String days = buildSelectedDaysString(); // 선택된 요일 문자열 생성
        String room = (String) cbRoom.getSelectedItem(); // 선택된 강의실

        // 3) 요일 미선택 예외 처리
        if (days.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "요일을 최소 1개 이상 선택해야 합니다.",
                    "입력 확인",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 4) 시간대 + 강의실 중복 체크
        if (main.hasLectureConflict(room, days, start, end, null)) {
            JOptionPane.showMessageDialog(
                    this,
                    "선택한 요일과 시간에 이미 같은 강의실에서 진행되는 강좌가 있습니다.\n"
                            + "시간이나 강의실을 다시 선택해 주세요.",
                    "강좌 중복",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 5) 최종 확인 팝업
        int result = JOptionPane.showConfirmDialog( // 확인 다이얼로그 표시
                this, // 부모 컴포넌트
                "강좌를 생성하시겠습니까?", // 메시지
                "신규 강좌 생성", // 타이틀
                JOptionPane.YES_NO_OPTION // 예/아니오 옵션
        );
        if (result != JOptionPane.YES_OPTION) { // 예가 아닐 경우
            return; // 저장 취소
        }

        // 6) TeacherMain 쪽에 신규 강좌 추가 + 테이블 갱신 요청
        main.addNewLecture(subject, start, end, days, room); // 메인에 전달

        // TODO: 실제 구현에서는 DB INSERT 로직도 이 시점에서 호출

        dispose(); // 다이얼로그 닫기
    }
}
