package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.ClassRoomSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRoomSubjectRepository extends JpaRepository<ClassRoomSubject, Long> {

    List<ClassRoomSubject> findByClassRoomIdAndActiveTrue(Long classRoomId);
}
