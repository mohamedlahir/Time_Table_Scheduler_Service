package com.laby.scheduling.scheduling_service.service;

import com.laby.scheduling.scheduling_service.entity.*;
import com.laby.scheduling.scheduling_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class TimetableGenerationService {

    private final WeeklyTimetableRepository weeklyTimetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final TutorSelectionService tutorSelectionService;
    private final SchoolRepository schoolRepository;

    private final Random random = new Random();

    public void generateNextWeekTimetable(Long schoolId, LocalDate weekStartDate) {

        Optional<WeeklyTimetable> existing =
                weeklyTimetableRepository.findBySchoolIdAndWeekStartDate(
                        schoolId,
                        weekStartDate
                );

        if (existing.isPresent()) {
            throw new RuntimeException("Timetable already exists for this week");
        }

        WeeklyTimetable week = new WeeklyTimetable();
        week.setSchoolId(schoolId);
        week.setWeekStartDate(weekStartDate);
        week.setWeekEndDate(weekStartDate.plusDays(5));
        week.setStatus(WeeklyTimetable.Status.GENERATED);

        weeklyTimetableRepository.save(week);

        List<ClassRoom> classRooms =
                classRoomRepository.findBySchoolId(schoolId);

        for (ClassRoom classRoom : classRooms) {
            generateForClass(week, schoolId, classRoom);
        }
    }

    private void generateForClass(
            WeeklyTimetable week,
            Long schoolId,
            ClassRoom classRoom
    ) {

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        int periodsPerDay = school.getPeriodsPerDay();

        List<Subject> subjects =
                subjectRepository.findBySchoolIdAndActiveTrue(schoolId);

        if (subjects.isEmpty()) {
            throw new RuntimeException("No active subjects configured");
        }

        for (DayOfWeek day : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY)) {

            LocalDate classDate =
                    week.getWeekStartDate().plusDays(day.getValue() - 1);

            for (int period = 1; period <= periodsPerDay; period++) {

                Optional<TimetableEntry> existingEntry =
                        timetableEntryRepository
                                .findByWeeklyTimetableIdAndClassRoomIdAndDayOfWeekAndPeriodNumber(
                                        week.getId(),
                                        classRoom.getId(),
                                        day,
                                        period
                                );

                TimetableEntry entry;

                if (existingEntry.isPresent()) {
                    entry = existingEntry.get();
                } else {
                    entry = new TimetableEntry();
                    entry.setWeeklyTimetableId(week.getId());
                    entry.setSchoolId(schoolId);
                    entry.setClassRoomId(classRoom.getId());
                    entry.setDayOfWeek(day);
                    entry.setPeriodNumber(period);
                }

                // 🎯 Random subject
                Subject subject =
                        subjects.get(random.nextInt(subjects.size()));

                entry.setSubjectId(subject.getId());

                // 🎯 Tutor selection with FULL context
                Tutor tutor =
                        tutorSelectionService.findEligibleTutor(
                                week.getId(),
                                schoolId,
                                subject.getId(),
                                day,
                                classDate,
                                period
                        );

                if (tutor != null) {
                    entry.setTutorId(tutor.getAuthUserId());
                    entry.setStatus(TimetableEntry.Status.ASSIGNED);
                } else {
                    entry.setTutorId(null);
                    entry.setStatus(TimetableEntry.Status.CONFLICT);
                }

                timetableEntryRepository.save(entry);
            }
        }
    }
}
