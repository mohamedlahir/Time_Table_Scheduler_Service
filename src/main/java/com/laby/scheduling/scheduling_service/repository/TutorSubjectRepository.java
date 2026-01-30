package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.TutorSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorSubjectRepository
        extends JpaRepository<TutorSubject, Long> {

    List<TutorSubject> findBySubjectId(Long subjectId);

    List<TutorSubject> findByTutorId(String tutorId);

    boolean existsByTutorIdAndSubjectId(String tutorId, Long subjectId);
}
