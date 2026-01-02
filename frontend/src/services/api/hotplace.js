import api from './index'

const hotplaceApi = {
  async getAllHotPlaces() {
    const response = await api.get('/api/hotplaces')
    return response.data
  },

  async getMyHotPlaces() {
    const response = await api.get('/api/hotplaces/my')
    return response.data
  },

  async getHotPlace(hotplaceId) {
    const response = await api.get(`/api/hotplaces/${hotplaceId}`)
    return response.data
  },

  async createHotPlace(data) {
    const response = await api.post('/api/hotplaces', data)
    return response.data
  },

  async updateHotPlace(hotplaceId, data) {
    const response = await api.put(`/api/hotplaces/${hotplaceId}`, data)
    return response.data
  },

  async deleteHotPlace(hotplaceId) {
    const response = await api.delete(`/api/hotplaces/${hotplaceId}`)
    return response.data
  },

  async addImage(hotplaceId, imageUrl, imageOrder) {
    const response = await api.post(`/api/hotplaces/${hotplaceId}/images`, {
      imageUrl,
      imageOrder
    })
    return response.data
  },

  async getImages(hotplaceId) {
    const response = await api.get(`/api/hotplaces/${hotplaceId}/images`)
    return response.data
  }
}

export default hotplaceApi

