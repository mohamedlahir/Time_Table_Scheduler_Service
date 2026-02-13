package com.laby.scheduling.scheduling_service.batch_processing;

import com.laby.scheduling.scheduling_service.entity.Subject;
import com.laby.scheduling.scheduling_service.entity.Tutor;
import com.laby.scheduling.scheduling_service.DTO.TutorExcelDTO;
import com.laby.scheduling.scheduling_service.DTO.SubjectExcelDTO;
import com.laby.scheduling.scheduling_service.DTO.TutorSubjectExcelDTO;
import com.laby.scheduling.scheduling_service.entity.TutorSubject;
import com.laby.scheduling.scheduling_service.repository.SubjectRepository;
import com.laby.scheduling.scheduling_service.repository.TutorRepository;
import com.laby.scheduling.scheduling_service.repository.TutorSubjectRepository;
import com.laby.scheduling.scheduling_service.utils.ExcelUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchImportService {

    private final TutorRepository tutorRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final SubjectRepository subjectRepository;

    // =========================================================
    // MAIN ENTRY POINT
    // =========================================================
    @Transactional
    public void importTutors(MultipartFile tutorsFile,
                             MultipartFile tutorSubjectsFile) {

        // ===============================
        // 1️⃣ PARSE EXCEL FILES
        // ===============================
        List<TutorExcelDTO> tutorDTOs =
                ExcelUtil.parseTutors(tutorsFile);

        List<TutorSubjectExcelDTO> tutorSubjectDTOs =
                ExcelUtil.parseTutorSubjects(tutorSubjectsFile);

        if (tutorDTOs.isEmpty()) {
            throw new RuntimeException("Tutors file is empty");
        }

        // ===============================
        // 2️⃣ VALIDATE RELATIONSHIPS
        // ===============================
        validateTutorSubjects(tutorDTOs, tutorSubjectDTOs);

        // ===============================
        // 3️⃣ MAP & SAVE TUTORS
        // ===============================
        Map<String, Tutor> tutorMap = saveTutors(tutorDTOs);

        // ===============================
        // 4️⃣ MAP & SAVE TUTOR SUBJECTS
        // ===============================
        saveTutorSubjects(tutorSubjectDTOs, tutorMap);
    }

    // =========================================================
    // SUBJECTS IMPORT (with Grade)
    // =========================================================
    @Transactional
    public void importSubjects(MultipartFile subjectsFile) {

        List<SubjectExcelDTO> subjectDTOs =
                ExcelUtil.parseSubjects(subjectsFile);

        if (subjectDTOs.isEmpty()) {
            throw new RuntimeException("Subjects file is empty");
        }

        for (SubjectExcelDTO dto : subjectDTOs) {
            String name = dto.getName();
            Long schoolId = dto.getSchoolId();
            String grade = dto.getGrade();

            if (name == null || name.isBlank()) {
                continue;
            }
            if (schoolId == null) {
                throw new RuntimeException("SchoolId is required for subject: " + name);
            }

            Subject subject = subjectRepository
                    .findBySchoolIdAndNameAndGrade(schoolId, name, grade)
                    .orElseGet(Subject::new);

            subject.setName(name);
            subject.setSchoolId(schoolId);
            subject.setGrade(grade);
            subject.setWeeklyRequiredPeriods(dto.getWeeklyRequiredPeriods());
            subject.setActive(dto.isActive());

            subjectRepository.save(subject);
        }
    }

    // =========================================================
    // VALIDATION
    // =========================================================
    private void validateTutorSubjects(List<TutorExcelDTO> tutors,
                                       List<TutorSubjectExcelDTO> subjects) {

        Set<String> tutorIds =
                tutors.stream()
                        .map(TutorExcelDTO::getTutorId)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(id -> !id.isEmpty())
                        .collect(Collectors.toSet());

        for (TutorSubjectExcelDTO dto : subjects) {

            String tutorId = dto.getTutorId();
            String grade = dto.getGrade();

            if (tutorId == null || tutorId.trim().isEmpty()) {
                throw new RuntimeException(
                        "TutorSubject row has EMPTY tutorId for subject: "
                                + dto.getSubjectCode()
                );
            }
            if (grade == null || grade.trim().isEmpty()) {
                throw new RuntimeException(
                        "TutorSubject row has EMPTY grade for subject: "
                                + dto.getSubjectCode()
                );
            }

            if (!tutorIds.contains(tutorId.trim())) {
                throw new RuntimeException(
                        "TutorSubject refers to unknown TutorId: " + tutorId
                );
            }
        }
    }


    // =========================================================
    // SAVE TUTORS
    // =========================================================
    private Map<String, Tutor> saveTutors(List<TutorExcelDTO> tutorDTOs) {

        tutorRepository.deleteAll();

        List<Tutor> tutors = tutorDTOs.stream()
                .map(dto -> {
                    Tutor tutor = new Tutor();

                    // ✅ CORRECT MAPPING
                    tutor.setAuthUserId(dto.getTutorId().trim());        // PK
                    tutor.setName(dto.getTutorName());
                    tutor.setMaxClassesPerDay(dto.getMaxWeeklyHours());

                    // Optional / required defaults
                    tutor.setSchoolId(1L);                         // or from context
                    tutor.setActive(true);

                    return tutor;
                })
                .toList();

        List<Tutor> savedTutors = tutorRepository.saveAll(tutors);

        // Map by authUserId (PK)
        return savedTutors.stream()
                .collect(Collectors.toMap(
                        Tutor::getAuthUserId,
                        tutor -> tutor
                ));
    }


    // =========================================================
    // SAVE TUTOR SUBJECTS
    // =========================================================
    private void saveTutorSubjects(List<TutorSubjectExcelDTO> subjectDTOs,
                                   Map<String, Tutor> tutorMap) {


        tutorSubjectRepository.deleteAll();

        List<TutorSubject> tutorSubjects = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();

        for (TutorSubjectExcelDTO dto : subjectDTOs) {
            String tutorId = dto.getTutorId().trim();
            Tutor tutor = tutorMap.get(tutorId);
            if (tutor == null) {
                throw new RuntimeException("Tutor not found: " + dto.getTutorId());
            }

            // ✅ SUBJECT LOOKUP
            Subject subject = subjectRepository
                    .findBySchoolIdAndNameAndGrade(
                            tutor.getSchoolId(),
                            dto.getSubjectCode(),
                            dto.getGrade()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Subject not found: " + dto.getSubjectCode()
                                            + " (grade " + dto.getGrade() + ")"
                            )
                    );

            String pairKey = tutorId + "|" + subject.getId();
            if (!seenPairs.add(pairKey)) {
                continue; // skip duplicate pair in the same import
            }

            TutorSubject tutorSubject = new TutorSubject();
            tutorSubject.setTutorId(tutor.getAuthUserId());
            tutorSubject.setSubjectId(subject.getId()); // ✅ FIX

            tutorSubjects.add(tutorSubject);
        }

        tutorSubjectRepository.saveAll(tutorSubjects);
    }


}
