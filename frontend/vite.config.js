import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite proxy avoids needing a backend CORS change during local development.
// All /api requests are forwarded to the Spring Boot monolith on 8080.
// When the backend moves behind a Gateway (Day 48), just change VITE_API_BASE_URL.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/payment': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
