# Trip-Board

> **Note**: This project was developed with the assistance of **Google Gemini**.

AI 기반 여행 계획 플랫폼 - Spring Boot & Vue 3 기반 웹 애플리케이션

## 프로젝트 구조

이 프로젝트는 **Backend (Spring Boot)**와 **Frontend (Vue 3)**가 통합된 구조로 구성되어 있습니다.

```
passprojectfinal/
├── frontend/                # Vue 3 Frontend Application
│   ├── src/
│   │   ├── components/      # Vue 컴포넌트
│   │   ├── views/           # 페이지 뷰
│   │   ├── stores/          # Pinia 상태 관리
│   │   ├── router/          # 라우팅 설정
│   │   └── assets/          # 정적 자원 (이미지, CSS 등)
│   ├── public/
│   ├── index.html
│   ├── package.json         # Frontend 의존성 관리
│   ├── vite.config.js       # Vite 설정
│   └── tailwind.config.js   # Tailwind CSS 설정
│
├── src/main/                # Spring Boot Backend Application
│   ├── java/com/tripboard/
│   │   ├── TripBoardApplication.java  # 메인 애플리케이션
│   │   ├── config/          # 설정 (Security, WebMvc, WebClient 등)
│   │   ├── controller/      # REST API 컨트롤러
│   │   ├── service/         # 비즈니스 로직 (AI, 여행계획 등)
│   │   ├── mapper/          # MyBatis 매퍼 인터페이스
│   │   ├── entity/          # 도메인 엔티티
│   │   ├── dto/             # 데이터 전송 객체
│   │   ├── exception/       # 예외 처리
│   │   └── util/            # 유틸리티
│   └── resources/
│       ├── application.yml  # 애플리케이션 설정
│       ├── mapper/          # MyBatis XML SQL 매퍼
│       └── db/              # DB 스키마 및 데이터
│
└── pom.xml                  # Backend 의존성 관리 (Maven)
```

## 기술 스택

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security 6, JWT (JSON Web Token)
- **Database**: MySQL 8.0
- **ORM**: MyBatis 3.0.3
- **Network**: Spring WebFlux (WebClient) - 외부 API 통신용
- **Build Tool**: Maven

### Frontend
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite 5
- **Styling**: Tailwind CSS 3
- **State Management**: Pinia
- **Routing**: Vue Router 4
- **HTTP Client**: Axios

### External APIs
- **OpenAI API**: AI 기반 여행 추천 기능에 사용
- **Korea Tourism Organization API (TourAPI)**: 국내 여행지 정보 조회에 사용

## 주요 기능

1. **사용자 인증**: JWT 기반 로그인/회원가입, 보안 설정 적용
2. **AI 여행 계획**: 사용자의 입력을 바탕으로 OpenAI를 활용한 여행 코스 추천
3. **여행지 검색**: 공공데이터포털 관광정보 API를 활용한 여행지 검색
4. **커뮤니티(게시판)**: 여행 계획 공유 및 소통 기능
5. **여행 기록**: 다녀온 여행에 대한 기록 및 사진 업로드 (Base64 처리)
6. **PDF 내보내기**: 작성된 여행 계획을 PDF로 저장

## 실행 방법

### 1. 데이터베이스 설정
MySQL 데이터베이스를 생성하고 `src/main/resources/application.yml` 파일에서 접속 정보를 수정합니다.
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/trip_board?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
```

### 2. API 키 설정 (환경 변수 또는 설정 파일)
OpenAI 및 공공데이터포털 API 키가 필요합니다. `application.yml` 내 관련 설정을 확인하세요.

### 3. Backend 실행
프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다.
```bash
mvn clean install
mvn spring-boot:run
```

### 4. Frontend 실행
`frontend` 디렉토리로 이동하여 의존성을 설치하고 개발 서버를 실행합니다.
```bash
cd frontend
npm install
npm run dev
```

브라우저에서 `http://localhost:5173` (Vite 기본 포트)으로 접속하여 확인합니다.

## API 엔드포인트 예시

- **Auth**: `/api/auth/login`, `/api/auth/register`
- **Plans**: `/api/plans` (GET, POST, PUT, DELETE)
- **Board**: `/api/board/posts`
- **Records**: `/api/records`
- **OpenAI**: `/api/openai/recommend`

---
Copyright © 2024 Trip-Board Project. All Rights Reserved.
