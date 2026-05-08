import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import '@testing-library/jest-dom'
import { BrowserRouter } from 'react-router-dom'
import LoginPage from '@/pages/LoginPage'
import api from '@/services/api'
import toast from 'react-hot-toast'

// Mock dependencies
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn(),
  },
}))

vi.mock('@/hooks/useNotification', () => ({
  useNotification: () => ({
    notify: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
    notification: { isOpen: false },
    close: vi.fn(),
  }),
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

describe('LoginPage - Quản lý người dùng', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('GUI-60: hiển thị form đăng nhập', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    )

    expect(screen.getByText('Quản lý TKB')).toBeInTheDocument()
    expect(screen.getByText('Hệ thống quản lý thời khóa biểu')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Nhập tên đăng nhập')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Nhập mật khẩu')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Đăng nhập' })).toBeInTheDocument()
  })

  it('GUI-61: báo lỗi validate khi thiếu thông tin', async () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    )

    const submitButton = screen.getByRole('button', { name: 'Đăng nhập' })
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Vui lòng nhập tên đăng nhập và mật khẩu')
    })
  })

  it('GUI-62: bật/tắt hiển thị mật khẩu', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    )

    const passwordInput = screen.getByPlaceholderText('Nhập mật khẩu')
    expect(passwordInput).toHaveAttribute('type', 'password')

    const toggleButton = screen.getByRole('button', { name: '' })
    fireEvent.click(toggleButton)

    expect(passwordInput).toHaveAttribute('type', 'text')
  })

  it('GUI-63: đăng nhập thành công lưu localStorage và chuyển hướng /', async () => {
    vi.mocked(api.post).mockResolvedValueOnce({
      data: {
        success: true,
        data: {
          token: 'jwt-token',
          id: 1,
          username: 'admin',
          email: 'admin@ptit.edu.vn',
          fullName: 'Admin',
          role: 'ADMIN',
        },
      },
    } as any)

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    )

    const usernameInput = screen.getByPlaceholderText('Nhập tên đăng nhập')
    const passwordInput = screen.getByPlaceholderText('Nhập mật khẩu')
    const submitButton = screen.getByRole('button', { name: 'Đăng nhập' })

    fireEvent.change(usernameInput, { target: { value: 'admin' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/')
    }, { timeout: 3000 })
  })

  it('GUI-64: xử lý các nhánh lỗi đăng nhập (parameterized)', async () => {
    const cases: Array<{
      label: string
      mock: () => void
      expected: string
    }> = [
      {
        label: 'sai mật khẩu',
        mock: () =>
          vi.mocked(api.post).mockRejectedValueOnce({ response: { data: { error: 'Bad credentials' } } }),
        expected: 'Sai tên đăng nhập hoặc mật khẩu',
      },
      {
        label: 'chưa kích hoạt',
        mock: () =>
          vi
            .mocked(api.post)
            .mockRejectedValueOnce({ response: { data: { error: 'Tài khoản chưa đươc kích hoạt' } } }),
        expected: 'Tài khoản chưa được kích hoạt. Vui lòng liên hệ quản trị viên.',
      },
      {
        label: 'không tồn tại',
        mock: () =>
          vi.mocked(api.post).mockRejectedValueOnce({ response: { data: { error: 'Tài khoản không tồn tại' } } }),
        expected: 'Tài khoản không tồn tại',
      },
      {
        label: 'error khác',
        mock: () => vi.mocked(api.post).mockRejectedValueOnce({ response: { data: { error: 'Server overload' } } }),
        expected: 'Server overload',
      },
      {
        label: 'message',
        mock: () => vi.mocked(api.post).mockRejectedValueOnce({ response: { data: { message: 'Custom msg' } } }),
        expected: 'Custom msg',
      },
      {
        label: 'response rỗng',
        mock: () => vi.mocked(api.post).mockRejectedValueOnce({ response: { data: {} } }),
        expected: 'Đăng nhập thất bại',
      },
      {
        label: 'request only',
        mock: () => vi.mocked(api.post).mockRejectedValueOnce({ request: {} }),
        expected: 'Không thể kết nối tới máy chủ',
      },
      {
        label: 'unknown error',
        mock: () => vi.mocked(api.post).mockRejectedValueOnce(new Error('x')),
        expected: 'Đã xảy ra lỗi. Vui lòng thử lại',
      },
      {
        label: 'success false',
        mock: () => vi.mocked(api.post).mockResolvedValueOnce({ data: { success: false, message: 'Sai mật khẩu' } } as any),
        expected: 'Sai mật khẩu',
      },
    ]

    for (const c of cases) {
      vi.clearAllMocks()
      c.mock()
      const { unmount } = render(
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>,
      )
      fireEvent.change(screen.getByPlaceholderText('Nhập tên đăng nhập'), { target: { value: 'u' } })
      fireEvent.change(screen.getByPlaceholderText('Nhập mật khẩu'), { target: { value: 'p' } })
      fireEvent.click(screen.getByRole('button', { name: 'Đăng nhập' }))
      // eslint-disable-next-line no-await-in-loop
      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith(c.expected)
      })
      unmount()
    }
  })

  it('GUI-65: điều hướng khi nhấn logo và nút đăng ký', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    )

    fireEvent.click(screen.getByAltText('PTIT Logo'))
    expect(mockNavigate).toHaveBeenCalledWith('/dashboard')

    const registerButton = screen.getByRole('button', { name: 'Đăng ký' })
    fireEvent.click(registerButton)

    expect(mockNavigate).toHaveBeenCalledWith('/register')
  })
})
