# looper_backend
게임 사용자 관리 및 캐릭터 데이터를 포함한 백엔드 서버입니다.

## 🛠 Tech Stack
- Backend: Spring Boot, Spring Security, JPA/Hibernate
- Frontend: React (챗봇 위젯, 커뮤니티 UI)
- Database: MariaDB
- Others: MCP Server (MariaDB), JWT

## ✨ Features
- 회원가입 / 로그인 (세션 기반 → JWT 리팩토링 완료)
- 아이디 / 비밀번호 찾기 & 재설정
- 게시글 CRUD + 댓글 CRUD (JWT 인증 적용)
- 다중 이미지 업로드, 페이지네이션
- 관리자(Admin) 페이지 (회원 관리, 게시글·댓글 삭제)
- React 기반 챗봇 위젯 (MCP 서버 연동 중, 자연어 DB 질의 응답)

## 🚀 Planned
- Unity 게임 데이터 ↔ 웹 연동 (캐릭터 정보, 전략 공유)
- MCP 서버 고도화 & 챗봇 응답 개선
- UI/UX 다크 테마 적용 (던전 느낌 스크롤 이벤트)  