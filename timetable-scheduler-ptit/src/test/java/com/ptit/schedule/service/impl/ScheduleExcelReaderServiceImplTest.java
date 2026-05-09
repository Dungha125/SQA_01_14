package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.ScheduleEntry;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleExcelReaderServiceImplTest {

    private final ScheduleExcelReaderServiceImpl service = new ScheduleExcelReaderServiceImpl();

    private static MockMultipartFile toMultipartFile(Workbook workbook, String filename) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return new MockMultipartFile("file", filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bos.toByteArray());
        }
    }

    @Test
    @DisplayName("LL-EXCEL-01: validateScheduleExcelFormat returns false when missing columns")
    void validateScheduleExcelFormat_shouldReturnFalseWhenMissingColumns() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        // Too few columns (<44)
        header.createCell(0).setCellValue("A");
        MockMultipartFile file = toMultipartFile(wb, "tkb.xlsx");

        boolean ok = service.validateScheduleExcelFormat(file);
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("LL-EXCEL-02: validateScheduleExcelFormat returns true for valid header column count")
    void validateScheduleExcelFormat_shouldReturnTrueWhenValidColumns() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        // Create at least 44 columns
        for (int i = 0; i < 44; i++) {
            header.createCell(i).setCellValue("H" + i);
        }
        MockMultipartFile file = toMultipartFile(wb, "tkb.xlsx");

        boolean ok = service.validateScheduleExcelFormat(file);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("LL-EXCEL-03: readScheduleFromExcel parses entries and timeSlots, skips online rows")
    void readScheduleFromExcel_shouldParseAndSkipOnline() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();

        // Header rows (0..2)
        for (int r = 0; r < 3; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < 44; c++) row.createCell(c).setCellValue("H");
        }

        // Data row #1: valid schedule entry with week marker "x"
        Row row1 = sheet.createRow(3);
        for (int c = 0; c < 44; c++) row1.createCell(c).setCellValue("");
        // Subject code (B=1), name (C=2), classGroup (D=3)
        row1.getCell(1).setCellValue("INT1001");
        row1.getCell(2).setCellValue("Nhap mon");
        row1.getCell(3).setCellValue("N1");
        // Day (G=6), shift (H=7), start(I=8), num(J=9)
        row1.getCell(6).setCellValue("2");
        row1.getCell(7).setCellValue("1");
        row1.getCell(8).setCellValue("1");
        row1.getCell(9).setCellValue("3");
        // Room (K=10), Building (L=11)
        row1.getCell(10).setCellValue("401");
        row1.getCell(11).setCellValue("A1");
        // Student count (T=19), teacher id (V=21), teacher name (W=22)
        row1.getCell(19).setCellValue("50");
        row1.getCell(21).setCellValue("GV01");
        row1.getCell(22).setCellValue("Teacher 01");
        // Week 1 marker (AB=27)
        row1.getCell(27).setCellValue("x");

        // Data row #2: online entry -> should be skipped
        Row row2 = sheet.createRow(4);
        for (int c = 0; c < 44; c++) row2.createCell(c).setCellValue("");
        row2.getCell(1).setCellValue("INT9999");
        row2.getCell(10).setCellValue("online");
        row2.getCell(11).setCellValue("lms");
        row2.getCell(21).setCellValue("GV99");
        row2.getCell(22).setCellValue("Teacher 99");
        row2.getCell(6).setCellValue("CN");
        row2.getCell(7).setCellValue("1");
        row2.getCell(8).setCellValue("1");
        row2.getCell(9).setCellValue("1");
        row2.getCell(27).setCellValue("x");

        MockMultipartFile file = toMultipartFile(wb, "tkb.xlsx");
        List<ScheduleEntry> entries = service.readScheduleFromExcel(file);

        assertThat(entries).hasSize(1);
        ScheduleEntry e = entries.get(0);
        assertThat(e.getSubjectCode()).isEqualTo("INT1001");
        assertThat(e.getTeacherId()).isEqualTo("GV01");
        assertThat(e.getRoom()).contains("401");
        assertThat(e.getTimeSlots()).hasSize(1);
        assertThat(e.getTimeSlots().get(0).getDayOfWeek()).isEqualTo("Thứ 2");
        assertThat(e.getTimeSlots().get(0).getDate()).isEqualTo("Tuần 1");
    }

    @Test
    @DisplayName("LL-EXCEL-04: readScheduleFromExcel converts dayOfWeek default and custom")
    void readScheduleFromExcel_shouldConvertDayOfWeekVariants() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        for (int r = 0; r < 3; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < 44; c++) row.createCell(c).setCellValue("H");
        }

        // Row with empty day -> "Không xác định"
        Row row1 = sheet.createRow(3);
        for (int c = 0; c < 44; c++) row1.createCell(c).setCellValue("");
        row1.getCell(1).setCellValue("INT1001");
        row1.getCell(6).setCellValue("");
        row1.getCell(7).setCellValue("1");
        row1.getCell(8).setCellValue("1");
        row1.getCell(9).setCellValue("1");
        row1.getCell(10).setCellValue("401");
        row1.getCell(11).setCellValue("A1");
        row1.getCell(21).setCellValue("GV01");
        row1.getCell(22).setCellValue("Teacher 01");
        row1.getCell(27).setCellValue("x");

        // Row with day=9 -> default "Thứ 9"
        Row row2 = sheet.createRow(4);
        for (int c = 0; c < 44; c++) row2.createCell(c).setCellValue("");
        row2.getCell(1).setCellValue("INT1002");
        row2.getCell(6).setCellValue("9");
        row2.getCell(7).setCellValue("1");
        row2.getCell(8).setCellValue("1");
        row2.getCell(9).setCellValue("1");
        row2.getCell(10).setCellValue("402");
        row2.getCell(11).setCellValue("A1");
        row2.getCell(21).setCellValue("GV02");
        row2.getCell(22).setCellValue("Teacher 02");
        row2.getCell(27).setCellValue("x");

        MockMultipartFile file = toMultipartFile(wb, "tkb.xlsx");
        List<ScheduleEntry> entries = service.readScheduleFromExcel(file);
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getTimeSlots().get(0).getDayOfWeek()).isEqualTo("Không xác định");
        assertThat(entries.get(1).getTimeSlots().get(0).getDayOfWeek()).isEqualTo("Thứ 9");
    }
}

