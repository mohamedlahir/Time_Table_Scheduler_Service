package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.WeeklyTimetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyTimetableRepository
        extends JpaRepository<WeeklyTimetable, Long> {

    Optional<WeeklyTimetable> findBySchoolIdAndWeekStartDate(
            Long schoolId,
            LocalDate weekStartDate
    );
}
