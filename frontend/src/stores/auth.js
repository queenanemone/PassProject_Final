import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi from '@/services/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  async function login(email, password) {
    try {
      const response = await authApi.login(email, password)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      return { success: true }
    } catch (error) {
      // 에러 메시지 추출 (401 에러 등)
      const errorMessage = error.response?.data?.message || error.message || '로그인에 실패했습니다'
      return { success: false, message: errorMessage }
    }
  }

  async function register(email, password, name) {
    try {
      const response = await authApi.register(email, password, name)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      return { success: true }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }

  async function googleLogin(userInfo) {
    try {
      const response = await authApi.googleLogin(userInfo)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      return { success: true }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    googleLogin,
    logout
  }
})

