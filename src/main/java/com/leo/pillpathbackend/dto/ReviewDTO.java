package com.leo.pillpathbackend.dto;

import jakarta.persistence.GeneratedValue;
import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private int id;
    private String email;
    private String reviewText;
    private int rating;
    private LocalDate date;
    private boolean status;
}
