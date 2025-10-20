package com.leo.pillpathbackend.dto.reroute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerouteCandidateDTO {
    private Long pharmacyId;
    private String name;
    private String address;
    private Double distanceKm;
    private Double rating;
    private String deliveryEtaMinutes;
    private Boolean acceptingReroute;
}

