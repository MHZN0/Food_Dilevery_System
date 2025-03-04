// Review.java
package Models;

import Managers.FileHandeler;

import java.util.ArrayList;
import java.util.List;

public class Review {
    private String reviewId;
    private String orderId;
    private String customerId;
    private String vendorId;
    private String itemId;
    private int rating;
    private String comment;
    private static final String REVIEW_FILE = "data/reviews.txt";


    public Review(String reviewId, String orderId,
                  String customerId, String itemId, int rating, String reviewComment) {
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.vendorId = itemId.split("_")[0];
        this.itemId = itemId;
        this.rating = rating;
        this.comment = reviewComment;
    }

    // Getters
    public String getReviewId() {
        return reviewId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getVendorId() {
        return vendorId;
    }


    @Override
    public String toString() {
        return reviewId + "," + orderId + "," + customerId + "," + itemId + "," +
                rating + "," + comment;
    }

}

