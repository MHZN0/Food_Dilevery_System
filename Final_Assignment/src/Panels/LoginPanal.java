package Panels;

import Managers.FileHandeler;
import Managers.UserManager;
import Models.User;

import javax.swing.*;
import java.awt.*;

public class LoginPanal extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private FileHandeler dataManager;
    private UserManager userManager;

    public LoginPanal() {
        dataManager = new FileHandeler();
        userManager = UserManager.getInstance();

        setupUI();
    }

    private void setupUI() {
        setTitle("Food Ordering System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Title label
        JLabel titleLabel = new JLabel("Welcome to Food Ordering System");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Username field
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.setBackground(new Color(245, 245, 245));
        JLabel usernameLabel = new JLabel("Username: ");
        usernameField = new JTextField(20);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        mainPanel.add(usernamePanel);

        // Password field
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setBackground(new Color(245, 245, 245));
        JLabel passwordLabel = new JLabel("Password: ");
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        mainPanel.add(passwordPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Login button
        loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setForeground(Color.BLUE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> handleLogin());
        mainPanel.add(loginButton);

        add(mainPanel);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User userInfo = userManager.authenticateUser(username, password);
        if (userInfo != null) {
            String role = userInfo.getRole(); // Assuming role is stored at index 2
            String userId = userInfo.getUserId(); // Assuming userId is stored at index 0
            openAppropriateFrame(role, userId);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAppropriateFrame(String role, String userId) {
        this.dispose(); // Close login frame

        SwingUtilities.invokeLater(() -> {
            switch (role.toLowerCase()) {
                case "admin":
                    new AdminPanel(userId).setVisible(true);
                    break;
                case "customer":
                    new CustomerPanal(userId).setVisible(true);
                    break;
                case "vendor":
                    new VendorPanel(userId).setVisible(true);
                    break;
                case "runner":
                    new RunnerPanel(userId).setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Invalid role", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginPanal().setVisible(true);
        });
    }
}
