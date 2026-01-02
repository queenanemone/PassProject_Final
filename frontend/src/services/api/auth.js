import api from './index'

const authApi = {
  async login(email, password) {
    try {
      const response = await api.post('/api/auth/login', { email, password })
      if (response.data.success) {
        return response.data.data
      }
      throw new Error(response.data.message || '로그인에 실패했습니다')
    } catch (error) {
      // 401 에러 등 API 에러 처리
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      }
      throw error
    }
  },

  async register(email, password, name) {
    const response = await api.post('/api/auth/register', { email, password, name })
    if (response.data.success) {
      return response.data.data
    }
    throw new Error(response.data.message || '회원가입에 실패했습니다')
  },

  async getGoogleClientId() {
    const response = await api.get('/api/auth/google/client-id')
    if (response.data.success) {
      return response.data.data
    }
    throw new Error(response.data.message || 'Google Client ID를 가져올 수 없습니다')
  },

  async googleLogin(userInfo) {
    const response = await api.post('/api/auth/google', {
      email: userInfo.email,
      name: userInfo.name,
      picture: userInfo.picture
    })
    if (response.data.success) {
      return response.data.data
    }
    throw new Error(response.data.message || 'Google 로그인에 실패했습니다')
  },

  async forgotPassword(email) {
    const response = await api.post('/api/auth/forgot-password', { email })
    return response.data
  }
}

export default authApi

