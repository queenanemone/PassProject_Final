# Trip-Board

AI 기반 여행 계획 플랫폼 - Spring Boot 기반 웹 애플리케이션

## 프로젝트 구조

이 프로젝트는 **Spring Boot 3.2.0** 기반으로 구성되어 있습니다.

```
src/main/
├── java/com/tripboard/
│   ├── TripBoardApplication.java    # Spring Boot 메인 애플리케이션
│   ├── config/                      # 설정 클래스
│   │   ├── SecurityConfig.java      # Spring Security 설정
│   │   ├── JwtTokenProvider.java    # JWT 토큰 처리
│   │   ├── JwtAuthenticationFilter.java  # JWT 인증 필터
│   │   ├── WebClientConfig.java     # WebClient 설정
│   │   └── WebMvcConfig.java        # Web MVC 설정
│   ├── controller/                  # REST API Controller
│   │   ├── ViewController.java      # View 라우팅
│   │   ├── AuthController.java      # 인증 API
│   │   ├── TravelPlanController.java # 여행 계획 API
│   │   ├── BoardController.java     # 게시판 API
│   │   ├── TravelRecordController.java # 여행 기록 API
│   │   └── PdfExportController.java # PDF 내보내기 API
│   ├── service/                     # 비즈니스 로직
│   │   ├── AuthService.java
│   │   ├── TravelPlanService.java
│   │   ├── BoardService.java
│   │   ├── TravelRecordService.java
│   │   ├── KoreanTourApiService.java
│   │   ├── OpenAiService.java
│   │   └── PdfExportService.java
│   ├── mapper/                      # MyBatis Mapper 인터페이스
│   ├── entity/                      # 도메인 엔티티
│   ├── dto/                         # 데이터 전송 객체
│   └── util/                        # 유틸리티 클래스
└── resources/
    ├── application.yml              # Spring Boot 설정
    ├── db/schema.sql               # 데이터베이스 스키마
    ├── mapper/                     # MyBatis XML Mapper
    └── static/                     # 정적 리소스 (HTML, CSS, JS)
        ├── index.html
        ├── login.html
        ├── dashboard.html
        └── js/
```

## 기술 스택

- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security + JWT
- **Database**: MySQL 8.0
- **ORM**: MyBatis 3.0.3
- **Build Tool**: Maven
- **Java Version**: 17

## 주요 기능

1. **사용자 인증**: JWT 기반 로그인/회원가입
2. **여행 계획 생성**: AI 기반 여행 계획 추천
3. **게시판**: 여행 계획 공유 커뮤니티
4. **여행 기록**: 여행 후기 및 사진 관리
5. **외부 API 연동**: 한국관광공사 API, OpenAI API

## 실행 방법

1. MySQL 데이터베이스 생성 및 설정
2. `application.yml`에서 데이터베이스 연결 정보 설정
3. API 키 설정 (한국관광공사, OpenAI)
4. Maven 빌드 및 실행:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API 엔드포인트

- `/api/auth/login` - 로그인
- `/api/auth/register` - 회원가입
- `/api/plans` - 여행 계획 CRUD
- `/api/board/posts` - 게시판 글 조회/작성
- `/api/records` - 여행 기록 CRUD

## Spring Boot 특징

- **RESTful API**: `@RestController`를 사용한 REST API 제공
- **의존성 주입**: `@Autowired`, `@RequiredArgsConstructor` 사용
- **트랜잭션 관리**: `@Transactional` 어노테이션
- **보안**: Spring Security + JWT
- **설정 관리**: `application.yml` 기반 설정
- **정적 리소스**: `src/main/resources/static` 디렉토리
