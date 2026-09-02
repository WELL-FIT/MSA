package com.lecture.enrollment.repository;

import com.lecture.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByUserId(Long userId);

    List<Enrollment> findByUserIdAndStatus(Long userId, Enrollment.Status status);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // 수강 완료(ACTIVE)된 강의 ID 목록 - 추천 서비스용
    List<Enrollment> findByUserIdAndStatusIn(Long userId, List<Enrollment.Status> statuses);
}
