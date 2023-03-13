package com.fleet.management.routeevaluation.service.delegate;

import com.fleet.management.routeevaluation.model.Notification;
import com.fleet.management.routeevaluation.model.TrackingDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducerService {

    @Value(value = "${fleet.topic.notification-topic}")
    private String topic;

    @NotNull
    private final ReactiveKafkaProducerTemplate<String, Notification> kafkaProducerTemplate;

    public void send(Notification notification) {
        log.info("send to topic={}, {}={},", topic, Object.class.getSimpleName(), notification);
        kafkaProducerTemplate.send(topic, notification)
                .doOnSuccess(senderResult -> log.info("sent {} offset : {}", notification, senderResult.recordMetadata().offset()))
                .subscribe();
    }
}
