package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.TutorSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TutorSubjectRepository
        extends JpaRepository<TutorSubject, Long> {

    List<TutorSubject> findBySubjectId(Long subjectId);

    List<TutorSubject> findByTutorId(String tutorId);

    boolean existsByTutorIdAndSubjectId(String tutorId, Long subjectId);

    @Query("""
        select ts.tutorId
        from TutorSubject ts
        where ts.subjectId = :subjectId
    """)
    List<String> findTutorIdsBySubjectId(Long subjectId);
}
