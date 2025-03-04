package Panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import Managers.*;
import Models.*;
import Models.MenuItem;

import java.util.List;
import java.util.Map;

public class VendorPanel extends JFrame {
    private String vendorId;
    private JPanel contentPanel;
    private ItemManager itemManager;
    private OrderManager orderManager;
    private NotificationManager notificationManager;
    private TransactionManager transactionManager;
    private ReviewManager reviewManager;
    private JLabel revenueLabel;

    public VendorPanel(String vendorId) {
        this.vendorId = vendorId;
        this.itemManager = new ItemManager();
        this.orderManager = new OrderManager();
        this.notificationManager = new NotificationManager();
        this.transactionManager = new TransactionManager();
        reviewManager = new ReviewManager(itemManager); // Pass itemManager to ReviewManager constructor
        setupUI();
    }

    private void refreshTransactions() {
        JTable transactionTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(4)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        model.setRowCount(0);

        List<Transaction> transactions = transactionManager.getTransactionsForVendor(vendorId);
        for (Transaction transaction : transactions) {
            model.addRow(new Object[]{transaction.getTransactionId(), transaction.getOrderId(), transaction.getUserId(), transaction.getAmount()});
        }

        updateRevenue();
    }

    private void setupUI() {
        setTitle("Vendor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top bar with centered buttons
        JPanel topBar = createTopBar();
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Content panel (replaces split pane)
        contentPanel = new JPanel(new CardLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add different content panels
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createAddItemPanel(), "ADD_ITEM");
        contentPanel.add(createUpdateItemsPanel(), "UPDATE_ITEMS");
        contentPanel.add(createProcessOrdersPanel(), "PROCESS_ORDERS");
        contentPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        contentPanel.add(createOrderHistoryPanel(), "ORDER_HISTORY");
        contentPanel.add(createReviewsPanel(), "REVIEWS");

        add(mainPanel);
    }


    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new GridBagLayout()); // GridBagLayout for centering
        topBar.setBackground(new Color(70, 130, 180));

        String[] menuItems = {
                "Dashboard", "Add Items", "Update Items",
                "Process Orders", "Notifications", "Order History",
                "Reviews", "Logout"
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing around buttons

        for (int i = 0; i < menuItems.length; i++) {
            JButton menuButton = createMenuButton(menuItems[i]);
            gbc.gridx = i;
            gbc.gridy = 0;
            topBar.add(menuButton, gbc);
        }

        return topBar;
    }


    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addActionListener(e -> {
            switch (text) {
                case "Dashboard":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "DASHBOARD");
                    updateRevenue();
                    break;
                case "Add Items":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "ADD_ITEM");
                    break;
                case "Update Items":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "UPDATE_ITEMS");
                    refreshItems();
                    break;
                case "Process Orders":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "PROCESS_ORDERS");
                    refreshOrders();
                    break;
                case "Notifications":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "NOTIFICATIONS");
                    refreshNotifications();
                    break;
                case "Order History":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "ORDER_HISTORY");
                    refreshOrderHistory();
                    break;
                case "Reviews":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "REVIEWS");
                    refreshReviews();
                    break;
                case "Logout":
                    handleLogout();
                    break;
            }
        });

        return button;
    }

    private void updateRevenue() {
        Map<String, Double> revenue = orderManager.getVendorRevenue(vendorId, null, null);
        revenueLabel.setText(String.format("Total Revenue: Rs. %.2f", revenue.get("total")));
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Recent transactions table
        String[] columnNames = {"Date", "Order ID", "Item", "Amount", "Status"};
        JTable transactionTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        panel.add(new JLabel("Recent Transactions", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Refresh transactions
        List<Order> orders = orderManager.getVendorOrders(vendorId);
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        for (Order order : orders) {
            Object[] row = new Object[] {
                    order.getOrderTime(),
                    order.getOrderId(),
                    order.getItemId(),
                    order.getPrice(),
                    order.getStatus()
            };
            model.addRow(row);
        }

        return panel;
    }

    private JPanel createAddItemPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField itemNameField = new JTextField(20);
        JTextField priceField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        panel.add(itemNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        JButton addButton = new JButton("Add Item");
        addButton.setBackground(new Color(70, 130, 180));
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(addButton, gbc);

        addButton.addActionListener(e -> {
            String itemName = itemNameField.getText();
            String priceStr = priceField.getText();

            if (itemName.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    JOptionPane.showMessageDialog(this, "Price must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (itemManager.addItem(vendorId, itemName, price)) {
                    JOptionPane.showMessageDialog(this, "Item added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    itemNameField.setText("");
                    priceField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add item", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createUpdateItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Item ID", "Name", "Price", "Action"};
        JTable itemTable = new JTable();
        itemTable.setModel(new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only allow editing of the Action column
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshItems() {
        JTable itemTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(2)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) itemTable.getModel();
        model.setRowCount(0);

        List<MenuItem> items = itemManager.getVendorItems(vendorId);
        for (MenuItem item : items) {
            model.addRow(new Object[]{item.getItemId(), item.getName(), item.getPrice(), "Update"});
        }

        // Add mouse listener for update action
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = itemTable.rowAtPoint(e.getPoint());
                int col = itemTable.columnAtPoint(e.getPoint());
                if (col == 3 && row >= 0) { // Action column
                    Object vendorId = itemTable.getValueAt(row, 0);
                    Object name = itemTable.getValueAt(row, 1);
                    Object price = itemTable.getValueAt(row, 2);

                    String vendorIdStr = vendorId != null ? vendorId.toString() : "";
                    String nameStr = name != null ? name.toString() : "";
                    String priceStr = price != null ? price.toString() : "";

                    showUpdateItemDialog(vendorIdStr, nameStr, priceStr);
                }
            }
        });
    }

    private void showUpdateItemDialog(String itemId, String currentName, String currentPrice) {
        JDialog dialog = new JDialog(this, "Update Item", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(currentName);
        JTextField priceField = new JTextField(currentPrice);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        dialog.add(priceField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton updateButton = new JButton("Update");
        updateButton.setBackground(new Color(70, 130, 180));
        buttonPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(220, 50, 50));
        buttonPanel.add(deleteButton);

        gbc.gridx = 1; gbc.gridy = 2;
        dialog.add(buttonPanel, gbc);

        updateButton.addActionListener(e -> {
            try {
                double newPrice = Double.parseDouble(priceField.getText());
                if (newPrice <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Price must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (itemManager.updateItem(vendorId, itemId, nameField.getText(), newPrice)) {
                    JOptionPane.showMessageDialog(dialog, "Item updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshItems();
                    refreshReviews(); // Add this line

                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update item", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid price format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete this item?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (itemManager.deleteItem(vendorId, itemId)) {
                    JOptionPane.showMessageDialog(dialog, "Item deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshItems(); // Already present
                    refreshReviews(); // Add this line
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to delete item", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setVisible(true);
    }

    private JPanel createProcessOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Order ID", "Customer", "Item", "Price", "Actions"};
        JTable orderTable = new JTable();
        orderTable.setModel(new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only allow editing of the Actions column
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshOrders() {
        JTable orderTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(3)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
        model.setRowCount(0);

        List<Order> orders = orderManager.getVendorOrders(vendorId);
        System.out.println("Orders: " + orders); // Print the orders to the console

        for (Order order : orders) {
            System.out.println("Order Status: " + order.getStatus()); // Print the order status to the console
            if (order.getStatus().toUpperCase().equals("PENDING")) {
                model.addRow(new Object[]{order.getOrderId(), order.getCustomerId(), order.getItemId(), order.getPrice(), "Accept/Reject"});
            }
        }

        // Add this line to fire a table data changed event
        model.fireTableDataChanged();

        // Add mouse listener for accept/reject actions
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = orderTable.rowAtPoint(e.getPoint());
                int col = orderTable.columnAtPoint(e.getPoint());
                System.out.println("Row: " + row + ", Col: " + col);
                if (col == 4 && row >= 0) { // Actions column
                    String orderId = (String) orderTable.getValueAt(row, 0);
                    System.out.println("Order ID: " + orderId);

                    Object[] options = {"Accept", "Reject"};
                    int choice = JOptionPane.showOptionDialog(VendorPanel.this,
                            "What would you like to do with this order?",
                            "Process Order",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    System.out.println("Choice: " + choice);
                    if (choice == 0) { // Accept
                        if (orderManager.acceptOrder(orderId)) {
                            // Remove the order from the orderTable
                            System.out.println("Order accepted successfully.");
                            model.removeRow(row);
                            refreshOrders();
                            refreshTransactions(); // Update the transactionTable
                            orderManager.saveOrders(); // Add this line
                        }
                    } else if (choice == 1) { // Reject
                        if (orderManager.rejectOrder(orderId)) {
                            // Remove the order from the orderTable
                            System.out.println("Order not accepted.");
                            model.removeRow(row);
                            refreshOrders();
                            orderManager.saveOrders(); // Add this line
                        }
                    }
                }
            }
        });
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Message", "Action"};
        JTable notificationTable = new JTable();
        notificationTable.setModel(new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only allow editing of the Action column
            }
        });

        JScrollPane scrollPane = new JScrollPane(notificationTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshNotifications() {
        JTable notificationTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(4)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) notificationTable.getModel();
        model.setRowCount(0);

        List<Notification> notifications = notificationManager.getUserNotifications(vendorId);
        for (Notification notification : notifications) {
            model.addRow(new Object[]{notification.getMessage(), "✓"});
        }

        // Add mouse listener for checkmark action
        notificationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = notificationTable.rowAtPoint(e.getPoint());
                int col = notificationTable.columnAtPoint(e.getPoint());
                if (col == 1 && row >= 0) { // Action column
                    String message = (String) notificationTable.getValueAt(row, 0);
                    notificationManager.markAsRead(vendorId, message);
                    model.removeRow(row);
                }
            }
        });
    }

    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Order ID", "Customer", "ItemID", "Status", "Price"};
        JTable historyTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshOrderHistory() {
        JTable historyTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(5)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);

        List<String[]> orders = orderManager.getVendorOrderHistory(vendorId);
        for (String[] order : orders) {
            model.addRow(order);
        }
    }

    private JPanel createReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Item", "Rating", "Review"};
        JTable reviewTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(reviewTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshReviews() {
        JTable reviewTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(6)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) reviewTable.getModel();
        model.setRowCount(0);

        List<String[]> reviews = reviewManager.getVendorReviews(vendorId);
        for (String[] review : reviews) {
            model.addRow(review);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginPanal().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VendorPanel("V001").setVisible(true);
        });
    }
}
