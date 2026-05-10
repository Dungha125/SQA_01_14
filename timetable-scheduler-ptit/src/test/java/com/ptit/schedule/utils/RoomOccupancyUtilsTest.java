package com.ptit.schedule.utils;

import com.ptit.schedule.entity.Room;
import com.ptit.schedule.entity.RoomType;
import com.ptit.schedule.entity.RoomStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test tat ca static methods cua RoomOccupancyUtils
 */
public class RoomOccupancyUtilsTest {

    private Room makeRoom(String name, String building) {
        Room r = new Room();
        r.setName(name);
        r.setBuilding(building);
        r.setCapacity(50);
        r.setType(RoomType.GENERAL);
        r.setStatus(RoomStatus.AVAILABLE);
        return r;
    }

    @Test
    void testBuildUniqueKey_FromStringCode() {
        // PH-57: buildUniqueKey(roomCode, dayOfWeek, period)
        String key = RoomOccupancyUtils.buildUniqueKey("404-A2", 5, 1);
        assertEquals("404-A2|5|1", key);
    }

    @Test
    void testBuildUniqueKey_FromRoom() {
        // PH-58: buildUniqueKey(Room, dayOfWeek, period)
        Room room = makeRoom("404", "A2");
        String key = RoomOccupancyUtils.buildUniqueKey(room, 3, 2);
        assertEquals("404-A2|3|2", key);
    }

    @Test
    void testBuildRoomCode_FromRoom() {
        // PH-59: buildRoomCode(Room)
        Room room = makeRoom("101", "B1");
        String code = RoomOccupancyUtils.buildRoomCode(room);
        assertEquals("101-B1", code);
    }

    @Test
    void testBuildRoomCode_FromStrings() {
        // PH-60: buildRoomCode(name, building)
        String code = RoomOccupancyUtils.buildRoomCode("202", "NT");
        assertEquals("202-NT", code);
    }

    @Test
    void testParseUniqueKey_Valid() {
        // PH-61: parseUniqueKey hop le
        String[] parts = RoomOccupancyUtils.parseUniqueKey("404-A2|5|1");
        assertNotNull(parts);
        assertEquals(3, parts.length);
        assertEquals("404-A2", parts[0]);
        assertEquals("5", parts[1]);
        assertEquals("1", parts[2]);
    }

    @Test
    void testParseUniqueKey_Null() {
        // PH-62: parseUniqueKey voi null
        assertNull(RoomOccupancyUtils.parseUniqueKey(null));
    }

    @Test
    void testParseUniqueKey_Empty() {
        // PH-63: parseUniqueKey voi chuoi rong
        assertNull(RoomOccupancyUtils.parseUniqueKey(""));
    }

    @Test
    void testParseUniqueKey_InvalidFormat() {
        // PH-64: parseUniqueKey voi format sai
        assertNull(RoomOccupancyUtils.parseUniqueKey("404-A2|5"));
    }

    @Test
    void testExtractRoomCode_Valid() {
        // PH-65: extractRoomCode tu unique key hop le
        String roomCode = RoomOccupancyUtils.extractRoomCode("404-A2|5|1");
        assertEquals("404-A2", roomCode);
    }

    @Test
    void testExtractRoomCode_Invalid() {
        // PH-66: extractRoomCode voi key khong hop le
        assertNull(RoomOccupancyUtils.extractRoomCode("invalid"));
    }

    @Test
    void testExtractDayOfWeek_Valid() {
        // PH-67: extractDayOfWeek hop le
        Integer day = RoomOccupancyUtils.extractDayOfWeek("404-A2|5|1");
        assertEquals(5, day);
    }

    @Test
    void testExtractDayOfWeek_Invalid() {
        // PH-68: extractDayOfWeek voi key null
        assertNull(RoomOccupancyUtils.extractDayOfWeek(null));
    }

    @Test
    void testExtractDayOfWeek_NonNumeric() {
        // PH-69: extractDayOfWeek voi gia tri khong phai so
        assertNull(RoomOccupancyUtils.extractDayOfWeek("404-A2|abc|1"));
    }

    @Test
    void testExtractPeriod_Valid() {
        // PH-70: extractPeriod hop le
        Integer period = RoomOccupancyUtils.extractPeriod("404-A2|5|3");
        assertEquals(3, period);
    }

    @Test
    void testExtractPeriod_Invalid() {
        // PH-71: extractPeriod voi key null
        assertNull(RoomOccupancyUtils.extractPeriod(null));
    }

    @Test
    void testExtractPeriod_NonNumeric() {
        // PH-72: extractPeriod voi gia tri khong phai so
        assertNull(RoomOccupancyUtils.extractPeriod("404-A2|5|xyz"));
    }

    @Test
    void testParseRoomCode_Valid() {
        // PH-73: parseRoomCode hop le
        String[] parts = RoomOccupancyUtils.parseRoomCode("404-A2");
        assertNotNull(parts);
        assertEquals("404", parts[0]);
        assertEquals("A2", parts[1]);
    }

    @Test
    void testParseRoomCode_Null() {
        // PH-74: parseRoomCode null
        assertNull(RoomOccupancyUtils.parseRoomCode(null));
    }

    @Test
    void testParseRoomCode_Empty() {
        // PH-75: parseRoomCode empty
        assertNull(RoomOccupancyUtils.parseRoomCode(""));
    }

    @Test
    void testParseRoomCode_InvalidFormat() {
        // PH-76: parseRoomCode chi 1 phan (khong co dau gach)
        assertNull(RoomOccupancyUtils.parseRoomCode("404A2"));
    }

    @Test
    void testExtractRoomName_Valid() {
        // PH-77: extractRoomName tu room code
        assertEquals("404", RoomOccupancyUtils.extractRoomName("404-A2"));
    }

    @Test
    void testExtractRoomName_Invalid() {
        // PH-78: extractRoomName tu code sai
        assertNull(RoomOccupancyUtils.extractRoomName("invalid"));
    }

    @Test
    void testExtractBuilding_Valid() {
        // PH-79: extractBuilding tu room code
        assertEquals("A2", RoomOccupancyUtils.extractBuilding("404-A2"));
    }

    @Test
    void testExtractBuilding_Invalid() {
        // PH-80: extractBuilding tu code sai
        assertNull(RoomOccupancyUtils.extractBuilding("invalid"));
    }

    @Test
    void testIsValidUniqueKey_Valid() {
        // PH-81: isValidUniqueKey voi key hop le
        assertTrue(RoomOccupancyUtils.isValidUniqueKey("404-A2|2|1"));
        assertTrue(RoomOccupancyUtils.isValidUniqueKey("404-A2|7|6"));
    }

    @Test
    void testIsValidUniqueKey_Null() {
        // PH-82: isValidUniqueKey null
        assertFalse(RoomOccupancyUtils.isValidUniqueKey(null));
    }

    @Test
    void testIsValidUniqueKey_DayOutOfRange() {
        // PH-83: isValidUniqueKey voi day ngoai range
        assertFalse(RoomOccupancyUtils.isValidUniqueKey("404-A2|1|1"));
        assertFalse(RoomOccupancyUtils.isValidUniqueKey("404-A2|8|1"));
    }

    @Test
    void testIsValidUniqueKey_PeriodOutOfRange() {
        // PH-84: isValidUniqueKey voi period ngoai range
        assertFalse(RoomOccupancyUtils.isValidUniqueKey("404-A2|3|0"));
        assertFalse(RoomOccupancyUtils.isValidUniqueKey("404-A2|3|7"));
    }

    @Test
    void testIsValidUniqueKey_NonNumeric() {
        // PH-85: isValidUniqueKey voi gia tri khong phai so
        assertFalse(RoomOccupancyUtils.isValidUniqueKey("404-A2|abc|xyz"));
    }
}
