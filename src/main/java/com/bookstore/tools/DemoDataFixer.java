package com.bookstore.tools;

import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DemoDataFixer {
    public static void main(String[] args) throws SQLException {
        try (Connection connection = DbUtil.getConnection()) {
            upsertUsers(connection);
            upsertBooks(connection);
        }
        System.out.println("中文演示数据修复完成。");
    }

    private static void upsertUsers(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO users (username, password_hash, real_name, employee_no, gender, age, role)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    real_name = VALUES(real_name),
                    employee_no = VALUES(employee_no),
                    gender = VALUES(gender),
                    age = VALUES(age),
                    role = VALUES(role)
                """;
        Object[][] users = {
                {"admin", "0192023a7bbd73250516f069df18b500", "超级管理员", "EMP-ADMIN", "男", 30, "super_admin"},
                {"manager", "e10adc3949ba59abbe56e057f20f883e", "普通管理员", "EMP-001", "女", 24, "admin"},
                {"buyer", "e10adc3949ba59abbe56e057f20f883e", "采购管理员", "EMP-002", "男", 26, "admin"},
                {"cashier", "e10adc3949ba59abbe56e057f20f883e", "收银管理员", "EMP-003", "女", 23, "admin"}
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] user : users) {
                statement.setString(1, (String) user[0]);
                statement.setString(2, (String) user[1]);
                statement.setString(3, (String) user[2]);
                statement.setString(4, (String) user[3]);
                statement.setString(5, (String) user[4]);
                statement.setInt(6, (Integer) user[5]);
                statement.setString(7, (String) user[6]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void upsertBooks(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO books (isbn, title, author, publisher, retail_price, stock_qty)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    title = VALUES(title),
                    author = VALUES(author),
                    publisher = VALUES(publisher),
                    retail_price = VALUES(retail_price),
                    stock_qty = VALUES(stock_qty)
                """;
        Object[][] books = {
                {"9787115428028", "Java核心技术", "Cay S. Horstmann", "人民邮电出版社", "98.00", 20},
                {"9787115546081", "MySQL必知必会", "Ben Forta", "人民邮电出版社", "59.00", 30},
                {"9787302517597", "数据结构", "严蔚敏", "清华大学出版社", "45.00", 15},
                {"9787111612728", "算法导论", "Thomas H. Cormen", "机械工业出版社", "128.00", 12},
                {"9787111213826", "Effective Java", "Joshua Bloch", "机械工业出版社", "89.00", 18},
                {"9787115293800", "深入理解计算机系统", "Randal E. Bryant", "机械工业出版社", "139.00", 10},
                {"9787115547644", "Python编程：从入门到实践", "Eric Matthes", "人民邮电出版社", "99.00", 25},
                {"9787302423287", "数据库系统概论", "王珊", "高等教育出版社", "49.00", 22},
                {"9787115521644", "JavaScript高级程序设计", "Matt Frisbie", "人民邮电出版社", "129.00", 16},
                {"9787115472588", "Spring实战", "Craig Walls", "人民邮电出版社", "79.00", 14},
                {"9787115607584", "Vue.js设计与实现", "霍春阳", "人民邮电出版社", "119.00", 20},
                {"9787115594839", "Redis设计与实现", "黄健宏", "机械工业出版社", "89.00", 13},
                {"9787115504265", "鸟哥的Linux私房菜", "鸟哥", "人民邮电出版社", "118.00", 11}
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] book : books) {
                statement.setString(1, (String) book[0]);
                statement.setString(2, (String) book[1]);
                statement.setString(3, (String) book[2]);
                statement.setString(4, (String) book[3]);
                statement.setBigDecimal(5, new BigDecimal((String) book[4]));
                statement.setInt(6, (Integer) book[5]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
