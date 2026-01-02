import api from './index'

const userApi = {
  async getUser() {
    const response = await api.get('/api/user')
    return response.data
  },

  async getUserInfo() {
    const response = await api.get('/api/user/info')
    return response.data
  },

  async updateNickname(nickname) {
    const response = await api.put('/api/user/nickname', { nickname })
    return response.data
  },

  async updateProfileImage(profileImage) {
    const response = await api.put('/api/user/profile-image', { profileImage })
    return response.data
  },

  async updateBio(bio) {
    const response = await api.put('/api/user/bio', { bio })
    return response.data
  },

  async getPreference() {
    const response = await api.get('/api/user/preference')
    return response.data
  },

  async savePreference(data) {
    const response = await api.post('/api/user/preference', data)
    return response.data
  },

  async getTravelPreferences() {
    const response = await api.get('/api/user/travel-preferences')
    return response.data
  },

  async updateTravelPreferences(data) {
    const response = await api.put('/api/user/travel-preferences', data)
    return response.data
  },

  async changePassword(currentPassword, newPassword) {
    const response = await api.put('/api/user/password', {
      currentPassword,
      newPassword
    })
    return response.data
  },

  async deleteAccount() {
    const response = await api.delete('/api/user')
    return response.data
  }
}

export default userApi

