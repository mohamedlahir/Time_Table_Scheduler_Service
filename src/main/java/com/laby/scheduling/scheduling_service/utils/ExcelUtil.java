package com.laby.scheduling.scheduling_service.utils;

import com.laby.scheduling.scheduling_service.DTO.TutorExcelDTO;
import com.laby.scheduling.scheduling_service.DTO.TutorSubjectExcelDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    private static final DataFormatter formatter = new DataFormatter();

    // =========================================================
    // PARSE TUTORS FILE
    // =========================================================
    public static List<TutorExcelDTO> parseTutors(MultipartFile file) {

        List<TutorExcelDTO> tutors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(0);
            validateTutorHeader(header);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String tutorId = getString(row, 0);
                if (tutorId.isBlank()) continue; // ✅ SKIP EMPTY ROWS

                TutorExcelDTO tutor = new TutorExcelDTO();
                tutor.setTutorId(tutorId);
                tutor.setTutorName(getString(row, 1));
                tutor.setEmail(getString(row, 2));
                tutor.setMaxWeeklyHours(getInt(row, 3));

                tutors.add(tutor);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse tutors excel", e);
        }

        return tutors;
    }

    // =========================================================
    // PARSE TUTOR SUBJECTS FILE ✅ FULL FIX
    // =========================================================
    public static List<TutorSubjectExcelDTO> parseTutorSubjects(MultipartFile file) {

        List<TutorSubjectExcelDTO> subjects = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(0);
            validateTutorSubjectHeader(header);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String tutorId = getString(row, 0);
                String subjectCode = getString(row, 1);

                // 🚨 HARD GUARD — THIS IS THE FIX
                if (tutorId.isBlank() || subjectCode.isBlank()) {
                    continue; // skip empty / broken rows
                }

                TutorSubjectExcelDTO dto = new TutorSubjectExcelDTO();
                dto.setTutorId(tutorId);
                dto.setSubjectCode(subjectCode);
                dto.setMaxWeeklyPeriods(getInt(row, 2));

                subjects.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse tutor-subjects excel", e);
        }

        return subjects;
    }

    // =========================================================
    // HEADER VALIDATION
    // =========================================================
    private static void validateTutorHeader(Row header) {
        if (header == null ||
                !"TutorId".equalsIgnoreCase(getString(header, 0)) ||
                !"TutorName".equalsIgnoreCase(getString(header, 1)) ||
                !"Email".equalsIgnoreCase(getString(header, 2)) ||
                !"MaxWeeklyHours".equalsIgnoreCase(getString(header, 3))) {

            throw new RuntimeException("Invalid Tutors Excel header");
        }
    }

    private static void validateTutorSubjectHeader(Row header) {
        if (header == null ||
                !"TutorId".equalsIgnoreCase(getString(header, 0)) ||
                !"SubjectCode".equalsIgnoreCase(getString(header, 1)) ||
                !"MaxWeeklyPeriods".equalsIgnoreCase(getString(header, 2))) {

            throw new RuntimeException("Invalid TutorSubjects Excel header");
        }
    }

    // =========================================================
    // SAFE CELL READERS
    // =========================================================
    private static String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private static int getInt(Row row, int index) {
        String value = getString(row, index);
        if (value.isEmpty()) return 0;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number: " + value);
        }
    }
}
