// Order.java
package Models;

import java.time.LocalDateTime;

public class Order {
    private String orderId;
    private String customerId;
    private String vendorId;
    private String itemId;
    private String runnerId;
    private double amount;
    private String status; // PENDING, ACCEPTED, REJECTED, DELIVERED, CANCELLED
    private LocalDateTime orderTime;
    private String deliveryType; // DINE_IN, TAKEAWAY, DELIVERY
    private String updatedBy; // Move this field to the top


    public Order(String orderId, String customerId, String vendorId, String itemId,
                 double amount, String deliveryType) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.vendorId = vendorId;
        this.itemId = itemId;
        this.amount = amount;
        this.status = "PENDING";
        this.orderTime = LocalDateTime.now();
        this.deliveryType = deliveryType;
        this.updatedBy = "Unknown"; // Initialize with a default value
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getRunnerId() {
        return runnerId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public String getDeliveryBy() {
        return runnerId;
    }

    public double getPrice() {
        return amount;
    }

    public String getUpdatedBy() {
        return updatedBy;
    } // Add a getter for updatedBy

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRunnerId(String runnerId) {
        this.runnerId = runnerId;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    } // Move this method to the top

    @Override
    public String toString() {
        return orderId + "," + customerId + "," + vendorId + "," + itemId + "," +
                runnerId + "," + amount + "," + status + "," + orderTime + "," + deliveryType + "," + updatedBy;
    }

    public String getItemName() {
        String vendorId = Managers.ItemManager.getInstance().getVendorIdFromItem(itemId);
        MenuItem item = Managers.ItemManager.getInstance().getItem(vendorId, itemId);
        return item != null ? item.getName() : "Unknown Item";
    }


    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }
}

