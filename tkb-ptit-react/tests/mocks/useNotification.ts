import { vi } from 'vitest'

export const createUseNotificationMock = () => ({
  notify: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
  notification: { isOpen: false },
  close: vi.fn(),
})
