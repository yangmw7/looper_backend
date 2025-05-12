# looper_backend  
게임 사용자 관리 및 캐릭터 데이터를 포함한 전반적인 백엔드 기능을 처리하는 서버입니다.

# 🎓 Graduation Project - Game Backend API

## 📅 작업 히스토리

---

### 2025.05.04

✅ **회원가입 API 구현**  
→ `id`, `password`, `email` 파라미터 수신  
→ 회원가입 성공 시 `"success"` 메시지 반환  
→ 비밀번호 암호화 적용 (`BCryptPasswordEncoder` 사용)  
→ `H2` DB 연동 및 저장 확인  
→ `Talend API Tester`로 기능 테스트 완료  

---

### 2025.05.05

✅ **로그인 API 구현**  
→ `id`, `password` 입력 후 로그인  
→ 로그인 성공 시 `"login success"` 메시지 반환  
→ `Talend API Tester`로 기능 테스트 완료  

---

### 2025.05.06

✅ **Mustache 기반 회원가입/로그인/로그아웃 UI 구현**  
→ `join.mustache`, `login.mustache`, `home.mustache`  
→ ViewController를 통한 화면 연동 처리  
→ 로그인 시 `HttpSession` 사용하여 로그인 상태 유지  
→ Spring Security 설정 수정 (`.logout().disable()` 적용)  
→ 브라우저 테스트 완료 (회원가입 → 로그인 → 로그아웃)  
→ `H2 Console`을 통한 DB 저장 확인  

---

### 2025.05.07

✅ **아이디 찾기 기능 구현 (Mustache 기반)**  
→ `find-id.mustache`, `find-id-result.mustache`  
→ 이메일 입력 → DB 조회 → 아이디 반환  
→ ViewController를 통한 결과 페이지 분리 처리  

✅ **비밀번호 찾기 및 재설정 기능 구현 (Mustache 기반)**  
→ `find-password.mustache`, `reset-password.mustache`, `reset-success.mustache`  
→ 아이디 + 이메일 유효성 검사 → 세션 저장  
→ 새 비밀번호 입력 및 일치 여부 확인 → DB 비밀번호 갱신  

✅ **Header/Footer Mustache 분리 및 레이아웃 통일**  
→ `layout/header.mustache`, `layout/footer.mustache`  
→ 모든 화면에 공통 적용하여 UI 일관성 확보  

✅ **페이지별 UI 및 CSS 개선**  
→ 로그인, 회원가입, 아이디/비밀번호 찾기 페이지 디자인 개선  
→ 각 컴포넌트별 스타일 통일  

---

### 2025.05.11  
✅ **아이디 찾기 기능 구현 (REST API)**  
→ `POST /api/find-id` 엔드포인트 구현  
→ 이메일 입력 시 DB 조회 후 username 반환  
→ 실패 시 오류 메시지 반환  
→ `FindIdRequest DTO` 생성, `MemberService` 분리 적용  
→ `Talend API Tester`로 테스트 완료  

✅ **비밀번호 재설정 기능 구현 (REST API)**  
→ 2단계 방식 구현  
  1) `POST /api/reset-password/request`: 아이디+이메일 유효성 검사  
  2) `POST /api/reset-password`: 새 비밀번호 입력 및 확인 → 암호화 후 DB 저장  
→ `ResetPasswordRequest`, `ResetPasswordChangeRequest DTO` 생성  
→ `newPassword`와 `confirmPassword` 일치 검사 포함  
→ `MemberService`에서 비즈니스 로직 처리  
→ `Talend API Tester`로 전체 흐름 검증 완료  


### 2025.05.12
✅** 세션 기반 로그인 기능 구현 (HttpSession 방식)**  
→ `POST /api/login` 엔드포인트 구현  
→ 로그인 성공 시 `HttpSession`에 사용자 정보(Member) 저장  
→ 이후 요청에서 `HttpServletRequest.getSession()`을 통해 사용자 인증  
→ 게시글 작성 시 세션의 사용자 닉네임을 `writer`로 자동 설정  
→ 로그인 실패 시 401 상태 코드 반환  
→ `MemberService` 내에서 인증 로직 분리 처리  
→ `Postman`로 로그인 흐름 정상 작동 확인  

**이 구현은 추후 React 프론트와의 통신을 위해 JWT 방식으로 리팩토링 예정이며,
해당 브랜치(auth/session-login)는 세션 기반 인증 흐름 보관용으로 유지됩니다.**

## 🛠 예정 작업

- 🛡 **로그인 시 존재하지 않는 계정 예외 처리 및 메시지 출력**
- 🛡 **회원가입 시 아이디 중복 확인 및 중복 방지 로직 추가**
- 🔜 **게시판 기능 구현** (글 작성, 목록, 상세 보기, 수정, 삭제)
- 🔜 **게시글 작성 시 로그인 세션 정보 연동**
- ⏸ **캐릭터 정보 API 연동** (게임 시스템과 연동 후 진행 예정)

---
