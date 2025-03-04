package Managers;

import Models.MenuItem;
import Models.Order;
import Models.Review;

import java.util.*;

public class ItemManager {
    private static ItemManager instance;
    private Map<String, MenuItem> items;
    private static final String ITEMS_FILE = "items.txt";
    private OrderManager orderManager;
    private ReviewManager reviewManager;

    public ItemManager() {
        reviewManager = new ReviewManager(this);
        items = new HashMap<>();
        loadItems();
    }

    public static ItemManager getInstance() {
        if (instance == null) {
            instance = new ItemManager();
        }
        return instance;
    }

    private void loadItems() {
        List<String> lines = FileHandeler.readFile(ITEMS_FILE);
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length < 3) {
                // Handle the case where the line has less than 3 comma-separated values
                continue;
            }
            String vendorId_ItemId = parts[0];
            String[] vendorId_ItemIdParts = vendorId_ItemId.split("_");
            if (vendorId_ItemIdParts.length < 2) {
                // Handle the case where the first comma-separated value does not contain an underscore
                continue;
            }
            String vendorId = vendorId_ItemIdParts[0];
            String itemId = vendorId_ItemIdParts[1];
            String name = parts[1];
            double price = Double.parseDouble(parts[2]);

            MenuItem item = new MenuItem(itemId, vendorId, name, price);
            items.put(vendorId + "_" + itemId, item);
        }
    }

    public String generateItemId(String vendorId) {
        int count = 1;
        while (items.containsKey(vendorId + "_" + String.format("%03d", count))) {
            count++;
        }
        return String.format("%03d", count);
    }

    public boolean addItem(String vendorId, String name, double price) {
        if (price < 0) {
            return false;
        }

        String itemId = generateItemId(vendorId);
        MenuItem item = new MenuItem(itemId, vendorId, name, price);
        items.put(vendorId + "_" + itemId, item);
        FileHandeler.appendToFile(ITEMS_FILE, item.toString());
        return true;
    }

    public boolean updateItem(String vendorId, String itemId, String name, double price) {
        String key = vendorId + "_" + itemId;
        if (!items.containsKey(key) || price < 0) {
            return false;
        }

        MenuItem item = items.get(key);
        item.setName(name);
        item.setPrice(price);
        saveItems();
        return true;
    }

    public boolean deleteItem(String vendorId, String itemId) {
        String key = vendorId + "_" + itemId;
        if (!items.containsKey(key)) {
            return false;
        }

        items.remove(key);
        saveItems();
        return true;
    }

    private void saveItems() {
        List<String> lines = new ArrayList<>();
        for (MenuItem item : items.values()) {
            lines.add(item.toString());
        }
        FileHandeler.writeFile(ITEMS_FILE, lines);
    }

    public List<MenuItem> getAllItems() {
        return new ArrayList<>(items.values());
    }

    public List<MenuItem> getVendorItems(String vendorId) {
        return items.values().stream()
                .filter(item -> item.getVendorId().equals(vendorId))
                .toList();
    }

    public MenuItem getItem(String vendorId, String itemId) {
        return items.get(vendorId + "_" + itemId);
    }

    public List<MenuItem> searchItems(String keyword) {
        keyword = keyword.toLowerCase();
        String finalKeyword = keyword;
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(finalKeyword))
                .toList();
    }

    public List<MenuItem> getItemsByPriceRange(double minPrice, double maxPrice) {
        return items.values().stream()
                .filter(item -> item.getPrice() >= minPrice && item.getPrice() <= maxPrice)
                .toList();
    }

    public MenuItem getItemDetails(String itemId) {
        // Add some debugging statements to help identify the issue
        System.out.println("Getting item details for item ID: " + itemId);
        MenuItem item = items.get(itemId); // assuming items is a Map<String, MenuItem>
        if (item == null) {
            return null;
        }
        return item;
    }
    public List<Review> getItemReviews(String itemId) {
        return reviewManager.getReviewByItemId(itemId);
    }

    public String getVendorId(String itemId) {
        for (MenuItem item : items.values()) {
            if (item.getItemId().equals(itemId)) {
                return item.getVendorId();
            }
        }
        return null;
    }

    public String getDeliveryType(String itemId) {
        Order order = OrderManager.getInstance().getOrder(itemId);
        if (order != null) {
            return order.getDeliveryBy();
        }
        return null;
    }

    public String getVendorIdFromItem(String itemId) {
        for (MenuItem item : items.values()) {
            if (item.getItemId().equals(itemId)) {
                return item.getVendorId();
            }
        }
        return null;
    }
}
