// 학생 - 개인정보 수정 창
package OOLL_P_Student; // 패키지 선언

// import 선언
import javax.swing.*; // swing GUI(Graphical User Interface) 컴포넌트
import java.awt.*; // GUI를 위한 컴포넌트(Layout 등 관련)
import java.awt.event.*; // 이벤트 처리
import java.sql.SQLException; // DB 예외 처리
//패키지별 클래스 전체 가져오기
import OOLL_P_Student.*; // 학생 기능 관련 패키지 불러오기
import OOLL_P_Teacher.*; // 강사 기능 관련 패키지 불러오기
import OOLL_P_Login.*; // 로그인 기능 관련 패키지 불러오기
import OOLL_P_Manager.*; // 관리자 기능 관련 패키지 불러오기

// 개인정보 수정 전용 다이얼로그(독립 클래스)
// 비밀번호 변경은 PasswordChangeDialog 사용
public class StudentEditDialog extends JDialog {
    private boolean saved = false; // 저장 여부 확인(저장 시 true)

    // 생성자(부모 프레임, 서비스 객체, 회원 아이디 전달)
    public StudentEditDialog(Frame owner, StudentService service, String memberId) {
        super(owner, "개인정보 수정", true); // 모달 다이얼로그 생성
        setLayout(new GridBagLayout()); // 레이아웃 지정
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8); // 컴포넌트 여백
        g.fill = GridBagConstraints.HORIZONTAL; // 가로 방향 채우기

        // 필드 생성
        JTextField idField = new JTextField(15); // 회원 아이디
        idField.setEditable(false); // 아이디 수정 불가
        JPasswordField currentPwField = new JPasswordField(15); // 현재 비밀번호
        currentPwField.setEditable(false); // 현재 필드 수정 불가능(버튼 클릭 시 수정 가능)
        JTextField nameField = new JTextField(15); // 회원 이름
        JTextField phoneField = new JTextField(15); // 회원 연락처
        JTextField emailField = new JTextField(20); // 회원 이메일
        JTextField addressField = new JTextField(25); // 회원 주소

        // 초기 데이터 로드
        try {
            StudentInfo info = service.loadStudentInfo(memberId); // DB에서 회원 정보 가져오기
            if (info != null) {
                idField.setText(info.memberId);
                nameField.setText(info.name);
                phoneField.setText(info.phone);
                addressField.setText(info.address);
                if(info.email != null) emailField.setText(info.email);
            }
            String pw = service.getPassword(memberId); // DB에서 현재 회원의 비밀번호 가져오기
            if (pw != null) currentPwField.setText(pw);
        } catch (SQLException ex) {
        	// DB 쿼리 수행 중 에러 발생 시
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터 로드 오류: " + ex.getMessage());
        }

        // 라벨과 필드 배치
        g.gridx = 0; g.gridy = 0; add(new JLabel("아이디"), g);
        g.gridx = 1; g.gridy = 0; add(idField, g);

        g.gridx = 0; g.gridy = 1; add(new JLabel("현재 비밀번호"), g);
        JPanel pwP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // 비밀번호 패널
        pwP.add(currentPwField);
        JButton changePwBtn = new JButton("비밀번호 변경");
        changePwBtn.addActionListener(e -> { // 비밀번호 변경 버튼 클릭 시 이벤트 처리
            PasswordChangeDialog pd = new PasswordChangeDialog(owner, service, memberId);
            pd.setVisible(true);
        });
        pwP.add(changePwBtn);
        g.gridx = 1; g.gridy = 1; add(pwP, g);

        // 나머지 필드 배치
        g.gridx = 0; g.gridy = 2; add(new JLabel("이름"), g);
        g.gridx = 1; g.gridy = 2; add(nameField, g);

        g.gridx = 0; g.gridy = 3; add(new JLabel("연락처"), g);
        g.gridx = 1; g.gridy = 3; add(phoneField, g);
        
        g.gridx = 0; g.gridy = 4; add(new JLabel("이메일"), g);
        g.gridx = 1; g.gridy = 4; add(emailField, g);

        g.gridx = 0; g.gridy = 5; add(new JLabel("주소"), g);
        g.gridx = 1; g.gridy = 5; add(addressField, g);

        // 버튼 패널(저장&취소)
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("취소");
        JButton save = new JButton("저장");
        btnP.add(cancel);
        btnP.add(save);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; add(btnP, g);

        cancel.addActionListener(e -> dispose()); // 취소 클릭 시 다이얼로그 종료

        save.addActionListener(e -> { // 저장 버튼 클릭 시 이벤트 처리
            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newAddr = addressField.getText().trim();
            
            // 입력 검증
            if (newName.isEmpty() || newPhone.isEmpty() || newAddr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이름, 연락처, 주소는 필수 입력입니다.", "입력 필요", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 전화번호 형식 검증
            if (!Utils.isValidPhone(newPhone)) {
                JOptionPane.showMessageDialog(this, "유효한 전화번호를 입력하세요.", "전화번호 형식 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 이메일 형식 검증
            if (!Utils.isValidEmail(newEmail)) {
                JOptionPane.showMessageDialog(this, "유효한 이메일 형식을 입력하세요.", "이메일 형식 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 이메일 도메인 허용 체크
            if (!Utils.isAllowedEmailDomain(newEmail)) {
                JOptionPane.showMessageDialog(this, "사용 가능한 이메일 도메인은 naver.com, google.com, daum.net 입니다.", "이메일 도메인 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 최종 확인 다이얼로그
            int confirm = JOptionPane.showConfirmDialog(this, "정말 수정하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // DB 업데이트 수행
            try {
                boolean ok = service.updateMemberInfo(memberId, newName, newPhone, newAddr, newEmail);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "정보가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
                    saved = true; // 저장 완료
                    dispose(); // 해당 프레임 종료(나머지 프레임 살아있음)
                } else {
                	// 수정 실패 시
                    JOptionPane.showMessageDialog(this, "수정 실패", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
            	// DB 쿼리 수행 중 에러 발생 시
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack(); // 크기 자동 조절
        setMinimumSize(new Dimension(400, 350)); // 최소 크기 지정
        setLocationRelativeTo(owner); // 부모 프레임(학생 페이지)에 중앙에 위치
    }

    // 저장 여부 반환
    public boolean isSaved() {
        return saved;
    }
}
