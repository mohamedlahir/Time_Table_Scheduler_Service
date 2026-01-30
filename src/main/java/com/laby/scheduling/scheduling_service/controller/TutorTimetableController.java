package com.laby.scheduling.scheduling_service.controller;

import com.laby.scheduling.scheduling_service.entity.TimetableEntry;
import com.laby.scheduling.scheduling_service.repository.TimetableEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scheduler/api/tutor/timetable")
@RequiredArgsConstructor
public class TutorTimetableController {

    private final TimetableEntryRepository timetableEntryRepository;

    @GetMapping
    public ResponseEntity<List<TimetableEntry>> getTutorTimetable(
            @RequestParam String tutorId,
            @RequestParam Long weeklyTimetableId
    ) {
        return ResponseEntity.ok(
                timetableEntryRepository
                        .findByWeeklyTimetableIdAndTutorId(
                                weeklyTimetableId,
                                tutorId
                        )
        );
    }
}

