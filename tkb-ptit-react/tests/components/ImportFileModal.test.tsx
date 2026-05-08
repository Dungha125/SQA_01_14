import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ImportFileModal from '@/components/ImportFileModal'
import type { Mock } from 'vitest'
import toast from 'react-hot-toast'

// Mock dependencies
vi.mock('@/services/api', () => ({
  semesterService: {
    getAll: vi.fn(),
  },
}))

vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

import { semesterService } from '@/services/api'

describe('ImportFileModal - Dùng chung/Infrastructure', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    ;(semesterService.getAll as Mock).mockResolvedValue({
      data: {
        success: true,
        data: [
          { id: 1, semesterName: 'Học kỳ 1', academicYear: '2024-2025' },
          { id: 2, semesterName: 'Học kỳ 2', academicYear: '2024-2025' },
        ],
      },
    })
  })

  it('GUI-20: hiển thị modal khi mở và ẩn khi đóng', () => {
    const onClose = vi.fn()
    const onConfirm = vi.fn()

    const { rerender } = render(
      <ImportFileModal
        isOpen={true}
        onClose={onClose}
        onConfirm={onConfirm}
        title="Test Import"
      />
    )

    expect(screen.getByText('Test Import')).toBeInTheDocument()
    expect(screen.getByText(/Chọn file/)).toBeInTheDocument()
    expect(screen.getByText(/Tải file mẫu/)).toBeInTheDocument()

    rerender(<ImportFileModal isOpen={false} onClose={onClose} onConfirm={onConfirm} title="Test Import" />)
    expect(screen.queryByText('Test Import')).not.toBeInTheDocument()
    expect(screen.queryByText(/Chọn file/)).not.toBeInTheDocument()
  })

  it('GUI-21: validate file (sai extension / quá dung lượng)', async () => {
    // sai extension
    const { unmount } = render(
      <ImportFileModal isOpen onClose={vi.fn()} onConfirm={vi.fn()} title="Import" accept=".xlsx" />,
    )
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    const bad = new File(['x'], 'a.pdf', { type: 'application/pdf' })
    fireEvent.change(input, { target: { files: [bad] } })
    expect(toast.error).toHaveBeenCalled()
    unmount()

    // quá dung lượng
    const user = userEvent.setup()
    render(
      <ImportFileModal
        isOpen={true}
        onClose={vi.fn()}
        onConfirm={vi.fn()}
        title="Import"
        accept=".xlsx"
        maxSizeMB={0.000001}
      />,
    )
    const inputBig = document.querySelector('input[type="file"]') as HTMLInputElement
    const big = new File([new Uint8Array(5000)], 'huge.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    await user.upload(inputBig, big)
    expect(toast.error).toHaveBeenCalledWith(expect.stringContaining('File quá lớn'))
  })

  it('GUI-22: chọn file hợp lệ rồi xác nhận (có/không có học kỳ)', async () => {
    const user = userEvent.setup()
    const onConfirm = vi.fn()
    const { unmount } = render(
      <ImportFileModal
        isOpen={true}
        onClose={vi.fn()}
        onConfirm={onConfirm}
        title="Import"
        accept=".xlsx"
      />,
    )
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    const f = new File(['x'], 'ok.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    await user.upload(input, f)
    await user.click(screen.getByRole('button', { name: /Lưu \/ Xác nhận/i }))
    expect(onConfirm).toHaveBeenCalledWith(f, undefined)
    unmount()

    // có chọn học kỳ: thiếu học kỳ => báo lỗi
    const onConfirm2 = vi.fn()
    render(
      <ImportFileModal isOpen onClose={vi.fn()} onConfirm={onConfirm2} title="Import" showSemesterSelect accept=".xlsx" />,
    )
    await waitFor(() => expect(screen.getByRole('combobox')).toBeInTheDocument())
    const input3 = document.querySelector('input[type="file"]') as HTMLInputElement
    await user.upload(
      input3,
      new File(['x'], 'ok.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      }),
    )
    await user.click(screen.getByRole('button', { name: /Lưu \/ Xác nhận/i }))
    expect(toast.error).toHaveBeenCalledWith('Vui lòng chọn học kỳ trước khi import')
    expect(onConfirm2).not.toHaveBeenCalled()
  })

  it('GUI-23: kéo thả file hợp lệ', () => {
    const onConfirm = vi.fn()
    render(
      <ImportFileModal isOpen onClose={vi.fn()} onConfirm={onConfirm} title="I" accept=".xlsx" />,
    )
    const zone = screen.getByText(/Chọn hoặc kéo thả file vào đây/i).closest('.border-dashed')
    expect(zone).toBeTruthy()
    const file = new File(['x'], 'd.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    fireEvent.dragOver(zone!)
    fireEvent.drop(zone!, { dataTransfer: { files: [file] } })
    expect(screen.getByText('d.xlsx')).toBeInTheDocument()
  })

  it('GUI-24: tải file mẫu (có URL / không URL)', async () => {
    const open = vi.spyOn(window, 'open').mockImplementation(() => null)
    const user = userEvent.setup()
    const { unmount } = render(
      <ImportFileModal
        isOpen
        onClose={vi.fn()}
        onConfirm={vi.fn()}
        title="I"
        sampleFileUrl="https://example.com/mau.xlsx"
      />,
    )
    await user.click(screen.getByRole('button', { name: /Tải file mẫu về/i }))
    expect(open).toHaveBeenCalledWith('https://example.com/mau.xlsx', '_blank')
    open.mockRestore()
    unmount()

    const click = vi.fn()
    const createEl = document.createElement.bind(document)
    const spy = vi.spyOn(document, 'createElement').mockImplementation((tag: string) => {
      const el = createEl(tag)
      if (tag === 'a') {
        Object.defineProperty(el, 'click', { value: click })
      }
      return el as HTMLElement
    })
    render(<ImportFileModal isOpen onClose={vi.fn()} onConfirm={vi.fn()} title="I" sampleFileName="mau.xlsx" />)
    await user.click(screen.getByRole('button', { name: /Tải file mẫu về/i }))
    expect(click).toHaveBeenCalled()
    spy.mockRestore()
  })

  it('GUI-25: loadSemesters lỗi -> toast.error', async () => {
    ;(semesterService.getAll as Mock).mockRejectedValueOnce(new Error('network'))
    render(
      <ImportFileModal isOpen onClose={vi.fn()} onConfirm={vi.fn()} title="I" showSemesterSelect />,
    )
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Không thể tải danh sách học kỳ')
    })
  })

  it('GUI-26: nhấn Hủy gọi onClose', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    render(<ImportFileModal isOpen onClose={onClose} onConfirm={vi.fn()} title="T" />)
    await user.click(screen.getByRole('button', { name: 'Hủy' }))
    expect(onClose).toHaveBeenCalled()
  })

  it('GUI-27: isLoading hiển thị trạng thái đang xử lý', async () => {
    const user = userEvent.setup()
    render(
      <ImportFileModal
        isOpen
        onClose={vi.fn()}
        onConfirm={vi.fn()}
        title="T"
        accept=".xlsx"
        isLoading
      />,
    )
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    await user.upload(
      input,
      new File(['x'], 'ok.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      }),
    )
    expect(screen.getByText('Đang xử lý...')).toBeInTheDocument()
  })
})

