# looper_backend
게임 사용자 관리 및 캐릭터 데이터를 포함한 전반적인 백엔드 기능을 처리하는 서버입니다.

# Graduation Project - Game Backend API

## 📅 작업 히스토리

### 2025.05.04
- ✅ 회원가입 API 구현 (id, password, email 받기)
- ✅ 비밀번호 암호화 적용 (BCrypt 사용)
- ✅ 회원가입 성공 시 "success" 메시지 반환
- ✅ H2 DB 연동 및 저장 확인 완료
- ✅ Talend API Tester로 테스트 완료
  

### 2025.05.05
- ✅ 로그인 API 구현 (id, password로 로그인)
- ✅ 로그인 성공 시 "login success" 메시지 반환
- ✅ Talend API Tester로 테스트 완료


### 2025.05.06
- ✅ Mustache 기반 회원가입/로그인/로그아웃 UI 화면 구현 (join.mustache, login.mustache, home.mustache)
- ✅ ViewController 추가로 화면 연동 처리
- ✅ HttpSession을 활용한 로그인 상태 유지
- ✅ Spring Security 설정 수정 (.logout().disable() 포함)
- ✅ 브라우저 테스트 완료 (회원가입 → 로그인 → 로그아웃)
- ✅ H2 Console을 통해 DB 저장 여부 직접 확인


### 2025.05.07
- ✅ Mustache 기반 아이디 찾기 기능 구현 (find-id.mustache, find-id-result.mustache)
- ✅ 이메일 입력 → DB 조회 → 아이디 반환 로직 구현
- ✅ ViewController를 통한 화면 처리 및 결과 페이지 분리
- ✅ 헤더/푸터 컴포넌트 분리 및 레이아웃 통일 (layout/header.mustache, layout/footer.mustache)
- ✅ 각 페이지별 디자인 CSS 개선 (로그인, 회원가입, 아이디 찾기 등)


### 예정 작업
- 🔜 비밀번호 찾기 및 재설정 기능 구현
- 🔜 게시판 기능 구현 (글 작성, 목록, 상세 보기, 수정, 삭제)
- 🔜 로그인 유저 세션을 활용한 게시글 작성자 연동
- ⏸ 캐릭터 정보 API 연동 (게임 시스템과 연동 후 진행 예정)



