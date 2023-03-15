package com.fleet.management.routeevaluation.service.delegate;

import com.fleet.management.routeevaluation.model.Notification;
import com.fleet.management.routeevaluation.model.TrackingDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducerService {

    @Value(value = "${fleet.route-evaluation.producer.topic.notification-topic}")
    private String topic;

    @Value(value = "${fleet.route-evaluation.producer.retry-count}")
    private long maxRetryCount;

    @NotNull
    private final ReactiveKafkaProducerTemplate<String, Notification> kafkaProducerTemplate;

    public void send(Notification notification) {
        log.info("send to topic={}, {}={},", topic, Object.class.getSimpleName(), notification);
        kafkaProducerTemplate.send(topic, notification)
                .doOnSuccess(senderResult -> log.info("sent {} offset : {}", notification, senderResult.recordMetadata().offset()))
                .retryWhen(Retry.backoff(maxRetryCount, Duration.ofMillis(200)).transientErrors(true))
                .onErrorResume(err -> {
                    log.info("Retries exhausted for " + notification);
                    return Mono.empty();
                })
                .subscribe();
    }
}
