package com.laby.scheduling.scheduling_service.service;

import com.laby.scheduling.scheduling_service.entity.TimetableEntry;
import com.laby.scheduling.scheduling_service.entity.Tutor;
import com.laby.scheduling.scheduling_service.entity.WeeklyTimetable;
import com.laby.scheduling.scheduling_service.repository.TimetableEntryRepository;
import com.laby.scheduling.scheduling_service.repository.TutorLeaveRepository;
import com.laby.scheduling.scheduling_service.repository.WeeklyTimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorLeaveCompensationService {

    private final TutorLeaveRepository tutorLeaveRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final TutorSelectionService tutorSelectionService;
    private final WeeklyTimetableRepository weeklyTimetableRepository;

    /**
     * Call this AFTER a tutor leave is approved
     */
    public void compensateTutorLeave(
            String tutorId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<WeeklyTimetable> affectedWeeks =
                weeklyTimetableRepository.findAll()
                        .stream()
                        .filter(week ->
                                !(toDate.isBefore(week.getWeekStartDate())
                                        || fromDate.isAfter(week.getWeekEndDate()))
                        )
                        .toList();

        for (WeeklyTimetable week : affectedWeeks) {
            compensateForWeek(week, tutorId, fromDate, toDate);
        }
    }

    private void compensateForWeek(
            WeeklyTimetable week,
            String tutorId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<TimetableEntry> affectedEntries =
                timetableEntryRepository
                        .findByWeeklyTimetableIdAndTutorId(
                                week.getId(),
                                tutorId
                        );

        for (TimetableEntry entry : affectedEntries) {

            LocalDate classDate =
                    week.getWeekStartDate()
                            .plusDays(entry.getDayOfWeek().getValue() - 1);

            if (classDate.isBefore(fromDate) || classDate.isAfter(toDate)) {
                continue;
            }

            Tutor replacement =
                    tutorSelectionService.findEligibleTutor(
                            week.getId(),                 // ✅ weeklyTimetableId
                            entry.getSchoolId(),          // ✅ schoolId
                            entry.getSubjectId(),         // ✅ subjectId
                            entry.getDayOfWeek(),         // ✅ day
                            classDate,                    // ✅ classDate
                            entry.getPeriodNumber()       // ✅ periodNumber
                    );

            if (replacement != null) {
                entry.setTutorId(replacement.getAuthUserId());
                entry.setStatus(TimetableEntry.Status.REPLACED);
            } else {
                entry.setStatus(TimetableEntry.Status.CONFLICT);
            }

            timetableEntryRepository.save(entry);
        }

        week.setStatus(WeeklyTimetable.Status.ADJUSTED);
        weeklyTimetableRepository.save(week);
    }
}
