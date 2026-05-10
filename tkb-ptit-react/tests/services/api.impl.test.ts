import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDelete = vi.fn()
const mockPatch = vi.fn()

let requestOnFulfilled: ((c: { headers: Record<string, string> }) => unknown) | undefined
let requestOnRejected: ((e: unknown) => unknown) | undefined
let responseOnFulfilled: ((r: unknown) => unknown) | undefined
let responseOnRejected: ((e: unknown) => unknown) | undefined

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: mockGet,
      post: mockPost,
      put: mockPut,
      delete: mockDelete,
      patch: mockPatch,
      interceptors: {
        request: {
          use: (onFulfilled: typeof requestOnFulfilled, onRejected?: typeof requestOnRejected) => {
            requestOnFulfilled = onFulfilled
            requestOnRejected = onRejected
            return 0
          },
        },
        response: {
          use: (onFulfilled: typeof responseOnFulfilled, onRejected?: typeof responseOnRejected) => {
            responseOnFulfilled = onFulfilled
            responseOnRejected = onRejected
            return 0
          },
        },
      },
    })),
  },
}))

describe('api.ts — instance, interceptors, services', () => {
  const originalLocation = window.location
  const lsMem: Record<string, string> = {}

  beforeEach(async () => {
    vi.resetModules()
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
    mockGet.mockResolvedValue({ data: { success: true, data: [] } })
    mockPost.mockResolvedValue({ data: { success: true } })
    mockPut.mockResolvedValue({ data: { success: true } })
    mockDelete.mockResolvedValue({ data: { success: true } })
    mockPatch.mockResolvedValue({ data: { success: true } })

    Object.defineProperty(window, 'location', {
      configurable: true,
      value: { ...originalLocation, pathname: '/dashboard', href: '' },
    })

    await import('@/services/api')
  })

  afterEach(() => {
    Object.defineProperty(window, 'location', { configurable: true, value: originalLocation })
    vi.unstubAllEnvs()
  })

  it('GUI-66: request interceptor: không gắn Authorization khi không có token', () => {
    const cfg = { headers: {} as Record<string, string> }
    const out = requestOnFulfilled!(cfg)
    expect(out).toBe(cfg)
    expect(cfg.headers.Authorization).toBeUndefined()
  })

  it('GUI-67: request interceptor: gắn Bearer khi có authToken', () => {
    localStorage.setItem('authToken', 'abc')
    const cfg = { headers: {} as Record<string, string> }
    requestOnFulfilled!(cfg)
    expect(cfg.headers.Authorization).toBe('Bearer abc')
  })

  it('GUI-68: request interceptor onRejected: reject error', async () => {
    const err = new Error('req')
    await expect(requestOnRejected!(err)).rejects.toBe(err)
  })

  it('GUI-69: response onFulfilled: trả về response', () => {
    const res = { data: 1 }
    expect(responseOnFulfilled!(res)).toBe(res)
  })

  it('GUI-70: response onRejected: 401 — xóa token và redirect khi không phải auth', async () => {
    localStorage.setItem('authToken', 't')
    localStorage.setItem('user', '{}')
    const removeItem = vi.spyOn(window.localStorage, 'removeItem')
    ;(window.location as { pathname: string; href: string }).pathname = '/dashboard'
    ;(window.location as { href: string }).href = ''

    const err = {
      response: { status: 401 },
      config: { url: '/subjects' },
    }
    await expect(responseOnRejected!(err)).rejects.toBe(err)
    expect(removeItem).toHaveBeenCalledWith('authToken')
    expect(removeItem).toHaveBeenCalledWith('user')
    expect(window.location.href).toBe('/login')
    removeItem.mockRestore()
  })

  it('GUI-71: response onRejected: 401 — không redirect ở /login', async () => {
    ;(window.location as { pathname: string; href: string }).pathname = '/login'
    ;(window.location as { href: string }).href = ''

    const err = { response: { status: 401 }, config: { url: '/subjects' } }
    await expect(responseOnRejected!(err)).rejects.toBe(err)
    expect(window.location.href).not.toBe('/login')
  })

  it('GUI-72: response onRejected: 401 — không redirect khi request /auth/login', async () => {
    ;(window.location as { pathname: string; href: string }).pathname = '/dashboard'
    ;(window.location as { href: string }).href = ''

    const err = { response: { status: 401 }, config: { url: '/api/auth/login' } }
    await expect(responseOnRejected!(err)).rejects.toBe(err)
    expect(window.location.href).not.toBe('/login')
  })

  it('GUI-73: response onRejected: 401 — không redirect khi request /auth/register', async () => {
    const err = { response: { status: 401 }, config: { url: '/auth/register' } }
    await expect(responseOnRejected!(err)).rejects.toBe(err)
  })

  it('GUI-74: response onRejected: không phải 401 — chỉ reject', async () => {
    const err = { response: { status: 500 } }
    await expect(responseOnRejected!(err)).rejects.toBe(err)
  })

  it('GUI-75: subjectService.getAll — gọi đúng query (page 0-based, filters)', async () => {
    const { subjectService } = await import('@/services/api')
    await subjectService.getAll(2, 10, 'x', 'sem', '2024', 'CN', 'CQ', '2024-2025', 'code', 'desc')
    expect(mockGet).toHaveBeenCalled()
    const url = mockGet.mock.calls[0][0] as string
    expect(url).toContain('/subjects?')
    expect(url).toContain('page=1')
    expect(url).toContain('size=10')
    expect(url).toContain('search=x')
    expect(url).toContain('semester=sem')
    expect(url).toContain('classYear=2024')
    expect(url).toContain('majorCode=CN')
    expect(url).toContain('programType=CQ')
    expect(url).toContain('academicYear=2024-2025')
    expect(url).toContain('sortBy=code')
    expect(url).toContain('sortDir=desc')
  })

  it('GUI-76: subjectService.getAll — không append filter rỗng', async () => {
    const { subjectService } = await import('@/services/api')
    await subjectService.getAll()
    const url = mockGet.mock.calls[0][0] as string
    expect(url).toContain('page=0')
    expect(url).not.toContain('search=')
  })

  it('GUI-77: subjectService CRUD và các endpoint', async () => {
    const { subjectService } = await import('@/services/api')
    await subjectService.getById(5)
    expect(mockGet).toHaveBeenCalledWith('/subjects/5')

    await subjectService.getByMajor(3)
    expect(mockGet).toHaveBeenCalledWith('/subjects/major/3')

    await subjectService.search('Toán')
    expect(mockGet).toHaveBeenCalledWith('/subjects/search?name=Toán')

    const body = {
      subjectCode: 'A',
      subjectName: 'B',
      numberOfClasses: 1,
      academicYear: '2024-2025',
      credits: 3,
      theoryHours: 30,
      exerciseHours: 0,
      projectHours: 0,
      labHours: 0,
      selfStudyHours: 0,
      department: 'CNTT',
      examFormat: 'Thi',
      classYear: '2024',
      programType: 'CQ',
      numberOfStudents: 60,
    }
    await subjectService.create(body)
    expect(mockPost).toHaveBeenCalledWith('/subjects', body)

    await subjectService.update(1, body)
    expect(mockPut).toHaveBeenCalledWith('/subjects/1', body)

    await subjectService.delete(9)
    expect(mockDelete).toHaveBeenCalledWith('/subjects/9')

    await subjectService.getGroupMajors('HK1', '2024-2025', '2024', 'CQ')
    expect(mockGet.mock.calls.some((c) => String(c[0]).includes('/subjects/group-majors'))).toBe(true)

    await subjectService.getByMajors('HK1', '2024-2025', '2024', 'CQ', ['CN', 'AT'])
    const majorsUrl = mockGet.mock.calls.find((c) => String(c[0]).includes('/subjects/majors'))?.[0] as string
    expect(majorsUrl).toContain('majorCodes=CN')
    expect(majorsUrl).toContain('majorCodes=AT')

    await subjectService.getAllProgramTypes()
    expect(mockGet).toHaveBeenCalledWith('/subjects/program-types')

    await subjectService.getAllClassYears()
    expect(mockGet).toHaveBeenCalledWith('/subjects/class-years')

    await subjectService.getCommonSubjects('HK1', '2024-2025')
    expect(mockGet.mock.calls.some((c) => String(c[0]).includes('/subjects/common-subjects'))).toBe(true)

    await subjectService.deleteBySemester('HK1')
    expect(mockDelete).toHaveBeenCalledWith('/subjects/semester/HK1')
  })

  it('GUI-78: majorService & facultyService', async () => {
    const { majorService, facultyService } = await import('@/services/api')
    await majorService.getAll()
    expect(mockGet).toHaveBeenCalledWith('/majors')
    await facultyService.getAll()
    expect(mockGet).toHaveBeenCalledWith('/faculties')
  })

  it('GUI-79: semesterService — getAll, getById, getByName, getActive, getAllNames, create, update, delete, setActive', async () => {
    const { semesterService } = await import('@/services/api')
    await semesterService.getAll()
    expect(mockGet).toHaveBeenCalledWith('/semesters')
    await semesterService.getById(2)
    expect(mockGet).toHaveBeenCalledWith('/semesters/2')
    await semesterService.getByName('HK1')
    expect(mockGet).toHaveBeenCalledWith('/semesters/name/HK1')
    await semesterService.getActive()
    expect(mockGet).toHaveBeenCalledWith('/semesters/active')
    await semesterService.getAllNames()
    expect(mockGet).toHaveBeenCalledWith('/semesters/names')

    const req = { semesterName: 'HK1', academicYear: '2024-2025' }
    await semesterService.create(req)
    expect(mockPost).toHaveBeenCalledWith('/semesters', req)
    await semesterService.update(1, req)
    expect(mockPut).toHaveBeenCalledWith('/semesters/1', req)
    await semesterService.delete(3)
    expect(mockDelete).toHaveBeenCalledWith('/semesters/3')
    await semesterService.setActive(4)
    expect(mockPatch).toHaveBeenCalledWith('/semesters/4/activate')

    await semesterService.deleteSubjectsBySemesterName('Học kỳ 1')
    expect(
      mockDelete.mock.calls.some((c) =>
        String(c[0]).startsWith('/subjects/semester-name/'),
      ),
    ).toBe(true)

    await semesterService.deleteSubjectsBySemesterNameAndAcademicYear('HK1', '2024-2025')
    expect(
      mockDelete.mock.calls.some((c) => String(c[0]).includes('/academic-year/')),
    ).toBe(true)
  })

  it('GUI-80: semesterService.getAcademicYears — map unique years khi success', async () => {
    const { semesterService } = await import('@/services/api')
    mockGet.mockResolvedValueOnce({
      data: {
        success: true,
        data: [
          { academicYear: '2024-2025', isActive: true },
          { academicYear: '2024-2025', isActive: false },
          { academicYear: '2023-2024', isActive: false },
        ],
      },
    })
    const result = await semesterService.getAcademicYears()
    expect(result.data).toEqual([
      { year: '2024-2025', isActive: true },
      { year: '2023-2024', isActive: false },
    ])
  })

  it('GUI-81: semesterService.getAcademicYears — trả response.data khi không success', async () => {
    const { semesterService } = await import('@/services/api')
    const payload = { success: false, message: 'x', data: null as null }
    mockGet.mockResolvedValueOnce({ data: payload })
    const result = await semesterService.getAcademicYears()
    expect(result).toEqual(payload)
  })

  it('GUI-82: roomService — tất cả method', async () => {
    const { roomService } = await import('@/services/api')
    await roomService.getAll()
    expect(mockGet).toHaveBeenCalledWith('/rooms')
    await roomService.getById(1)
    expect(mockGet).toHaveBeenCalledWith('/rooms/1')
    const payload = { name: 'A101', building: 'A1', capacity: 50, type: 'GENERAL' }
    await roomService.create(payload)
    expect(mockPost).toHaveBeenCalledWith('/rooms', payload)
    await roomService.update(2, payload)
    expect(mockPut).toHaveBeenCalledWith('/rooms/2', payload)
    await roomService.updateStatus(3, 'OCCUPIED')
    expect(mockPatch).toHaveBeenCalledWith('/rooms/3/status', { status: 'OCCUPIED' })
    await roomService.delete(4)
    expect(mockDelete).toHaveBeenCalledWith('/rooms/4')
    await roomService.getByBuilding('A1')
    expect(mockGet).toHaveBeenCalledWith('/rooms/building/A1')
    await roomService.getByStatus('AVAILABLE')
    expect(mockGet).toHaveBeenCalledWith('/rooms/status/AVAILABLE')
    await roomService.getByType('LAB')
    expect(mockGet).toHaveBeenCalledWith('/rooms/type/LAB')
    await roomService.getAvailable(40)
    expect(mockGet).toHaveBeenCalledWith('/rooms/available?capacity=40')
    await roomService.updateStatusByRoomCodes(['A101'], 'UNAVAILABLE')
    expect(mockPatch).toHaveBeenCalledWith('/rooms/bulk-status', {
      roomCodes: ['A101'],
      status: 'UNAVAILABLE',
    })
    await roomService.updateStatusByRoomIds([1, 2], 'AVAILABLE')
    expect(mockPatch).toHaveBeenCalledWith('/rooms/bulk-status', { roomIds: [1, 2], status: 'AVAILABLE' })
    await roomService.saveResults()
    expect(mockPost).toHaveBeenCalledWith('/rooms/save-results')
    await roomService.getRoomOccupancies(7)
    expect(mockGet).toHaveBeenCalledWith('/room-occupancies/room/7')
  })

  it('GUI-83: curriculumService.importExcel', async () => {
    const { curriculumService } = await import('@/services/api')
    const file = new File(['a'], 't.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    await curriculumService.importExcel(file, 'HK1')
    expect(mockPost).toHaveBeenCalled()
    const [url, fd, opts] = mockPost.mock.calls[0]
    expect(url).toBe('/subjects/upload-excel')
    expect(fd).toBeInstanceOf(FormData)
    expect((opts as { transformRequest?: unknown[] }).transformRequest).toEqual(
      expect.arrayContaining([expect.any(Function)]),
    )
  })

  it('GUI-84: scheduleValidationService', async () => {
    const { scheduleValidationService } = await import('@/services/api')
    const file = new File(['b'], 's.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    await scheduleValidationService.validateFormat(file)
    expect(mockPost).toHaveBeenCalledWith(
      '/schedule-validation/validate-format',
      expect.any(FormData),
      expect.objectContaining({
        transformRequest: expect.arrayContaining([expect.any(Function)]),
      }),
    )
    await scheduleValidationService.analyzeSchedule(file)
    expect(mockPost).toHaveBeenCalledWith(
      '/schedule-validation/analyze',
      expect.any(FormData),
      expect.objectContaining({
        transformRequest: expect.arrayContaining([expect.any(Function)]),
      }),
    )
    await scheduleValidationService.getConflictDetails('room', 'A101', 'T1')
    const conflictCall = mockGet.mock.calls.find((c) => String(c[0]).includes('/schedule-validation/conflicts/room'))
    expect(conflictCall?.[0]).toContain('room=A101')
    expect(conflictCall?.[0]).toContain('teacherId=T1')

    await scheduleValidationService.getConflictDetails('teacher')
    const tCall = mockGet.mock.calls.find((c) => String(c[0]).includes('/schedule-validation/conflicts/teacher'))
    expect(String(tCall?.[0])).toMatch(/\?$/)
  })

  it('GUI-85: userService & tkbService', async () => {
    const { userService, tkbService } = await import('@/services/api')
    await userService.getAll()
    expect(mockGet).toHaveBeenCalledWith('/admin/users')
    await userService.getById(1)
    expect(mockGet).toHaveBeenCalledWith('/admin/users/1')
    await userService.toggleStatus(2, false)
    expect(mockPatch).toHaveBeenCalledWith('/admin/users/2/toggle-status', { enabled: false })
    await userService.delete(3)
    expect(mockDelete).toHaveBeenCalledWith('/admin/users/3')
    await tkbService.resetLastSlotIdx()
    expect(mockPost).toHaveBeenCalledWith('/tkb/reset-last-slot-idx')
  })

  it('GUI-86: default export api gọi được get', async () => {
    const apiDefault = (await import('@/services/api')).default
    await apiDefault.get('/ping')
    expect(mockGet).toHaveBeenCalledWith('/ping')
  })
})

describe('getApiBaseUrl qua import.meta (module riêng)', () => {
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.resetModules()
  })

  it('GUI-87: ưu tiên VITE_API_BASE_URL', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'https://api.example.com')
    vi.resetModules()
    const mod = await import('@/services/api')
    expect(mod.API_BASE_URL).toBe('https://api.example.com')
  })
})
