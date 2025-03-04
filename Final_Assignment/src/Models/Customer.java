// Customer.java
package Models;

public class Customer extends User {
    private double balance;

    public Customer(String userId, String username, String password) {
        super(userId, username, password, "Customer");
        this.balance = 0.0;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

