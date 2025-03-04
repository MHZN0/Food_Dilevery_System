// Transaction.java
package Models;

import java.time.LocalDateTime;
import java.util.List;

public class Transaction {
    private String transactionId;
    private String orderId; // New field to store the order ID
    private String userId;
    private double amount;
    private String type; // TOP_UP, PAYMENT, EARNING
    private LocalDateTime timestamp;

    public Transaction(String transactionId, String orderId, String userId, double amount, String type) {
        this.transactionId = transactionId;
        this.orderId = orderId; // Initialize the orderId field in the constructor
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getOrderId() {
        return orderId;
    } // New getter method for the orderId field

    @Override
    public String toString() {
        return transactionId + "," + userId + "," + amount + "," + type + "," + timestamp + "," + orderId;
    }
}

