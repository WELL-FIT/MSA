import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // '' 접두사로 VITE_ 없는 변수(OPENAI_API_KEY)까지 전부 로드
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      host: 'localhost',
      port: 3000,
      strictPort: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        },
        '/oauth2': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        },
        '/login': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        },
        '/logout': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        },
        '/userinfo': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        },
        // AI 진단 기능용 — 브라우저에 API 키를 노출하지 않기 위해
        // dev 서버가 서버 측에서 Authorization 헤더를 주입해 OpenAI로 프록시
        '/ai': {
          target: 'https://api.openai.com',
          changeOrigin: true,
          secure: true,
          rewrite: (path) => path.replace(/^\/ai/, ''),
          headers: {
            Authorization: `Bearer ${env.OPENAI_API_KEY || ''}`
          }
        }
      }
    }
  }
})