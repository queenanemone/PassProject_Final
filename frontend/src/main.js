import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'

// 다크모드 초기화 - 앱 시작 시 실행
const initializeTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  const html = document.documentElement
  
  if (savedTheme === 'light') {
    html.classList.remove('dark')
  } else {
    // 기본값은 다크모드 (savedTheme이 없거나 'dark'인 경우)
    html.classList.add('dark')
    if (!savedTheme) {
      localStorage.setItem('theme', 'dark')
    }
  }
}

// 앱 마운트 전에 테마 초기화
initializeTheme()

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 전역 alert, confirm, prompt 함수를 위한 플레이스홀더
window._customAlert = null
window._customConfirm = null
window._customPrompt = null

// 브라우저 기본 alert, confirm, prompt 오버라이드
window._originalAlert = window.alert
window._originalConfirm = window.confirm
window._originalPrompt = window.prompt

window.alert = function(message) {
  if (window._customAlert) {
    window._customAlert(message, '', 'info')
  } else {
    window._originalAlert(message)
  }
}

window.confirm = function(message) {
  if (window._customConfirm) {
    return window._customConfirm(message, '')
  } else {
    return Promise.resolve(window._originalConfirm(message))
  }
}

window.prompt = function(message, defaultValue = '') {
  if (window._customPrompt) {
    return window._customPrompt(message, defaultValue, '')
  } else {
    return Promise.resolve(window._originalPrompt(message, defaultValue))
  }
}

app.mount('#app')

