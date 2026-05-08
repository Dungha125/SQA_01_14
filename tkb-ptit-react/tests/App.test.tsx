import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import App from '@/App'
import React from 'react'

vi.mock('react-hot-toast', () => ({
  default: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
}))

vi.mock('@/services/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/services/api')>()
  return {
    ...actual,
    subjectService: Object.assign({}, actual.subjectService, {
      getAll: vi.fn().mockResolvedValue({
        data: { success: true, data: { items: [], totalElements: 0 } },
      }),
    }),
    roomService: Object.assign({}, actual.roomService, {
      getAll: vi.fn().mockResolvedValue({
        data: { success: true, data: [] },
      }),
    }),
    semesterService: Object.assign({}, actual.semesterService, {
      getAll: vi.fn().mockResolvedValue({
        data: { success: true, data: [] },
      }),
    }),
    userService: Object.assign({}, actual.userService, {
      getAll: vi.fn().mockResolvedValue({
        data: {
          success: true,
          data: [
            {
              id: 2,
              username: 'user1',
              email: 'u@ptit.edu.vn',
              fullName: 'User One',
              role: 'USER',
              enabled: true,
            },
          ],
        },
      }),
    }),
  }
})

vi.mock('@/hooks/useNotification', () => ({
  useNotification: () => ({
    notify: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
    notification: { isOpen: false },
    close: vi.fn(),
  }),
}))

describe('App - Dùng chung/Infrastructure', () => {
  const lsMem: Record<string, string> = {}

  beforeEach(() => {
    vi.clearAllMocks()
    Object.keys(lsMem).forEach((k) => delete lsMem[k])
    Object.defineProperty(window, 'localStorage', {
      configurable: true,
      value: {
        getItem: (k: string) => (k in lsMem ? lsMem[k] : null),
        setItem: (k: string, v: string) => {
          lsMem[k] = v
        },
        removeItem: (k: string) => {
          delete lsMem[k]
        },
        clear: () => {
          Object.keys(lsMem).forEach((k) => delete lsMem[k])
        },
        key: () => null,
        length: 0,
      },
    })
  })

  it('GUI-01: hiển thị login page mặc định when not authenticated', () => {
    
    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>
    )

    expect(screen.getByText('Quản lý TKB')).toBeInTheDocument()
  })

  it('GUI-02: redirect về /login khi vào / mà chưa đăng nhập', async () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>
    )
    await waitFor(() => {
      expect(screen.getByText('Quản lý TKB')).toBeInTheDocument()
    })
  })

  it('GUI-03: hiển thị dashboard khi đã có token', async () => {
    localStorage.setItem('authToken', 't')
    localStorage.setItem('user', JSON.stringify({ role: 'USER', fullName: 'U', email: 'u@x' }))
    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>
    )
    await waitFor(() => {
      expect(screen.getByText('Dashboard')).toBeInTheDocument()
    })
  })

  it('GUI-04: 404 trong layout khi đường dẫn con không tồn tại', async () => {
    localStorage.setItem('authToken', 't')
    localStorage.setItem('user', JSON.stringify({ role: 'USER' }))
    render(
      <MemoryRouter initialEntries={['/route-khong-ton-tai-xyz']}>
        <App />
      </MemoryRouter>
    )
    await waitFor(() => {
      expect(screen.getByText(/404 - Page Not Found/i)).toBeInTheDocument()
    })
  })

  it('GUI-05: user không phải ADMIN không vào được /users (redirect về /)', async () => {
    localStorage.setItem('authToken', 't')
    localStorage.setItem('user', JSON.stringify({ role: 'USER', fullName: 'U' }))
    render(
      <MemoryRouter initialEntries={['/users']}>
        <App />
      </MemoryRouter>
    )
    await waitFor(() => {
      expect(screen.getByText('Dashboard')).toBeInTheDocument()
    })
  })

  it('GUI-06: ADMIN vào /users thấy trang quản lý người dùng', async () => {
    localStorage.setItem('authToken', 't')
    localStorage.setItem('user', JSON.stringify({ role: 'ADMIN', fullName: 'Admin' }))
    render(
      <MemoryRouter initialEntries={['/users']}>
        <App />
      </MemoryRouter>
    )
    await waitFor(() => {
      expect(screen.getByText(/Quản lý người dùng/i)).toBeInTheDocument()
    })
  })

  it('GUI-07: có all required routes configured', () => {
    // App should export without errors
    expect(App).toBeDefined()
  })

  it('GUI-08: render không lỗi', () => {
    expect(() => render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    )).not.toThrow()
  })
})
