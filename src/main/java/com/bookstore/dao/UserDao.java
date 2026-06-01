package com.bookstore.dao;

import com.bookstore.model.User;
import com.bookstore.util.DbUtil;
import com.bookstore.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, real_name, employee_no, gender, age, role FROM users WHERE username = ? AND password_hash = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, PasswordUtil.md5(password));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
                return null;
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, real_name, employee_no, gender, age, role FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
    }

    public void createAdmin(String username, String password, String realName, String employeeNo, String gender, int age) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, real_name, employee_no, gender, age, role) VALUES (?, ?, ?, ?, ?, ?, 'admin')";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, PasswordUtil.md5(password));
            statement.setString(3, realName);
            statement.setString(4, employeeNo);
            statement.setString(5, gender);
            statement.setInt(6, age);
            statement.executeUpdate();
        }
    }

    public boolean deleteAdmin(long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ? AND role = 'admin'";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean resetAdminPassword(long userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ? AND role = 'admin'";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PasswordUtil.md5(newPassword));
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public void updateProfile(long userId, String realName, String employeeNo, String gender, int age, String newPassword) throws SQLException {
        boolean changePassword = newPassword != null && !newPassword.isBlank();
        String sql = changePassword
                ? "UPDATE users SET real_name = ?, employee_no = ?, gender = ?, age = ?, password_hash = ? WHERE id = ?"
                : "UPDATE users SET real_name = ?, employee_no = ?, gender = ?, age = ? WHERE id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, realName);
            statement.setString(2, employeeNo);
            statement.setString(3, gender);
            statement.setInt(4, age);
            if (changePassword) {
                statement.setString(5, PasswordUtil.md5(newPassword));
                statement.setLong(6, userId);
            } else {
                statement.setLong(5, userId);
            }
            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setRealName(resultSet.getString("real_name"));
        user.setEmployeeNo(resultSet.getString("employee_no"));
        user.setGender(resultSet.getString("gender"));
        user.setAge(resultSet.getInt("age"));
        user.setRole(resultSet.getString("role"));
        return user;
    }
}
