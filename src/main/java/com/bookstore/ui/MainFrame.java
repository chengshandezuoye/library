package com.bookstore.ui;

import com.bookstore.model.User;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {
    public MainFrame(User currentUser) {
        setTitle("图书销售管理系统 - 当前用户：" + currentUser.getUsername());
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("图书库存", new BookPanel());
        tabs.addTab("进货管理", new PurchasePanel(currentUser));
        tabs.addTab("图书销售", new SalePanel(currentUser));
        tabs.addTab("财务账单", new FinancePanel());
        tabs.addTab("用户管理", new UserPanel(currentUser));

        add(tabs, BorderLayout.CENTER);
    }
}
