package com.Webapp.BikeRentalSystem.Filehandling;

import com.Webapp.BikeRentalSystem.Model.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewFileHandler {

    private final Path file;

    public ReviewFileHandler(@Value("${review.file.path}") String path) throws IOException {

        file = Paths.get(path).toAbsolutePath().normalize();

        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        if (Files.notExists(file)) {
            Files.createFile(file);
        }
    }

    public synchronized Review[] findAll() throws IOException {
        List<Review> reviews = new ArrayList<>();

        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            try {
                reviews.add(Review.fromFileString(line));
            } catch (IllegalArgumentException ex) {
                System.out.println("Skipping invalid review line: " + line);
            }
        }

        return reviews.toArray(new Review[0]);
    }

    public synchronized Review findById(String id) throws IOException {
        for (Review review : findAll()) {
            if (review.getReviewId().equals(id)) {
                return review;
            }
        }
        return null;
    }

    public synchronized Review create(Review save) throws IOException {

        int nextId = 1;

        for (Review review : findAll()) {
            String reviewId = review.getReviewId();
            try {
                if (reviewId != null && reviewId.startsWith("RV")) {
                    int currentId = Integer.parseInt(reviewId.substring(2));
                    if (currentId >= nextId) {
                        nextId = currentId + 1;
                    }
                }
            } catch (NumberFormatException ignored) {
                System.out.println(ignored);
            }
        }

        Review saved = new Review(
                String.format("RV%03d", nextId),
                save.getUserId(),
                save.getBikeId(),
                save.getRating(),
                save.getComment(),
                save.getReviewDate()
        );

        Files.writeString(file,
                saved.toFileString() + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);

        return saved;
    }

    public synchronized boolean update(Review update) throws IOException {

        Review[] reviews = findAll();
        boolean found = false;

        for (int i = 0; i < reviews.length; i++) {
            if (reviews[i].getReviewId().equals(update.getReviewId())) {
                reviews[i] = update;
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        writeAll(reviews);
        return true;
    }

    public synchronized boolean delete(String id) throws IOException {

        Review[] reviews = findAll();
        List<Review> kept = new ArrayList<>();

        for (Review review : reviews) {
            if (!review.getReviewId().equals(id)) {
                kept.add(review);
            }
        }

        if (kept.size() == reviews.length) {
            return false;
        }

        writeAll(kept.toArray(new Review[0]));
        return true;
    }

    private void writeAll(Review[] reviews) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Review review : reviews) {
            lines.add(review.toFileString());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}