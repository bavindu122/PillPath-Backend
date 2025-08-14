package com.leo.pillpathbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddAnnouncementRequest {
    @NotBlank(message = "Title cannot be empty")
    private String title;
    @NotBlank(message = "Content cannot be empty")
    private String content;
    private LocalDate expiryDate;
}
