package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeratorListItemDTO {
    private String id;        // formatted mod_###
    private String username;
    private String createdAt; // yyyy-MM-dd HH:mm:ss
}

