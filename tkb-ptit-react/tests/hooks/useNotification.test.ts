import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import '@testing-library/jest-dom'
import React, { useEffect } from 'react'
import { useNotification } from '@/hooks/useNotification'

// 👉 nếu bạn chưa export type từ hook thì dùng any (an toàn test)
// 👉 nếu có thì thay bằng: NotificationHook
type NotifyType = ReturnType<typeof useNotification>

const TestComponent = ({
  onMount,
}: {
  onMount?: (notify: NotifyType) => void
}) => {
  const notify = useNotification()

  // ✅ tránh re-render loop
  useEffect(() => {
    if (onMount) onMount(notify)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return React.createElement(
    'div',
    null,
    React.createElement('button', { onClick: () => notify.success('Success!') }, 'Success'),
    React.createElement('button', { onClick: () => notify.error('Error!') }, 'Error'),
    React.createElement('button', { onClick: () => notify.warning('Warning!') }, 'Warning'),
    React.createElement('button', { onClick: () => notify.info('Info!') }, 'Info'),
    React.createElement('button', { onClick: () => notify.close() }, 'Close'),
    React.createElement('div', { 'data-testid': 'notification-open' }, notify.notification?.isOpen ? 'open' : 'closed')
  )
}

describe('useNotification - Dùng chung/Infrastructure', () => {
  it('GUI-48: initializes with closed state', () => {
    render(React.createElement(TestComponent))
    expect(screen.getByTestId('notification-open')).toHaveTextContent('closed')
  })

  it('GUI-49: opens notification on success call', async () => {
    render(React.createElement(TestComponent))

    fireEvent.click(screen.getByRole('button', { name: 'Success' }))

    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('open')
    })
  })

  it('GUI-50: opens notification on error call', async () => {
    render(React.createElement(TestComponent))

    fireEvent.click(screen.getByRole('button', { name: 'Error' }))

    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('open')
    })
  })

  it('GUI-51: opens notification on warning call', async () => {
    render(React.createElement(TestComponent))

    fireEvent.click(screen.getByRole('button', { name: 'Warning' }))

    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('open')
    })
  })

  it('GUI-52: opens notification on info call', async () => {
    render(React.createElement(TestComponent))

    fireEvent.click(screen.getByRole('button', { name: 'Info' }))

    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('open')
    })
  })

  it('GUI-53: closes notification on close call', async () => {
    render(React.createElement(TestComponent))

    // mở trước
    fireEvent.click(screen.getByRole('button', { name: 'Success' }))

    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('open')
    })

    // đóng
    fireEvent.click(screen.getByRole('button', { name: 'Close' }))

    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('closed')
    })
  })
})