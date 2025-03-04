package Panels;

import javax.swing.*;
import Models.Notification;
import Models.User;
import Models.Order;
import Models.Review;
import javax.swing.table.DefaultTableModel;
import Managers.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class RunnerPanel extends JFrame {
    private ReviewManager reviewManager;
    private NotificationManager notificationManager;
    private TransactionManager transactionManager;
    private String runnerId;
    private OrderManager orderManager;
    private JLabel revenueLabel;
    private JTable reviewTable;
    private ItemManager itemManager;
    private UserManager userManager;
    private JPanel contentPanel;

    public RunnerPanel(String runnerId) {
        this.orderManager = new OrderManager();
        this.notificationManager = new NotificationManager();
        this.userManager = UserManager.getInstance();
        this.itemManager = new ItemManager(); // Initialize the itemManager
        this.reviewManager = new ReviewManager(itemManager);
        this.runnerId = runnerId;
        this.transactionManager = new TransactionManager();

        setupUI();
    }

    private void setupUI() {
        setTitle("Vendor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top bar with centered buttons
        JPanel topBar = createTopBar();
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Content panel (replaces sidebar)
        contentPanel = new JPanel(new CardLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add different content panels
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createDiscoverTasksPanel(), "DISCOVER_TASKS");
        contentPanel.add(createTaskStatusPanel(), "TASK_STATUS");
        contentPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        contentPanel.add(createHistoryPanel(), "HISTORY");
        contentPanel.add(createReviewsPanel(), "REVIEWS");

        add(mainPanel);
    }


    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new GridBagLayout()); // Center buttons
        topBar.setBackground(new Color(70, 130, 180));

        String[] menuItems = {
                "Dashboard", "Discover Tasks", "Task Status",
                "Notifications", "History", "Reviews", "Logout"
        };

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center-align buttons
        buttonPanel.setOpaque(false);

        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            buttonPanel.add(menuButton);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0); // Padding

        topBar.add(buttonPanel, gbc);

        return topBar;
    }


    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 40)); // Uniform button size
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 100, 100)); // Slightly lighter on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 70, 70));
            }
        });

        button.addActionListener(e -> {
            switch (text) {
                case "Dashboard":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "DASHBOARD");
                    updateRevenue();
                    break;
                case "Discover Tasks":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "DISCOVER_TASKS");
                    refreshDiscoverTasks();
                    break;
                case "Task Status":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "TASK_STATUS");
                    refreshTaskStatus();
                    break;
                case "Notifications":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "NOTIFICATIONS");
                    refreshNotifications();
                    break;
                case "History":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "HISTORY");
                    refreshHistory();
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
        double revenue = transactionManager.getRunnerRevenue(runnerId);
        int deliveredOrders = getDeliveredOrders();
        revenue += deliveredOrders * 5;
        revenueLabel.setText(String.format("Total Revenue: Rs. %.2f", revenue));
    }

    private int getDeliveredOrders() {
        if (contentPanel == null) {
            System.out.println("Content panel is not initialized.");
            return 0;
        }

        int deliveredOrders = 0;
        JTable statusTable = null;

        // Look through components in contentPanel to find the correct JTable.
        for (Component component : contentPanel.getComponents()) {
            if (component instanceof JScrollPane) {
                statusTable = (JTable) ((JScrollPane) component).getViewport().getView();
                break;
            }
        }

        if (statusTable == null) {
            System.out.println("Could not find statusTable in contentPanel.");
            return 0;
        }

        DefaultTableModel model = (DefaultTableModel) statusTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String status = (String) model.getValueAt(i, 4); // Assuming status is in the 5th column
            if ("DELIVERED".equals(status)) {
                deliveredOrders++;
            }
        }
        return deliveredOrders;
    }


    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        User runner = userManager.getUser(runnerId);
        String runnerName = runner.getUsername();

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, Delivery Runner " + runnerName, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createDiscoverTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Order ID", "Customer", "Vendor", "Item", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        List<Order> acceptedOrders = orderManager.getOrderByStatus("ACCEPTED");

        for (Order order : acceptedOrders) {
            Object[] row = {
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getVendorId(),
                    order.getItemId(),
                    new JButton("ACCEPT")
            };
            model.addRow(row);
        }

        JTable taskTable = new JTable(model);
        taskTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        taskTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(taskTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshDiscoverTasks() {
        JTable taskTable = getTaskTable();
        if (taskTable == null) {
            System.out.println("Could not find taskTable in contentPanel");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
        model.setRowCount(0);

        List<Order> tasks = orderManager.getAvailableDeliveries();
        for (Order task : tasks) {
            String customerId = task.getCustomerId();
            Object[] row = {
                    task.getOrderId(),
                    customerId,
                    task.getVendorId(),
                    task.getItemId(),
                    new JButton("ACCEPT")
            };
            model.addRow(row);
        }
    }

    private JTable getTaskTable() {
        for (Component component : contentPanel.getComponents()) {
            if (component instanceof JScrollPane) {
                return (JTable) ((JScrollPane) component).getViewport().getView();
            } else if (component instanceof JPanel) {
                for (Component panelComponent : ((JPanel) component).getComponents()) {
                    if (panelComponent instanceof JScrollPane) {
                        return (JTable) ((JScrollPane) panelComponent).getViewport().getView();
                    }
                }
            }
        }
        return null;
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("ACCEPT");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JButton) {
                return (JButton) value;
            } else {
                return this;
            }
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JButton button = new JButton("ACCEPT");
            button.addActionListener(e -> {
                String orderId = (String) table.getValueAt(row, 0);
                int result = JOptionPane.showConfirmDialog(RunnerPanel.this, "Are you sure you want to accept this delivery?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    if (orderManager.assignRunner(orderId, runnerId)) {
                        JOptionPane.showMessageDialog(RunnerPanel.this, "Delivery accepted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshDiscoverTasks();
                        refreshTaskStatus();
                        updateRevenue();
                    }
                }
            });
            return button;
        }
    }

    private JPanel createTaskStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Order ID", "Customer", "Vendor", "Item", "Status", "Action"};
        JTable statusTable = new JTable();
        statusTable.setModel(new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        });

        statusTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = statusTable.rowAtPoint(e.getPoint());
                if (row >= 0 && statusTable.getColumnModel().getColumnIndexAtX(e.getX()) == 5) {
                    String status = (String) statusTable.getValueAt(row, 4);
                    if (status.equals("ASSIGNED")) {
                        // Update status to "DELIVERED"
                        statusTable.setValueAt("DELIVERED", row, 4);
                        updateRevenue();
                    }
                }
            }
        });

        panel.add(new JScrollPane(statusTable), BorderLayout.CENTER);
        return panel;
    }


    private void refreshTaskStatus() {
        System.out.println("Refreshing task status...");
        JTable statusTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(2)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) statusTable.getModel();
        model.setRowCount(0);

        List<Order> tasks = orderManager.getRunnerTasks(runnerId);
        System.out.println("Retrieved tasks: " + tasks.size());
        for (Order task : tasks) {
            System.out.println("Task: " + task.getOrderId() + ", " + task.getCustomerId() + ", " + task.getVendorId() + ", " + task.getItemId() + ", " + task.getStatus());
            if (task.getRunnerId().equals(runnerId)) {
                if (task.getStatus().equals("DELIVERED")) {
                    model.addRow(new Object[]{task.getOrderId(), task.getCustomerId(), task.getVendorId(), task.getItemId(), task.getStatus(), "DELIVERED"});
                } else  {
                    model.addRow(new Object[]{task.getOrderId(), task.getCustomerId(), task.getVendorId(), task.getItemId(), task.getStatus(), "UPDATE"});
                }
            }
        }

        System.out.println("Task status refreshed.");
    }

    private JPanel createNotificationsPanel() {
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
        JTable notificationTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(3)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) notificationTable.getModel();
        model.setRowCount(0);

        List<Notification> notifications = notificationManager.getUserNotifications(runnerId);
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
                    notificationManager.markAsRead(runnerId, message);
                    model.removeRow(row);
                }
            }
        });
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {"Order ID", "Customer", "Vendor", "Item", "Status"};
        JTable historyTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshHistory() {
        JTable historyTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(4)).getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);

        List<String[]> deliveries = orderManager.getRunnerDeliveryHistory(runnerId);
        for (String[] delivery : deliveries) {
            model.addRow(delivery);
        }
    }

    private JPanel createReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Order ID", "Customer", "Rating", "Review"};
        reviewTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(reviewTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshReviews() {
        if (reviewManager != null) {
            DefaultTableModel model = (DefaultTableModel) reviewTable.getModel();
            model.setRowCount(0);

            List<Review> reviews = reviewManager.getReviews();
            System.out.println("Number of reviews: " + reviews.size());
            if (reviews.size() == 0) {
                System.out.println("No reviews found");
            }

            List<String> completedOrders = orderManager.getOrderIdsCompletedByRunner(runnerId); // Get the order IDs completed by the runner

            System.out.println("Completed Orders: " + completedOrders);
            System.out.println("Number of completed orders: " + completedOrders.size());
            if (completedOrders.size() == 0) {
                System.out.println("No completed orders found");
            }

            // Use a Set for faster lookup
            Set<String> completedOrdersSet = new HashSet<>(completedOrders);

            for (Review review : reviews) {
                System.out.println("Review Order ID: " + review.getOrderId());
                // Extract the order ID from the review ID
                String orderId = review.getOrderId().split("_review")[0];
                if (completedOrdersSet.contains(orderId)) {
                    System.out.println("Found matching review for order ID: " + orderId);
                    Object[] reviewArray = new Object[]{review.getOrderId(), review.getCustomerId(), String.valueOf(review.getRating()), review.getComment()};
                    model.addRow(reviewArray);
                }
            }
            reviewTable.revalidate();
            reviewTable.repaint();
        } else {
            System.out.println("Review manager is null");
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
            new RunnerPanel("R001").setVisible(true);
        });
    }
}