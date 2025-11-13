import axios from "axios";
import { useLoadingStore } from "@/store/loading";

// Function to determine the correct API URL based on current origin
const getApiUrl = () => {
  const currentOrigin = window.location.origin;

  // If accessing from dev tunnel frontend, use dev tunnel backend
  if (currentOrigin === "https://shared-services.dennisdao.com") {
    return "https://api-shared-services.dennisdao.com/api/v1";
  }

  // If accessing from workshop frontend, use workshop backend
  if (currentOrigin === "https://app.workshop.dennisdao.com") {
    return "https://api.workshop.dennisdao.com/api/v1";
  }

  // Otherwise use environment variable or localhost
  return import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";
};

// Define primary and fallback URLs
const PRIMARY_API_URL = getApiUrl();
const FALLBACK_API_URL = "https://api-shared-services.dennisdao.com/api/v1";

// Create axios instance with primary URL
const api = axios.create({
  baseURL: PRIMARY_API_URL,
  timeout: 10000, // 10 second timeout
});

api.interceptors.request.use((config) => {
  // Start global loading on every request
  try {
    const store = useLoadingStore.getState();
    store.increment();
  } catch {}
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    try {
      const store = useLoadingStore.getState();
      store.decrement();
    } catch {}
    return response;
  },
  async (error) => {
    try {
      const store = useLoadingStore.getState();
      store.decrement();
    } catch {}

    // Check if this is a network error or timeout and we're using the primary URL
    const isNetworkError =
      !error.response &&
      (error.code === "ECONNREFUSED" ||
        error.code === "ENOTFOUND" ||
        error.code === "ETIMEDOUT");
    const isTimeoutError = error.code === "ECONNABORTED";
    const shouldRetryWithFallback =
      (isNetworkError || isTimeoutError) &&
      error.config?.baseURL === PRIMARY_API_URL;

    if (shouldRetryWithFallback && !error.config?._retryWithFallback) {
      console.warn(
        `Primary API (${PRIMARY_API_URL}) failed, retrying with fallback URL (${FALLBACK_API_URL})`
      );

      // Mark this request as already retried to prevent infinite loops
      error.config._retryWithFallback = true;
      error.config.baseURL = FALLBACK_API_URL;

      try {
        return await api.request(error.config);
      } catch (fallbackError) {
        console.error("Both primary and fallback APIs failed:", fallbackError);
        return Promise.reject(fallbackError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
