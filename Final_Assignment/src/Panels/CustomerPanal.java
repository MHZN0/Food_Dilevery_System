package Panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import Managers.*;
import Models.MenuItem;
import Models.Notification;
import Models.Order;
import Models.Review;

import java.util.List;

public class CustomerPanal extends JFrame {
    private final String customerId;
    private JPanel contentPanel;
    private final TransactionManager transactionManager;
    private final OrderManager orderManager;
    public ItemManager itemManager;
    private final NotificationManager notificationManager;
    private JLabel balanceLabel;
    private final ReviewManager reviewManager;
    public CustomerPanal(String customerId) {
        this.customerId = customerId;
        this.transactionManager = new TransactionManager();
        this.orderManager = new OrderManager();
        this.itemManager = new ItemManager();
        this.notificationManager = new NotificationManager();
        reviewManager = new ReviewManager(itemManager);        setupUI();
        updateBalance();
    }

    private void setupUI() {
        setTitle("Customer Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main split pane for sidebar and content
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(1);

        // Sidebar
        JPanel sidebarPanel = createSidebar();
        splitPane.setLeftComponent(sidebarPanel);

        // Content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        splitPane.setRightComponent(contentPanel);

        // Add panels
        contentPanel.add(createMenuPanel(), "MENU");
        contentPanel.add(createOrderStatusPanel(), "ORDER_STATUS");
        contentPanel.add(createNotificationPanel(), "NOTIFICATIONS");
        contentPanel.add(createHistoryPanel(), "HISTORY");

        add(splitPane);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Balance display
        balanceLabel = new JLabel("Balance: Rs. 0.00");
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(balanceLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] menuItems = {
                "Menu",
                "Order Status",
                "Notifications",
                "History",
                "Logout"
        };

        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebar.add(menuButton);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return sidebar;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addActionListener(e -> {
            switch (text) {
                case "Menu":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "MENU");
                    refreshMenu();
                    break;
                case "Order Status":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "ORDER_STATUS");
                    refreshOrderStatus();
                    break;
                case "Notifications":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "NOTIFICATIONS");
                    refreshNotifications();
                    break;
                case "History":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "HISTORY");
                    refreshHistory();
                    break;
                case "Logout":
                    handleLogout();
                    break;
            }
        });

        return button;
    }

    private void updateBalance() {
        double balance = transactionManager.calculateBalance(customerId);
        balanceLabel.setText(String.format("Balance: Rs. %.2f", balance));
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Create table for menu items
        String[] columnNames = {"Item ID", "Item Name", "Vendor", "Price", "Action"};
        JTable menuTable = new JTable();
        menuTable.setModel(new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only allow editing of the Action column
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshMenu() {
        JTable menuTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(0)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) menuTable.getModel();
        model.setRowCount(0);

        List<MenuItem> items = itemManager.getAllItems();
        for (MenuItem item : items) {
            model.addRow(new Object[]{item.getItemId(), item.getName(), item.getVendorId(), item.getPrice(), "Buy"});
        }

        // Add mouse listener for item details
        menuTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = menuTable.rowAtPoint(e.getPoint());
                int col = menuTable.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) { // Action column
                    String itemId = (String) menuTable.getValueAt(row, 0);
                    showItemDetails(itemId);
                }
            }
        });
    }

    private void showItemDetails(String line) {
        String[] parts = line.split("_");
        String vendorId, itemId;

        if (parts.length < 2) {
            // Assume it's just the itemId
            vendorId = "V001"; // default vendorId
            itemId = line;
        } else {
            vendorId = parts[0];
            itemId = parts[1];
        }

        String fullItemId = vendorId + "_" + itemId;

        try {
            MenuItem itemDetails = itemManager.getItemDetails(fullItemId);
            if (itemDetails == null) {
                JOptionPane.showMessageDialog(this, "Item not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(this, "Item Details", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);

            JPanel detailsPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Add item details
            gbc.gridx = 0; gbc.gridy = 0;
            detailsPanel.add(new JLabel("Item Name: " + itemDetails.getName()), gbc);
            gbc.gridy = 1;
            detailsPanel.add(new JLabel("Price: Rs. " + itemDetails.getPrice()), gbc);

            // Add buy button
            JButton buyButton = new JButton("Buy");
            gbc.gridy = 2;
            detailsPanel.add(buyButton, gbc);

            buyButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Are you sure you want to buy this item?",
                        "Confirm Purchase",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    double price = itemDetails.getPrice();
                    double balance = transactionManager.calculateBalance(customerId);

                    if (balance < price) {
                        JOptionPane.showMessageDialog(dialog, "Insufficient balance", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String[] options = {"Takeaway", "Delivery Runner"};
                    int choice = JOptionPane.showOptionDialog(dialog,
                            "How would you like to receive your order?",
                            "Order Options",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    String deliveryType = (choice == 0) ? "takeaway" : "delivery_runner";

                    if (deliveryType.equals("takeaway")) {
                        // Deduct balance directly and add to transactions.txt
                        transactionManager.createTransaction(customerId, price, "PAYMENT", vendorId);
                        JOptionPane.showMessageDialog(dialog, "Takeaway order placed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Write to orders.txt
                        Order order = orderManager.createOrder(customerId, vendorId, itemId, deliveryType);
                        if (order != null) {
                            transactionManager.createTransaction(customerId, price, "PAYMENT", vendorId);
                            JOptionPane.showMessageDialog(dialog, "Delivery order placed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Failed to place delivery order", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    updateBalance();
                    dialog.dispose();
                    refreshOrderStatus();
                }
            });

            // Add reviews
            JTextArea reviewsArea = new JTextArea(5, 30);
            reviewsArea.setEditable(false);
            List<Review> reviews = itemManager.getItemReviews(fullItemId);
            for (Review review : reviews) {
                reviewsArea.append(review + "\n");
            }
            JScrollPane reviewsScroll = new JScrollPane(reviewsArea);
            gbc.gridy = 3;
            detailsPanel.add(reviewsScroll, gbc);

            dialog.add(detailsPanel, BorderLayout.CENTER);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error getting item details", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Log the error for debugging
        }
    }
    private JPanel createOrderStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columnNames = {"Order ID", "Item", "Status", "Action"};
        JTable orderTable = new JTable();
        orderTable.setModel(new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only allow editing of the Action column
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshOrderStatus() {
        JTable orderTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(1)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
        model.setRowCount(0); // Clear existing rows

        List<Order> orders = orderManager.getCustomerOrders(customerId);
        for (Order order : orders) {
            if (!order.getStatus().equals("delivered") && !order.getStatus().equals("cancelled")) {
                model.addRow(new Object[]{order.getOrderId(), order.getItemName(), order.getVendorId(), "Cancel"});
            }
        }

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add buy button (Ensure it's connected to the logic, if needed)
        JButton cancelButton = new JButton("Buy");
        gbc.gridy = 2;
        detailsPanel.add(cancelButton, gbc);

        // Add mouse listener for cancel action
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = orderTable.rowAtPoint(e.getPoint());
                int col = orderTable.columnAtPoint(e.getPoint());

                // Only handle clicks in the action column (Cancel button column)
                if (col == 3 && row >= 0) {
                    String orderId = (String) orderTable.getValueAt(row, 0);
                    boolean success = orderManager.cancelOrder(orderId);

                    // Display appropriate message based on cancel result
                    if (success) {
                        JOptionPane.showMessageDialog(CustomerPanal.this, "Order cancelled successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshOrderStatus(); // Refresh the table to reflect updated status
                    } else {
                        JOptionPane.showMessageDialog(CustomerPanal.this, "Failed to cancel order", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

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
        JTable notificationTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(2)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) notificationTable.getModel();
        model.setRowCount(0);

        List<Notification> notifications = notificationManager.getUserNotifications(customerId);
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
                    notificationManager.markAsRead(customerId, message);
                    model.removeRow(row);
                }
            }
        });
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columnNames = {"Order ID", "Item Name", "Vendor", "Status", "Price", "Actions"};
        JTable historyTable = new JTable();
        historyTable.setModel(new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only allow editing of the Actions column
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshHistory() {
        JTable historyTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(3)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);

        List<Order> orders = orderManager.getCustomerOrders(customerId);
        for (Order order : orders) {
            model.addRow(new Object[]{order.getOrderId(), order.getItemName(), order.getVendorId(), order.getStatus(), order.getAmount(), "Reorder/Review"});
        }

        // Add mouse listener for reorder and review actions
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = historyTable.rowAtPoint(e.getPoint());
                int col = historyTable.columnAtPoint(e.getPoint());
                if (col == 5 && row >= 0) { // Actions column
                    String orderId = (String) historyTable.getValueAt(row, 0);
                    String itemId = orderManager.getItemIdFromOrder(orderId);

                    Object[] options = {"Reorder", "Add Review"};
                    int choice = JOptionPane.showOptionDialog(CustomerPanal.this,
                            "What would you like to do?",
                            "Order Actions",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    if (choice == 0) { // Reorder
                        showItemDetails(itemId);
                    } else if (choice == 1) { // Add Review
                        showReviewDialog(itemId);
                    }
                }
            }
        });
    }

    private void showReviewDialog(String itemId) {
        JDialog dialog = new JDialog(this, "Add Review", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel reviewPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextArea reviewArea = new JTextArea(5, 30);
        JScrollPane scrollPane = new JScrollPane(reviewArea);

        String[] ratings = {"1", "2", "3", "4", "5"};
        JComboBox<String> ratingCombo = new JComboBox<>(ratings);

        gbc.gridx = 0; gbc.gridy = 0;
        reviewPanel.add(new JLabel("Rating:"), gbc);
        gbc.gridx = 1;
        reviewPanel.add(ratingCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        reviewPanel.add(new JLabel("Review:"), gbc);

        gbc.gridy = 2;
        reviewPanel.add(scrollPane, gbc);

        JButton submitButton = new JButton("Submit Review");
        submitButton.setBackground(new Color(68, 167, 255));
        gbc.gridy = 3;
        reviewPanel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String review = reviewArea.getText();
            int rating = Integer.parseInt((String) ratingCombo.getSelectedItem());

            if (review.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a review", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Retrieve the vendorId associated with the itemId
            String vendorId = itemManager.getVendorIdFromItem(itemId);

            // Retrieve the orderId associated with the itemId
            Order order = orderManager.getOrderByIdItemAndCustomer(itemId, customerId);
            if (order != null) {
                String orderId = order.getOrderId();

                if (reviewManager.addReview(itemId, orderId, vendorId, customerId, rating, review)) {
                    JOptionPane.showMessageDialog(dialog, "Review added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add review", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "No order found for this item", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(reviewPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
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
        SwingUtilities.invokeLater(() -> new CustomerPanal("C001").setVisible(true));
        SwingUtilities.invokeLater(() -> new CustomerPanal("C001").setVisible(true));
    }
}
