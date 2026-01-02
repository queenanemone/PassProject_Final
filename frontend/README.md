# Trip-Board Frontend (Vue.js)

Vue.js 기반 프론트엔드 프로젝트

## 기술 스택

- Vue 3 (Composition API)
- Vue Router 4
- Pinia (상태 관리)
- Vite (빌드 도구)
- Tailwind CSS
- Axios (HTTP 클라이언트)

## 설치 및 실행

### 1. 의존성 설치

```bash
cd frontend
npm install
```

### 2. 환경 변수 설정

`.env` 파일을 생성하고 필요한 변수를 설정하세요:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=your-google-client-id-here
```

### 3. 개발 서버 실행

```bash
npm run dev
```

개발 서버는 `http://localhost:3000`에서 실행되며, API 요청은 `http://localhost:8080`으로 프록시됩니다.

### 4. 프로덕션 빌드

```bash
npm run build
```

빌드 결과물은 `../src/main/resources/static` 디렉토리에 생성됩니다.

## 프로젝트 구조

```
src/
├── components/      # 재사용 가능한 컴포넌트
│   └── Header.vue
├── views/           # 페이지 컴포넌트
│   ├── HomeView.vue
│   ├── LoginView.vue
│   ├── DashboardView.vue
│   ├── BoardView.vue
│   ├── PostDetailView.vue
│   ├── MyRecordsView.vue
│   ├── RecordView.vue
│   ├── RecordDetailView.vue
│   ├── MyPageView.vue
│   ├── NewPlanView.vue
│   └── SharePlanView.vue
├── router/          # Vue Router 설정
│   └── index.js
├── stores/          # Pinia 상태 관리
│   └── auth.js
├── services/        # API 서비스 레이어
│   └── api/
│       ├── index.js
│       ├── auth.js
│       ├── board.js
│       ├── plan.js
│       ├── record.js
│       └── user.js
├── App.vue          # 루트 컴포넌트
└── main.js          # 진입점
```

## 주요 기능

- ✅ 라우팅 (Vue Router)
- ✅ 상태 관리 (Pinia)
- ✅ API 서비스 레이어
- ✅ 인증 상태 관리
- ✅ 모든 페이지 컴포넌트 변환 완료

## Spring Boot와 통합

빌드된 파일은 `src/main/resources/static`에 배치되므로, Spring Boot 서버를 실행하면 Vue 앱이 자동으로 서빙됩니다.

## 개발 팁

- 개발 중에는 `npm run dev`로 별도 서버를 실행하고, API는 Spring Boot 서버로 프록시됩니다.
- 프로덕션 배포 시에는 `npm run build`로 빌드한 후 Spring Boot 서버를 실행하면 됩니다.
