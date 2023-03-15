package com.fleet.management.routeevaluation.service.delegate;

import com.fleet.management.routeevaluation.constant.APIConstant;
import com.fleet.management.routeevaluation.model.RouteDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
public class RouteServiceClient {

    @NotNull
    private final WebClient.Builder webClientBuilder;

    @Value("${fleet.route-evaluation.baseUrl.route-details}")
    private String routeDetailsBaseUrl;

    public Mono<RouteDetail> getRouteDetails(String routeId) {
        return webClientBuilder.baseUrl(routeDetailsBaseUrl).build()
                .get()
                .uri(APIConstant.API_GET_ROUTE, routeId)
                .retrieve()
                .bodyToMono(RouteDetail.class);
    }
}
