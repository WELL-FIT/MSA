package com.lecture.enrollment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.enrollment-completed}")
    private String enrollmentCompletedTopic;

    /**
     * enrollment.completed 이벤트 발행
     * → Recommend Service가 수신하여 추천 갱신
     */
    public void publishEnrollmentCompleted(KafkaEvent.EnrollmentCompletedEvent event) {
        log.info("[Kafka Producer] enrollment.completed 발행 - enrollmentId: {}, userId: {}, courseId: {}",
                event.getEnrollmentId(), event.getUserId(), event.getCourseId());

        kafkaTemplate.send(enrollmentCompletedTopic, String.valueOf(event.getUserId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka Producer] enrollment.completed 발행 실패: {}", ex.getMessage());
                    } else {
                        log.info("[Kafka Producer] enrollment.completed 발행 성공 - offset: {}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
