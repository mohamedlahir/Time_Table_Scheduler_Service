package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TutorRepository extends JpaRepository<Tutor, String> {

    List<Tutor> findBySchoolIdAndActiveTrue(Long schoolId);


    List<Tutor> findBySchoolId(Long schoolId);

    boolean existsByAuthUserIdAndSchoolIdAndActiveTrue(String tutorId, Long schoolId);

    @Query("select t.maxClassesPerDay from Tutor t where t.authUserId = :tutorId")
    int findMaxClassesPerDay(String tutorId);

}
