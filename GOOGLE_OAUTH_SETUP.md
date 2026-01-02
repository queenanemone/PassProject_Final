# Google OAuth 설정 가이드

## 오류: redirect_uri_mismatch 해결 방법

### 1. Google Cloud Console 접속
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 선택

### 2. OAuth 2.0 클라이언트 ID 설정 확인
1. **API 및 서비스** > **사용자 인증 정보** 이동
2. OAuth 2.0 클라이언트 ID 클릭 (또는 새로 생성)

### 3. 승인된 리디렉션 URI 추가

다음 URI들을 **승인된 리디렉션 URI**에 추가하세요:

#### 개발 환경 (Vite 개발 서버)
```
http://localhost:3000
http://localhost:3000/
http://localhost:3000/login
```

#### 프로덕션 환경 (Spring Boot)
```
http://localhost:8080
http://localhost:8080/
http://localhost:8080/login
```

#### 배포 환경 (실제 도메인 사용 시)
```
https://yourdomain.com
https://yourdomain.com/
https://yourdomain.com/login
```

### 4. 승인된 JavaScript 원본 추가

다음 원본들을 **승인된 JavaScript 원본**에 추가하세요:

#### 개발 환경
```
http://localhost:3000
```

#### 프로덕션 환경
```
http://localhost:8080
```

#### 배포 환경
```
https://yourdomain.com
```

### 5. OAuth 동의 화면 확인
1. **OAuth 동의 화면** 메뉴로 이동
2. 사용자 유형: **외부** 선택
3. 앱 정보 입력
4. 범위: `email`, `profile` 추가
5. 테스트 사용자 추가 (개발 중인 경우)

### 6. 현재 설정 확인

현재 `application.yml`에 설정된 Client ID:
```yaml
google:
  oauth:
    client-id: ${GOOGLE_CLIENT_ID:602519230142-mmdfce8jv1gc5g4j7pfdaqssp30neffq.apps.googleusercontent.com}
```

### 7. 변경 사항 저장 후 확인
- 변경 사항 저장 후 **최대 5분** 정도 기다려야 반영될 수 있습니다
- 브라우저 캐시를 지우고 다시 시도하세요

### 8. 문제 해결 체크리스트
- [ ] Google Cloud Console에서 Client ID 확인
- [ ] 승인된 리디렉션 URI에 모든 환경 추가
- [ ] 승인된 JavaScript 원본에 모든 환경 추가
- [ ] OAuth 동의 화면에서 범위 설정 확인
- [ ] 변경 사항 저장 후 대기 (최대 5분)
- [ ] 브라우저 캐시 삭제 후 재시도

### 참고
- OAuth 2.0 토큰 방식은 팝업 방식이므로 리디렉션 URI가 필요하지 않을 수도 있지만, Google Cloud Console 설정에서는 여전히 필요할 수 있습니다
- 개발 중에는 테스트 사용자를 추가해야 로그인할 수 있습니다


