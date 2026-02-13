package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findBySchoolIdAndActiveTrue(Long schoolId);

    List<Subject> findBySchoolIdAndGradeAndActiveTrue(Long schoolId, String grade);

    Optional<Subject> findBySchoolIdAndNameAndGrade(Long schoolId, String name, String grade);
//    Optional<Subject> findBySubjectCode(String subjectCode);
    Optional<Subject> findByName(String name); // ✅ MATCHES ENTITY
}
