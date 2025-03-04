// Notification.java
package Models;

import java.time.LocalDateTime;

public class Notification {
    private String notificationId;
    private String userId;
    private String message;
    private boolean isRead;
    private LocalDateTime timestamp;

    public Notification(String notificationId, String userId, String message) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.isRead = false;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return notificationId + "," + userId + "," + message + "," + isRead + "," + timestamp;
    }
}