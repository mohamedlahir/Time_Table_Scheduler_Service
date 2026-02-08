package com.laby.scheduling.scheduling_service.controller;

import com.laby.scheduling.scheduling_service.service.InternalGenerationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/scheduler/api/internal/timetable")
public class InternalTimeTableGenerator {



    private final InternalGenerationService internalGenerationService;

    public InternalTimeTableGenerator(InternalGenerationService internalGenerationService) {
        this.internalGenerationService = internalGenerationService;
    }

    @PostMapping("/generate-timetable")
    public String generateTimetable(@RequestParam Long schoolId, @RequestParam LocalDate weekStartDate) {
        internalGenerationService.generateTimetable(schoolId, weekStartDate);
        return "Timetable generation triggered";
    }

}
