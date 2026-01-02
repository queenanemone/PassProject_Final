# 한국관광공사 API 키 등록 방법

## 🔑 API 키 등록 위치

한국관광공사 API 키는 **두 가지 방법**으로 등록할 수 있습니다.

---

## 방법 1: application.yml 파일에 직접 입력 (추천 - 간단)

### 1. 파일 열기
`src/main/resources/application.yml` 파일을 엽니다.

### 2. API 키 입력
33번째 줄을 수정합니다:

```yaml
# 한국관광공사 API 설정
tour:
  api:
    base-url: http://apis.data.go.kr/B551011/KorService1
    photo-base-url: http://apis.data.go.kr/B551011/PhotoGalleryService1
    service-key: 여기에_실제_API_키_입력  # 이 부분을 수정
    cache-duration: 604800
```

**예시:**
```yaml
service-key: abc123def456ghi789jkl012mno345pqr678stu901vwx234yz
```

### 3. 저장 후 재시작
- 파일 저장 후 애플리케이션을 재시작하면 적용됩니다.

---

## 방법 2: 환경 변수로 설정 (보안에 유리)

### Windows에서 설정:

#### PowerShell 사용:
```powershell
$env:TOUR_API_SERVICE_KEY="여기에_실제_API_키_입력"
```

#### CMD 사용:
```cmd
set TOUR_API_SERVICE_KEY=여기에_실제_API_키_입력
```

#### VS Code 터미널에서:
```bash
export TOUR_API_SERVICE_KEY="여기에_실제_API_키_입력"
```

### 영구적으로 설정하려면:

1. **시스템 환경 변수 설정:**
   - Windows: 제어판 → 시스템 → 고급 시스템 설정 → 환경 변수
   - 변수 이름: `TOUR_API_SERVICE_KEY`
   - 변수 값: 실제 API 키

2. **또는 .env 파일 사용 (추천):**
   - 프로젝트 루트에 `.env` 파일 생성
   - 내용: `TOUR_API_SERVICE_KEY=여기에_실제_API_키_입력`
   - VS Code의 Java Extension이 자동으로 읽습니다

---

## 📝 API 키 발급 방법

1. **공공데이터포털 접속**
   - https://www.data.go.kr/

2. **한국관광공사 API 검색**
   - 검색: "한국관광공사_국문 관광정보 서비스"

3. **활용신청**
   - 원하는 API 선택
   - 활용신청 클릭
   - 승인 대기 (보통 즉시 또는 몇 시간 내)

4. **API 키 확인**
   - 마이페이지 → 개발계정 → 인증키 확인

---

## ⚠️ 주의사항

1. **보안:**
   - API 키는 민감한 정보이므로 Git에 커밋하지 마세요
   - `.gitignore`에 `.env` 파일이 포함되어 있는지 확인

2. **현재 설정:**
   - `application.yml`에서 `${TOUR_API_SERVICE_KEY:your_api_key_here}` 형식은:
     - 먼저 환경 변수 `TOUR_API_SERVICE_KEY`를 찾고
     - 없으면 기본값 `your_api_key_here`를 사용합니다

3. **테스트:**
   - API 키를 등록한 후 애플리케이션을 재시작하세요
   - 한국관광공사 API 호출이 정상 작동하는지 확인하세요

---

## 🔍 API 키 확인 방법

등록한 API 키가 제대로 읽혔는지 확인하려면:

1. 애플리케이션 실행 후 로그 확인
2. `KoreanTourApiService`에서 `serviceKey` 값이 올바른지 확인
3. API 호출 시 401 오류가 나오지 않는지 확인

---

## 💡 추천 방법

**개발 환경:** `application.yml`에 직접 입력 (간단)
**운영 환경:** 환경 변수 사용 (보안)

