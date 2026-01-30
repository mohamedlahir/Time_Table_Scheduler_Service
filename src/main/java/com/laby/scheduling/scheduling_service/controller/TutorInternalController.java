package com.laby.scheduling.scheduling_service.controller;

import com.laby.scheduling.scheduling_service.entity.Tutor;
import com.laby.scheduling.scheduling_service.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler/internal/tutor")
@RequiredArgsConstructor
public class TutorInternalController {

    private final TutorRepository tutorRepository;

    @PostMapping
    public ResponseEntity<Void> registerTutor(@RequestBody Tutor tutor) {
        tutorRepository.save(tutor);
        return ResponseEntity.ok().build();
    }
}

