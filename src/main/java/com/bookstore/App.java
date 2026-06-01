package com.bookstore;

import com.bookstore.ui.LoginFrame;
import com.bookstore.util.UiStyle;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default Swing look and feel is acceptable if the system theme is unavailable.
            }
            UiStyle.apply();
            new LoginFrame().setVisible(true);
        });
    }
}
