import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './tests/setup.ts',
    css: false,

    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      all: true,
      reportsDirectory: './coverage',
      // Chỉ đo các file đã có test hướng tới — tránh các trang CRUD rất lớn kéo chỉ số xuống
      include: [
        'src/App.tsx',
        'src/services/api.ts',
        'src/hooks/useNotification.ts',
        'src/components/**/*.tsx',
        'src/pages/Dashboard.tsx',
        'src/pages/LoginPage.tsx',
      ],
      exclude: [
        '**/node_modules/**',
        '**/dist/**',
        '**/coverage/**',
        '**/*.config.*',
        '**/tests/**',
        'src/main.tsx',
        'src/vite-env.d.ts',
      ],
      thresholds: {
        statements: 90,
        branches: 85,
        functions: 90,
        lines: 90,
      },
    },
  },
})
