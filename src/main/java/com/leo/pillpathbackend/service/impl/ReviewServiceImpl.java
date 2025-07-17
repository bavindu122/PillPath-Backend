package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.ReviewDTO;
import com.leo.pillpathbackend.entity.Review;
import com.leo.pillpathbackend.repository.ReviewRepository;
import com.leo.pillpathbackend.service.ReviewService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final Mapper mapper;

    @Override
    public ReviewDTO saveReview(ReviewDTO reviewDTO) {
        Review tempReview = mapper.convertToReviewEntity(reviewDTO);
        tempReview.setDate(LocalDate.now());
        Review savedReview = reviewRepository.save(tempReview);
        return mapper.convertToReviewDTO(savedReview);
    }

    @Override
    public ReviewDTO getReviewById(int id) {

        return null;
    }

    @Override
    public ReviewDTO updateReview(ReviewDTO reviewDTO) {
        Review existingReview = reviewRepository.findById(reviewDTO.getId())
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewDTO.getId()));

        existingReview.setEmail(reviewDTO.getEmail());
        existingReview.setReviewText(reviewDTO.getReviewText());
        existingReview.setRating(reviewDTO.getRating());
        existingReview.setStatus(reviewDTO.isStatus());

        Review updatedReview = reviewRepository.save(existingReview);
        return mapper.convertToReviewDTO(updatedReview);
    }

    @Override
    public void deleteReview(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        reviewRepository.delete(review);
    }

    @Override
    public List<ReviewDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return mapper.convertToReviewDTOList(reviews);
    }
}
