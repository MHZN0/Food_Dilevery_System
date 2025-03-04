// MenuItem.java
package Models;

public class MenuItem {
    private String itemId;
    private String vendorId;
    private String name;
    private double price;

    public MenuItem(String itemId, String vendorId, String name, double price) {
        this.itemId = itemId;
        this.vendorId = vendorId;
        this.name = name;
        this.price = price;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return getItemId() + "," + getName() + "," + getPrice();
    }
}

