package Managers;

import Models.MenuItem;
import Models.Order;
import Models.Review;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ReviewManager {

    private ItemManager itemManager;
    private List<Review> reviews;

    public ReviewManager(ItemManager itemManager) {
        this.itemManager = itemManager;
        this.reviews = new ArrayList<>();
        loadReview();
    }

    private static final String REVIEW_FILE = "reviews.txt";

    private void loadReview() {
        List<String> lines = FileHandeler.readFile(REVIEW_FILE);
        for (String line : lines) {
            String[] parts = line.split(",");
            String reviewId = parts[0];
            String orderId = parts[1];
            String customerId = parts[2];
            String itemId = parts[3];
            int rating = Integer.parseInt(parts[4]);
            String reviewComment = parts[5];

            Review review = new Review(reviewId, orderId, customerId, itemId, rating, reviewComment);
            reviews.add(review);
        }
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public List<Review> getReviewByOrderId(String orderId) {
        List<Review> reviewsByOrderId = new ArrayList<>();
        for (Review review : reviews) {
            if (review.getOrderId().equals(orderId)) {
                reviewsByOrderId.add(review);
            }
        }
        return reviewsByOrderId;
    }

    public List<Review> getReviewByItemId(String itemId) {
        List<Review> reviewsByItemId = new ArrayList<>();
        for (Review review : reviews) {
            if (review.getItemId().equals(itemId)) {
                reviewsByItemId.add(review);
            }
        }
        return reviewsByItemId;
    }

    public boolean addReview(String itemId, String orderId, String vendorId, String customerId, int rating, String review) {
        // Generate a new ReviewID based on the ItemID
        String reviewId = orderId + vendorId + "_" + itemId +"_review" + (reviews.size() + 1);

        // Create a new Review object
        Review reviewObj = new Review(reviewId, orderId, customerId, itemId, rating, review);

        // Add the review to the list of reviews
        reviews.add(reviewObj);

        // Save the reviews to a file
        String reviewData = reviewObj.toString();
        FileHandeler.appendToFile("reviews.txt", reviewData);

        return true; // Return true if the review is added successfully
    }

    public List<String[]> getVendorReviews(String vendorId) {
        List<String[]> reviews = new ArrayList<>();
        // Get all items belonging to the vendor
        List<MenuItem> vendorItems = itemManager.getVendorItems(vendorId);

        // Iterate over each item and retrieve its reviews
        for (MenuItem item : vendorItems) {
            List<Review> itemReviews = getReviewByItemId(item.getItemId());

            // Add each review to the list of reviews
            for (Review review : itemReviews) {
                String[] reviewData = new String[] {
                        item.getName(),
                        String.valueOf(review.getRating()),
                        review.getComment()
                };
                reviews.add(reviewData);
            }
        }

        return reviews;
    }
}