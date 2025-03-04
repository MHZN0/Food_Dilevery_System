package Managers;

import Models.Order;
import Models.MenuItem;
import java.util.*;
import java.time.LocalDateTime;

public class OrderManager {
    private static OrderManager instance;
    private Map<String, Order> orders;
    private static final String ORDERS_FILE = "orders.txt";


    public OrderManager() {
        orders = new HashMap<>();
        loadOrders();
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    private void loadOrders() {
        List<String> lines = FileHandeler.readFile(ORDERS_FILE);
        for (String line : lines) {
            String[] parts = line.split(",");
            String orderId = parts[0];
            String customerId = parts[1];
            String vendorId = parts[2];
            String itemId = parts[3];
            String runnerId = parts[4].isEmpty() ? null : parts[4];
            double amount = Double.parseDouble(parts[5]);
            String status = parts[6];
            String deliveryType = parts[8];


            Order order = new Order(orderId, customerId, vendorId, itemId, amount, deliveryType);
            order.setStatus(status);
            if (runnerId != null) {
                order.setRunnerId(runnerId);
            }
            orders.put(orderId, order);
        }
    }

    public String generateOrderId() {
        int count = 1;
        while (orders.containsKey("O" + String.format("%03d", count))) {
            count++;
        }
        return "O" + String.format("%03d", count);
    }

    public Order createOrder(String customerId, String vendorId, String itemId, String deliveryType) {
        if (vendorId == null) {
            System.out.println("Vendor ID is required to create an order");
            return null;
        }

        // Validate customer balance first
        double customerBalance = TransactionManager.getInstance().calculateBalance(customerId);
        MenuItem item = ItemManager.getInstance().getItem(vendorId, itemId);
        System.out.println("Creating order for customer " + customerId + " with vendor " + vendorId + " and item " + itemId);

        // Check if item is null
        if (item == null) {
            System.out.println("Item not found for vendor " + vendorId + " and item " + itemId);
            return null;
        }

        // Check if customer balance is sufficient
        if (customerBalance < item.getPrice()) {
            return null;
        }

        String orderId = generateOrderId();
        Order order = new Order(orderId, customerId, vendorId, itemId, item.getPrice(), deliveryType);
        orders.put(orderId, order);

        // Create transaction for payment
        TransactionManager.getInstance().createTransaction(customerId, item.getPrice(), "PAYMENT", orderId);

        // Notify vendor
        NotificationManager.getInstance().createNotification(
                vendorId,
                "New order received: " + orderId + " for " + item.getName()
        );

        FileHandeler.appendToFile(ORDERS_FILE, order.toString());
        return order;
    }

    public boolean updateOrderStatus(String orderId, String status, String updatedBy) {
        if (!orders.containsKey(orderId)) {
            return false;
        }

        Order order = orders.get(orderId);
        String oldStatus = order.getStatus();
        order.setStatus(status);
        if (updatedBy != null) {
            order.setUpdatedBy(updatedBy);
        }
        orders.put(orderId, order); // Update the orders map

        // Handle notifications based on status change
        switch (status) {
            case "ACCEPTED":
                NotificationManager.getInstance().createNotification(
                        order.getCustomerId(),
                        "Your order " + orderId + " has been accepted by the vendor"
                );
                break;
            case "REJECTED":
                // Refund the customer
                TransactionManager.getInstance().createTransaction(
                        order.getCustomerId(),
                        order.getAmount(),
                        "REFUND",
                        orderId
                );
                NotificationManager.getInstance().createNotification(
                        order.getCustomerId(),
                        "Your order " + orderId + " has been rejected. Amount refunded."
                );
                break;
            case "DELIVERED":
                // Add revenue to vendor
                TransactionManager.getInstance().createTransaction(
                        order.getVendorId(),
                        order.getAmount() * 0.8, // 80% to vendor
                        "EARNING",
                        orderId
                );
                // Add earnings to runner
                if (order.getRunnerId() != null) {
                    TransactionManager.getInstance().createTransaction(
                            order.getRunnerId(),
                            order.getAmount() * 0.2, // 20% to runner
                            "EARNING",
                            orderId
                    );
                }
                NotificationManager.getInstance().createNotification(
                        order.getCustomerId(),
                        "Your order " + orderId + " has been delivered"
                );
                break;
        }
        saveOrders(); // Call saveOrders() only once at the end of the method
        return true;
    }

    public boolean assignRunner(String orderId, String runnerId) {
        System.out.println("Assigning runner to order: " + orderId);
        if (!orders.containsKey(orderId)) {
            System.out.println("Order not found: " + orderId);
            return false;
        }

        Order order = orders.get(orderId);
        if (order.getRunnerId() != null && !order.getRunnerId().equals("null") && !order.getStatus().equals("ACCEPTED")) {
            System.out.println("Order already has runner or status is not ACCEPTED: " + orderId);
            return false;
        }

        order.setRunnerId(runnerId);
        order.setStatus("DELIVERING"); // Update status to "DELIVERING"
        order.setDeliveryType("Delivery"); // Update delivery type to "runner"
        saveOrders();

        // Notify customer and vendor
        NotificationManager.getInstance().createNotification(
                order.getCustomerId(),
                "A runner has been assigned to your order: " + orderId
        );
        NotificationManager.getInstance().createNotification(
                order.getVendorId(),
                "Runner assigned to order: " + orderId
        );

        return true;
    }

    public void saveOrders() {
        System.out.println("Saving orders to file...");
        System.out.println("File path: " + ORDERS_FILE);

        List<String> lines = new ArrayList<>();
        for (Order order : orders.values()) {
            String orderData = order.toString();
            lines.add(orderData);
            System.out.println("Order: " + orderData);
        }

        try {
            FileHandeler.writeFile(ORDERS_FILE, lines);
            System.out.println("Orders saved to file.");
        } catch (Exception e) {
            System.out.println("Error writing orders to file: " + e.getMessage());
        }
    }

    public List<Order> getCustomerOrders(String customerId) {
        return orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .toList();
    }

    public List<Order> getVendorOrders(String vendorId) {
        return orders.values().stream()
                .filter(order -> order.getVendorId().equals(vendorId))
                .toList();
    }

    public List<Order> getVendorOrdersByStatus(String vendorId, String status) {
        return orders.values().stream()
                .filter(order -> order.getVendorId().equals(vendorId) &&
                        order.getStatus().equals(status))
                .toList();
    }

    public List<Order> getOrderByStatus(String status) {
        return orders.values().stream()
                .filter(order -> order.getStatus().equals(status))
                .toList();
    }

    public List<Order> getRunnerTasks(String runnerId) {
        try {
            // Retrieve tasks from database or data storage system
            List<Order> tasks = orders.values().stream()
                    .filter(order -> order.getRunnerId() != null && order.getRunnerId().equals(runnerId))
                    .toList();
            System.out.println("Retrieved tasks for runner " + runnerId + ": " + tasks.size());
            return tasks;
        } catch (Exception e) {
            System.out.println("Error retrieving tasks for runner " + runnerId + ": " + e.getMessage());
            return new ArrayList<>(); // Return an empty list if an error occurs
        }
    }

    public List<Order> getAvailableDeliveries() {
        return orders.values().stream()
                .filter(order -> order.getStatus().equals("ACCEPTED") &&
                        (order.getRunnerId() == null || order.getRunnerId().equals("null")))
                .toList();
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public Map<String, Double> getVendorRevenue(String vendorId, LocalDateTime start, LocalDateTime end) {
        Map<String, Double> revenue = new HashMap<>();
        revenue.put("total", 0.0);
        revenue.put("delivery", 0.0);
        revenue.put("takeaway", 0.0);
        revenue.put("dine_in", 0.0);

        orders.values().stream()
                .filter(order -> order.getVendorId().equals(vendorId) &&
                        order.getStatus().equals("DELIVERED") &&
                        (start == null || order.getOrderTime().isAfter(start)) &&
                        (end == null || order.getOrderTime().isBefore(end)))
                .forEach(order -> {
                    double amount = order.getAmount();
                    revenue.put("total", revenue.get("total") + amount);
                    String deliveryMethod = order.getDeliveryBy().toLowerCase();
                    revenue.put(deliveryMethod, revenue.getOrDefault(deliveryMethod, 0.0) + amount);
                });

        return revenue;
    }

    public String getItemIdFromOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            return null;
        }
        return order.getItemId();
    }

    public String getVendorIdFromOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            return null;
        }
        return order.getVendorId();
    }


    public boolean cancelOrder(String orderId) {
        if (!orders.containsKey(orderId)) {
            System.out.println("Order not found: " + orderId);
            return false;
        }

        Order order = orders.get(orderId);
        if (order.getStatus().equals("DELIVERED") || order.getStatus().equals("CANCELLED")) {
            System.out.println("Order already " + order.getStatus());
            return false;
        }

        order.setStatus("CANCELLED");
        System.out.println("Order status updated to CANCELLED for: " + orderId);
        saveOrders();

        // Refund the customer
        TransactionManager.getInstance().createTransaction(order.getCustomerId(), order.getAmount(), "REFUND", orderId);

        // Notify customer and vendor
        NotificationManager.getInstance().createNotification(order.getCustomerId(), "Your order " + orderId + " has been cancelled");
        NotificationManager.getInstance().createNotification(order.getRunnerId(), "You have cancelled the order " + orderId + ".");
        NotificationManager.getInstance().createNotification(order.getVendorId(), "Order " + orderId + " has been cancelled");

        return true;
    }


    public boolean rejectDelivery(String orderId, String runnerId) {
        // Check if the order exists
        if (!orders.containsKey(orderId)) {
            return false;
        }

        // Get the order object
        Order order = orders.get(orderId);

        // Check if the order is already assigned to a runner
        if (order.getRunnerId() != null && !order.getRunnerId().equals(runnerId)) {
            return false;
        }

        // Update the order status to "REJECTED"
        order.setStatus("REJECTED");

        // Save the updated order
        saveOrders();

        // Notify the customer and vendor
        NotificationManager.getInstance().createNotification(order.getCustomerId(), "Your order " + orderId + " has been rejected by the runner");
        NotificationManager.getInstance().createNotification(order.getVendorId(), "Order " + orderId + " has been rejected by the runner");

        return true;
    }

    public boolean completeDelivery(String orderId) {
        System.out.println("Accepting order " + orderId);
        if (!orders.containsKey(orderId)) {
            return false;
        }

        Order order = orders.get(orderId);
        System.out.println("Order status: " + order.getStatus());
        if (!order.getStatus().equalsIgnoreCase("DELIVERING")) {
            return false;
        }

        // Update the order status to "ACCEPTED"
        order.setStatus("DELIVERED");
        orders.put(orderId, order); // Update the orders map with the new status
        saveOrders(); // Call saveOrders after updating the orders map

        // Notify customer and vendor
        NotificationManager.getInstance().createNotification(order.getCustomerId(), "Your order " + orderId + " has been delivered.");
        NotificationManager.getInstance().createNotification(order.getRunnerId(), "You have delivered the order " + orderId + " successfully..");
        NotificationManager.getInstance().createNotification(order.getVendorId(), "Order " + orderId + " has been delivered.");

        return true;
    }

    public List<String[]> getRunnerDeliveryHistory(String runnerId) {
        // Implement logic to retrieve delivery history for the given runner ID
        // For example:
        List<String[]> deliveryHistory = new ArrayList<>();
        List<Order> orders = getRunnerTasks(runnerId);
        for (Order order : orders) {
            deliveryHistory.add(new String[]{order.getOrderId(), order.getCustomerId(), order.getVendorId(), order.getItemId(), order.getStatus()});
        }
        return deliveryHistory;
    }

    public boolean acceptOrder(String orderId) {
        System.out.println("Accepting order " + orderId);
        if (!orders.containsKey(orderId)) {
            return false;
        }

        Order order = orders.get(orderId);
        System.out.println("Order status: " + order.getStatus());
        if (!order.getStatus().equalsIgnoreCase("pending")) {
            return false;
        }

        // Update the order status to "ACCEPTED"
        order.setStatus("ACCEPTED");
        orders.put(orderId, order); // Update the orders map with the new status
        saveOrders(); // Call saveOrders after updating the orders map

        // Notify customer and vendor
        NotificationManager.getInstance().createNotification(order.getCustomerId(), "Your order " + orderId + " has been accepted");
        NotificationManager.getInstance().createNotification(order.getRunnerId(), "You have accepted the order " + orderId + ".");
        NotificationManager.getInstance().createNotification(order.getVendorId(), "Order " + orderId + " has been accepted");

        return true;
    }

    public boolean rejectOrder(String orderId) {
        System.out.println("Rejecting order " + orderId);
        if (!orders.containsKey(orderId)) {
            return false;
        }

        Order order = orders.get(orderId);
        System.out.println("Order status: " + order.getStatus());
        if (!order.getStatus().equalsIgnoreCase("pending")) {
            return false;
        }

        // Update the order status to "REJECTED"
        order.setStatus("REJECTED");
        orders.put(orderId, order); // Update the orders map with the new status
        saveOrders();

        // Notify customer and vendor
        NotificationManager.getInstance().createNotification(order.getCustomerId(), "Your order " + orderId + " has been rejected");
        NotificationManager.getInstance().createNotification(order.getVendorId(), "Order " + orderId + " has been rejected");

        // Refund the customer (if applicable)
        TransactionManager.getInstance().createTransaction(order.getCustomerId(), order.getAmount(), "REFUND", orderId);

        return true;
    }
    public List<String[]> getVendorOrderHistory(String vendorId) {
        List<String[]> orderHistory = new ArrayList<>();
        List<Order> orders = getVendorOrders(vendorId);

        for (Order order : orders) {
            String[] orderInfo = new String[] {
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getItemId(),
                    order.getStatus(),
                    String.valueOf(order.getPrice())
            };
            orderHistory.add(orderInfo);
        }

        return orderHistory;
    }

    public Order getOrderByIdItemAndCustomer(String itemId, String customerId) {
        List<Order> orders = getCustomerOrders(customerId);
        for (Order order : orders) {
            if (order.getItemId().equals(itemId)) {
                return order;
            }
        }
        return null;
    }

    public List<String> getOrderIdsCompletedByRunner(String runnerId) {
        List<String> completedOrders = new ArrayList<>();
        // Assuming you have an OrderManager class that provides a method to get the orders completed by a runner
        List<Order> orders = OrderManager.getInstance().getRunnerTasks(runnerId);
        for (Order order : orders) {
            if (order.getStatus().equals("DELIVERED")) { // Assuming the status is "DELIVERED" for completed orders
                completedOrders.add(order.getOrderId());
            }
        }
        return completedOrders;
    }

}
