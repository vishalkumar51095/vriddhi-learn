import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      // In dev, the app talks to the API Gateway on :8080. Set VITE_API_BASE_URL
      // to point elsewhere (see .env.example) — this proxy is just a convenience
      // so the browser can call same-origin /api/* paths without CORS friction.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
