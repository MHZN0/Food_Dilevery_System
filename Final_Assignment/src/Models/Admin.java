// Admin.java
package Models;

public class Admin extends User {
    public Admin(String userId, String username, String password) {
        super(userId, username, password, "Admin");
    }
}

