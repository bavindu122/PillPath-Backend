package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private String userName;
    private Integer rating;
    private String comment;
    private String dateString; // Use String for display purposes
    private Long customerId;
    private Long pharmacyId;
    private LocalDateTime createdAt;
    
    // Constructor for mock data (matches what you're using in PharmacyServiceImpl)
    public ReviewDTO(Long id, String userName, Integer rating, String comment, String dateString) {
        this.id = id;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.dateString = dateString;
    }

    public String getEmail() {
        return "";
    }

    public String getReviewText() {
        return "";
    }

    public boolean isStatus() {
        return false;
    }
}