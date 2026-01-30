package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    List<ClassRoom> findBySchoolIdAndActiveTrue(Long schoolId);

    List<ClassRoom> findBySchoolId(Long schoolId);
}
