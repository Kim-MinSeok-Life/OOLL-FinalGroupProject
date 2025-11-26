package OOLL_FinalTeamProject; // 패키지 선언

// ===== import 구간 =====
import javax.swing.*; // 스윙 컴포넌트
import java.awt.*; // 레이아웃

// ===== 기존 강좌 수정 다이얼로그 =====
public class TeacherLectureEdit extends LectureFormBase { // LectureFormBase 상속

    private JButton btnSave; // 저장 버튼
    private JButton btnClose; // 닫기 버튼

    private TeacherMain main; // TeacherMain 참조
    private TeacherMain.LectureInfo targetInfo; // 수정 대상 LectureInfo 객체

    // ===== 생성자 =====
    public TeacherLectureEdit( // 생성자
                               TeacherMain owner, // 부모 프레임 (TeacherMain)
                               TeacherMain.LectureInfo info // 수정할 강좌 정보 객체
    ) {
        super(owner, "강좌 수정"); // 수퍼 생성자 호출 (타이틀 포함)
        this.main = owner; // TeacherMain 참조 저장
        this.targetInfo = info; // 수정 대상 객체 저장

        applyOriginalValues(); // 기존 값들을 UI에 반영
        initButtons(); // 하단 버튼 구성
        setVisible(true); // 다이얼로그 표시
    }

    // ===== 기존 값으로 UI 채우기 =====
    private void applyOriginalValues() { // 기존 DB 값 적용 메서드
        setSubject(targetInfo.subject); // 과목명 콤보박스 선택
        setSelectedStartPeriod(targetInfo.startPeriod); // 시작 교시 콤보박스 선택
        setSelectedEndPeriod(targetInfo.endPeriod); // 종료 교시 콤보박스 선택
        applyDaysFromString(targetInfo.days); // 요일 체크박스 상태 설정
        setRoom(targetInfo.room); // 강의실 콤보박스 선택
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

    // ===== 저장 로직 구현 (강좌 수정) =====
    @Override
    protected void onSave() { // 추상 메서드 구현
        // 1) 시작/종료 교시 검증
        adjustPeriodIfInvalid(); // 시작/종료 교시 검증 및 자동 조정

        // 2) 현재 입력값 읽기
        String subject = (String) cbSubject.getSelectedItem(); // 수정된 과목명
        int start = getSelectedStartPeriod(); // 수정된 시작 교시
        int end = getSelectedEndPeriod(); // 수정된 종료 교시
        String days = buildSelectedDaysString(); // 수정된 요일 문자열
        String room = (String) cbRoom.getSelectedItem(); // 수정된 강의실

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

        // 4) 시간대 + 강의실 중복 체크 (자기 자신은 제외)
        if (main.hasLectureConflict(room, days, start, end, targetInfo)) {
            JOptionPane.showMessageDialog(
                    this,
                    "선택한 요일과 시간에 이미 같은 강의실에서 진행되는 다른 강좌가 있습니다.\n"
                            + "시간이나 강의실을 다시 선택해 주세요.",
                    "강좌 중복",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 5) 최종 확인 팝업
        int result = JOptionPane.showConfirmDialog( // 확인 다이얼로그 표시
                this, // 부모 컴포넌트
                "강좌를 수정하시겠습니까?", // 메시지
                "강좌 수정", // 타이틀
                JOptionPane.YES_NO_OPTION // 예/아니오 옵션
        );
        if (result != JOptionPane.YES_OPTION) { // 예가 아니면
            return; // 수정 취소
        }

        // 6) 대상 LectureInfo 객체에 수정된 값 반영
        targetInfo.subject = subject; // 과목명 갱신
        targetInfo.startPeriod = start; // 시작 교시 갱신
        targetInfo.endPeriod = end; // 종료 교시 갱신
        targetInfo.days = days; // 요일 갱신
        targetInfo.room = room; // 강의실 갱신

        // 7) TeacherMain 쪽 테이블 리프레시
        main.refreshLectureTable(); // 현재 검색 조건 기준으로 테이블 다시 그리기

        // TODO: 실제 구현에서는 DB UPDATE 로직도 여기서 수행

        dispose(); // 다이얼로그 닫기
    }
}
