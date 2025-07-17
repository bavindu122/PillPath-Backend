package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    ReviewDTO saveReview(ReviewDTO reviewDTO);
    ReviewDTO getReviewById(int id);
    ReviewDTO updateReview(ReviewDTO reviewDTO);
    void deleteReview(int id);
    List<ReviewDTO> getAllReviews();
}
