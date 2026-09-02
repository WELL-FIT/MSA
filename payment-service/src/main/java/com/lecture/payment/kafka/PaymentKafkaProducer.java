package com.lecture.payment.kafka;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.payment-completed}")
    private String paymentCompletedTopic;

    /**
     * payment.completed 이벤트 발행
     * → Enrollment Service가 수신하여 수강 활성화
     *
     * 개발/검증 단계에서는 전송 성공 여부를 즉시 확인하기 위해 동기적으로 기다린다.
     */
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("[Kafka Producer] payment.completed 발행 시도 - topic: {}, paymentId: {}, userId: {}, courseId: {}",
                paymentCompletedTopic, event.getPaymentId(), event.getUserId(), event.getCourseId());

        try {
            SendResult<String, Object> result = kafkaTemplate
                    .send(paymentCompletedTopic, String.valueOf(event.getUserId()), event)
                    .get(10, TimeUnit.SECONDS);

            log.info("[Kafka Producer] payment.completed 발행 성공 - topic: {}, partition: {}, offset: {}",
                    paymentCompletedTopic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("[Kafka Producer] payment.completed 발행 실패 - topic: {}, paymentId: {}, userId: {}, courseId: {}, error: {}",
                    paymentCompletedTopic,
                    event.getPaymentId(),
                    event.getUserId(),
                    event.getCourseId(),
                    e.getMessage(),
                    e);

            throw new RuntimeException("payment.completed Kafka 발행 실패", e);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentCompletedEvent {
        private Long paymentId;
        private Long userId;
        private Long courseId;
        private String status;
    }
}