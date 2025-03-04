package Managers;

import Models.Notification;
import java.util.*;
import java.time.LocalDateTime;

public class NotificationManager {
    private static NotificationManager instance;
    private Map<String, List<Notification>> userNotifications;
    private static final String NOTIFICATIONS_FILE = "notifications.txt";

    public NotificationManager() {
        userNotifications = new HashMap<>();
        loadNotifications();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    private void loadNotifications() {
        List<String> lines = FileHandeler.readFile(NOTIFICATIONS_FILE);
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length < 4) {
                System.out.println("Invalid notification data: " + line);
                continue;
            }
            String notificationId = parts[0];
            String userId = parts[1];
            String message = parts[2];
            boolean isRead = Boolean.parseBoolean(parts[3]);

            Notification notification = new Notification(notificationId, userId, message);
            notification.setRead(isRead);
            addNotificationToMap(notification);
        }
    }

    private void addNotificationToMap(Notification notification) {
        userNotifications.computeIfAbsent(notification.getUserId(), k -> new ArrayList<>())
                .add(notification);
    }

    public String generateNotificationId() {
        return "N" + System.currentTimeMillis();
    }

    public void createNotification(String userId, String message) {
        String notificationId = generateNotificationId();
        Notification notification = new Notification(notificationId, userId, message);
        addNotificationToMap(notification);
        FileHandeler.appendToFile(NOTIFICATIONS_FILE, notification.toString());
    }

    public void createNotificationForRole(String role, String message) {
        List<String> userIds = UserManager.getInstance().getAllUsers().stream()
                .filter(user -> user.getRole().equals(role))
                .map(user -> user.getUserId())
                .toList();

        for (String userId : userIds) {
            createNotification(userId, message);
        }
    }

    public List<Notification> getUserNotifications(String userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return getUserNotifications(userId).stream()
                .filter(n -> !n.isRead())
                .toList();
    }

    public void markAsRead(String userId, String notificationId) {
        List<Notification> notifications = getUserNotifications(userId);
        for (Notification notification : notifications) {
            if (notification.getNotificationId().equals(notificationId)) {
                notification.setRead(true);
                saveNotifications();
                break;
            }
        }
    }

    public void markAllAsRead(String userId) {
        List<Notification> notifications = getUserNotifications(userId);
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        saveNotifications();
    }

    public void deleteNotification(String userId, String notificationId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.removeIf(n -> n.getNotificationId().equals(notificationId));
            saveNotifications();
        }
    }

    public void deleteAllNotifications(String userId) {
        userNotifications.remove(userId);
        saveNotifications();
    }

    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        boolean changes = false;

        for (List<Notification> notifications : userNotifications.values()) {
            int originalSize = notifications.size();
            notifications.removeIf(n -> n.getTimestamp().isBefore(cutoffDate));
            if (notifications.size() != originalSize) {
                changes = true;
            }
        }

        if (changes) {
            saveNotifications();
        }
    }

    private void saveNotifications() {
        List<String> lines = new ArrayList<>();
        for (List<Notification> notifications : userNotifications.values()) {
            for (Notification notification : notifications) {
                lines.add(notification.toString());
            }
        }
        FileHandeler.writeFile(NOTIFICATIONS_FILE, lines);
    }

    public int getUnreadCount(String userId) {
        return (int) getUserNotifications(userId).stream()
                .filter(n -> !n.isRead())
                .count();
    }
}
