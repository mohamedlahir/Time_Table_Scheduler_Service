//package com.laby.scheduling.scheduling_service.repository;
//
//import com.laby.scheduling.scheduling_service.entity.TimetableEntry;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.DayOfWeek;
//import java.util.List;
//
//public interface TimetableEntryRepository
//        extends JpaRepository<TimetableEntry, Long> {
//
//    long countByTutorIdAndSchoolIdAndDayOfWeek(
//            String tutorId,
//            Long schoolId,
//            DayOfWeek dayOfWeek
//    );
//
//
//    List<TimetableEntry> findByWeeklyTimetableIdAndClassRoomId(
//            Long weeklyTimetableId,
//            Long classRoomId
//    );
//
//    List<TimetableEntry> findByWeeklyTimetableIdAndClassRoomIdOrderByDayOfWeekAscPeriodNumberAsc(
//            Long weeklyTimetableId,
//            Long classRoomId
//    );
//
//
//    List<TimetableEntry> findByWeeklyTimetableIdAndTutorId(
//            Long weeklyTimetableId,
//            String tutorId
//    );
//
//    boolean existsByWeeklyTimetableIdAndTutorIdAndDayOfWeekAndPeriodNumber(
//            Long weeklyTimetableId,
//            String tutorId,
//            DayOfWeek dayOfWeek,
//            int periodNumber
//    );
//
//    boolean existsByWeeklyTimetableIdAndClassRoomIdAndDayOfWeekAndPeriodNumber(
//            Long weeklyTimetableId,
//            Long classRoomId,
//            DayOfWeek dayOfWeek,
//            int periodNumber
//    );
//}
package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface TimetableEntryRepository
        extends JpaRepository<TimetableEntry, Long> {

    // ✅ Used to prevent duplicate slot creation
    boolean existsByWeeklyTimetableIdAndClassRoomIdAndDayOfWeekAndPeriodNumber(
            Long weeklyTimetableId,
            Long classRoomId,
            DayOfWeek dayOfWeek,
            int periodNumber
    );

    // ✅ Used when assigning tutors (UPDATE flow)
    List<TimetableEntry> findByWeeklyTimetableIdAndClassRoomId(
            Long weeklyTimetableId,
            Long classRoomId
    );

    // ✅ Used by TutorSelectionService (daily load limit)
    long countByTutorIdAndSchoolIdAndDayOfWeek(
            String tutorId,
            Long schoolId,
            DayOfWeek dayOfWeek
    );

    // ✅ Used in leave compensation
    List<TimetableEntry> findByWeeklyTimetableIdAndTutorId(
            Long weeklyTimetableId,
            String tutorId
    );

    boolean existsByWeeklyTimetableIdAndTutorIdAndDayOfWeekAndPeriodNumber(
            Long weeklyTimetableId,
            String tutorId,
            DayOfWeek dayOfWeek,
            int periodNumber
    );

    Optional<TimetableEntry>
    findByWeeklyTimetableIdAndClassRoomIdAndDayOfWeekAndPeriodNumber(
            Long weeklyTimetableId,
            Long classRoomId,
            DayOfWeek dayOfWeek,
            int periodNumber
    );


    List<TimetableEntry> findByWeeklyTimetableIdAndClassRoomIdOrderByDayOfWeekAscPeriodNumberAsc(Long id, Long classRoomId);
}
