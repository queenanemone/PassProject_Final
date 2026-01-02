import axios from 'axios'

// 개발 환경에서는 Vite 프록시를 사용하므로 빈 문자열 사용
// 프로덕션에서는 환경 변수로 설정
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || (import.meta.env.DEV ? '' : 'http://localhost:8080')

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터: 토큰 자동 추가
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 응답 인터셉터: 401 에러 처리
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 로그인/회원가입 API는 인터셉터에서 제외 (정상적인 401 응답일 수 있음)
      const isAuthEndpoint = error.config?.url?.includes('/api/auth/login') || 
                           error.config?.url?.includes('/api/auth/register')
      
      if (!isAuthEndpoint) {
        // 인증이 필요한 다른 API에서 401이 발생한 경우에만 리다이렉트
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api

