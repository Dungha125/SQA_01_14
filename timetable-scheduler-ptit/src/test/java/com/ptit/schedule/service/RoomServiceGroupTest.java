package com.ptit.schedule.service;

import com.ptit.schedule.repository.RoomRepository;
import com.ptit.schedule.repository.RoomOccupancyRepository;
import com.ptit.schedule.service.impl.RoomServiceImpl;
import com.ptit.schedule.service.impl.RoomOccupancyServiceImpl;
import com.ptit.schedule.entity.Room;
import com.ptit.schedule.entity.RoomStatus;
import com.ptit.schedule.entity.RoomType;
import com.ptit.schedule.dto.RoomResponse;
import com.ptit.schedule.dto.RoomRequest;
import com.ptit.schedule.dto.RoomStatusUpdateRequest;
import com.ptit.schedule.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Gộp các test Service và ServiceImpl thuộc module Phòng Học.
 */
@ExtendWith(MockitoExtension.class)
public class RoomServiceGroupTest {

    @Mock private RoomRepository roomRepository;
    @InjectMocks private RoomServiceImpl roomService;

    @Mock private RoomOccupancyRepository roomOccupancyRepository;
    @InjectMocks private RoomOccupancyServiceImpl roomOccupancyService;

    @Mock private SubjectRoomMappingService mappingService;
    @Mock private MajorBuildingPreferenceService majorPreferenceService;
    @Mock private DataLoaderService dataLoaderService;

    // Helper method tạo mock Room đầy đủ các trường thiết yếu để không bị NPE khi map.
    private Room createValidMockRoom(Long id, String name, String building) {
        Room r = new Room();
        r.setId(id);
        r.setName(name);
        r.setCapacity(50);
        r.setBuilding(building);
        r.setType(RoomType.GENERAL);
        r.setStatus(RoomStatus.AVAILABLE);
        return r;
    }

    @Test
    void testRoomService_Init() {
        // PH-39: Mục đích: Test khởi tạo RoomService
        assertNotNull(roomService);
    }

    @Test
    void testRoomService_FindAll() {
        // PH-67: Mục đích: Test lấy toàn bộ danh sách phòng.
        Room r = createValidMockRoom(1L, "101", "A1");
        lenient().when(roomRepository.findAll()).thenReturn(Collections.singletonList(r));
        List<RoomResponse> result = roomService.getAllRooms();
        assertEquals(1, result.size());
        assertEquals("101", result.get(0).getName());
    }

    @Test
    void testRoomService_GetRoomById_Found() {
        // PH-40: Mục đích: Test trả về theo ID cho RoomService.
        Room r = createValidMockRoom(10L, "X1", "A3");
        lenient().when(roomRepository.findById(10L)).thenReturn(Optional.of(r));
        RoomResponse resp = roomService.getRoomById(10L);
        assertEquals("X1", resp.getName());
        assertEquals("A3", resp.getBuilding());
    }

    @Test
    void testRoomService_GetRoomById_NotFound() {
        // PH-35: Mục đích: Không tìm thấy Room theo ID thì ném Exception.
        lenient().when(roomRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomById(99L));
    }

    @Test
    void testRoomService_CreateRoom_Success() {
        // PH-36: Mục đích: Tạo phòng mới thành công.
        RoomRequest req = new RoomRequest(); 
        req.setName("202"); req.setBuilding("A2"); req.setCapacity(60); req.setType(RoomType.CLC);
        
        lenient().when(roomRepository.findByNameAndBuilding("202", "A2")).thenReturn(Optional.empty());
        
        Room savedRoom = createValidMockRoom(5L, "202", "A2");
        savedRoom.setType(RoomType.CLC);
        lenient().when(roomRepository.save(any())).thenReturn(savedRoom);
        
        RoomResponse resp = roomService.createRoom(req);
        assertNotNull(resp);
        assertEquals("202", resp.getName());
    }

    @Test
    void testRoomService_CreateRoom_Duplicate() {
        // PH-37: Mục đích: Báo lỗi khi tạo phòng trùng Name & Building.
        RoomRequest req = new RoomRequest(); req.setName("101"); req.setBuilding("A1");
        Room existing = createValidMockRoom(1L, "101", "A1");
        lenient().when(roomRepository.findByNameAndBuilding("101", "A1")).thenReturn(Optional.of(existing));
        
        assertThrows(RuntimeException.class, () -> roomService.createRoom(req));
    }

    @Test
    void testRoomService_UpdateRoomStatus() {
        // PH-41: Mục đích: Cập nhật Status cho 1 phòng xác định.
        Room r = createValidMockRoom(2L, "105", "A1");
        RoomStatusUpdateRequest req = new RoomStatusUpdateRequest(); req.setStatus(RoomStatus.UNAVAILABLE);
        
        lenient().when(roomRepository.findById(2L)).thenReturn(Optional.of(r));
        lenient().when(roomRepository.save(any())).thenReturn(r);
        
        RoomResponse resp = roomService.updateRoomStatus(2L, req);
        assertNotNull(resp);
    }

    @Test
    void testRoomService_DeleteRoom_Success() {
        // PH-38: Mục đích: Xóa phòng thành công sau khi check tồn tại.
        lenient().when(roomRepository.existsById(3L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(3L);
        roomService.deleteRoom(3L);
        verify(roomRepository, times(1)).deleteById(3L);
    }

    @Test
    void testRoomService_DeleteRoom_Fail() {
        // PH-42: Mục đích: Xóa phòng thất bại do id sai.
        lenient().when(roomRepository.existsById(99L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> roomService.deleteRoom(99L));
    }

    @Test
    void testRoomService_GetByBuilding() {
        // PH-43: Mục đích: Lấy danh sách qua Building.
        Room r = createValidMockRoom(4L, "B1", "NT");
        lenient().when(roomRepository.findByDay("NT")).thenReturn(Collections.singletonList(r));
        List<RoomResponse> lst = roomService.getRoomsByBuilding("NT");
        assertEquals(1, lst.size());
        assertEquals("B1", lst.get(0).getName());
    }

    @Test
    void testRoomService_GetByStatus() {
        // PH-44: Mục đích: Lấy danh sách qua Status.
        Room r = createValidMockRoom(5L, "C1", "A3");
        r.setStatus(RoomStatus.UNAVAILABLE);
        lenient().when(roomRepository.findByStatus(RoomStatus.UNAVAILABLE)).thenReturn(Collections.singletonList(r));
        List<RoomResponse> lst = roomService.getRoomsByStatus(RoomStatus.UNAVAILABLE);
        assertEquals(1, lst.size());
    }

    // ==========================================
    // NHÓM TEST CASE PHÁT HIỆN LỖI BLACK-BOX TỪ USER
    // ==========================================

    @Test
    void testRoomService_SearchByCode_BlackBoxBug() {
        // PH-45: Mục đích: Phát hiện lỗi tìm kiếm theo tên phòng.
        // Đầu vào: Tìm tên phòng chứa từ khóa "1".
        // Kỳ vọng (Expected): Trả về phòng "101", KHÔNG trả về phòng "202" dù nằm ở toà "A1" (do bộ lọc rò rỉ search sang cả tên toà nhà).
        // Fake logic db mock
        Room r1 = createValidMockRoom(1L, "101", "A1");
        Room r2 = createValidMockRoom(2L, "202", "A1");
        
        // Mô phỏng việc Repository trả về sai logic do lỗi LIKE %1% ở cả 2 field Name và Building
        lenient().when(roomRepository.findByDay("1")).thenReturn(Arrays.asList(r1, r2)); // giả sử service dùng hàm này
        
        // Kịch bản TDD: Test sẽ FAIL nếu response chứa phần tử r2 (room 202). 
        // Khi fix xong code thực (ví dụ sửa lại @Query của Repo chỉ check Name), test này sẽ Pass.
        List<RoomResponse> resp = roomService.getRoomsByBuilding("1"); // Hoặc hàm API search tương ứng
        boolean chuaPhongSai = resp.stream().anyMatch(r -> r.getName().equals("202"));
        assertFalse(chuaPhongSai, "Lỗi Black-box: Tìm phòng '1' nhưng kết quả trả về chứa phòng '202' do dính toà 'A1'");
    }

    @Test
    void testRoomService_CreateRoom_CapacityBoundaryBug() {
        // PH-46: Mục đích: Bắt lỗi nhập số tầng/sức chứa vượt mức tưởng tượng (vd: 1000 tầng, 5000 người).
        // Kỳ vọng (Expected): @Valid, @Max(30), hoặc Service logic chặn lại và ném Exception.
        // Thực tế: Lỗi vì hiện tại class RoomRequest thiếu ràng buộc, cho phép pass giá trị 1000 bình thường.
        RoomRequest req = new RoomRequest(); 
        req.setName("303"); 
        req.setBuilding("A1"); 
        req.setCapacity(9999); // Sức chứa ảo
        req.setType(RoomType.GENERAL);
        
        Room mockSaved = createValidMockRoom(50L, "303", "A1");
        lenient().when(roomRepository.save(any())).thenReturn(mockSaved);
        
        // Kịch bản TDD: Chúng ta kỳ vọng nó NÊN ném Exception cản lại tạo ảo, 
        // nếu chạy tạo thành công (không ném Exception) có nghĩa là BUG VẪN TỒN TẠI.
        assertThrows(IllegalArgumentException.class, () -> roomService.createRoom(req),
            "Lỗi Black-box: Hệ thống cho phép sức chứa và tầng số lượng lớn ảo (1000+) mà không ràng buộc Constraint.");
    }
}
