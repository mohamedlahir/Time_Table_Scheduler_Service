package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorRepository extends JpaRepository<Tutor, String> {

    List<Tutor> findBySchoolIdAndActiveTrue(Long schoolId);


    List<Tutor> findBySchoolId(Long schoolId);
}
