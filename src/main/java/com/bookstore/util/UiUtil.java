package com.bookstore.util;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class UiUtil {
    private UiUtil() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, Exception exception) {
        JOptionPane.showMessageDialog(parent, exception.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "警告", JOptionPane.WARNING_MESSAGE);
    }
}
