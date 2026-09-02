import api from './index.js'

export const enrollmentApi = {
  getMyEnrollments() {
    return api.get('/api/enrollments/my')
  },
  enroll(courseId) {
    return api.post('/api/enrollments', { courseId })
  },
  cancel(enrollmentId) {
    return api.delete(`/api/enrollments/${enrollmentId}`)
  },
  getRecommendations(userId) {
    return api.get(`/api/recommend/${userId}`)
  }
}
