package com.airline.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteInfo {
    private Long id;
    private String origin;
    private String destination;
    private BigDecimal distanceKm;
}

