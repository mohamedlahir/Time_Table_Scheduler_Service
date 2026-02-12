package com.laby.scheduling.scheduling_service.service;

import com.laby.scheduling.scheduling_service.repository.TimetableEntryRepository;
import com.laby.scheduling.scheduling_service.repository.TutorLeaveRepository;
import com.laby.scheduling.scheduling_service.repository.TutorRepository;
import com.laby.scheduling.scheduling_service.repository.TutorSubjectRepository;
import com.laby.scheduling.scheduling_service.repository.ClassRoomRepository;
import com.laby.scheduling.scheduling_service.entity.ClassRoom;
import com.laby.scheduling.scheduling_service.entity.Level;
import com.laby.scheduling.scheduling_service.entity.Tutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TutorSelectionService {

    private final TutorSubjectRepository tutorSubjectRepository;
    private final TutorLeaveRepository tutorLeaveRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final TutorRepository tutorRepository;
    private final ClassRoomRepository classRoomRepository;

    /**
     * Returns tutorId if an eligible tutor exists
     */
    public Optional<String> findEligibleTutor(
            Long schoolId,
            Long classRoomId,
            Long subjectId,
            DayOfWeek dayOfWeek,
            LocalDate weekStartDate,
            Long weeklyTimetableId,
            int periodNumber
    ) {

        LocalDate classDate =
                weekStartDate.plusDays(dayOfWeek.getValue() - 1);

        List<String> tutorIds =
                tutorSubjectRepository.findTutorIdsBySubjectId(subjectId);

        Level classLevel = classRoomRepository.findById(classRoomId)
                .map(ClassRoom::getLevel)
                .orElse(null);

        Map<String, Level> tutorLevelById = new HashMap<>();
        if (!tutorIds.isEmpty()) {
            for (Tutor tutor : tutorRepository.findByAuthUserIdIn(tutorIds)) {
                tutorLevelById.put(tutor.getAuthUserId(), tutor.getLevel());
            }
        }

        Optional<String> sameLevelTutor =
                findEligibleFromPool(
                        tutorIds,
                        tutorLevelById,
                        classLevel,
                        null,
                        schoolId,
                        dayOfWeek,
                        classDate,
                        weeklyTimetableId,
                        periodNumber
                );

        if (sameLevelTutor.isPresent()) {
            return sameLevelTutor;
        }

        if (classLevel == null) {
            return Optional.empty();
        }

        Level oneAbove = classLevel.oneLevelAbove();
        if (oneAbove == null) {
            return Optional.empty();
        }

        return findEligibleFromPool(
                tutorIds,
                tutorLevelById,
                classLevel,
                oneAbove,
                schoolId,
                dayOfWeek,
                classDate,
                weeklyTimetableId,
                periodNumber
        );
    }

    private Optional<String> findEligibleFromPool(
            List<String> tutorIds,
            Map<String, Level> tutorLevelById,
            Level classLevel,
            Level fallbackLevel,
            Long schoolId,
            DayOfWeek dayOfWeek,
            LocalDate classDate,
            Long weeklyTimetableId,
            int periodNumber
    ) {

        for (String tutorId : tutorIds) {

            if (classLevel != null) {
                Level tutorLevel = tutorLevelById.get(tutorId);
                if (tutorLevel == null) {
                    continue;
                }

                if (fallbackLevel == null) {
                    if (tutorLevel != classLevel) {
                        continue;
                    }
                } else {
                    if (tutorLevel != fallbackLevel) {
                        continue;
                    }
                }
            }

            boolean onLeave =
                    tutorLeaveRepository
                            .existsByTutorIdAndApprovedTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                                    tutorId,
                                    classDate,
                                    classDate
                            );

            if (onLeave) continue;

            boolean slotBusy =
                    timetableEntryRepository
                            .existsByTutorIdAndWeeklyTimetableIdAndDayOfWeekAndPeriodNumber(
                                    tutorId,
                                    weeklyTimetableId,
                                    dayOfWeek,
                                    periodNumber
                            );

            if (slotBusy) continue;

            long dailyCount =
                    timetableEntryRepository
                            .countByTutorIdAndSchoolIdAndWeeklyTimetableIdAndDayOfWeek(
                                    tutorId,
                                    schoolId,
                                    weeklyTimetableId,
                                    dayOfWeek
                            );
            int maxPerDay = tutorRepository.findMaxClassesPerDay(tutorId);
            if (dailyCount >= maxPerDay) continue;

            return Optional.of(tutorId);
        }

        return Optional.empty();
    }
}
