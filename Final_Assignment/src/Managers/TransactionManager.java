package Managers;

import Models.Order;
import Models.Transaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionManager {
    private static TransactionManager instance;
    private Map<String, List<Transaction>> userTransactions;
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    public TransactionManager() {
        userTransactions = new HashMap<>();
        loadTransactions();
    }

    public static TransactionManager getInstance() {
        if (instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    private void loadTransactions() {
        List<String> lines = FileHandeler.readFile(TRANSACTIONS_FILE);
        for (String line : lines) {
            String[] parts = line.split(",");
            String transactionId = parts[0];
            String orderId = parts[1];
            String userId = parts[2];
            String amountStr = parts[3];
            String type = parts[4];

            double amount;
            if (amountStr.equals("REFUND")) {
                amount = 0; // or some other default value for refund transactions
            } else if (amountStr.equals("TOP_UP") || amountStr.equals("PAYMENT")) {
                // handle the TOP_UP or PAYMENT case
                amount = 0; // or some other default value
            } else {
                amount = Double.parseDouble(amountStr);
            }

            Transaction transaction = new Transaction(transactionId, orderId, userId, amount, type);
            addTransactionToMap(transaction);
        }
    }

    public List<Transaction> getTransactionsForVendor(String vendorId) {
        List<Transaction> vendorTransactions = new ArrayList<>();
        for (List<Transaction> transactions : userTransactions.values()) {
            for (Transaction transaction : transactions) {
                if (transaction.getOrderId() != null) {
                    Order order = OrderManager.getInstance().getOrder(transaction.getOrderId());
                    if (order != null && order.getVendorId().equals(vendorId)) {
                        vendorTransactions.add(transaction);
                    }
                }
            }
        }
        return vendorTransactions;
    }

    private void addTransactionToMap(Transaction transaction) {
        userTransactions.computeIfAbsent(transaction.getUserId(), k -> new ArrayList<>())
                .add(transaction);
    }

    public String generateTransactionId() {
        return "T" + System.currentTimeMillis();
    }

    public Transaction createTransaction(String userId, double amount, String type, String orderId) {
        // Validate transaction
        if (amount <= 0) {
            return null;
        }

        // For payments, check if user has sufficient balance
        if (type.equals("PAYMENT")) {
            double balance = calculateBalance(userId);
            if (balance < amount) {
                return null;
            }
        }

        String transactionId = generateTransactionId();
        Transaction transaction = new Transaction(transactionId, orderId, userId, amount, type);
        addTransactionToMap(transaction);
        FileHandeler.appendToFile(TRANSACTIONS_FILE, transaction.toString());

        // Send notification based on transaction type
        String notificationMessage = switch (type) {
            case "TOP_UP" -> String.format("Account topped up with $%.2f", amount);
            case "PAYMENT" -> String.format("Payment made: $%.2f", amount);
            case "REFUND" -> String.format("Refund received: $%.2f", amount);
            case "EARNING" -> String.format("Earnings received: $%.2f", amount);
            default -> String.format("Transaction completed: $%.2f", amount);
        };

        NotificationManager.getInstance().createNotification(userId, notificationMessage);
        return transaction;
    }

    public double calculateBalance(String customerId) {
        double balance = 0.0;
        try (BufferedReader reader = new BufferedReader(new FileReader("data/transactions.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    continue; // Skip invalid lines
                }
                if (parts[1].equals(customerId) && parts[3].equals("TOP_UP")) {
                    balance += Double.parseDouble(parts[2]);
                } else if (parts[1].equals(customerId) && parts[3].equals("PAYMENT")) {
                    balance -= Double.parseDouble(parts[2]);
                }
            }
        } catch (IOException e) {
            // Handle exception
        }
        return balance;
    }

    public List<Transaction> getUserTransactions(String userId) {
        return userTransactions.getOrDefault(userId, new ArrayList<>());
    }

    public List<Transaction> getUserTransactionsByType(String userId, String type) {
        return getUserTransactions(userId).stream()
                .filter(t -> t.getType().equals(type))
                .toList();
    }

    public List<Transaction> getUserTransactionsByDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return getUserTransactions(userId).stream()
                .filter(t -> t.getTimestamp().isAfter(start) &&
                        t.getTimestamp().isBefore(end))
                .toList();
    }

    public Map<String, Double> getTransactionSummary(String userId) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("total_balance", 0.0);
        summary.put("total_top_up", 0.0);
        summary.put("total_payment", 0.0);
        summary.put("total_refund", 0.0);
        summary.put("total_earning", 0.0);

        List<Transaction> transactions = getUserTransactions(userId);
        for (Transaction t : transactions) {
            switch (t.getType()) {
                case "TOP_UP":
                    summary.put("total_top_up", summary.get("total_top_up") + t.getAmount());
                    summary.put("total_balance", summary.get("total_balance") + t.getAmount());
                    break;
                case "PAYMENT":
                    summary.put("total_payment", summary.get("total_payment") + t.getAmount());
                    summary.put("total_balance", summary.get("total_balance") - t.getAmount());
                    break;
                case "REFUND":
                    summary.put("total_refund", summary.get("total_refund") + t.getAmount());
                    summary.put("total_balance", summary.get("total_balance") + t.getAmount());
                    break;
                case "EARNING":
                    summary.put("total_earning", summary.get("total_earning") + t.getAmount());
                    summary.put("total_balance", summary.get("total_balance") + t.getAmount());
                    break;
            }
        }
        return summary;
    }

    public double getTotalSystemRevenue() {
        return userTransactions.values().stream()
                .flatMap(List::stream)
                .filter(t -> t.getType().equals("PAYMENT"))
                .mapToDouble(Transaction::getAmount)
                .sum() * 0.1; // System takes 10% of all payments
    }

    public List<Transaction> getRecentTransactions(String userId, int limit) {
        return getUserTransactions(userId).stream()
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .limit(limit)
                .toList();
    }

    public boolean processTopUp(String userId, double amount) {
        if (amount <= 0) {
            return false;
        }

        String orderId = "adminTopup_" + generateTransactionId();
        Transaction transaction = createTransaction(userId, amount, "TOP_UP", orderId);
        return transaction != null;
    }

    public boolean processRefund(String orderId) {
        Models.Order order = OrderManager.getInstance().getOrder(orderId);
        if (order == null || !order.getStatus().equals("REJECTED")) {
            return false;
        }

        Transaction transaction = createTransaction(order.getCustomerId(), order.getAmount(), "REFUND", orderId);
        return transaction != null;
    }

    private void saveTransactions() {
        List<String> lines = new ArrayList<>();
        for (List<Transaction> transactions : userTransactions.values()) {
            for (Transaction transaction : transactions) {
                lines.add(transaction.toString());
            }
        }
        FileHandeler.writeFile(TRANSACTIONS_FILE, lines);
    }

    public double getRunnerRevenue(String runnerId) {
        OrderManager orderManager = OrderManager.getInstance();
        List<Order> orders = orderManager.getRunnerTasks(runnerId);
        List<Transaction> transactions = new ArrayList<>();
        for (Order order : orders) {
            // Assuming you have a method to get transactions for an order
            transactions.addAll(getTransactionsForOrder(order));
        }
        return transactions.stream()
                .mapToDouble(t -> t.getAmount())
                .sum();
    }

    public List<Transaction> getTransactionsForOrder(Order order) {
        TransactionManager transactionManager = TransactionManager.getInstance();
        return transactionManager.getUserTransactions(order.getCustomerId()).stream()
                .filter(t -> t.getOrderId().equals(order.getOrderId()))
                .toList();
    }

    public List<String[]> getRunnerTransactions(String runnerId) {
        List<Transaction> transactions = getTransactionsForRunner(runnerId);
        List<String[]> result = transactions.stream()
                .map(t -> new String[] {t.getTransactionId(), t.getOrderId(), t.getUserId(), String.valueOf(t.getAmount())})
                .toList();
        return result;
    }

    private List<Transaction> getTransactionsForRunner(String runnerId) {
        OrderManager orderManager = OrderManager.getInstance();
        List<Order> orders = orderManager.getRunnerTasks(runnerId);
        List<Transaction> transactions = new ArrayList<>();
        for (Order order : orders) {
            transactions.addAll(getTransactionsForOrder(order));
        }
        return transactions;
    }

}