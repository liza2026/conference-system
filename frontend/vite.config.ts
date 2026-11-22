import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/oauth2': 'http://localhost:8080',
      '/.well-known': 'http://localhost:8080',
      '/userinfo': 'http://localhost:8080',
      '/connect': 'http://localhost:8080',
    },
  },
})
