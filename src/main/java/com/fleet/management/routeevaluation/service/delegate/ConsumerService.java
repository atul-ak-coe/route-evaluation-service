package com.fleet.management.routeevaluation.service.delegate;

import com.fleet.management.routeevaluation.exception.ReceiverRecordException;
import com.fleet.management.routeevaluation.model.TrackingDetails;
import com.fleet.management.routeevaluation.service.TrackingDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService implements CommandLineRunner {

    @NotNull
    private final ReactiveKafkaConsumerTemplate<String, TrackingDetails> reactiveKafkaConsumerTemplate;

    @NotNull
    private final TrackingDetailsService trackingDetailsService;

    @Value(value = "${fleet.route-evaluation.consumer.retry-count}")
    private long maxRetryCount;

    private Flux<TrackingDetails> consumer() {
        return reactiveKafkaConsumerTemplate
                .receive()
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> {
                    log.info("received key={}, value={} from topic={}, offset={}",
                            consumerRecord.key(),
                            consumerRecord.value(),
                            consumerRecord.topic(),
                            consumerRecord.offset());
                    if (consumerRecord.value().equals("fail")) {
                        throw new ReceiverRecordException(consumerRecord, new RuntimeException("Failed to consume message."));
                    }
                    consumerRecord.receiverOffset().acknowledge();
                })
                .retryWhen(Retry.backoff(maxRetryCount, Duration.ofMillis(500)).transientErrors(true))
                .map(ConsumerRecord::value)
                .flatMap(trackingDetailsService::processTrackingDetails)
                .doOnNext(trackingDetails -> log.info("successfully consumed {}={}", TrackingDetails.class.getSimpleName(), trackingDetails))
                .onErrorContinue((err, record) -> {
                    ReceiverRecordException ex = (ReceiverRecordException) err.getCause();
                    log.error("Retries exhausted for " + ex.getReceiverRecord().value());
                    ex.getReceiverRecord().receiverOffset().acknowledge();
                });
    }

    @Override
    public void run(String... args) throws Exception {
        consumer().subscribe();
    }
}
