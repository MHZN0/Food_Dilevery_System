package Managers;

import Models.*;
import java.util.*;

public class UserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private static final String USERS_FILE = "users.txt";

    private UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private void loadUsers() {
        List<String> lines = FileHandeler.readFile(USERS_FILE);
        for (String line : lines) {
            String[] parts = line.split(",");
            String userId = parts[0];
            String username = parts[1];
            String password = parts[2];
            String role = parts[3];

            User user = createUserByRole(userId, username, password, role);
            if (user != null) {
                users.put(userId, user);
            }
        }
    }

    private User createUserByRole(String userId, String username, String password, String role) {
        return switch (role) {
            case "Admin" -> new Admin(userId, username, password);
            case "Customer" -> new Customer(userId, username, password);
//            case "Vendor" -> new Vendor(userId, username, password);
//            case "Runner" -> new Runner(userId, username, password);
            default -> null;
        };
    }

    public User authenticateUser(String username, String password) {
        return users.values().stream()
                .filter(u -> u.getUsername().trim().equals(username.trim()) &&
                        u.getPassword().trim().equals(password.trim()))
                .findFirst()
                .orElse(null);
    }


    public String generateUserId(String role) {
        String prefix = switch (role) {
            case "Admin" -> "A";
            case "Customer" -> "C";
//            case "Vendor" -> "V";
//            case "Runner" -> "R";
            default -> throw new IllegalArgumentException("Invalid role");
        };

        int count = 1;
        while (users.containsKey(prefix + String.format("%03d", count))) {
            count++;
        }
        return prefix + String.format("%03d", count);
    }

    public boolean registerUser(String username, String password, String role) {
        // Validate inputs
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        // Check if username already exists
        if (isUsernameExists(username)) {
            return false;
        }

        String userId = generateUserId(role);
        User user = createUserByRole(userId, username, password, role);

        if (user == null) {
            return false;
        }

        users.put(userId, user);
        FileHandeler.appendToFile(USERS_FILE, user.toString());

        // Create welcome notification
        NotificationManager.getInstance().createNotification(
                userId,
                "Welcome to the Food Ordering System! Your account has been created successfully."
        );

        return true;
    }

    public boolean updateUser(String userId, String username, String password, String confirmPassword) {
        // Check if the user exists
        System.out.println("Username to update: " + username);
        System.out.println("Password to update: " + password);
        User user = getUser(userId);
        if (user == null) {
            return false;
        }

        // Check if the password and confirm password match
        if (!password.equals(confirmPassword)) {
            return false;
        }

        // Update the user data
        user.setUsername(username);
        user.setPassword(password);

        // Print out the updated user data
        System.out.println("Updated user data: " + user.toString());

        // Save the updated user data to the file
        return saveUsers();
    }

    public boolean deleteUser(String userId) {
        // Check if the user exists
        User user = getUser(userId);
        if (user == null) {
            return false;
        }

        // Remove the user from the list
        users.remove(userId); // Remove by ID, not by user object

        // Print out the updated users map
        System.out.println("Updated users map: " + users.toString());

        // Save the updated user data to the file
        return saveUsers();
    }

    public boolean isUsernameExists(String username) {
        return users.values().stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }

    private boolean saveUsers() {
        try {
            List<String> lines = new ArrayList<>();
            for (User user : users.values()) {
                lines.add(user.toString());
            }
            FileHandeler.writeFile(USERS_FILE, lines);
            System.out.println("Wrote to file: " + USERS_FILE);
            return true;
        } catch (Exception e) {
            System.out.println("Error writing to file: " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<User> getUsersByRole(String role) {
        return users.values().stream()
                .filter(u -> u.getRole().equals(role))
                .toList();
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public User getUserByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public boolean isAdmin(String userId) {
        User user = getUser(userId);
        return user != null && user.getRole().equals("Admin");
    }

    public int getUserCount() {
        return users.size();
    }

    public int getUserCountByRole(String role) {
        return (int) users.values().stream()
                .filter(u -> u.getRole().equals(role))
                .count();
    }

    public Map<String, Integer> getUserStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_users", getUserCount());
        stats.put("admin_count", getUserCountByRole("Admin"));
        stats.put("customer_count", getUserCountByRole("Customer"));
//        stats.put("vendor_count", getUserCountByRole("Vendor"));
//        stats.put("runner_count", getUserCountByRole("Runner"));
        return stats;
    }
}
