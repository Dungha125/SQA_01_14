import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter, MemoryRouter } from 'react-router-dom'
import Layout from '@/components/Layout'
import toast from 'react-hot-toast'

// Mock dependencies
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const mockUser = {
  id: 1,
  username: 'admin',
  email: 'admin@ptit.edu.vn',
  fullName: 'Administrator',
  role: 'ADMIN',
}

describe('Layout - Dùng chung/Infrastructure', () => {
  const lsMem: Record<string, string> = {}

  beforeEach(() => {
    vi.clearAllMocks()
    mockNavigate.mockClear()
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

  it('GUI-28: hiển thị layout và sidebar', () => {
    localStorage.setItem('user', JSON.stringify(mockUser))
    render(
      <BrowserRouter>
        <Layout />
      </BrowserRouter>
    )

    expect(screen.getByAltText('PTIT Logo')).toBeInTheDocument()
  })

  it('GUI-29: menu theo role (ADMIN/USER) + toggle sidebar + nhấn logo', async () => {
    const user = userEvent.setup()

    // ADMIN
    localStorage.setItem('user', JSON.stringify(mockUser))
    const { unmount } = render(
      <MemoryRouter initialEntries={['/subjects']}>
        <Layout />
      </MemoryRouter>,
    )
    expect(screen.getByRole('navigation')).toBeInTheDocument()
    expect(document.body.textContent || '').toContain('CT Đào tạo')
    expect(screen.getByText('Quản lý người dùng')).toBeInTheDocument()

    const toggle = screen.getByRole('button', { name: /toggle sidebar/i })
    await user.click(toggle)
    await user.click(screen.getByAltText('PTIT Logo'))
    expect(mockNavigate).toHaveBeenCalledWith('/')
    unmount()

    // USER
    localStorage.setItem('user', JSON.stringify({ ...mockUser, role: 'USER' }))
    render(
      <BrowserRouter>
        <Layout />
      </BrowserRouter>,
    )
    expect(screen.queryByText('Quản lý người dùng')).not.toBeInTheDocument()
  })

  it('GUI-30: đăng xuất (hover + click) xóa token và điều hướng /login', async () => {
    const user = userEvent.setup()
    localStorage.setItem('authToken', 't')
    localStorage.setItem('user', JSON.stringify(mockUser))
    const { container } = render(
      <BrowserRouter>
        <Layout />
      </BrowserRouter>,
    )
    const profile = container.querySelector('.group')
    expect(profile).toBeTruthy()
    await user.hover(profile!)
    const logout = await screen.findByRole('button', { name: 'Đăng xuất' })
    await user.click(logout)
    expect(localStorage.getItem('authToken')).toBeNull()
    expect(toast.success).toHaveBeenCalledWith('Đã đăng xuất')
    expect(mockNavigate).toHaveBeenCalledWith('/login')
  })
})
