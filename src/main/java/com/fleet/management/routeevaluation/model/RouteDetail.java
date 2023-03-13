package com.fleet.management.routeevaluation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RouteDetail {

    private Long routeId;

    private String routeName;

    private String routeDesc;

    private List<Coordinate> coordinates;

    private double distance;

    private String routeType;
}
