package com.leo.pillpathbackend.util;

import com.leo.pillpathbackend.dto.ReviewDTO;
import com.leo.pillpathbackend.dto.UserDTO;
import com.leo.pillpathbackend.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.modelmapper.*;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final ModelMapper modelMapper;

    public Review convertToReviewEntity(ReviewDTO review) {
        return modelMapper.map(review, Review.class);
    }

    public ReviewDTO convertToReviewDTO(Review review) {
        return modelMapper.map(review, ReviewDTO.class);
    }

    public List<ReviewDTO> convertToReviewDTOList(List<Review> reviews) {
        return reviews.stream()
                .map(this::convertToReviewDTO)
                .toList();
    }
}
