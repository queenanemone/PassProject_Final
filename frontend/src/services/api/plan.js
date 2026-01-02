import api from './index'

const planApi = {
  async getPlans() {
    const response = await api.get('/api/plans')
    return response.data
  },

  async getPlan(planId) {
    const response = await api.get(`/api/plans/${planId}`)
    return response.data
  },

  async createPlan(data) {
    const response = await api.post('/api/plans', data)
    return response.data
  },

  async updatePlan(planId, data) {
    const response = await api.put(`/api/plans/${planId}`, data)
    return response.data
  },

  async deletePlan(planId) {
    const response = await api.delete(`/api/plans/${planId}`)
    return response.data
  },

  async updatePlanTitle(planId, title) {
    const response = await api.put(`/api/plans/${planId}/title`, { title })
    return response.data
  },

  async fillPlan(planId, updateData = {}) {
    const response = await api.post(`/api/plans/${planId}/fill`, updateData)
    return response.data
  },

  async getRecommendDestinations(lat, lon) {
    const response = await api.get('/api/plans/recommend-destinations', {
      params: { lat, lon }
    })
    return response.data
  },

  async addItem(planId, data) {
    const response = await api.post(`/api/plans/${planId}/add-item`, data)
    return response.data
  },

  async deleteItem(planId, item) {
    let endpoint = ''
    if (item.type === 'attraction') {
      const spotOrder = item.data.planDestination.spotOrder
      endpoint = `/api/plans/spots/${planId}/${spotOrder}`
    } else if (item.type === 'hotel') {
      const order = item.data.accommodationOrder
      const checkIn = item.data.checkInDate
      endpoint = `/api/plans/accommodations/${planId}?checkInDate=${checkIn}&accommodationOrder=${order}`
    } else if (item.type === 'transport') {
      const order = item.data.transportOrder
      endpoint = `/api/plans/transports/${planId}/${order}`
    } else {
      throw new Error(`Unknown item type: ${item.type}`)
    }

    const response = await api.delete(endpoint)
    return response.data
  },

  async moveItem(planId, type, idData, newDate) {
    const response = await api.put(`/api/plans/${planId}/move-item`, { type, idData, newDate })
    return response.data
  },

  async reorderItems(planId, data) {
    const response = await api.put(`/api/plans/${planId}/reorder`, data)
    return response.data
  },

  async searchTrain(params) {
    const response = await api.get('/api/plans/search/train', { params })
    return response.data
  },

  async searchTour(params) {
    const response = await api.get('/api/plans/search/tour', { params })
    return response.data
  },

  async getAiRecommendations(planId) {
    const response = await api.get(`/api/plans/${planId}/ai-recommendations`)
    return response.data
  },

  async addAiRecommendation(planId, data) {
    const response = await api.post(`/api/plans/${planId}/ai-recommendations/direct-add`, data)
    return response.data
  }
}

export default planApi

