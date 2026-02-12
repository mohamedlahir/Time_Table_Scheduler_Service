package com.laby.scheduling.scheduling_service.service;

import com.laby.scheduling.scheduling_service.entity.TimetableEntry;
import com.laby.scheduling.scheduling_service.entity.Tutor;
import com.laby.scheduling.scheduling_service.entity.WeeklyTimetable;
import com.laby.scheduling.scheduling_service.repository.TimetableEntryRepository;
import com.laby.scheduling.scheduling_service.repository.TutorRepository;
import com.laby.scheduling.scheduling_service.repository.WeeklyTimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TutorLeaveCompensationService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final TutorSelectionService tutorSelectionService;
    private final WeeklyTimetableRepository weeklyTimetableRepository;
    private final TutorRepository tutorRepository;

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

            // ✅ FIX: expect Optional<String>, not Tutor
            Optional<String> replacementTutorId =
                    tutorSelectionService.findEligibleTutor(
                            entry.getSchoolId(),          // schoolId
                            entry.getClassRoomId(),       // classRoomId
                            entry.getSubjectId(),         // subjectId
                            entry.getDayOfWeek(),         // day
                            week.getWeekStartDate(),      // week start
                            week.getId(),                 // week id
                            entry.getPeriodNumber()       // period
                    );

            if (replacementTutorId.isPresent()) {
                String newTutorId = replacementTutorId.get();
                entry.setTutorId(newTutorId);
                String tutorName = tutorRepository
                        .findById(newTutorId)
                        .map(Tutor::getName)
                        .orElse(null);
                entry.setTutorName(tutorName);
                entry.setStatus(TimetableEntry.Status.REPLACED);
            } else {
                entry.setTutorId(null);
                entry.setTutorName(null);
                entry.setStatus(TimetableEntry.Status.CONFLICT);
            }

            timetableEntryRepository.save(entry);
        }

        week.setStatus(WeeklyTimetable.Status.ADJUSTED);
        weeklyTimetableRepository.save(week);
    }
}
