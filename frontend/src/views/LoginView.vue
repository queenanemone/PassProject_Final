<template>
  <div class="relative flex min-h-screen w-full flex-col bg-background-dark">
    <header class="flex h-16 shrink-0 items-center justify-center px-4 md:px-6">
      <RouterLink class="flex items-center gap-2 text-lg font-bold text-text-primary-dark" to="/">
        <span class="material-symbols-outlined text-primary text-3xl">explore</span>
        <span>Trip-Board</span>
      </RouterLink>
    </header>
    
    <main class="flex flex-1 items-center justify-center p-4">
      <div class="flex w-full max-w-md flex-col items-center justify-center gap-6 rounded-xl bg-surface-dark/50 p-6 sm:p-8 border border-border-dark">
        <div class="text-center">
          <h1 class="text-2xl font-bold tracking-tight text-text-primary-dark sm:text-3xl">
            Trip-Board에 오신 것을 환영합니다
          </h1>
          <p class="mt-2 text-sm text-text-secondary-dark">당신만의 여행을 계획하고 추천받으세요</p>
        </div>
        
        <div class="w-full">
          <div class="flex w-full rounded-lg bg-background-dark p-1">
            <button
              @click="activeTab = 'login'"
              :class="[
                'flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
                activeTab === 'login'
                  ? 'bg-primary text-slate-900'
                  : 'text-text-secondary-dark hover:bg-surface-dark'
              ]"
            >
              로그인
            </button>
            <button
              @click="activeTab = 'register'"
              :class="[
                'flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
                activeTab === 'register'
                  ? 'bg-primary text-slate-900'
                  : 'text-text-secondary-dark hover:bg-surface-dark'
              ]"
            >
              회원가입
            </button>
          </div>
        </div>
        
        <!-- 로그인 폼 -->
        <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="flex w-full flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-text-secondary-dark" for="email">이메일</label>
            <input
              v-model="loginForm.email"
              class="block w-full rounded-lg border-border-dark bg-background-dark text-text-primary-dark placeholder-text-secondary-dark/70 focus:border-primary focus:ring-primary px-4 py-3"
              id="email"
              placeholder="email@example.com"
              type="email"
              required
            />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-text-secondary-dark" for="password">비밀번호</label>
            <input
              v-model="loginForm.password"
              class="block w-full rounded-lg border-border-dark bg-background-dark text-text-primary-dark placeholder-text-secondary-dark/70 focus:border-primary focus:ring-primary px-4 py-3"
              id="password"
              placeholder="••••••••"
              type="password"
              required
            />
          </div>
          <div class="flex items-center justify-end">
            <button 
              type="button"
              @click="showForgotPasswordModal = true"
              class="text-sm text-primary hover:underline"
            >
              비밀번호를 잊으셨나요?
            </button>
          </div>
          <button
            type="submit"
            :disabled="isLoading"
            class="flex h-11 w-full cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-primary px-5 text-base font-bold text-slate-900 transition-colors hover:bg-primary/90 disabled:opacity-50"
          >
            <span class="truncate">{{ isLoading ? '로그인 중...' : '로그인' }}</span>
          </button>
        </form>
        
        <!-- 회원가입 폼 -->
        <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="flex w-full flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-text-secondary-dark" for="regEmail">이메일</label>
            <input
              v-model="registerForm.email"
              class="block w-full rounded-lg border-border-dark bg-background-dark text-text-primary-dark placeholder-text-secondary-dark/70 focus:border-primary focus:ring-primary px-4 py-3"
              id="regEmail"
              placeholder="email@example.com"
              type="email"
              required
            />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-text-secondary-dark" for="regPassword">비밀번호</label>
            <input
              v-model="registerForm.password"
              class="block w-full rounded-lg border-border-dark bg-background-dark text-text-primary-dark placeholder-text-secondary-dark/70 focus:border-primary focus:ring-primary px-4 py-3"
              id="regPassword"
              placeholder="최소 8자 이상"
              type="password"
              required
              minlength="8"
            />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-text-secondary-dark" for="name">이름</label>
            <input
              v-model="registerForm.name"
              class="block w-full rounded-lg border-border-dark bg-background-dark text-text-primary-dark placeholder-text-secondary-dark/70 focus:border-primary focus:ring-primary px-4 py-3"
              id="name"
              placeholder="이름을 입력하세요"
              type="text"
              required
            />
          </div>
          <button
            type="submit"
            :disabled="isLoading"
            class="flex h-11 w-full cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-primary px-5 text-base font-bold text-slate-900 transition-colors hover:bg-primary/90 disabled:opacity-50"
          >
            <span class="truncate">{{ isLoading ? '회원가입 중...' : '회원가입' }}</span>
          </button>
        </form>
        
        <div class="relative w-full">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-border-dark"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="bg-surface-dark/50 px-2 text-text-secondary-dark">소셜 계정으로 간편하게 시작하기</span>
          </div>
        </div>
        
        <div class="flex w-full flex-col">
          <div
            id="google-signin-button"
            @click="handleGoogleSignIn"
            class="flex w-full cursor-pointer items-center justify-center gap-2 overflow-hidden rounded-lg border border-border-dark bg-surface-dark px-4 py-2.5 text-sm font-medium text-text-primary-dark transition-colors hover:bg-border-dark"
          >
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M22.5777 12.2618C22.5777 11.4545 22.5055 10.6691 22.3801 9.90909H12.2182V14.2255H18.1091C17.8546 15.6582 17.0328 16.8982 15.8201 17.7055V20.3345H19.5237C21.46 18.52 22.5777 15.6582 22.5777 12.2618Z" fill="#4285F4"></path>
              <path d="M12.2182 23.0001C15.2328 23.0001 17.7473 22.0219 19.5237 20.3346L15.82 17.7055C14.8019 18.4346 13.6001 18.8419 12.2182 18.8419C9.58185 18.8419 7.33458 17.0564 6.55458 14.651H2.73822V17.3728C4.46185 20.7819 8.01458 23.0001 12.2182 23.0001Z" fill="#34A853"></path>
              <path d="M6.55451 14.6509C6.32724 13.9436 6.19633 13.1909 6.19633 12.4145C6.19633 11.6382 6.32724 10.8854 6.54542 10.1782V7.45636H2.73815C1.99633 8.98182 1.5 10.6582 1.5 12.4145C1.5 14.1709 1.99633 15.8473 2.73815 17.3727L6.55451 14.6509Z" fill="#FBBC05"></path>
              <path d="M12.2182 5.98727C13.7837 5.98727 15.0673 6.54545 15.5891 7.03818L19.5946 3.23999C17.7382 1.54363 15.2237 0.827273 12.2182 0.827273C8.01458 0.827273 4.46185 3.04545 2.73822 6.45454L6.54549 9.17636C7.33458 6.77091 9.58185 5.98727 12.2182 5.98727Z" fill="#EA4335"></path>
            </svg>
            <span class="truncate">Google 계정으로 로그인</span>
          </div>
        </div>
        
        <footer class="mt-4 text-center text-xs text-text-secondary-dark">
          <p>
            <a class="underline hover:text-primary" href="#">이용약관</a>
            <span> 및 </span>
            <a class="underline hover:text-primary" href="#">개인정보처리방침</a>
            <span>에 동의합니다.</span>
          </p>
        </footer>
      </div>
    </main>

    <!-- 비밀번호 찾기 모달 -->
    <div 
      v-if="showForgotPasswordModal" 
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
      @click.self="closeForgotPasswordModal"
    >
      <div class="bg-card-dark rounded-xl border border-border-dark p-6 sm:p-8 max-w-md w-full mx-4">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-text-dark">비밀번호 찾기</h2>
          <button 
            @click="closeForgotPasswordModal"
            class="flex items-center justify-center w-8 h-8 rounded-lg text-text-secondary-dark hover:bg-background-dark transition-colors"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <form @submit.prevent="handleForgotPassword" class="flex flex-col gap-4">
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium text-text-dark" for="forgotEmail">이메일</label>
            <input
              v-model="forgotPasswordForm.email"
              class="block w-full rounded-lg border-border-dark bg-background-dark text-text-dark placeholder-text-secondary-dark/70 focus:border-primary focus:ring-primary px-4 py-3"
              id="forgotEmail"
              placeholder="email@example.com"
              type="email"
              required
            />
            <p class="text-xs text-text-secondary-dark">등록된 이메일 주소를 입력해주세요.</p>
          </div>
          
          <div v-if="emailSent" class="p-4 rounded-lg bg-primary/20 border border-primary/50">
            <div class="flex items-center gap-2 mb-2">
              <span class="material-symbols-outlined text-primary">check_circle</span>
              <p class="text-sm font-medium text-text-dark">이메일 발송 완료</p>
            </div>
            <p class="text-sm text-text-secondary-dark">
              입력하신 이메일 주소로 임시 비밀번호가 발송되었습니다.<br/>
              <span class="text-xs text-text-secondary-dark/80 mt-1 block">
                ⏱️ 이메일 전송에 시간이 소요될 수 있습니다. 잠시 후 이메일을 확인해주세요.
              </span>
              <br/>
              이메일을 확인하시고 임시 비밀번호로 로그인 후 비밀번호를 변경해주세요.
            </p>
          </div>
          
          <div class="flex gap-3">
            <button
              type="button"
              @click="closeForgotPasswordModal"
              class="flex-1 px-4 py-3 rounded-lg border border-border-dark bg-background-dark text-text-dark hover:bg-surface-dark transition-colors"
            >
              {{ emailSent ? '닫기' : '취소' }}
            </button>
            <button
              v-if="!emailSent"
              type="submit"
              :disabled="isLoadingForgotPassword"
              class="flex-1 px-4 py-3 rounded-lg bg-primary text-slate-900 font-bold hover:bg-primary/90 transition-colors disabled:opacity-50"
            >
              {{ isLoadingForgotPassword ? '처리 중...' : '임시 비밀번호 받기' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import authApi from '@/services/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const activeTab = ref(route.query.tab === 'register' ? 'register' : 'login')
const isLoading = ref(false)
const showForgotPasswordModal = ref(false)
const isLoadingForgotPassword = ref(false)
const emailSent = ref(false)

const loginForm = ref({
  email: '',
  password: ''
})

const registerForm = ref({
  email: '',
  password: '',
  name: ''
})

const forgotPasswordForm = ref({
  email: ''
})

/**
 * 로그인 처리
 */
const handleLogin = async () => {
  isLoading.value = true
  try {
    const result = await authStore.login(loginForm.value.email, loginForm.value.password)
    if (result.success) {
      router.push('/')
    } else {
      alert(result.message || '로그인에 실패했습니다')
    }
  } catch (error) {
    alert('로그인 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'))
  } finally {
    isLoading.value = false
  }
}

/**
 * 회원가입 처리
 */
const handleRegister = async () => {
  isLoading.value = true
  try {
    const result = await authStore.register(
      registerForm.value.email,
      registerForm.value.password,
      registerForm.value.name
    )
    if (result.success) {
      router.push('/')
    } else {
      alert(result.message || '회원가입에 실패했습니다')
    }
  } catch (error) {
    alert('회원가입 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'))
  } finally {
    isLoading.value = false
  }
}

const googleClientId = ref(null)

/**
 * 구글 로그인 버튼 핸들러
 */
const handleGoogleSignIn = async () => {
  if (!googleClientId.value) {
    alert('Google Client ID를 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
    return
  }

  if (typeof window.google === 'undefined' || !window.google.accounts || !window.google.accounts.oauth2) {
    alert('Google 로그인 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
    return
  }

  try {
    const tokenClient = window.google.accounts.oauth2.initTokenClient({
      client_id: googleClientId.value,
      scope: 'email profile',
      callback: async (tokenResponse) => {
        if (tokenResponse.error) {
          console.error('Google 로그인 오류:', tokenResponse.error)
          alert('Google 로그인에 실패했습니다: ' + tokenResponse.error)
          return
        }
        
        try {
          const userInfoResponse = await fetch(`https://www.googleapis.com/oauth2/v2/userinfo?access_token=${tokenResponse.access_token}`)
          if (!userInfoResponse.ok) {
            throw new Error(`HTTP error! status: ${userInfoResponse.status}`)
          }
          const userInfo = await userInfoResponse.json()
          await handleGoogleUserInfo(userInfo)
        } catch (error) {
          console.error('사용자 정보 가져오기 오류:', error)
          alert('사용자 정보를 가져오는 중 오류가 발생했습니다: ' + error.message)
        }
      }
    })
    
    tokenClient.requestAccessToken({ prompt: 'consent' })
  } catch (error) {
    console.error('Google 로그인 초기화 오류:', error)
    alert('Google 로그인 초기화 중 오류가 발생했습니다: ' + error.message)
  }
}

/**
 * 구글 사용자 정보로 백엔드 로그인/가입 처리
 */
const handleGoogleUserInfo = async (userInfo) => {
  try {
    isLoading.value = true
    const result = await authStore.googleLogin({
      email: userInfo.email,
      name: userInfo.name,
      picture: userInfo.picture
    })
    
    if (result.success) {
      router.push('/')
    } else {
      alert(result.message || 'Google 로그인에 실패했습니다')
    }
  } catch (error) {
    console.error('Google 로그인 오류:', error)
    alert('Google 로그인 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류'))
  } finally {
    isLoading.value = false
  }
}

const waitForGoogleScript = (callback, maxAttempts = 50) => {
  let attempts = 0
  const checkInterval = setInterval(() => {
    attempts++
    if (typeof window.google !== 'undefined' && window.google.accounts && window.google.accounts.oauth2) {
      clearInterval(checkInterval)
      console.log('Google Identity Services 로드 완료')
      callback()
    } else if (attempts >= maxAttempts) {
      clearInterval(checkInterval)
      console.error('Google Identity Services 로드 시간 초과')
    }
  }, 100)
}

/**
 * 비밀번호 찾기 처리
 */
const handleForgotPassword = async () => {
  if (!forgotPasswordForm.value.email) {
    alert('이메일을 입력해주세요')
    return
  }
  
  isLoadingForgotPassword.value = true
  
  try {
    // 비동기 이메일 발송 요청 (즉시 응답)
    const result = await authApi.forgotPassword(forgotPasswordForm.value.email)
    
    if (result.success) {
      // 즉시 완료 메시지 표시 (이메일은 백그라운드에서 발송 중)
      emailSent.value = true
      isLoadingForgotPassword.value = false
    } else {
      // 실패 시 사용자에게 알림
      emailSent.value = false
      isLoadingForgotPassword.value = false
      alert(result.message || '비밀번호 재설정에 실패했습니다')
    }
  } catch (error) {
    // 에러 발생 시 사용자에게 알림
    emailSent.value = false
    isLoadingForgotPassword.value = false
    const errorMessage = error.response?.data?.message || error.message || '비밀번호 재설정 중 오류가 발생했습니다'
    alert(errorMessage)
  }
}

/**
 * 비밀번호 찾기 모달 닫기
 */
const closeForgotPasswordModal = () => {
  showForgotPasswordModal.value = false
  forgotPasswordForm.value.email = ''
  emailSent.value = false
}

/**
 * 구글 로그인 초기화
 */
const initializeGoogleSignIn = async () => {
  try {
    const clientId = await authApi.getGoogleClientId()
    if (clientId && clientId !== 'YOUR_GOOGLE_CLIENT_ID_HERE') {
      googleClientId.value = clientId
      
      waitForGoogleScript(() => {
        // Script loaded
      })
    } else {
      console.warn('Google Client ID가 설정되지 않았습니다.')
      alert('Google Client ID가 설정되지 않았습니다. 관리자에게 문의하세요.')
    }
  } catch (error) {
    console.error('Google Client ID 로드 오류:', error)
  }
}

// 라우트 쿼리 파라미터 변경 감지
watch(() => route.query.tab, (newTab) => {
  if (newTab === 'register') {
    activeTab.value = 'register'
  } else {
    activeTab.value = 'login'
  }
})

onMounted(() => {
  // Google Sign-In 스크립트 로드
  if (!window.google) {
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = () => {
      window.googleScriptLoaded = true
      initializeGoogleSignIn()
    }
    document.head.appendChild(script)
  } else {
    initializeGoogleSignIn()
  }
  
  // 이미 로그인된 경우 메인화면으로 리다이렉트
  if (authStore.isAuthenticated) {
    router.push('/')
  }
})
</script>

<style scoped>
/* 다크 모드는 index.html의 html 태그에 class="dark"를 추가하여 설정 */
</style>

