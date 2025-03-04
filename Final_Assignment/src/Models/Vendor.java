package Models;

public class Vendor extends User {
    private double revenue;

    public Vendor(String userId, String username, String password) {
        super(userId, username, password, "Vendor");
        this.revenue = 0.0;
    }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
}


