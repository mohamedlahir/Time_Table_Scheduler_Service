package com.laby.scheduling.scheduling_service.controller;

import com.laby.scheduling.scheduling_service.service.TimetableGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/scheduler/api/admin/timetable")
@RequiredArgsConstructor
public class AdminTimetableController {

    private final TimetableGenerationService timetableGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateTimetable(
            @RequestParam Long schoolId,
            @RequestParam LocalDate weekStartDate
    ) {
        timetableGenerationService.generateNextWeekTimetable(
                schoolId,
                weekStartDate
        );
        return ResponseEntity.ok("Timetable generation triggered");
    }
}
