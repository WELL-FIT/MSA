package com.lecture.enrollment.service;

import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentWriteService {

    private final EnrollmentRepository enrollmentRepository;

    /**
     * 반드시 독립 트랜잭션으로 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Enrollment createPendingEnrollment(Long userId, Long courseId) {

        Enrollment enrollment = enrollmentRepository.save(
                Enrollment.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .build()
        );

        log.info("[EnrollmentWriteService] PENDING enrollment 생성 완료 - enrollmentId: {}, userId: {}, courseId: {}",
                enrollment.getId(), userId, courseId);

        return enrollment;
    }
}