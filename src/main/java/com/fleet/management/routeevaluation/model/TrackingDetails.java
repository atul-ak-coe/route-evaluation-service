package com.fleet.management.routeevaluation.model;

import lombok.*;

import java.util.Map;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class TrackingDetails {

    private Long trackingId;

    private String routeId;

    private Integer stepNum;

    private Boolean isDiversion;

    private String fuelLevel;

    private Boolean emergencyServiceRequire;

    private Double speedRate;

    private Map<Integer, Coordinate> coordinates;
}
