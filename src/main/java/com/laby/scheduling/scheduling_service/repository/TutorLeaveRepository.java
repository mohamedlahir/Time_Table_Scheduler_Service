//package com.laby.scheduling.scheduling_service.repository;
//
//import com.laby.scheduling.scheduling_service.entity.TutorLeave;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface TutorLeaveRepository extends JpaRepository<TutorLeave, Long> {
//
//    List<TutorLeave> findByTutorIdAndApprovedTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
//            String tutorId,
//            LocalDate date1,
//            LocalDate date2
//    );
//}
package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.TutorLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TutorLeaveRepository
        extends JpaRepository<TutorLeave, Long> {

    // ✅ Used when fetching leave records
    List<TutorLeave> findByTutorIdAndApprovedTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            String tutorId,
            LocalDate fromDate,
            LocalDate toDate
    );

    // ✅ Used by TutorSelectionService (FAST boolean check)
    boolean existsByTutorIdAndApprovedTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            String tutorId,
            LocalDate fromDate,
            LocalDate toDate
    );


}
