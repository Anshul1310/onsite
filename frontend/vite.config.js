import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../public',
    emptyOutDir: true
  },
  server: {
    proxy: {
      '/generate': 'http://localhost:3000',
      '/attendance': 'http://localhost:3000',
      '/mark': 'http://localhost:3000',
      '/login': 'http://localhost:3000',
      '/qr': 'http://localhost:3000'
    }
  }
})
