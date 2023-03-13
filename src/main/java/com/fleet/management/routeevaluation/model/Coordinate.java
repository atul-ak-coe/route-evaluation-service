package com.fleet.management.routeevaluation.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Coordinate {

    private Integer stepNum;

    private String latitude;

    private String longitude;
}
