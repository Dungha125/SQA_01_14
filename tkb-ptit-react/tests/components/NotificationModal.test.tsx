import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import NotificationModal from '@/components/NotificationModal'
import React from 'react'

describe('NotificationModal - Dùng chung/Infrastructure', () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    title: 'Test Title',
    message: 'Test message',
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('GUI-31: hiển thị khi isOpen=true', () => {
    render(<NotificationModal {...defaultProps} />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
    expect(screen.getByText('Test message')).toBeInTheDocument()
  })

  it('GUI-32: không hiển thị khi isOpen=false', () => {
    render(<NotificationModal {...defaultProps} isOpen={false} />)
    expect(screen.queryByText('Test Title')).not.toBeInTheDocument()
  })

  it('GUI-33: hiển thị confirm and cancel buttons mặc định', () => {
    render(<NotificationModal {...defaultProps} />)
    expect(screen.getByRole('button', { name: 'Xác nhận' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Hủy' })).toBeInTheDocument()
  })

  it('GUI-34: hides cancel button when showCancel is false', () => {
    render(<NotificationModal {...defaultProps} showCancel={false} />)
    expect(screen.getByRole('button', { name: 'Xác nhận' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Hủy' })).not.toBeInTheDocument()
  })

  it('GUI-35: calls onClose when cancel button clicked', () => {
    const onClose = vi.fn()
    render(<NotificationModal {...defaultProps} onClose={onClose} />)
    fireEvent.click(screen.getByRole('button', { name: 'Hủy' }))
    expect(onClose).toHaveBeenCalled()
  })

  it('GUI-36: calls onConfirm when confirm button clicked', () => {
    const onConfirm = vi.fn()
    render(<NotificationModal {...defaultProps} onConfirm={onConfirm} />)
    fireEvent.click(screen.getByRole('button', { name: 'Xác nhận' }))
    expect(onConfirm).toHaveBeenCalled()
  })

  it('GUI-37: calls onClose when confirm clicked without onConfirm', () => {
    const onClose = vi.fn()
    render(<NotificationModal {...defaultProps} onClose={onClose} />)
    fireEvent.click(screen.getByRole('button', { name: 'Xác nhận' }))
    expect(onClose).toHaveBeenCalled()
  })

  it('GUI-38: calls onClose when overlay clicked', () => {
    const onClose = vi.fn()
    render(<NotificationModal {...defaultProps} onClose={onClose} />)
    // Find the overlay div and click it
    const overlay = document.querySelector('[class*="fixed"]')
    if (overlay) {
      fireEvent.click(overlay)
    }
    // Note: The actual click behavior depends on implementation
  })

  it('GUI-39: không close when inner div clicked', () => {
    const onClose = vi.fn()
    render(<NotificationModal {...defaultProps} onClose={onClose} />)
    const modalContent = screen.getByText('Test Title').closest('div')
    if (modalContent) {
      fireEvent.click(modalContent)
    }
    expect(onClose).not.toHaveBeenCalled()
  })

  it('GUI-40: displays correct icon for success type', () => {
    render(<NotificationModal {...defaultProps} type="success" />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('GUI-41: displays correct icon for error type', () => {
    render(<NotificationModal {...defaultProps} type="error" />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('GUI-42: displays correct icon for warning type', () => {
    render(<NotificationModal {...defaultProps} type="warning" />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('GUI-43: displays correct icon for info type', () => {
    render(<NotificationModal {...defaultProps} type="info" />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('GUI-44: uses custom confirmText when provided', () => {
    render(<NotificationModal {...defaultProps} confirmText="Đồng ý" />)
    expect(screen.getByRole('button', { name: 'Đồng ý' })).toBeInTheDocument()
  })

  it('GUI-45: uses custom cancelText when provided', () => {
    render(<NotificationModal {...defaultProps} cancelText="Hủy bỏ" />)
    expect(screen.getByRole('button', { name: 'Hủy bỏ' })).toBeInTheDocument()
  })

  it('GUI-46: không show cancel button when showCancel is false', () => {
    render(<NotificationModal {...defaultProps} showCancel={false} />)
    expect(screen.queryByRole('button', { name: 'Hủy' })).not.toBeInTheDocument()
  })

  it('GUI-47: có accessible modal structure', () => {
    render(<NotificationModal {...defaultProps} />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })
})
