package com.leo.pillpathbackend.dto.reroute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerouteCandidatesResponse {
    private List<RerouteCandidateDTO> items;
}

