package com.bookstore.ui;

import com.bookstore.dao.UserDao;
import com.bookstore.model.User;
import com.bookstore.util.UiStyle;
import com.bookstore.util.UiUtil;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserPanel extends JPanel {
    private final User currentUser;
    private final UserDao userDao = new UserDao();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();
    private final JTextField realNameField = new JTextField();
    private final JTextField employeeNoField = new JTextField();
    private final JTextField genderField = new JTextField();
    private final JTextField ageField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "用户名", "真实姓名", "工号", "性别", "年龄", "角色"}, 0);
    private final JTable table = new JTable(tableModel);

    public UserPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        fieldsPanel.add(new JLabel("用户名"));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(new JLabel("新密码"));
        fieldsPanel.add(passwordField);
        fieldsPanel.add(new JLabel("真实姓名"));
        fieldsPanel.add(realNameField);
        fieldsPanel.add(new JLabel("确认新密码"));
        fieldsPanel.add(confirmPasswordField);
        fieldsPanel.add(new JLabel("工号"));
        fieldsPanel.add(employeeNoField);
        fieldsPanel.add(new JLabel("性别"));
        fieldsPanel.add(genderField);
        fieldsPanel.add(new JLabel("年龄"));
        fieldsPanel.add(ageField);
        fieldsPanel.add(new JPanel());
        fieldsPanel.add(new JPanel());

        JButton createButton = new JButton("创建普通管理员");
        JButton updateButton = new JButton("修改本人信息");
        usernameField.setEditable(currentUser.isSuperAdmin());
        passwordField.setEnabled(currentUser.isSuperAdmin());
        confirmPasswordField.setEnabled(currentUser.isSuperAdmin());
        createButton.setEnabled(currentUser.isSuperAdmin());
        createButton.addActionListener(event -> createAdmin());
        updateButton.addActionListener(event -> updateSelf());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 8));
        buttonPanel.add(createButton);
        buttonPanel.add(updateButton);

        JPanel formPanel = new JPanel(new BorderLayout(0, 8));
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        UiStyle.tuneTable(table);
        setupAdminPopupMenu();
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

    private void setupAdminPopupMenu() {
        if (!currentUser.isSuperAdmin()) {
            return;
        }
        JPopupMenu popupMenu = new JPopupMenu();
        JButton resetPasswordButton = createPopupButton("重置密码");
        JButton deleteButton = createPopupButton("删除普通管理员");
        resetPasswordButton.addActionListener(event -> {
            popupMenu.setVisible(false);
            resetAdminPassword();
        });
        deleteButton.addActionListener(event -> {
            popupMenu.setVisible(false);
            deleteAdmin();
        });
        popupMenu.add(resetPasswordButton);
        popupMenu.add(deleteButton);
        table.setComponentPopupMenu(popupMenu);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                selectPopupRow(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                selectPopupRow(event);
            }
        });
    }

    private JButton createPopupButton(String text) {
        JButton button = new JButton(text);
        button.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 16));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private void selectPopupRow(MouseEvent event) {
        if (!event.isPopupTrigger()) {
            return;
        }
        int row = table.rowAtPoint(event.getPoint());
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        }
    }

    private void createAdmin() {
        try {
            if (!currentUser.isSuperAdmin()) {
                UiUtil.warn(this, "只有超级管理员可以创建普通管理员账号");
                return;
            }
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            if (username.isBlank() || password.isBlank()) {
                UiUtil.warn(this, "用户名和密码不能为空");
                return;
            }
            if (!password.equals(confirmPassword)) {
                UiUtil.warn(this, "两次输入的新密码不一致");
                return;
            }
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定创建普通管理员：" + username + "？",
                    "确认创建",
                    JOptionPane.YES_NO_OPTION
            );
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            userDao.createAdmin(
                    username,
                    password,
                    realNameField.getText().trim(),
                    employeeNoField.getText().trim(),
                    genderField.getText().trim(),
                    Integer.parseInt(ageField.getText().trim())
            );
            UiUtil.info(this, "普通管理员创建成功");
            passwordField.setText("");
            confirmPasswordField.setText("");
            loadCurrentUser();
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void deleteAdmin() {
        try {
            if (!currentUser.isSuperAdmin()) {
                UiUtil.warn(this, "只有超级管理员可以删除普通管理员账号");
                return;
            }
            int modelRow = getSelectedAdminRow("删除");
            if (modelRow < 0) {
                return;
            }
            long userId = ((Number) tableModel.getValueAt(modelRow, 0)).longValue();
            String username = String.valueOf(tableModel.getValueAt(modelRow, 1));
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定删除普通管理员：" + username + "？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION
            );
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            if (!userDao.deleteAdmin(userId)) {
                UiUtil.warn(this, "删除失败，只能删除普通管理员");
                return;
            }
            UiUtil.info(this, "普通管理员删除成功");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void resetAdminPassword() {
        try {
            if (!currentUser.isSuperAdmin()) {
                UiUtil.warn(this, "只有超级管理员可以重置普通管理员密码");
                return;
            }
            int modelRow = getSelectedAdminRow("重置密码");
            if (modelRow < 0) {
                return;
            }
            long userId = ((Number) tableModel.getValueAt(modelRow, 0)).longValue();
            String username = String.valueOf(tableModel.getValueAt(modelRow, 1));

            JPasswordField newPasswordField = new JPasswordField();
            JPasswordField confirmNewPasswordField = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
            panel.add(new JLabel("新密码"));
            panel.add(newPasswordField);
            panel.add(new JLabel("确认新密码"));
            panel.add(confirmNewPasswordField);

            int inputResult = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "重置密码：" + username,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (inputResult != JOptionPane.OK_OPTION) {
                return;
            }
            String newPassword = new String(newPasswordField.getPassword());
            String confirmNewPassword = new String(confirmNewPasswordField.getPassword());
            if (newPassword.isBlank()) {
                UiUtil.warn(this, "新密码不能为空");
                return;
            }
            if (!newPassword.equals(confirmNewPassword)) {
                UiUtil.warn(this, "两次输入的新密码不一致");
                return;
            }
            int confirmResult = JOptionPane.showConfirmDialog(
                    this,
                    "确定重置普通管理员 " + username + " 的密码？",
                    "确认重置",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmResult != JOptionPane.YES_OPTION) {
                return;
            }
            if (!userDao.resetAdminPassword(userId, newPassword)) {
                UiUtil.warn(this, "重置失败，只能重置普通管理员密码");
                return;
            }
            UiUtil.info(this, "密码重置成功");
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private int getSelectedAdminRow(String actionName) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UiUtil.warn(this, "请先在表格中选择要" + actionName + "的普通管理员");
            return -1;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String role = String.valueOf(tableModel.getValueAt(modelRow, 6));
        if (!"admin".equals(role)) {
            UiUtil.warn(this, "只能对普通管理员执行" + actionName + "操作，不能操作超级管理员");
            return -1;
        }
        return modelRow;
    }

    private void updateSelf() {
        try {
            String newPassword = currentUser.isSuperAdmin() ? new String(passwordField.getPassword()) : "";
            String confirmPassword = currentUser.isSuperAdmin() ? new String(confirmPasswordField.getPassword()) : "";
            if (!newPassword.isBlank() && !newPassword.equals(confirmPassword)) {
                UiUtil.warn(this, "两次输入的新密码不一致");
                return;
            }
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定修改本人信息？",
                    "确认修改",
                    JOptionPane.YES_NO_OPTION
            );
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            userDao.updateProfile(
                    currentUser.getId(),
                    realNameField.getText().trim(),
                    employeeNoField.getText().trim(),
                    genderField.getText().trim(),
                    Integer.parseInt(ageField.getText().trim()),
                    newPassword
            );
            currentUser.setRealName(realNameField.getText().trim());
            currentUser.setEmployeeNo(employeeNoField.getText().trim());
            currentUser.setGender(genderField.getText().trim());
            currentUser.setAge(Integer.parseInt(ageField.getText().trim()));
            UiUtil.info(this, "个人信息修改成功");
            passwordField.setText("");
            confirmPasswordField.setText("");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }
}
