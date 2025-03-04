import javax.swing.*;
import Panels.LoginPanal;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPanal loginPanal = new LoginPanal();
            loginPanal.setVisible(true);
        });
    }
}

