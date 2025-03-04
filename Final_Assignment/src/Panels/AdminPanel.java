package Panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;

import Managers.*;
import Models.Transaction;
import Models.User;

import java.util.List;

public class AdminPanel extends JFrame {
    private JPanel mainPanel;
    private JPanel contentPanel;
    public FileHandeler dataManager;
    public TransactionManager transactionManager;
    public UserManager userManager;

    public AdminPanel(String adminId) {
        this.dataManager = new Managers.FileHandeler();
        this.transactionManager = new TransactionManager();
        this.userManager = Managers.UserManager.getInstance(); // Initialize userManager here
        setupUI();
    }

    private JPanel userListPanel;

    private void setupUI() {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // Create the top navigation bar
        JPanel topBar = createTopBar();
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Welcome panel
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        JLabel welcomeLabel = new JLabel("Welcome to the Admin Dashboard!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        contentPanel.add(welcomePanel, "WELCOME");

        // Add other panels
        userListPanel = createListUsersPanel();
        contentPanel.add(userListPanel, "LIST_USERS");
        contentPanel.add(createRegisterUserPanel(), "REGISTER_USER");
        contentPanel.add(createTopUpPanel(), "TOP_UP");
        contentPanel.add(createTransactionPanel(), "TRANSACTION");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] menuItems = {"Register User", "List Users", "Customer Top-up", "Transaction Receipt", "Logout"};

        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebar.add(menuButton);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return sidebar;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel();
        topBar.setLayout(new GridBagLayout()); // Use GridBagLayout for centering
        topBar.setBackground(new Color(70, 130, 180));

        String[] menuItems = {"Register User", "List Users", "Customer Top-up", "Transaction Receipt", "Logout"};

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Inner panel to align buttons centrally
        buttonPanel.setOpaque(false); // Make background transparent

        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            buttonPanel.add(menuButton);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0); // Add padding

        topBar.add(buttonPanel, gbc);

        return topBar;
    }


    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add this line here

        button.addActionListener(e -> {
            switch (text) {
                case "Register User":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "REGISTER_USER");
                    break;
                case "List Users":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "LIST_USERS");
                    refreshUserList();
                    break;
                case "Customer Top-up":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "TOP_UP");
                    break;
                case "Transaction Receipt":
                    ((CardLayout) contentPanel.getLayout()).show(contentPanel, "TRANSACTION");
                    break;
                case "Logout":
                    handleLogout();
                    break;
            }
        });

        return button;
    }

    private JPanel createRegisterUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField retypePasswordField = new JPasswordField(20);
        String[] roles = {"Customer", "Vendor", "Runner"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);

        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Retype Password:"), gbc);
        gbc.gridx = 1;
        panel.add(retypePasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);

        JButton registerButton = new JButton("Register User");
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(registerButton, gbc);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String retypePassword = new String(retypePasswordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(retypePassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userManager.isUsernameExists(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userManager.registerUser(username, password, role)) {
                JOptionPane.showMessageDialog(this, "User registered successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText("");
                passwordField.setText("");
                retypePasswordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register user", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createListUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Get the user data from the UserManager
        UserManager userManager = UserManager.getInstance();
        List<User> users = userManager.getAllUsers();

        // Create a JTable to display the user data
        String[] columnNames = {"User ID", "Username", "Role", "Update", "Delete"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        for (User user : users) {
            model.addRow(new Object[]{user.getUserId(), user.getUsername(), user.getRole(), "", ""});
        }
        JTable table = new JTable(model);

        // Add a custom renderer for the "Update" and "Delete" columns
        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("Update"));
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Delete"));

        // Add a custom editor for the "Update" and "Delete" columns
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox("Update")));
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox("Delete")));

        // Add the JTable to a JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the JScrollPane to the panel
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add a mouse listener to handle the update and delete actions
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                if (column == 3) { // Update column
                    String userId = (String) table.getValueAt(row, 0);
                    // Show the update panel
                    JPanel updatePanel = createUpdateUserPanel(userId);
                    int result = JOptionPane.showConfirmDialog(null, updatePanel, "Update User", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        // Update the user data
                        String username = ((JTextField) updatePanel.getComponent(1)).getText();
                        String password = new String(((JPasswordField) updatePanel.getComponent(3)).getPassword());
                        String confirmPassword = new String(((JPasswordField) updatePanel.getComponent(5)).getPassword());
                        if (userManager.updateUser(userId, username, password, confirmPassword)) {
                            JOptionPane.showMessageDialog(null, "User updated successfully");
                        } else {
                            JOptionPane.showMessageDialog(null, "Failed to update user");
                        }
                    }
                } else if (column == 4) { // Delete column
                    String userId = (String) table.getValueAt(row, 0);
                    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this user?", "Delete User", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        if (userManager.deleteUser(userId)) {
                            JOptionPane.showMessageDialog(null, "User deleted successfully");
                        } else {
                            JOptionPane.showMessageDialog(null, "Failed to delete user");
                        }
                    }
                }

                refreshUserList();
            }
        });

        return panel;
    }

    // Custom renderer for the "Action" column
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        private final String action;

        public ButtonRenderer(String action) {
            this.action = action;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setText(action);
            return this;
        }
    }

    // Custom editor for the "Action" column
    static class ButtonEditor extends DefaultCellEditor {
        private String label;
        private final JCheckBox checkBox; // Declare checkBox as a class field

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            this.checkBox = checkBox; // Initialize the class field
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (String) value;
            checkBox.setText(label);
            checkBox.addActionListener(e -> fireEditingStopped());
            return checkBox;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

    // Add an action listener to the table


    // Create the update user panel
    private JPanel createUpdateUserPanel(String userId) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);

        // Fetch user details and populate fields
        User user = userManager.getUser(userId); // Assuming this method exists
        if (user != null) {
            usernameField.setText(user.getUsername());
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);

        return panel;
    }

    private void refreshUserList() {
        // Get the JTable component from the userListPanel
        Component[] components = userListPanel.getComponents();
        JTable table = null;
        for (Component component : components) {
            if (component instanceof JScrollPane scrollPane) {
                table = (JTable) scrollPane.getViewport().getView();
                break;
            }
        }

        if (table != null) {
            // Get the data from the table
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String userID = (String) model.getValueAt(i, 0);
                String username = (String) model.getValueAt(i, 1);
                String role = (String) model.getValueAt(i, 2);
                System.out.println("User ID: " + userID + ", Username: " + username + ", Role: " + role);
            }
        }
    }

    private JPanel createTopUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userIdField = new JTextField(20);
        JTextField amountField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        panel.add(userIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        JButton topUpButton = new JButton("Top Up");
        topUpButton.setBackground(new Color(70, 130, 180));
        topUpButton.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(topUpButton, gbc);

        topUpButton.addActionListener(e -> {
            String userId = userIdField.getText();
            String amountStr = amountField.getText();

            if (userId.isEmpty() || amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (transactionManager.processTopUp(userId, amount)) {
                    JOptionPane.showMessageDialog(this, "Top-up successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    userIdField.setText("");
                    amountField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to process top-up", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField userIdField = new JTextField(20);
        JButton generateButton = new JButton("Generate Receipt");
        generateButton.setBackground(new Color(70, 130, 180));
        generateButton.setForeground(Color.BLACK);

        inputPanel.add(new JLabel("User ID:"));
        inputPanel.add(userIdField);
        inputPanel.add(generateButton);

        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(receiptArea);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> {
            String userId = userIdField.getText();
            if (userId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter User ID", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<Transaction> transactions = transactionManager.getUserTransactions(userId);
            if (transactions.isEmpty()) {
                receiptArea.setText("No transactions found for this user.");
            } else {
                StringBuilder receipt = new StringBuilder("Transaction Receipt for User " + userId + "\n\n");
                for (Transaction transaction : transactions) {
                    receipt.append(transaction).append("\n");
                }
                receiptArea.setText(receipt.toString());

                // Send to notifications
                NotificationManager notificationManager = new NotificationManager();
                notificationManager.createNotification(userId, "Transaction Receipt: " + receipt);
            }
        });

        return panel;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginPanal().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminPanel("adminId").setVisible(true));
    }
}
