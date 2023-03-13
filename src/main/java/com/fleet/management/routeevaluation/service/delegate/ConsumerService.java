package com.fleet.management.routeevaluation.service.delegate;

import com.fleet.management.routeevaluation.model.TrackingDetails;
import com.fleet.management.routeevaluation.service.TrackingDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService implements CommandLineRunner {

    @NotNull
    private final ReactiveKafkaConsumerTemplate<String, TrackingDetails> reactiveKafkaConsumerTemplate;

    @NotNull
    private final TrackingDetailsService trackingDetailsService;

    private Flux<TrackingDetails> consumer() {
        return reactiveKafkaConsumerTemplate
                .receiveAutoAck()
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(),
                        consumerRecord.value(),
                        consumerRecord.topic(),
                        consumerRecord.offset())
                )
                .map(ConsumerRecord::value)
                .flatMap(trackingDetailsService::processTrackingDetails)
                .doOnNext(trackingDetails -> log.info("successfully consumed {}={}", TrackingDetails.class.getSimpleName(), trackingDetails))
                .doOnError(throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()));
    }

    @Override
    public void run(String... args) throws Exception {
        consumer().subscribe();
    }
}