package com.fleet.management.routeevaluation.service;

import com.fleet.management.routeevaluation.model.TrackingDetails;
import reactor.core.publisher.Mono;

public interface TrackingDetailsService {

    Mono<TrackingDetails> processTrackingDetails(TrackingDetails trackingDetails);
}
