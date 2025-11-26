package OOLL_FinalTeamProject; // 패키지 선언

// ===== import 구간 =====
import javax.swing.*; // 스윙 컴포넌트
import java.awt.*; // 레이아웃

// ===== 강사 개인정보 수정 다이얼로그 =====
public class TeacherProfileEdit extends JDialog { // JDialog 상속

    // ===== 필드: 소유자(Main) 참조 =====
    private TeacherMain main; // TeacherMain 참조

    // ===== 필드: 입력 컴포넌트 =====
    private JTextField tfId; // 아이디 (수정 불가)
    private JPasswordField pfPassword; // 비밀번호 입력 필드 (표시만, 수정 불가)
    private JButton btnManagePassword; // 비밀번호 관리 버튼
    private JTextField tfName; // 이름 입력 필드
    private JTextField tfPhone; // 연락처 입력 필드
    private JTextField tfAddress; // 주소 입력 필드

    private JButton btnApply; // 적용 버튼
    private JButton btnClose; // 닫기 버튼

    // ===== 생성자 =====
    public TeacherProfileEdit(
            TeacherMain owner, // 부모 프레임 (TeacherMain)
            String id, // 아이디
            String name, // 이름
            String phone, // 연락처
            String address // 주소
    ) {
        super(owner, "개인정보 수정", true); // 타이틀, 모달 설정
        this.main = owner; // TeacherMain 참조 저장

        setSize(350, 280); // 요구사항: 모든 팝업창 385x195
        setLocationRelativeTo(owner); // 부모 기준 중앙 정렬

        initComponents(id, name, phone, address); // UI 구성
        setVisible(true); // 다이얼로그 표시
    }

    // ===== 컴포넌트 초기화 =====
    private void initComponents(String id, String name, String phone, String address) { // 컴포넌트 구성 메서드
        Container c = getContentPane(); // 컨텐트팬 가져오기
        c.setLayout(new BorderLayout()); // BorderLayout 설정

        JPanel formPanel = new JPanel(new GridBagLayout()); // GridBagLayout으로 폼 패널 생성
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 여백 설정

        GridBagConstraints gbc = new GridBagConstraints(); // GridBagConstraints 생성
        gbc.insets = new Insets(3, 3, 3, 3); // 간격 설정
        gbc.fill = GridBagConstraints.HORIZONTAL; // 수평 방향으로 채우기

        int row = 0; // 현재 행 번호

        // ---- 아이디 (수정 불가, 회색) ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("아이디"), gbc); // 아이디 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        tfId = new JTextField(id); // 아이디 필드 생성
        tfId.setEditable(false); // 수정 불가능 설정
        tfId.setBackground(Color.LIGHT_GRAY); // 배경색 회색으로 설정
        formPanel.add(tfId, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 비밀번호 + 비밀번호 관리 버튼 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("비밀번호"), gbc); // 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        JPanel pwPanel = new JPanel(new BorderLayout()); // 비밀번호 + 버튼 패널
        pfPassword = new JPasswordField(10); // 비밀번호 입력 필드
        pfPassword.setEditable(false); // 여기서는 비밀번호 수정 불가
        pfPassword.setText("********"); // 그냥 마스킹된 값 보여주기 용
        btnManagePassword = new JButton("비밀번호 관리"); // 비밀번호 관리 버튼 생성

        btnManagePassword.addActionListener(e -> { // 비밀번호 관리 버튼 클릭 시
            new TeacherPasswordEdit(main); // 비밀번호 관리 다이얼로그 호출 (TeacherMain 넘김)
        });

        pwPanel.add(pfPassword, BorderLayout.CENTER); // 중앙에 비밀번호 필드
        pwPanel.add(btnManagePassword, BorderLayout.EAST); // 오른쪽에 관리 버튼
        formPanel.add(pwPanel, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 이름 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("이름"), gbc); // 이름 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        tfName = new JTextField(name); // 이름 입력 필드 생성
        formPanel.add(tfName, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 연락처 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("연락처"), gbc); // 연락처 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        tfPhone = new JTextField(phone); // 연락처 입력 필드 생성
        formPanel.add(tfPhone, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 주소 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("주소"), gbc); // 주소 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        tfAddress = new JTextField(address); // 주소 입력 필드 생성
        formPanel.add(tfAddress, gbc); // 폼 패널에 추가

        c.add(formPanel, BorderLayout.CENTER); // 중앙에 폼 패널 부착

        // ---- 하단 버튼 패널 ----
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 하단 버튼 패널 생성
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
        int result = JOptionPane.showConfirmDialog( // 확인 다이얼로그 표시
                this, // 부모 컴포넌트
                "정말 수정하시겠습니까?", // 메시지
                "개인정보 수정", // 타이틀
                JOptionPane.YES_NO_OPTION // 예/아니오 옵션
        );
        if (result != JOptionPane.YES_OPTION) { // 예가 아니면
            return; // 수정 취소
        }

        String newName = tfName.getText().trim(); // 이름 값
        String newPhone = tfPhone.getText().trim(); // 연락처 값
        String newAddress = tfAddress.getText().trim(); // 주소 값

        // 비밀번호는 여기서 절대 수정하지 않음 (요구사항)
        // TeacherMain 에 변경 내용 반영
        main.updateTeacherProfile(newName, newPhone, newAddress); // 메인에 변경 전달

        dispose(); // 다이얼로그 닫기
    }
}
