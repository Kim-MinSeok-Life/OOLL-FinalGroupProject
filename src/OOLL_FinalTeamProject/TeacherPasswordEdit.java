package OOLL_FinalTeamProject; // 패키지 선언

// ===== import 구간 =====
import javax.swing.*; // 스윙 컴포넌트
import java.awt.*; // 레이아웃

// ===== 비밀번호 관리 다이얼로그 =====
public class TeacherPasswordEdit extends JDialog { // JDialog 상속

    private TeacherMain main; // TeacherMain 참조

    private JPasswordField pfCurrentPassword; // 비밀번호 입력 필드
    private JComboBox<String> cbHintQuestion; // 비밀번호 힌트 질문 콤보박스
    private JTextField tfHintAnswer; // 비밀번호 힌트 답 입력 필드
    private JButton btnApply; // 적용 버튼
    private JButton btnClose; // 닫기 버튼

    // ===== 생성자 =====
    public TeacherPasswordEdit(TeacherMain owner) { // 생성자
        super(owner, "비밀번호 관리", true); // 타이틀, 모달 설정
        this.main = owner; // TeacherMain 참조 저장

        setSize(385, 195); // 요구사항: 모든 팝업창 385x195
        setLocationRelativeTo(owner); // 부모 기준 중앙 정렬

        initComponents(); // UI 구성
        setVisible(true); // 다이얼로그 표시
    }

    // ===== 컴포넌트 초기화 =====
    private void initComponents() { // UI 구성 메서드
        Container c = getContentPane(); // 컨텐트팬 가져오기
        c.setLayout(new BorderLayout()); // BorderLayout 설정

        JPanel formPanel = new JPanel(new GridBagLayout()); // GridBagLayout 폼 패널
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 여백 설정

        GridBagConstraints gbc = new GridBagConstraints(); // GridBagConstraints 생성
        gbc.insets = new Insets(3, 3, 3, 3); // 간격 설정
        gbc.fill = GridBagConstraints.HORIZONTAL; // 수평 채우기

        int row = 0; // 현재 행 번호

        // ---- 비밀번호 입력 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("비밀번호"), gbc); // 라벨: "비밀번호"

        gbc.gridx = 1; // 두 번째 열
        pfCurrentPassword = new JPasswordField(15); // 비밀번호 입력 필드 생성
        formPanel.add(pfCurrentPassword, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 비밀번호 힌트 질문 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("힌트 질문"), gbc); // 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        cbHintQuestion = new JComboBox<>( // 콤보박스 생성
                new String[]{ // 예시 질문 목록
                        "기억에 남는 선생님 성함은?",
                        "초등학교 이름은?",
                        "가장 좋아하는 음식은?",
                        "가장 친한 친구 이름은?"
                }
        );
        // TeacherMain 에 저장된 힌트 질문과 맞으면 그걸 선택하도록 함
        String savedQ = main.getTeacherPwHintQuestion(); // 저장된 힌트 질문
        if (savedQ != null) { // null이 아니면
            cbHintQuestion.setSelectedItem(savedQ); // 콤보박스에서 동일한 항목 선택 시도
        }

        formPanel.add(cbHintQuestion, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 비밀번호 힌트 답 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("힌트 답"), gbc); // 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        tfHintAnswer = new JTextField(15); // 힌트 답 입력 필드 생성
        String savedA = main.getTeacherPwHintAnswer(); // 저장된 힌트 답
        if (savedA != null) { // null이 아니면
            tfHintAnswer.setText(savedA); // 기본값으로 채워넣기
        }
        formPanel.add(tfHintAnswer, gbc); // 폼 패널에 추가

        c.add(formPanel, BorderLayout.CENTER); // 중앙에 폼 패널 부착

        // ---- 하단 버튼 패널 ----
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 버튼 패널 생성
        btnApply = new JButton("적용"); // 적용 버튼 생성
        btnClose = new JButton("닫기"); // 닫기 버튼 생성

        btnApply.addActionListener(e -> onApply()); // 적용 버튼 클릭 시 onApply 호출
        btnClose.addActionListener(e -> dispose()); // 닫기 버튼 클릭 시 다이얼로그 닫기

        bottomPanel.add(btnApply); // 패널에 적용 버튼 추가
        bottomPanel.add(btnClose); // 패널에 닫기 버튼 추가

        c.add(bottomPanel, BorderLayout.SOUTH); // 하단에 버튼 패널 부착
    }

    // ===== 적용 버튼 로직 =====
    private void onApply() { // 적용 버튼 클릭 처리 메서드
        int result = JOptionPane.showConfirmDialog( // 확인 다이얼로그
                this, // 부모 컴포넌트
                "정말 수정하시겠습니까?", // 메시지
                "비밀번호 관리", // 타이틀
                JOptionPane.YES_NO_OPTION // 예/아니오 옵션
        );
        if (result != JOptionPane.YES_OPTION) { // 예가 아니면
            return; // 수정 취소
        }

        String currentPw = new String(pfCurrentPassword.getPassword()); // 입력된 비밀번호 값
        String hintQ = (String) cbHintQuestion.getSelectedItem(); // 선택된 힌트 질문
        String hintA = tfHintAnswer.getText().trim(); // 입력된 힌트 답

        // TODO: 실제로는 여기서 비밀번호 검증 + DB UPDATE 로직이 들어가야 함
        // 요구사항: 기존과 같은 비밀번호 입력해도 별도 경고 팝업은 띄우지 않음

        // TeacherMain 쪽 내부 상태 업데이트
        main.updateTeacherPassword(currentPw, hintQ, hintA); // 메인에 변경 전달

        dispose(); // 다이얼로그 닫기
    }
}
