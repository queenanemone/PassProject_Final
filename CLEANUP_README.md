# 기존 HTML/JS 파일 정리 완료

## 백업 위치
기존 HTML 및 JavaScript 파일들은 다음 위치에 백업되었습니다:
- `src/main/resources/static-backup/`

## 변경 사항
1. ✅ 기존 HTML 파일들 (`*.html`) → `static-backup/`로 이동
2. ✅ 기존 JavaScript 파일들 (`js/`) → `static-backup/js/`로 이동
3. ✅ Vite 빌드 설정 변경: `emptyOutDir: true` (빌드 시 기존 파일 자동 삭제)

## 다음 단계
1. Vue 프로젝트 빌드:
   ```bash
   cd frontend
   npm run build
   ```

2. Spring Boot 서버 실행 후 Vue 앱이 정상 작동하는지 확인

3. 문제가 없으면 `static-backup/` 디렉토리 삭제 가능:
   ```bash
   # Windows
   rmdir /s /q src\main\resources\static-backup
   
   # Linux/Mac
   rm -rf src/main/resources/static-backup
   ```

## 주의사항
- 백업 디렉토리는 Vue 앱이 완전히 작동하는 것을 확인한 후에만 삭제하세요
- 문제가 발생하면 백업에서 파일을 복원할 수 있습니다


