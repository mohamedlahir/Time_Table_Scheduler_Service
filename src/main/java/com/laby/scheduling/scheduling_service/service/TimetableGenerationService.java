package com.laby.scheduling.scheduling_service.service;

import com.laby.scheduling.scheduling_service.entity.*;
import com.laby.scheduling.scheduling_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableGenerationService {

    private final WeeklyTimetableRepository weeklyTimetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRoomSubjectRepository classRoomSubjectRepository;
    private final SchoolRepository schoolRepository;
    private final TutorSelectionService tutorSelectionService;
    private final TutorRepository tutorRepository;

    /**
     * Entry point
     */
    public void generateNextWeekTimetable(Long schoolId, LocalDate weekStartDate) {

        School school =
                schoolRepository.findById(schoolId)
                        .orElseThrow(() -> new RuntimeException("School not found"));

        WeeklyTimetable week = new WeeklyTimetable();
        week.setSchoolId(schoolId);
        week.setWeekStartDate(weekStartDate);
        week.setWeekEndDate(weekStartDate.plusDays(6));
        week.setStatus(WeeklyTimetable.Status.GENERATED);

        weeklyTimetableRepository.save(week);

        List<ClassRoom> classRooms =
                classRoomRepository.findBySchoolId(schoolId);

        for (ClassRoom classRoom : classRooms) {
            generateForClass(week, school, classRoom);
        }
    }

    /**
     * Generate timetable for ONE class
     */
    private void generateForClass(
            WeeklyTimetable week,
            School school,
            ClassRoom classRoom
    ) {

        int periodsPerDay = school.getPeriodsPerDay();
        int totalSlots = periodsPerDay * 6; // Monday to Saturday

        List<ClassRoomSubject> classRoomSubjects =
                classRoomSubjectRepository.findByClassRoomIdAndActiveTrue(classRoom.getId());

        List<Subject> subjects;
        Map<Long, Integer> requiredBySubjectId = new HashMap<>();

        if (!classRoomSubjects.isEmpty()) {
            List<Long> subjectIds = classRoomSubjects.stream()
                    .map(ClassRoomSubject::getSubjectId)
                    .toList();

            subjects = subjectRepository.findAllById(subjectIds);
            for (ClassRoomSubject crs : classRoomSubjects) {
                requiredBySubjectId.put(
                        crs.getSubjectId(),
                        Math.max(0, crs.getWeeklyRequiredPeriods())
                );
            }
        } else {
            subjects = subjectRepository.findBySchoolIdAndActiveTrue(school.getId());
            for (Subject subject : subjects) {
                requiredBySubjectId.put(
                        subject.getId(),
                        Math.max(0, subject.getWeeklyRequiredPeriods())
                );
            }
        }

        if (subjects.isEmpty()) {
            return;
        }

        Map<Long, Subject> subjectById = new HashMap<>();
        Map<Long, Integer> remainingBySubjectId = new HashMap<>();

        int totalRequired = 0;
        for (Subject subject : subjects) {
            subjectById.put(subject.getId(), subject);
            int required = requiredBySubjectId.getOrDefault(subject.getId(), 0);
            remainingBySubjectId.put(subject.getId(), required);
            totalRequired += required;
        }

        if (totalRequired > totalSlots) {
            final int[] excess = { totalRequired - totalSlots };
            subjects.stream()
                    .sorted((a, b) -> {
                        int byRequired = Integer.compare(
                                a.getWeeklyRequiredPeriods(),
                                b.getWeeklyRequiredPeriods()
                        );
                        if (byRequired != 0) {
                            return byRequired;
                        }
                        return a.getId().compareTo(b.getId());
                    })
                    .forEach(subject -> {
                        if (excess[0] <= 0) {
                            return;
                        }
                        int remaining = remainingBySubjectId.get(subject.getId());
                        while (remaining > 0 && excess[0] > 0) {
                            remaining--;
                            excess[0]--;
                        }
                        remainingBySubjectId.put(subject.getId(), remaining);
                    });
        }

        List<Long> subjectPool = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : remainingBySubjectId.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                subjectPool.add(entry.getKey());
            }
        }
        Collections.shuffle(subjectPool);

        for (DayOfWeek day : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY)) {

            for (int period = 1; period <= periodsPerDay; period++) {

                TimetableEntry entry = new TimetableEntry();
                entry.setWeeklyTimetableId(week.getId());
                entry.setSchoolId(school.getId());
                entry.setClassRoomId(classRoom.getId());
                entry.setDayOfWeek(day);
                entry.setPeriodNumber(period);
                entry.setStatus(TimetableEntry.Status.PENDING);

                if (subjectPool.isEmpty()) {
                    entry.setStatus(TimetableEntry.Status.CONFLICT);
                    timetableEntryRepository.save(entry);
                    continue;
                }

                Long subjectId = subjectPool.remove(subjectPool.size() - 1);
                Subject subject = subjectById.get(subjectId);
                entry.setSubjectId(subject.getId());
                entry.setSubjectName(subject.getName());

                Optional<String> tutorOpt =
                        tutorSelectionService.findEligibleTutor(
                                school.getId(),
                                classRoom.getId(),
                                subject.getId(),
                                day,
                                week.getWeekStartDate(),
                                week.getId(),
                                period
                        );

                if (tutorOpt.isPresent()) {
                    String tutorName = tutorRepository
                            .findById(tutorOpt.get())
                            .map(Tutor::getName)
                            .orElse("Unknown Tutor");
                    entry.setTutorName(tutorName);
                    entry.setTutorId(tutorOpt.get());
                    entry.setStatus(TimetableEntry.Status.ASSIGNED);
                } else {
                    entry.setStatus(TimetableEntry.Status.CONFLICT);
                }

                timetableEntryRepository.save(entry);
            }
        }
    }
}
