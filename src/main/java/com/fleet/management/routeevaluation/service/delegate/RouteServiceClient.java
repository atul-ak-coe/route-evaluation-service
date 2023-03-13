package com.fleet.management.routeevaluation.service.delegate;

import com.fleet.management.routeevaluation.constant.APIConstant;
import com.fleet.management.routeevaluation.model.RouteDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
public class RouteServiceClient {

    @NotNull
    private final WebClient.Builder webClientBuilder;

    public Mono<RouteDetail> getRouteDetails(String routeId) {
        return webClientBuilder.build()
                .get()
                .uri(APIConstant.API_GET_ROUTE, routeId)
                .retrieve()
                .bodyToMono(RouteDetail.class);
    }
}
