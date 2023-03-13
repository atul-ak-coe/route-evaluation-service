package com.fleet.management.routeevaluation.service.impl;

import com.fleet.management.routeevaluation.model.Coordinate;
import com.fleet.management.routeevaluation.model.Notification;
import com.fleet.management.routeevaluation.model.TrackingDetails;
import com.fleet.management.routeevaluation.service.TrackingDetailsService;
import com.fleet.management.routeevaluation.service.delegate.ProducerService;
import com.fleet.management.routeevaluation.service.delegate.RouteServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingDetailsServiceImpl implements TrackingDetailsService {

    @NotNull
    private final RouteServiceClient routeServiceClient;

    @NotNull
    private final ProducerService producerService;

    @Override
    public Mono<TrackingDetails> processTrackingDetails(TrackingDetails trackingDetails) {
        return Mono.just(trackingDetails)
                .flatMap(this::routeDeviationCheck)
                .flatMap(this::overSpeedCheck)
                .flatMap(this::lowFuelCheck)
                .flatMap(this::emergencyServiceCheck)
                .doOnError(this::alertNotification);
    }

    private void alertNotification(Throwable err) {
        producerService.send(
                Notification.builder().message(err.getMessage()).build());
    }

    private Mono<TrackingDetails> routeDeviationCheck(TrackingDetails trackingDetails) {
        return routeServiceClient.getRouteDetails(trackingDetails.getRouteId())
                .flatMap(rd -> {
                    List<Coordinate> routeCoordinates = rd.getCoordinates();
                    List<Coordinate> trackingCoordinates = trackingDetails.getCoordinates();

                    boolean isRouteFollowed = routeCoordinates.containsAll(trackingCoordinates);
                    if (!isRouteFollowed) {
                        return Mono.error(new RuntimeException("Route not followed."));
                    }
                    return Mono.just(trackingDetails);
                });
    }

    private Mono<TrackingDetails> lowFuelCheck(TrackingDetails trackingDetails) {
        if("LOW".equals(trackingDetails.getFuelLevel())) {
            return Mono.error(new RuntimeException("Low fuel alert."));
        }

        return Mono.just(trackingDetails);
    }

    private Mono<TrackingDetails> emergencyServiceCheck(TrackingDetails trackingDetails) {
        if(trackingDetails.getEmergencyServiceRequire()) {
            return Mono.error(new RuntimeException("Emergency service alert."));
        }

        return Mono.just(trackingDetails);
    }

    private Mono<TrackingDetails> overSpeedCheck(TrackingDetails trackingDetails) {
        if(trackingDetails.getSpeedRate().compareTo(100.00) > 1) {
            return Mono.error(new RuntimeException("Over speed alert."));
        }

        return Mono.just(trackingDetails);
    }
}
