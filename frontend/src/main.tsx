import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App'
import { ErrorProvider } from './contexts/ErrorContext'
import { LoadingProvider } from './contexts/LoadingContext'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <LoadingProvider>
      <ErrorProvider>
        <App />
      </ErrorProvider>
    </LoadingProvider>
  </StrictMode>
)
