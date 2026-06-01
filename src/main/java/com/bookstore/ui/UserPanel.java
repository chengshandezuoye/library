package com.bookstore.ui;

import com.bookstore.dao.UserDao;
import com.bookstore.model.User;
import com.bookstore.util.UiStyle;
import com.bookstore.util.UiUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class UserPanel extends JPanel {
    private final User currentUser;
    private final UserDao userDao = new UserDao();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField realNameField = new JTextField();
    private final JTextField employeeNoField = new JTextField();
    private final JTextField genderField = new JTextField();
    private final JTextField ageField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "用户名", "真实姓名", "工号", "性别", "年龄", "角色"}, 0);
    private final JTable table = new JTable(tableModel);

    public UserPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        formPanel.add(new JLabel("用户名"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("新密码"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("真实姓名"));
        formPanel.add(realNameField);
        formPanel.add(new JLabel("工号"));
        formPanel.add(employeeNoField);
        formPanel.add(new JLabel("性别"));
        formPanel.add(genderField);
        formPanel.add(new JLabel("年龄"));
        formPanel.add(ageField);

        JButton createButton = new JButton("创建普通管理员");
        JButton updateButton = new JButton("修改本人信息");
        createButton.setEnabled(currentUser.isSuperAdmin());
        createButton.addActionListener(event -> createAdmin());
        updateButton.addActionListener(event -> updateSelf());
        formPanel.add(createButton);
        formPanel.add(updateButton);

        UiStyle.tuneTable(table);
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadCurrentUser();
        refresh();
    }

    private void loadCurrentUser() {
        usernameField.setText(currentUser.getUsername());
        realNameField.setText(currentUser.getRealName());
        employeeNoField.setText(currentUser.getEmployeeNo());
        genderField.setText(currentUser.getGender());
        ageField.setText(String.valueOf(currentUser.getAge()));
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            if (currentUser.isSuperAdmin()) {
                for (User user : userDao.findAll()) {
                    tableModel.addRow(new Object[]{
                            user.getId(), user.getUsername(), user.getRealName(), user.getEmployeeNo(),
                            user.getGender(), user.getAge(), user.getRole()
                    });
                }
            } else {
                tableModel.addRow(new Object[]{
                        currentUser.getId(), currentUser.getUsername(), currentUser.getRealName(),
                        currentUser.getEmployeeNo(), currentUser.getGender(), currentUser.getAge(), currentUser.getRole()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void createAdmin() {
        try {
            userDao.createAdmin(
                    usernameField.getText().trim(),
                    new String(passwordField.getPassword()),
                    realNameField.getText().trim(),
                    employeeNoField.getText().trim(),
                    genderField.getText().trim(),
                    Integer.parseInt(ageField.getText().trim())
            );
            UiUtil.info(this, "普通管理员创建成功");
            passwordField.setText("");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void updateSelf() {
        try {
            userDao.updateProfile(
                    currentUser.getId(),
                    realNameField.getText().trim(),
                    employeeNoField.getText().trim(),
                    genderField.getText().trim(),
                    Integer.parseInt(ageField.getText().trim()),
                    new String(passwordField.getPassword())
            );
            currentUser.setRealName(realNameField.getText().trim());
            currentUser.setEmployeeNo(employeeNoField.getText().trim());
            currentUser.setGender(genderField.getText().trim());
            currentUser.setAge(Integer.parseInt(ageField.getText().trim()));
            UiUtil.info(this, "个人信息修改成功");
            passwordField.setText("");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }
}
