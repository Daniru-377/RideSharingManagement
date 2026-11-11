package com.Webapp.BikeRentalSystem.DTO;

import java.time.LocalDate;

public class ReviewDTO {

    private String reviewId;
    private String userId;
    private String bikeId;
    private int rating = 5;
    private String comment;
    private LocalDate reviewDate;

    public ReviewDTO() {
    }

    public ReviewDTO(String reviewId, String userId, String bikeId, int rating, String comment, LocalDate reviewDate) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.bikeId = bikeId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBikeId() { return bikeId; }
    public void setBikeId(String bikeId) { this.bikeId = bikeId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
}