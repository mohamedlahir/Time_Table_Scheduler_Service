package com.laby.scheduling.scheduling_service.controller;

import com.laby.scheduling.scheduling_service.entity.TimetableEntry;
import com.laby.scheduling.scheduling_service.entity.Tutor;
import com.laby.scheduling.scheduling_service.entity.WeeklyTimetable;
import com.laby.scheduling.scheduling_service.repository.TimetableEntryRepository;
import com.laby.scheduling.scheduling_service.repository.TutorRepository;
import com.laby.scheduling.scheduling_service.repository.WeeklyTimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/scheduler/api/admin/timetable")
@RequiredArgsConstructor
public class AdminTimetableQueryController {

    private final WeeklyTimetableRepository weeklyTimetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    @GetMapping("/class")
    public ResponseEntity<List<TimetableEntry>> getClassTimetable(
            @RequestParam Long schoolId,
            @RequestParam Long classRoomId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStartDate
    ) {
        WeeklyTimetable timetable =
                weeklyTimetableRepository
                        .findBySchoolIdAndWeekStartDate(schoolId, weekStartDate)
                        .orElseThrow();

        return ResponseEntity.ok(
                timetableEntryRepository
                        .findByWeeklyTimetableIdAndClassRoomIdOrderByDayOfWeekAscPeriodNumberAsc(
                                timetable.getId(),
                                classRoomId
                        )
        );
    }

    @GetMapping("/tutor")
    public String hello() {
        return "Hello, Admin Timetable!";
    }
}

