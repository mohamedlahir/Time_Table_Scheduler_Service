//package com.laby.scheduling.scheduling_service.service;
//
//import com.laby.scheduling.scheduling_service.entity.Tutor;
//import com.laby.scheduling.scheduling_service.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class TutorSelectionService {
//
//    private final TutorRepository tutorRepository;
//    private final TutorSubjectRepository tutorSubjectRepository;
//    private final TutorLeaveRepository tutorLeaveRepository;
//    private final TimetableEntryRepository timetableEntryRepository;
//
//    public Tutor findEligibleTutor(
//            Long schoolId,
//            Long subjectId,
//            DayOfWeek dayOfWeek,
//            LocalDate classDate
//    ) {
//
//        List<Tutor> tutors =
//                tutorRepository.findBySchoolIdAndActiveTrue(schoolId);
//
//        for (Tutor tutor : tutors) {
//
//            // 1️⃣ Tutor must teach the subject
//            boolean teaches =
//                    tutorSubjectRepository
//                            .existsByTutorIdAndSubjectId(
//                                    tutor.getAuthUserId(),
//                                    subjectId
//                            );
//            if (!teaches) continue;
//
//            // 2️⃣ Tutor must NOT be on approved leave
//            boolean onLeave =
//                    tutorLeaveRepository
//                            .findByTutorIdAndApprovedTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
//                                    tutor.getAuthUserId(),
//                                    classDate,
//                                    classDate
//                            )
//                            .size() > 0;
//
//            if (onLeave) continue;
//
//            // 3️⃣ Tutor must NOT exceed daily limit
//            long classesToday =
//                    timetableEntryRepository
//                            .countByTutorIdAndSchoolIdAndDayOfWeek(
//                                    tutor.getAuthUserId(),
//                                    schoolId,
//                                    dayOfWeek
//                            );
//
//            if (classesToday >= tutor.getMaxClassesPerDay()) {
//                continue;
//            }
//
//            // ✅ Tutor is eligible
//            return tutor;
//        }
//
//        return null;
//    }
//}

package com.laby.scheduling.scheduling_service.service;

import com.laby.scheduling.scheduling_service.entity.Tutor;
import com.laby.scheduling.scheduling_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorSelectionService {

    private final TutorRepository tutorRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final TutorLeaveRepository tutorLeaveRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    public Tutor findEligibleTutor(
            Long weeklyTimetableId,
            Long schoolId,
            Long subjectId,
            DayOfWeek dayOfWeek,
            LocalDate classDate,
            int periodNumber
    ) {

        List<Tutor> tutors =
                tutorRepository.findBySchoolIdAndActiveTrue(schoolId);

        for (Tutor tutor : tutors) {

            // 1️⃣ Subject check
            boolean teaches =
                    tutorSubjectRepository.existsByTutorIdAndSubjectId(
                            tutor.getAuthUserId(),
                            subjectId
                    );
            if (!teaches) continue;

            // 2️⃣ Leave check
            boolean onLeave =
                    tutorLeaveRepository
                            .existsByTutorIdAndApprovedTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                                    tutor.getAuthUserId(),
                                    classDate,
                                    classDate
                            );
            if (onLeave) continue;

            // 3️⃣ Daily limit
            long classesToday =
                    timetableEntryRepository
                            .countByTutorIdAndSchoolIdAndDayOfWeek(
                                    tutor.getAuthUserId(),
                                    schoolId,
                                    dayOfWeek
                            );

            if (classesToday >= tutor.getMaxClassesPerDay()) continue;

            // 4️⃣ 🚨 CRITICAL: same slot check
            boolean alreadyBooked =
                    timetableEntryRepository
                            .existsByWeeklyTimetableIdAndTutorIdAndDayOfWeekAndPeriodNumber(
                                    weeklyTimetableId,
                                    tutor.getAuthUserId(),
                                    dayOfWeek,
                                    periodNumber
                            );

            if (alreadyBooked) continue;

            // ✅ ELIGIBLE
            return tutor;
        }

        return null;
    }

}
