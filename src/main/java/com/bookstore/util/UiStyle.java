package com.bookstore.util;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.util.Enumeration;

public final class UiStyle {
    private static final Font DEFAULT_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 16);
    private static final Font TABLE_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 16);

    private UiStyle() {
    }

    public static void apply() {
        FontUIResource fontResource = new FontUIResource(DEFAULT_FONT);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontResource);
            }
        }
        UIManager.put("Table.font", new FontUIResource(TABLE_FONT));
        UIManager.put("TableHeader.font", new FontUIResource(DEFAULT_FONT.deriveFont(Font.BOLD)));
        UIManager.put("Table.rowHeight", 28);
    }

    public static void tuneTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(28);
        table.getTableHeader().setFont(DEFAULT_FONT.deriveFont(Font.BOLD));
    }
}
