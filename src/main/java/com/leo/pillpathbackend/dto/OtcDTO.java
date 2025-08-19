package com.leo.pillpathbackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class OtcDTO {

    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0.01", message = "Price must be a positive number")
    private Double price;

    @NotNull(message = "Stock cannot be empty")
    @Min(value = 0, message = "Stock must be a non-negative integer")
    private Integer stock;

    private String imageUrl;

    private String status;
}