package com.lecture.payment.service;

import com.lecture.payment.dto.PaymentDto;
import com.lecture.payment.entity.Payment;
import com.lecture.payment.kafka.PaymentKafkaProducer;
import com.lecture.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentKafkaProducer kafkaProducer;

    /**
     * 내부 결제 요청 (Enrollment Service → Payment Service REST 호출)
     * 실습 환경에서는 PG 연동 없이 항상 성공으로 처리
     *
     * 처리 흐름:
     * 1. Payment 생성 (PENDING)
     * 2. PG 결제 처리 (실습: UUID 트랜잭션 ID 발급으로 대체)
     * 3. Payment 상태 → COMPLETED
     * 4. payment.completed 이벤트 발행 → Kafka
     */
    @Transactional
    public PaymentDto.InternalPaymentResult processInternalPayment(
            PaymentDto.InternalPaymentRequest request) {

        log.info("[PaymentService] 결제 요청 - userId: {}, courseId: {}, amount: {}",
                request.getUserId(), request.getCourseId(), request.getAmount());

        Payment payment = paymentRepository.save(
                Payment.builder()
                        .userId(request.getUserId())
                        .courseId(request.getCourseId())
                        .amount(request.getAmount())
                        .build()
        );

        try {
            String transactionId = UUID.randomUUID().toString();

            payment.complete(transactionId);
            log.info("[PaymentService] 결제 완료 처리 - paymentId: {}, transactionId: {}",
                    payment.getId(), transactionId);

            kafkaProducer.publishPaymentCompleted(
                    PaymentKafkaProducer.PaymentCompletedEvent.builder()
                            .paymentId(payment.getId())
                            .userId(request.getUserId())
                            .courseId(request.getCourseId())
                            .status("COMPLETED")
                            .build()
            );

            log.info("[PaymentService] 결제 최종 성공 - paymentId: {}", payment.getId());

            return PaymentDto.InternalPaymentResult.builder()
                    .paymentId(payment.getId())
                    .status("COMPLETED")
                    .build();

        } catch (Exception e) {
            payment.fail();

            log.error("[PaymentService] 결제 실패 - paymentId: {}, userId: {}, courseId: {}, error: {}",
                    payment.getId(),
                    request.getUserId(),
                    request.getCourseId(),
                    e.getMessage(),
                    e);

            return PaymentDto.InternalPaymentResult.builder()
                    .paymentId(payment.getId())
                    .status("FAILED")
                    .build();
        }
    }

    /**
     * 결제 단건 조회
     */
    public PaymentDto.PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + id));
        return PaymentDto.PaymentResponse.from(payment);
    }

    /**
     * 사용자 결제 내역 조회
     */
    public List<PaymentDto.PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(PaymentDto.PaymentResponse::from)
                .collect(Collectors.toList());
    }
}