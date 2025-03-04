// Runner.java
package Models;

public class Runner extends User {
    private double earnings;

    public Runner(String userId, String username, String password) {
        super(userId, username, password, "Runner");
        this.earnings = 0.0;
    }

    public double getEarnings() { return earnings; }
    public void setEarnings(double earnings) { this.earnings = earnings; }
}

