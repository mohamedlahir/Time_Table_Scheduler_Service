package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findBySchoolIdAndActiveTrue(Long schoolId);
}
