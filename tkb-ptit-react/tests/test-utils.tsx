import { beforeAll, afterEach, afterAll, vi } from 'vitest'
import { server } from './mocks/server'

// This will be called automatically by Vitest
beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

// Extend Vitest's expect with custom matchers
import { expect } from 'vitest'
import '@testing-library/jest-dom'
