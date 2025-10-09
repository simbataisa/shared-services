import axios from 'axios'
import { useLoadingStore } from '@/store/loading'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
})

api.interceptors.request.use((config) => {
  // Start global loading on every request
  try {
    const store = useLoadingStore.getState()
    store.increment()
  } catch {}
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    try {
      const store = useLoadingStore.getState()
      store.decrement()
    } catch {}
    return response
  },
  (error) => {
    try {
      const store = useLoadingStore.getState()
      store.decrement()
    } catch {}
    return Promise.reject(error)
  }
)

export default api