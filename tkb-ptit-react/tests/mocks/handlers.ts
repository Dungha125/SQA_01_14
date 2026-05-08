import { http, HttpResponse } from 'msw'

// Mock data
export const mockUser = {
  id: 1,
  username: 'admin',
  email: 'admin@ptit.edu.vn',
  fullName: 'Administrator',
  role: 'ADMIN',
  enabled: true,
  createdAt: '2025-01-01T00:00:00Z',
}

export const mockUsers = [
  mockUser,
  {
    id: 2,
    username: 'user1',
    email: 'user1@ptit.edu.vn',
    fullName: 'Nguyễn Văn A',
    role: 'USER',
    enabled: true,
    createdAt: '2025-02-01T00:00:00Z',
  },
]

export const mockSemester = {
  id: 1,
  semesterName: 'Học kỳ 1',
  academicYear: '2024-2025',
  startDate: '2024-09-01',
  endDate: '2025-01-31',
  isActive: true,
  description: 'Học kỳ 1 năm 2024-2025',
  subjectCount: 5,
}

export const mockSubject = {
  id: 1,
  subjectCode: 'CS101',
  subjectName: 'Lập trình cơ bản',
  credits: 3,
  theoryHours: 30,
  exerciseHours: 15,
  projectHours: 0,
  labHours: 0,
  selfStudyHours: 45,
  examFormat: 'Tự luận',
  classYear: '2024',
  programType: 'Chính quy',
  numberOfStudents: 60,
  numberOfClasses: 2,
  department: 'CNTT',
  studentsPerClass: 30,
  majorId: 1,
  majorCode: 'CN',
  majorName: 'Công nghệ thông tin',
  facultyId: 'K15',
  facultyName: 'Khoa CNTT',
  major: {
    id: 1,
    majorCode: 'CN',
    majorName: 'Công nghệ thông tin',
  },
}

export const mockRoom = {
  id: 1,
  roomCode: 'A101',
  building: 'A1',
  capacity: 60,
  roomType: 'GENERAL',
  status: 'AVAILABLE',
  floor: 1,
}

export const mockMajor = {
  id: 1,
  majorCode: 'CN',
  majorName: 'Công nghệ thông tin',
}

export const mockRoomOccupancy = {
  id: 1,
  roomId: 1,
  roomName: 'A101',
  building: 'A1',
  dayOfWeek: 2,
  period: 1,
  occupancyStatus: 'AVAILABLE',
}

// Generic success response
const successResponse = (data: any) =>
  HttpResponse.json({ success: true, data })

// Auth endpoints
export const authHandlers = [
  http.post('/api/auth/login', () =>
    successResponse({
      token: 'mock-jwt-token',
      id: mockUser.id,
      username: mockUser.username,
      email: mockUser.email,
      fullName: mockUser.fullName,
      role: mockUser.role,
    })
  ),
  http.post('/api/auth/register', () =>
    successResponse({ message: 'Đăng ký thành công' })
  ),
]

// Users endpoints
export const usersHandlers = [
  http.get('/api/admin/users', () =>
    successResponse(mockUsers.filter(u => u.role !== 'ADMIN'))
  ),
  http.patch('/api/admin/users/:id/toggle-status', () =>
    successResponse(mockUser)
  ),
  http.delete('/api/admin/users/:id', () =>
    HttpResponse.json({ success: true })
  ),
]

// Subjects endpoints
export const subjectsHandlers = [
  http.get('/api/subjects', () =>
    successResponse({
      items: [mockSubject],
      totalElements: 1,
      totalPages: 1,
    })
  ),
  http.post('/api/subjects', () => successResponse(mockSubject)),
  http.put('/api/subjects/:id', () => successResponse(mockSubject)),
  http.delete('/api/subjects/:id', () => HttpResponse.json({ success: true })),
  http.get('/api/subjects/program-types', () => successResponse(['Chính quy', 'Đặc thù'])),
  http.get('/api/subjects/class-years', () => successResponse(['2024', '2023'])),
  http.get('/api/majors', () => successResponse([
    { id: 1, majorCode: 'CN', majorName: 'Công nghệ thông tin' },
  ])),
  http.get('/api/subjects/group-majors', () => successResponse([['CN', 'AT', 'KH']])),
  http.get('/api/subjects/majors', () => successResponse([mockSubject])),
  http.get('/api/subjects/common-subjects', () => successResponse([mockSubject])),
  http.delete('/api/subjects/semester-name/:semester/academic-year/:year', () =>
    HttpResponse.json({ success: true, data: 5 })
  ),
]

// Semesters endpoints
export const semestersHandlers = [
  http.get('/api/semesters', () =>
    successResponse([mockSemester])
  ),
  http.get('/api/semesters/active', () => successResponse(mockSemester)),
  http.post('/api/semesters', () => successResponse(mockSemester)),
  http.put('/api/semesters/:id', () => successResponse(mockSemester)),
  http.delete('/api/semesters/:id', () => HttpResponse.json({ success: true })),
  http.patch('/api/semesters/:id/activate', () => successResponse(mockSemester)),
]

// Rooms endpoints
export const roomsHandlers = [
  http.get('/api/rooms', () =>
    successResponse([mockRoom])
  ),
  http.post('/api/rooms', () => successResponse(mockRoom)),
  http.put('/api/rooms/:id', () => successResponse(mockRoom)),
  http.patch('/api/rooms/:id/status', () => successResponse(mockRoom)),
  http.delete('/api/rooms/:id', () => HttpResponse.json({ success: true })),
  http.get('/api/v1/room-occupancies/rooms-status/semester/:id', () =>
    HttpResponse.json({
      content: [
        {
          id: 1,
          name: 'A101',
          building: 'A1',
          capacity: 60,
          type: 'GENERAL',
          typeDisplayName: 'Phòng thường',
          occupancyStatus: 'AVAILABLE',
          totalOccupiedSlots: 2,
          totalAvailableSlots: 46,
          occupancyRate: 4.17,
        },
      ],
      total: 1,
    })
  ),
  http.get('/api/v1/room-occupancies/room/:id', () =>
    HttpResponse.json({
      content: [
        { id: 1, dayOfWeek: 2, period: 1, dayOfWeekName: 'Thứ 2', periodName: 'Kíp 1' },
      ],
      total: 1,
    })
  ),
  http.delete('/api/v1/room-occupancies/semester/:id', () =>
    HttpResponse.json({ success: true })
  ),
  http.patch('/api/rooms/bulk-status', () =>
    HttpResponse.json({ success: true, data: 5 })
  ),
]

// Schedule endpoints
export const schedulesHandlers = [
  http.get('/api/schedules', () =>
    HttpResponse.json([
      {
        id: 1,
        classNumber: 1,
        studentYear: '2024',
        major: 'CN',
        specialSystem: 'Chính quy',
        siSoMotLop: 30,
        room: mockRoom,
        subject: {
          ...mockSubject,
          semester: mockSemester,
          major: { id: 1, majorCode: 'CN', majorName: 'CNTT' },
        },
        tkbTemplate: {
          dayOfWeek: 2,
          kip: 1,
          startPeriod: 1,
          periodLength: 3,
          weekSchedule: JSON.stringify([1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]),
          totalUsed: 2,
        },
      },
    ])
  ),
  http.post('/api/schedules/generate-batch', () =>
    HttpResponse.json({
      items: [
        {
          input: { ma_mon: 'CS101', ten_mon: 'Lập trình', nganh: 'CN', sotiet: 45, siso: 60 },
          rows: [
            { lop: 1, ma_mon: 'CS101', ten_mon: 'Lập trình', thu: 2, kip: 1, tiet_bd: 1, phong: 'A101', o_to_AG: ['x', 'x', 'x'] },
          ],
        },
      ],
      totalClasses: 1,
    })
  ),
  http.post('/api/schedules/save-batch', () =>
    HttpResponse.json({ success: true })
  ),
  http.post('/api/schedules/import-data', () =>
    successResponse({ semester: 'Học kỳ 1', academicYear: '2024-2025' })
  ),
  http.delete('/api/schedules/:id', () => HttpResponse.json({ success: true })),
  http.delete('/api/schedules/reset-last-slot-idx-redis', () =>
    HttpResponse.json({ success: true })
  ),
  http.post('/api/rooms/assign-rooms', () =>
    HttpResponse.json({
      success: true,
      data: {
        items: [
          {
            rows: [
              { lop: 1, ma_mon: 'CS101', ten_mon: 'Lập trình', thu: 2, kip: 1, tiet_bd: 1, phong: 'A101' },
            ],
          },
        ],
        warningsNoRoom: [],
      },
    })
  ),
  http.delete('/api/room-occupancies/semester/:id', () =>
    HttpResponse.json({ success: true })
  ),
]

// Curriculum import
export const curriculumHandlers = [
  http.post('/api/subjects/upload-excel', () =>
    successResponse({
      successCount: 10,
      skippedCount: 2,
      totalRows: 12,
      warnings: ['Môn trùng lặp: CS101'],
      validSubjects: [mockSubject],
    })
  ),
]

// Schedule validation
export const validationHandlers = [
  http.post('/api/schedule-validation/validate-format', () =>
    successResponse(true)
  ),
  http.post('/api/schedule-validation/analyze', () =>
    successResponse({
      conflictResult: {
        roomConflicts: [],
        teacherConflicts: [],
        totalConflicts: 0,
      },
      scheduleEntries: [],
      fileName: 'test.xlsx',
      totalEntries: 10,
      fileSize: 1024,
      hasConflicts: false,
      roomConflictCount: 0,
      teacherConflictCount: 0,
      formattedFileSize: '1 KB',
    })
  ),
]

// Combine all handlers
export const handlers = [
  ...authHandlers,
  ...usersHandlers,
  ...subjectsHandlers,
  ...semestersHandlers,
  ...roomsHandlers,
  ...schedulesHandlers,
  ...curriculumHandlers,
  ...validationHandlers,
]

// Export for use in tests
export const mockApi = {
  user: mockUser,
  users: mockUsers,
  semester: mockSemester,
  subject: mockSubject,
  room: mockRoom,
}
