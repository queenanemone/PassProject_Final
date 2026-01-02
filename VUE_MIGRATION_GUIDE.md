# Vue.js 마이그레이션 가이드

## 📋 개요

프론트엔드를 순수 HTML/JavaScript에서 Vue.js로 전환하는 작업을 진행 중입니다.

## 🏗️ 프로젝트 구조

```
frontend/
├── src/
│   ├── components/      # 재사용 가능한 컴포넌트
│   ├── views/           # 페이지 컴포넌트
│   ├── router/          # Vue Router 설정
│   ├── stores/          # Pinia 상태 관리
│   ├── services/        # API 서비스 레이어
│   ├── App.vue          # 루트 컴포넌트
│   └── main.js          # 진입점
├── package.json
├── vite.config.js       # Vite 빌드 설정
└── tailwind.config.js   # Tailwind CSS 설정
```

## 📝 완료된 작업

1. ✅ Vue 프로젝트 기본 구조 생성
2. ✅ Vue Router 설정
3. ✅ Pinia 상태 관리 설정
4. ✅ API 서비스 레이어 기본 구조
5. ✅ 공통 Header 컴포넌트
6. ✅ HomeView (메인 페이지) 컴포넌트

## 🚧 진행 중인 작업

- 각 페이지별 Vue 컴포넌트 변환
- API 서비스 레이어 완성
- Spring Boot와의 빌드 통합

## 📌 다음 단계

1. 나머지 뷰 컴포넌트 생성 (Login, Dashboard, Board 등)
2. API 서비스 레이어 완성
3. 빌드 및 배포 설정

## ⚠️ 중요 사항

- 현재 기존 HTML/JS 파일들은 유지됩니다
- Vue 빌드 결과물은 `src/main/resources/static`에 배치됩니다
- 개발 서버는 별도로 실행하며, API는 Spring Boot 서버로 프록시됩니다

