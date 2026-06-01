package com.bookstore.ui;

import com.bookstore.dao.UserDao;
import com.bookstore.model.User;
import com.bookstore.util.UiUtil;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField("admin");
    private final JPasswordField passwordField = new JPasswordField("admin123");
    private final UserDao userDao = new UserDao();

    public LoginFrame() {
        setTitle("图书销售管理系统 - 登录");
        setSize(360, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.add(new JLabel("用户名"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("密码"));
        formPanel.add(passwordField);

        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(event -> login());
        getRootPane().setDefaultButton(loginButton);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.add(formPanel, BorderLayout.CENTER);
        root.add(loginButton, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void login() {
        try {
            User user = userDao.login(usernameField.getText().trim(), new String(passwordField.getPassword()));
            if (user == null) {
                UiUtil.warn(this, "用户名或密码错误");
                return;
            }
            new MainFrame(user).setVisible(true);
            dispose();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }
}
