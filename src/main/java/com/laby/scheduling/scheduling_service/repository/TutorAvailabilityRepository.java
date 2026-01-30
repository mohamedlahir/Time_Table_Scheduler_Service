package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.TutorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface TutorAvailabilityRepository
        extends JpaRepository<TutorAvailability, Long> {

    List<TutorAvailability> findByTutorIdAndDayOfWeekAndAvailableTrue(
            String tutorId,
            DayOfWeek dayOfWeek
    );
}
