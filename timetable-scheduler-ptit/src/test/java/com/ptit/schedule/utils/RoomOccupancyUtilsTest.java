package com.ptit.schedule.utils;

import com.ptit.schedule.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.assertj.core.api.Assertions.assertThat;

class RoomOccupancyUtilsTest {

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("\n=== TEST: " + testInfo.getDisplayName() + " ===");
    }

    @Test
    @DisplayName("LL-128: Tạo unique key đúng định dạng")
    void buildUniqueKey_shouldConstructCorrectKey() {
        String roomCode = "404-A2";
        int dayOfWeek = 5;
        int period = 1;
        String result = RoomOccupancyUtils.buildUniqueKey(roomCode, dayOfWeek, period);
        System.out.println("INPUT: roomCode=" + roomCode + ", dayOfWeek=" + dayOfWeek + ", period=" + period);
        System.out.println("OUTPUT: uniqueKey=" + result);
        System.out.println("EXPECTED: 404-A2|5|1");
        assertThat(result).isEqualTo("404-A2|5|1");
    }

    @Test
    @DisplayName("LL-129: Tạo unique key với nhiều giá trị khác nhau")
    void buildUniqueKey_shouldHandleDifferentValues() {
        String result1 = RoomOccupancyUtils.buildUniqueKey("401-A1", 2, 3);
        assertThat(result1).isEqualTo("401-A1|2|3");

        String result2 = RoomOccupancyUtils.buildUniqueKey("B202-B3", 7, 6);
        System.out.println("INPUT: keys=(401-A1,2,3) & (B202-B3,7,6)");
        System.out.println("OUTPUT: result1=" + result1 + ", result2=" + result2);
        assertThat(result2).isEqualTo("B202-B3|7|6");
    }

    @Test
    @DisplayName("LL-130: Tạo unique key từ đối tượng phòng")
    void buildUniqueKeyFromRoom_shouldBuildCorrectKey() {
        Room room = Room.builder()
                .name("404")
                .building("A2")
                .id(1L)
                .build();

        String result = RoomOccupancyUtils.buildUniqueKey(room, 5, 1);
        System.out.println("INPUT: room=" + room.getName() + "-" + room.getBuilding() + ", dayOfWeek=5, period=1");
        System.out.println("OUTPUT: uniqueKey=" + result);
        System.out.println("EXPECTED: 404-A2|5|1");
        assertThat(result).isEqualTo("404-A2|5|1");
    }

    @Test
    @DisplayName("LL-131: Tạo mã phòng từ tên + tòa")
    void buildRoomCode_shouldConstructCorrectCode() {
        String name = "404";
        String building = "A2";
        String result = RoomOccupancyUtils.buildRoomCode(name, building);
        System.out.println("INPUT: name=" + name + ", building=" + building);
        System.out.println("OUTPUT: roomCode=" + result);
        System.out.println("EXPECTED: 404-A2");
        assertThat(result).isEqualTo("404-A2");
    }

    @Test
    @DisplayName("LL-132: Tạo mã phòng từ đối tượng phòng")
    void buildRoomCodeFromRoom_shouldConstructCorrectCode() {
        Room room = Room.builder()
                .name("401")
                .building("A1")
                .build();

        String result = RoomOccupancyUtils.buildRoomCode(room);
        System.out.println("INPUT: room=" + room.getName() + "-" + room.getBuilding());
        System.out.println("OUTPUT: roomCode=" + result);
        System.out.println("EXPECTED: 401-A1");
        assertThat(result).isEqualTo("401-A1");
    }

    @Test
    @DisplayName("LL-133: Parse unique key: trả về null khi key null")
    void parseUniqueKey_shouldReturnNullWhenKeyIsNull() {
        String input = null;
        String[] result = RoomOccupancyUtils.parseUniqueKey(input);
        System.out.println("INPUT: uniqueKey=null");
        System.out.println("OUTPUT: result=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-134: Parse unique key: trả về null khi key rỗng")
    void parseUniqueKey_shouldReturnNullWhenKeyIsEmpty() {
        String input = "";
        String[] result = RoomOccupancyUtils.parseUniqueKey(input);
        System.out.println("INPUT: uniqueKey=\"\"");
        System.out.println("OUTPUT: result=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-135: Parse unique key: trả về null khi format sai")
    void parseUniqueKey_shouldReturnNullWhenInvalidFormat() {
        String[] result1 = RoomOccupancyUtils.parseUniqueKey("404-A2");
        assertThat(result1).isNull();

        String[] result2 = RoomOccupancyUtils.parseUniqueKey("404-A2|5");
        assertThat(result2).isNull();

        String[] result3 = RoomOccupancyUtils.parseUniqueKey("404-A2|5|1|extra");
        System.out.println("INPUT: invalidKeys=[404-A2, 404-A2|5, 404-A2|5|1|extra]");
        System.out.println("OUTPUT: result1=" + result1 + ", result2=" + result2 + ", result3=" + result3);
        assertThat(result3).isNull();
    }

    @Test
    @DisplayName("LL-136: Parse unique key: parse đúng key hợp lệ")
    void parseUniqueKey_shouldParseValidKey() {
        String input = "404-A2|5|1";
        String[] result = RoomOccupancyUtils.parseUniqueKey(input);
        System.out.println("INPUT: uniqueKey=" + input);
        System.out.println("OUTPUT: parsed=" + (result == null ? null : String.join(",", result)));
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result[0]).isEqualTo("404-A2");
        assertThat(result[1]).isEqualTo("5");
        assertThat(result[2]).isEqualTo("1");
    }

    @Test
    @DisplayName("LL-137: Lấy mã phòng từ unique key")
    void extractRoomCode_shouldReturnRoomCode() {
        String input = "404-A2|5|1";
        String result = RoomOccupancyUtils.extractRoomCode(input);
        System.out.println("INPUT: uniqueKey=" + input);
        System.out.println("OUTPUT: roomCode=" + result);
        System.out.println("EXPECTED: 404-A2");
        assertThat(result).isEqualTo("404-A2");
    }

    @Test
    @DisplayName("LL-138: Lấy mã phòng: trả về null khi key không hợp lệ")
    void extractRoomCode_shouldReturnNullForInvalidKey() {
        String input = "invalid-key";
        String result = RoomOccupancyUtils.extractRoomCode(input);
        System.out.println("INPUT: uniqueKey=" + input);
        System.out.println("OUTPUT: roomCode=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-139: Lấy thứ trong tuần từ unique key")
    void extractDayOfWeek_shouldReturnDay() {
        String input = "404-A2|5|1";
        Integer result = RoomOccupancyUtils.extractDayOfWeek(input);
        System.out.println("INPUT: uniqueKey=" + input);
        System.out.println("OUTPUT: dayOfWeek=" + result);
        System.out.println("EXPECTED: 5");
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("LL-140: Lấy thứ: trả về null khi thứ không phải số")
    void extractDayOfWeek_shouldReturnNullForInvalidKey() {
        String input = "404-A2|abc|1";
        Integer result = RoomOccupancyUtils.extractDayOfWeek(input);
        System.out.println("INPUT: uniqueKey=" + input);
        System.out.println("OUTPUT: dayOfWeek=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-141: Lấy tiết từ unique key")
    void extractPeriod_shouldReturnPeriod() {
        String input = "404-A2|5|1";
        Integer result = RoomOccupancyUtils.extractPeriod(input);
        System.out.println("INPUT: uniqueKey=" + input);
        System.out.println("OUTPUT: period=" + result);
        System.out.println("EXPECTED: 1");
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("LL-142: Lấy tiết: trả về null khi tiết không phải số")
    void extractPeriod_shouldReturnNullForInvalidKey() {
        String input = "404-A2|5|xyz";
        Integer result = RoomOccupancyUtils.extractPeriod(input);
        System.out.println("INPUT: uniqueKey=" + input);
        System.out.println("OUTPUT: period=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-143: Parse mã phòng: trả về tên + tòa")
    void parseRoomCode_shouldReturnNameAndBuilding() {
        String input = "404-A2";
        String[] result = RoomOccupancyUtils.parseRoomCode(input);
        System.out.println("INPUT: roomCode=" + input);
        System.out.println("OUTPUT: parsed=" + (result == null ? null : String.join(",", result)));
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo("404");
        assertThat(result[1]).isEqualTo("A2");
    }

    @Test
    @DisplayName("LL-144: Parse mã phòng: trả về null khi format sai")
    void parseRoomCode_shouldReturnNullForInvalidFormat() {
        String[] result1 = RoomOccupancyUtils.parseRoomCode(null);
        assertThat(result1).isNull();

        String[] result2 = RoomOccupancyUtils.parseRoomCode("404");
        assertThat(result2).isNull();

        String[] result3 = RoomOccupancyUtils.parseRoomCode("404-A2-B3");
        System.out.println("INPUT: invalidRoomCodes=[null, 404, 404-A2-B3]");
        System.out.println("OUTPUT: result1=" + result1 + ", result2=" + result2 + ", result3=" + result3);
        assertThat(result3).isNull();
    }

    @Test
    @DisplayName("LL-145: Lấy tên phòng từ mã phòng")
    void extractRoomName_shouldReturnName() {
        String input = "404-A2";
        String result = RoomOccupancyUtils.extractRoomName(input);
        System.out.println("INPUT: roomCode=" + input);
        System.out.println("OUTPUT: roomName=" + result);
        System.out.println("EXPECTED: 404");
        assertThat(result).isEqualTo("404");
    }

    @Test
    @DisplayName("LL-146: Lấy tên phòng: trả về null khi code không hợp lệ")
    void extractRoomName_shouldReturnNullForInvalidCode() {
        String input = "invalid";
        String result = RoomOccupancyUtils.extractRoomName(input);
        System.out.println("INPUT: roomCode=" + input);
        System.out.println("OUTPUT: roomName=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-147: Lấy tòa nhà từ mã phòng")
    void extractBuilding_shouldReturnBuilding() {
        String input = "404-A2";
        String result = RoomOccupancyUtils.extractBuilding(input);
        System.out.println("INPUT: roomCode=" + input);
        System.out.println("OUTPUT: building=" + result);
        System.out.println("EXPECTED: A2");
        assertThat(result).isEqualTo("A2");
    }

    @Test
    @DisplayName("LL-148: Lấy tòa nhà: trả về null khi code không hợp lệ")
    void extractBuilding_shouldReturnNullForInvalidCode() {
        String input = "invalid";
        String result = RoomOccupancyUtils.extractBuilding(input);
        System.out.println("INPUT: roomCode=" + input);
        System.out.println("OUTPUT: building=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-149: Kiểm tra unique key hợp lệ: trả về true")
    void isValidUniqueKey_shouldReturnTrueForValidKey() {
        System.out.println("INPUT: validKeys=[404-A2|5|1, 401-A1|2|6, B202-B3|7|3]");
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|5|1")).isTrue();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("401-A1|2|6")).isTrue();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("B202-B3|7|3")).isTrue();
        System.out.println("OUTPUT: allValid=true");
    }

    @Test
    @DisplayName("LL-150: Kiểm tra unique key sai format: trả về false")
    void isValidUniqueKey_shouldReturnFalseForInvalidKey() {
        System.out.println("INPUT: invalidKeys=[null, \"\", 404-A2, 404-A2|5, 404-A2|5|1|extra]");
        assertThat(RoomOccupancyUtils.isValidUniqueKey(null)).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|5")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|5|1|extra")).isFalse();
        System.out.println("OUTPUT: allInvalid=false");
    }

    @Test
    @DisplayName("LL-151: Kiểm tra unique key sai thứ trong tuần: trả về false")
    void isValidUniqueKey_shouldReturnFalseForInvalidDayOfWeek() {
        System.out.println("INPUT: invalidDayKeys=[404-A2|1|1, 404-A2|8|1, 404-A2|0|1]");
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|1|1")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|8|1")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|0|1")).isFalse();
        System.out.println("OUTPUT: allInvalid=false");
    }

    @Test
    @DisplayName("LL-152: Kiểm tra unique key sai tiết: trả về false")
    void isValidUniqueKey_shouldReturnFalseForInvalidPeriod() {
        System.out.println("INPUT: invalidPeriodKeys=[404-A2|5|0, 404-A2|5|7, 404-A2|5|-1]");
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|5|0")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|5|7")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|5|-1")).isFalse();
        System.out.println("OUTPUT: allInvalid=false");
    }

    @Test
    @DisplayName("LL-153: Kiểm tra unique key chứa giá trị không phải số: trả về false")
    void isValidUniqueKey_shouldReturnFalseForNonNumericValues() {
        System.out.println("INPUT: nonNumericKeys=[404-A2|abc|1, 404-A2|5|xyz]");
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|abc|1")).isFalse();
        assertThat(RoomOccupancyUtils.isValidUniqueKey("404-A2|5|xyz")).isFalse();
        System.out.println("OUTPUT: allInvalid=false");
    }

    @Test
    @DisplayName("LL-154: Tạo unique key: xử lý các trường hợp biên")
    void buildUniqueKey_shouldHandleEdgeCases() {
        // Test with room code containing special characters
        String result1 = RoomOccupancyUtils.buildUniqueKey("Lab-101-A", 3, 2);
        assertThat(result1).isEqualTo("Lab-101-A|3|2");

        // Test with boundary values
        String result2 = RoomOccupancyUtils.buildUniqueKey("R1", 2, 1);
        assertThat(result2).isEqualTo("R1|2|1");

        String result3 = RoomOccupancyUtils.buildUniqueKey("R100", 7, 6);
        System.out.println("INPUT: edgeCases=[(Lab-101-A,3,2), (R1,2,1), (R100,7,6)]");
        System.out.println("OUTPUT: result1=" + result1 + ", result2=" + result2 + ", result3=" + result3);
        assertThat(result3).isEqualTo("R100|7|6");
    }

    @Test
    @DisplayName("LL-155: Parse mã phòng: xử lý trường hợp có dấu '-' trong tên")
    void parseRoomCode_shouldHandleEdgeCases() {
        String[] result1 = RoomOccupancyUtils.parseRoomCode("Lab-101");
        assertThat(result1).isNotNull();
        assertThat(result1[0]).isEqualTo("Lab");
        assertThat(result1[1]).isEqualTo("101");

        String[] result2 = RoomOccupancyUtils.parseRoomCode("404");
        System.out.println("INPUT: roomCodes=[Lab-101, 404]");
        System.out.println("OUTPUT: parsed1=" + (result1 == null ? null : String.join(",", result1)) + ", parsed2=" + result2);
        assertThat(result2).isNull(); // No building part
    }
}
