package com.Webapp.BikeRentalSystem.Service;

import com.Webapp.BikeRentalSystem.DTO.BookingDTO;
import com.Webapp.BikeRentalSystem.DTO.ReviewDTO;
import com.Webapp.BikeRentalSystem.DTO.ReviewResponse;
import com.Webapp.BikeRentalSystem.Filehandling.ReviewFileHandler;
import com.Webapp.BikeRentalSystem.Model.Review;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewFileHandler reviewFileHandler;
    private final BookingService bookingService;

    public ReviewService(ReviewFileHandler reviewFileHandler, BookingService bookingService) {
        this.reviewFileHandler = reviewFileHandler;
        this.bookingService = bookingService;
    }

    // ---------- read ----------

    public ReviewResponse getAllReviews() {
        try {
            Review[] reviews = reviewFileHandler.findAll();
            return new ReviewResponse(200, "success", null, toDTOs(reviews), average(reviews));
        } catch (IOException e) {
            return ReviewResponse.message(500, e.getMessage());
        }
    }

    public ReviewResponse getReviewsByBike(String bikeId) {
        try {
            List<Review> matches = new ArrayList<>();
            for (Review r : reviewFileHandler.findAll()) {
                if (r.getBikeId().equals(bikeId)) {
                    matches.add(r);
                }
            }
            Review[] reviews = matches.toArray(new Review[0]);
            return new ReviewResponse(200, "success", null, toDTOs(reviews), average(reviews));
        } catch (IOException e) {
            return ReviewResponse.message(500, e.getMessage());
        }
    }

    public ReviewResponse getReviewsByUser(String userId) {
        try {
            List<Review> matches = new ArrayList<>();
            for (Review r : reviewFileHandler.findAll()) {
                if (r.getUserId().equals(userId)) {
                    matches.add(r);
                }
            }
            Review[] reviews = matches.toArray(new Review[0]);
            return new ReviewResponse(200, "success", null, toDTOs(reviews), average(reviews));
        } catch (IOException e) {
            return ReviewResponse.message(500, e.getMessage());
        }
    }

    public ReviewResponse getReviewById(String reviewId) {
        try {
            Review review = reviewFileHandler.findById(reviewId);
            if (review == null) {
                return ReviewResponse.message(404, "Review not found");
            }
            return new ReviewResponse(200, "success", toDTO(review), null, 0);
        } catch (IOException e) {
            return ReviewResponse.message(500, e.getMessage());
        }
    }

    public double getAverageRating(String bikeId) {
        return getReviewsByBike(bikeId).getAverageRating();
    }

    // ---------- create ----------

    public synchronized ReviewResponse saveReview(ReviewDTO dto, String userId) {

        if (userId == null || userId.isBlank()) {
            return ReviewResponse.message(401, "Please sign in to write a review");
        }
        if (dto.getBikeId() == null || dto.getBikeId().isBlank()) {
            return ReviewResponse.message(400, "Bike ID is required");
        }
        String error = validateRatingAndComment(dto);
        if (error != null) {
            return ReviewResponse.message(400, error);
        }

        String bikeId = dto.getBikeId().trim();

        try {
            // rule: one review per user per bike
            for (Review r : reviewFileHandler.findAll()) {
                if (r.getUserId().equals(userId) && r.getBikeId().equals(bikeId)) {
                    return ReviewResponse.message(409, "You have already reviewed this bike. Edit your review instead.");
                }
            }

            Review saved = reviewFileHandler.create(new Review(
                    null, userId, bikeId, dto.getRating(), cleanComment(dto.getComment()), LocalDate.now()));

            return new ReviewResponse(200, "Review added", toDTO(saved), null, 0);

        } catch (IOException e) {
            return ReviewResponse.message(500, e.getMessage());
        }
    }
    // ---------- update ----------

    public synchronized ReviewResponse updateReview(String reviewId, ReviewDTO dto, String userId, String role) {

        String error = validateRatingAndComment(dto);
        if (error != null) {
            return ReviewResponse.message(400, error);
        }

        try {
            Review review = reviewFileHandler.findById(reviewId);
            if (review == null) {
                return ReviewResponse.message(404, "Review not found");
            }
            if (!canManage(review, userId, role)) {
                return ReviewResponse.message(403, "You can only edit your own review");
            }

            // only the rating and comment can change
            review.setRating(dto.getRating());
            review.setComment(cleanComment(dto.getComment()));
            reviewFileHandler.update(review);

            return new ReviewResponse(200, "Review updated", toDTO(review), null, 0);

        } catch (IOException e) {
            return ReviewResponse.message(500, e.getMessage());
        }
    }

    // ---------- delete ----------

    public synchronized ReviewResponse deleteReview(String reviewId, String userId, String role) {
        try {
            Review review = reviewFileHandler.findById(reviewId);
            if (review == null) {
                return ReviewResponse.message(404, "Review not found");
            }
            if (!canManage(review, userId, role)) {
                return ReviewResponse.message(403, "You can only delete your own review");
            }

            reviewFileHandler.delete(reviewId);
            return ReviewResponse.message(200, "Review deleted");

        } catch (IOException e) {
            return ReviewResponse.message(500, e.getMessage());
        }
    }

    // ---------- rules and helpers ----------

    private boolean canManage(
            Review review,
            String userId,
            String role) {

        return "Customer".equalsIgnoreCase(role)
                && userId != null
                && userId.equals(review.getUserId());
    }

    private boolean hasCompletedBooking(String userId, String bikeId) {
        BookingDTO[] bookings = bookingService.getAllBookings().getBookings();
        if (bookings == null) {
            return false;
        }
        for (BookingDTO b : bookings) {
            if (userId.equals(b.getUserId())
                    && bikeId.equals(b.getBikeId())
                    && "Completed".equalsIgnoreCase(b.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private String validateRatingAndComment(ReviewDTO dto) {
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            return "Rating must be a whole number from 1 to 5";
        }
        if (dto.getComment() != null && dto.getComment().length() > 300) {
            return "Comment must be 300 characters or fewer";
        }
        return null;
    }

    private String cleanComment(String comment) {
        return comment == null ? "" : comment.trim().replace('|', '/');
    }

    private double average(Review[] reviews) {
        if (reviews.length == 0) {
            return 0;
        }
        int total = 0;
        for (Review r : reviews) {
            total += r.getRating();
        }
        return Math.round((total * 10.0) / reviews.length) / 10.0;   // one decimal, e.g. 4.8
    }

    private ReviewDTO toDTO(Review r) {
        return new ReviewDTO(r.getReviewId(), r.getUserId(), r.getBikeId(),
                r.getRating(), r.getComment(), r.getReviewDate());
    }

    private ReviewDTO[] toDTOs(Review[] reviews) {
        ReviewDTO[] dtos = new ReviewDTO[reviews.length];
        for (int i = 0; i < reviews.length; i++) {
            dtos[i] = toDTO(reviews[i]);
        }
        return dtos;
    }
}