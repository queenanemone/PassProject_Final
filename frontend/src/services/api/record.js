import api from './index'

const recordApi = {
  async getUserRecords() {
    const response = await api.get('/api/records/user')
    return response.data
  },

  async getRecord(recordId) {
    const response = await api.get(`/api/records/${recordId}`)
    return response.data
  },

  async createRecord(data) {
    const response = await api.post('/api/records', data)
    return response.data
  },

  async updateRecord(recordId, data) {
    const response = await api.put(`/api/records/${recordId}`, data)
    return response.data
  },

  async deleteRecord(recordId) {
    const response = await api.delete(`/api/records/${recordId}`)
    return response.data
  },

  async getRecordImages(recordId) {
    const response = await api.get(`/api/records/${recordId}/images`)
    return response.data
  },

  async uploadRecordImages(images) {
    const response = await api.post('/api/records/images', { images })
    return response.data
  }
}

export default recordApi

