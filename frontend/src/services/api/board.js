import api from './index'

const boardApi = {
  async getPosts(params = {}) {
    const response = await api.get('/api/board/posts', { params })
    return response.data
  },

  async getPost(postId) {
    const response = await api.get(`/api/board/posts/${postId}`)
    return response.data
  },

  async createPost(data) {
    const response = await api.post('/api/board/posts', data)
    return response.data
  },

  async updatePost(postId, data) {
    const response = await api.put(`/api/board/posts/${postId}`, data)
    return response.data
  },

  async deletePost(postId) {
    const response = await api.delete(`/api/board/posts/${postId}`)
    return response.data
  },

  async likePost(postId) {
    const response = await api.post(`/api/board/posts/${postId}/like`)
    return response.data
  },

  async getComments(postId) {
    const response = await api.get(`/api/board/posts/${postId}/comments`)
    return response.data
  },

  async addComment(postId, data) {
    const response = await api.post(`/api/board/posts/${postId}/comments`, data)
    return response.data
  },

  async updateComment(commentId, data) {
    const response = await api.put(`/api/board/comments/${commentId}`, data)
    return response.data
  },

  async deleteComment(commentId) {
    const response = await api.delete(`/api/board/comments/${commentId}`)
    return response.data
  },

  async getMyPosts() {
    const response = await api.get('/api/board/posts/my')
    return response.data
  },

  async getLikedPosts() {
    const response = await api.get('/api/board/posts/liked')
    return response.data
  },

  async getCommentedPosts() {
    const response = await api.get('/api/board/posts/commented')
    return response.data
  }
}

export default boardApi

