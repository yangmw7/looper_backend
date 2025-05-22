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

---

### 2025.05.12  
~~✅ **세션 기반 로그인 기능 구현 (HttpSession 방식)**~~  
~~→ `POST /api/login` 엔드포인트 구현~~  
~~→ 로그인 성공 시 `HttpSession`에 사용자 정보(Member) 저장~~  
~~→ 이후 요청에서 `HttpServletRequest.getSession()`을 통해 사용자 인증~~  
~~→ 게시글 작성 시 세션의 사용자 닉네임을 `writer`로 자동 설정~~  
~~→ 로그인 실패 시 401 상태 코드 반환~~  
~~→ `MemberService` 내에서 인증 로직 분리 처리~~  
~~→ `Postman`로 로그인 흐름 정상 작동 확인~~

**이 구현은 추후 React 프론트와의 통신을 위해 JWT 방식으로 리팩토링 예정이며,  
해당 브랜치(auth/session-login)는 세션 기반 인증 흐름 보관용으로 유지됩니다.**  

---

### 2025.05.14  
✅ **JWT 기반 로그인 및 게시판 인증 처리 구현**  
→ `POST /api/login` 시 JWT 토큰 발급 (`JwtUtil` 사용)  
→ 토큰을 `Authorization: Bearer <토큰>` 헤더로 전달하여 인증  
→ `JwtUtil.extractUsername()`을 통해 사용자 식별  
→ 게시글 작성 시 사용자 닉네임 자동 삽입  
→ 게시글 수정 시 작성자와 토큰 주인 일치 여부 확인  
→ `PostController`, `PostService` 기반 게시판 CRUD 완성  
→ `Postman`로 전체 흐름 테스트 완료


---

### 2025.05.19
✅ **MySQL 연동 및 데이터 저장 테스트 완료**  
→ H2 대신 `MySQL 8.0` 로컬 서버 연동 (`looper_db` 사용)  
→ `application.properties`에 `MySQL` 접속 정보 설정  
→ `member`, `post` 테이블 자동 생성 (`JPA Entity` 기반)  
→ `Postman`으로 회원가입/로그인/게시글 작성 요청 시 데이터 정상 저장 확인  
→ `MySQL Workbench`를 통해 DB 직접 조회 및 구조 확인 완료  
→ 전체 백엔드 흐름이 `MySQL` 기준으로 완전히 전환됨  


---

**2025.05.22**  
✅ 댓글 작성 기능 구현 (닉네임 저장 포함)  
→ 게시글 상세보기에서 댓글 작성 `API` 구현 완료  
→ 댓글 작성 시, `JWT` 인증을 통해 로그인 사용자 정보 추출  
→ `Comment` 엔티티에 `Member`와 `Post` 연관 매핑   
→ 사용자 닉네임(`nickname`)을 별도 필드로 DB에 저장  
→ `Postman`으로 댓글 요청 테스트 시, DB에 정상 저장 확인  
→ 댓글 데이터는 `MySQL`의 `comment` 테이블에 반영됨  
→ 외래키 제약 조건 고려해 `member`, `post`와 연결 완료  


---


**🛠 예정 작업**  
⏸ 캐릭터 정보 API 연동 (게임 시스템과 연동 후 진행 예정)  
🧩 프론트엔드 구현 (`React`)  
 → 로그인 / 회원가입 UI 페이지 구성  
 → 게시글 목록 및 상세 조회 페이지 구현  
 → 게시글 작성 / 수정 / 삭제 기능 연동  
 → 댓글 목록 / 작성 UI 추가 및 연동  
 → JWT 토큰 저장 및 인증 상태 관리 (`localStorage` or `Redux`)  
 → 로그인 여부에 따른 UI 분기 처리 (작성/수정/삭제 권한 표시)  
 → 추후 게임 캐릭터 정보 조회 및 전략 공유 기능 UI 계획  
 → 디자인 및 사용자 경험(UI/UX) 개선  


---
