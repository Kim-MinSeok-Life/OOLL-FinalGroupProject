package OOLL_FinalTeamProject; // 패키지 선언

// ===== import 구간 =====
import javax.swing.*; // 스윙 컴포넌트
import java.awt.*; // 레이아웃
import java.util.LinkedHashMap; // 순서 유지 Map
import java.util.Map; // Map 인터페이스

// ===== 강좌 개설/수정 공통 수퍼 클래스 =====
public abstract class LectureFormBase extends JDialog { // LectureFormBase, JDialog 상속

    // ===== 공통 UI 컴포넌트 필드 =====
    protected JComboBox<String> cbSubject; // 과목명 콤보박스
    protected JComboBox<String> cbStartPeriod; // 시작 교시 콤보박스
    protected JComboBox<String> cbEndPeriod; // 종료 교시 콤보박스
    protected JCheckBox[] dayCheckBoxes; // 요일 체크박스 배열 (월~일)
    protected JComboBox<String> cbRoom; // 강의실 콤보박스

    // 교시 번호 → "2(10:00~10:50)" 형식 문자열 매핑
    protected Map<Integer, String> periodToTimeMap; // 교시-시간 매핑 Map

    // ===== 생성자 =====
    public LectureFormBase(Frame owner, String title) { // 생성자
        super(owner, title, true); // 부모, 타이틀, 모달 여부 전달
        setSize(450, 300); // 기본 강좌 개설/수정 창 크기 (적당히 설정)
        setLocationRelativeTo(owner); // 부모 기준 중앙 배치

        initPeriodTimeMap(); // 교시-시간 매핑 초기화
        initCommonUI(); // 공통 UI 구성
    }

    // ===== 교시-시간 매핑 초기화 =====
    private void initPeriodTimeMap() { // 교시-시간 맵 초기화 메서드
        periodToTimeMap = new LinkedHashMap<>(); // 순서 유지되는 LinkedHashMap 생성
        periodToTimeMap.put(1, "1(09:00~09:50)"); // 1교시 시간
        periodToTimeMap.put(2, "2(10:00~10:50)"); // 2교시 시간
        periodToTimeMap.put(3, "3(11:00~11:50)"); // 3교시 시간
        periodToTimeMap.put(4, "4(13:00~13:50)"); // 4교시 시간
        periodToTimeMap.put(5, "5(14:00~14:50)"); // 5교시 시간
        periodToTimeMap.put(6, "6(15:00~15:50)"); // 6교시 시간
        periodToTimeMap.put(7, "7(16:00~16:50)"); // 7교시 시간
    }

    // ===== 공통 UI 구성 (과목명/교시/요일/강의실) =====
    private void initCommonUI() { // 공통 UI 구성 메서드
        Container c = getContentPane(); // 컨텐트팬 가져오기
        c.setLayout(new BorderLayout()); // BorderLayout 설정

        JPanel formPanel = new JPanel(new GridBagLayout()); // GridBagLayout으로 폼 패널 생성
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 여백 설정

        GridBagConstraints gbc = new GridBagConstraints(); // GridBagConstraints 생성
        gbc.insets = new Insets(5, 5, 5, 5); // 컴포넌트 간 간격
        gbc.fill = GridBagConstraints.HORIZONTAL; // 가로 방향으로 늘어남

        int row = 0; // 현재 행 번호

        // ---- 과목명 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("과목명"), gbc); // 과목명 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        cbSubject = new JComboBox<>( // 과목명 콤보박스 생성
                new String[]{"파이썬", "자바", "C 언어", "알고리즘"} // 예시 과목 목록
        );
        formPanel.add(cbSubject, gbc); // 폼 패널에 콤보박스 추가

        row++; // 다음 행으로 이동

        // ---- 시작 교시 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("시작 교시"), gbc); // 시작 교시 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        cbStartPeriod = new JComboBox<>(); // 시작 교시 콤보박스 생성
        for (Integer key : periodToTimeMap.keySet()) { // 맵의 키(교시) 순회
            cbStartPeriod.addItem(periodToTimeMap.get(key)); // "2(10:00~10:50)" 같은 문자열 추가
        }
        formPanel.add(cbStartPeriod, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 종료 교시 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("종료 교시"), gbc); // 종료 교시 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        cbEndPeriod = new JComboBox<>(); // 종료 교시 콤보박스 생성
        for (Integer key : periodToTimeMap.keySet()) { // 교시 순회
            cbEndPeriod.addItem(periodToTimeMap.get(key)); // 동일한 형식으로 추가
        }
        formPanel.add(cbEndPeriod, gbc); // 폼 패널에 추가

        row++; // 다음 행

        // ---- 요일 체크박스 (월~일 7개) ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("요일"), gbc); // 요일 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 요일 체크박스용 패널
        String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"}; // 요일 이름 배열
        dayCheckBoxes = new JCheckBox[dayNames.length]; // 체크박스 배열 생성
        for (int i = 0; i < dayNames.length; i++) { // 요일 개수만큼 반복
            dayCheckBoxes[i] = new JCheckBox(dayNames[i]); // 각 요일에 해당하는 체크박스 생성
            dayPanel.add(dayCheckBoxes[i]); // 패널에 체크박스 추가
        }
        formPanel.add(dayPanel, gbc); // 폼 패널에 요일 패널 추가

        row++; // 다음 행

        // ---- 강의실 ----
        gbc.gridx = 0; // 첫 번째 열
        gbc.gridy = row; // 현재 행
        formPanel.add(new JLabel("강의실"), gbc); // 강의실 라벨 추가

        gbc.gridx = 1; // 두 번째 열
        cbRoom = new JComboBox<>( // 강의실 콤보박스 생성
                new String[]{"201호", "202호", "203호", "301호"} // 예시 강의실 목록
        );
        formPanel.add(cbRoom, gbc); // 폼 패널에 강의실 콤보박스 추가

        c.add(formPanel, BorderLayout.CENTER); // 다이얼로그 중앙에 폼 패널 부착

        // 하단 버튼(저장/닫기)은 서브 클래스에서 따로 구성
    }

    // ===== 시작/종료 교시 검증 + 자동 조정 =====
    protected void adjustPeriodIfInvalid() { // 시작교시 > 종료교시일 때 처리 메서드
        int start = getSelectedStartPeriod(); // 선택된 시작 교시 숫자
        int end = getSelectedEndPeriod(); // 선택된 종료 교시 숫자

        if (start > end) { // 시작이 종료보다 큰 경우
            JOptionPane.showMessageDialog( // 알림 다이얼로그 표시
                    this, // 부모 컴포넌트
                    "시작 교시가 종료 교시보다 늦을 수 없습니다.\n종료 교시를 시작 교시와 같게 변경합니다.", // 메시지
                    "교시 확인", // 타이틀
                    JOptionPane.INFORMATION_MESSAGE // 정보 아이콘
            );
            setSelectedEndPeriod(start); // 종료 교시를 시작 교시와 동일하게 변경
        }
    }

    // ===== 시작 교시 숫자 반환 (예: "2(10:00~10:50)" → 2) =====
    protected int getSelectedStartPeriod() { // 시작 교시 숫자 반환 메서드
        String selected = (String) cbStartPeriod.getSelectedItem(); // 선택된 문자열
        return parsePeriodNumber(selected); // 숫자 부분만 파싱해서 반환
    }

    // ===== 종료 교시 숫자 반환 =====
    protected int getSelectedEndPeriod() { // 종료 교시 숫자 반환 메서드
        String selected = (String) cbEndPeriod.getSelectedItem(); // 선택된 문자열
        return parsePeriodNumber(selected); // 숫자만 파싱해서 반환
    }

    // ===== 시작 교시 콤보박스를 특정 교시로 설정 =====
    protected void setSelectedStartPeriod(int period) { // 시작 교시 설정 메서드
        for (Map.Entry<Integer, String> entry : periodToTimeMap.entrySet()) { // 맵 엔트리 순회
            if (entry.getKey() == period) { // 키(교시번호)가 일치하면
                cbStartPeriod.setSelectedItem(entry.getValue()); // 해당 문자열 선택
                break; // 루프 종료
            }
        }
    }

    // ===== 종료 교시 콤보박스를 특정 교시로 설정 =====
    protected void setSelectedEndPeriod(int period) { // 종료 교시 설정 메서드
        for (Map.Entry<Integer, String> entry : periodToTimeMap.entrySet()) { // 맵 엔트리 순회
            if (entry.getKey() == period) { // 키가 일치하면
                cbEndPeriod.setSelectedItem(entry.getValue()); // 해당 문자열 선택
                break; // 루프 종료
            }
        }
    }

    // ===== "2(10:00~10:50)" 에서 2만 파싱 =====
    private int parsePeriodNumber(String text) { // 문자열에서 교시 번호만 추출
        if (text == null || text.isEmpty()) { // null 또는 빈 문자열이면
            return 1; // 기본값 1교시 반환
        }
        int idx = text.indexOf('('); // '(' 문자 인덱스 찾기
        if (idx > 0) { // 앞에 숫자가 있는 경우
            String numStr = text.substring(0, idx); // 앞부분 잘라냄
            try { // 예외 처리
                return Integer.parseInt(numStr); // 정수로 변환 후 반환
            } catch (NumberFormatException e) { // 숫자 파싱 실패 시
                return 1; // 기본값 1 반환
            }
        }
        return 1; // 괜찮은 포맷이 아니면 1 반환
    }

    // ===== 선택된 요일 문자열 만들기 (예: "월/수/금") =====
    protected String buildSelectedDaysString() { // 선택된 요일 문자열 생성 메서드
        StringBuilder sb = new StringBuilder(); // 문자열을 누적할 StringBuilder
        for (JCheckBox cb : dayCheckBoxes) { // 요일 체크박스 순회
            if (cb.isSelected()) { // 체크된 요일이면
                if (sb.length() > 0) { // 이미 문자열이 있으면
                    sb.append("/"); // 구분자 '/' 추가
                }
                sb.append(cb.getText()); // 체크박스 텍스트(요일) 추가
            }
        }
        return sb.toString(); // 완성된 문자열 반환
    }

    // ===== "월/수/금" 문자열로 체크박스 상태 적용 =====
    protected void applyDaysFromString(String daysString) { // 요일 문자열로 체크박스 세팅
        if (daysString == null || daysString.isEmpty()) { // null/빈 문자열이면
            for (JCheckBox cb : dayCheckBoxes) { // 모든 체크박스 순회
                cb.setSelected(false); // 전부 해제
            }
            return; // 메서드 종료
        }

        String[] parts = daysString.split("/"); // '/' 기준으로 분리
        for (JCheckBox cb : dayCheckBoxes) { // 모든 체크박스 순회
            cb.setSelected(false); // 일단 해제
            for (String p : parts) { // 문자열 배열 순회
                if (cb.getText().equals(p.trim())) { // 텍스트가 일치하면
                    cb.setSelected(true); // 체크
                    break; // 안쪽 루프 탈출
                }
            }
        }
    }

    // ===== 과목명 콤보박스 선택값 설정 =====
    protected void setSubject(String subject) { // 과목명 설정 메서드
        cbSubject.setSelectedItem(subject); // 해당 과목명 선택
    }

    // ===== 강의실 콤보박스 선택값 설정 =====
    protected void setRoom(String room) { // 강의실 설정 메서드
        cbRoom.setSelectedItem(room); // 해당 강의실 선택
    }

    // ===== 저장 버튼에서 호출할 추상 메서드 =====
    protected abstract void onSave(); // 서브 클래스에서 구현해야 하는 추상 메서드
}
