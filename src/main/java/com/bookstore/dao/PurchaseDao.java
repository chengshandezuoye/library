package com.bookstore.dao;

import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDao {
    public List<Object[]> findAll() throws SQLException {
        String sql = """
                SELECT po.id, po.status, po.total_amount, po.created_at,
                       poi.book_id, poi.isbn, poi.title, poi.author, poi.publisher, poi.purchase_price, poi.quantity
                FROM purchase_orders po
                JOIN purchase_order_items poi ON poi.purchase_order_id = po.id
                ORDER BY po.id DESC
                """;
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new Object[]{
                        resultSet.getLong("id"),
                        resultSet.getString("status"),
                        resultSet.getString("isbn"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getString("publisher"),
                        resultSet.getBigDecimal("purchase_price"),
                        resultSet.getInt("quantity"),
                        resultSet.getBigDecimal("total_amount"),
                        resultSet.getTimestamp("created_at")
                });
            }
        }
        return rows;
    }

    public void create(Long bookId, String isbn, String title, String author, String publisher, BigDecimal purchasePrice, int quantity, long operatorId) throws SQLException {
        BigDecimal total = purchasePrice.multiply(BigDecimal.valueOf(quantity));
        String orderSql = "INSERT INTO purchase_orders (status, operator_id, total_amount) VALUES ('unpaid', ?, ?)";
        String itemSql = """
                INSERT INTO purchase_order_items
                (purchase_order_id, book_id, isbn, title, author, publisher, purchase_price, quantity, subtotal)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DbUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement orderStatement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement itemStatement = connection.prepareStatement(itemSql)) {
                orderStatement.setLong(1, operatorId);
                orderStatement.setBigDecimal(2, total);
                orderStatement.executeUpdate();
                long purchaseOrderId;
                try (ResultSet keys = orderStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("创建进货单失败：未返回进货单ID");
                    }
                    purchaseOrderId = keys.getLong(1);
                }
                itemStatement.setLong(1, purchaseOrderId);
                if (bookId == null) {
                    itemStatement.setNull(2, java.sql.Types.BIGINT);
                } else {
                    itemStatement.setLong(2, bookId);
                }
                itemStatement.setString(3, isbn);
                itemStatement.setString(4, title);
                itemStatement.setString(5, author);
                itemStatement.setString(6, publisher);
                itemStatement.setBigDecimal(7, purchasePrice);
                itemStatement.setInt(8, quantity);
                itemStatement.setBigDecimal(9, total);
                itemStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void pay(long purchaseOrderId, long operatorId) throws SQLException {
        String selectSql = "SELECT status, total_amount FROM purchase_orders WHERE id = ? FOR UPDATE";
        String updateSql = "UPDATE purchase_orders SET status = 'paid' WHERE id = ?";
        String financeSql = """
                INSERT INTO finance_records (type, amount, related_type, related_id, remark, operator_id)
                VALUES ('expense', ?, 'purchase', ?, '进货付款', ?)
                """;
        try (Connection connection = DbUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                 PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                 PreparedStatement financeStatement = connection.prepareStatement(financeSql)) {
                selectStatement.setLong(1, purchaseOrderId);
                BigDecimal total;
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("进货单不存在");
                    }
                    if (!"unpaid".equals(resultSet.getString("status"))) {
                        throw new SQLException("只有未付款进货单可以付款");
                    }
                    total = resultSet.getBigDecimal("total_amount");
                }
                updateStatement.setLong(1, purchaseOrderId);
                updateStatement.executeUpdate();
                financeStatement.setBigDecimal(1, total);
                financeStatement.setLong(2, purchaseOrderId);
                financeStatement.setLong(3, operatorId);
                financeStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void returnOrder(long purchaseOrderId) throws SQLException {
        String sql = "UPDATE purchase_orders SET status = 'returned' WHERE id = ? AND status = 'unpaid'";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, purchaseOrderId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("只有未付款进货单可以退货");
            }
        }
    }

    public void stockIn(long purchaseOrderId, BigDecimal retailPrice) throws SQLException {
        String selectSql = """
                SELECT po.status, poi.book_id, poi.isbn, poi.title, poi.author, poi.publisher, poi.quantity
                FROM purchase_orders po
                JOIN purchase_order_items poi ON poi.purchase_order_id = po.id
                WHERE po.id = ?
                FOR UPDATE
                """;
        String updateBookSql = "UPDATE books SET stock_qty = stock_qty + ?, retail_price = ? WHERE id = ?";
        String insertBookSql = "INSERT INTO books (isbn, title, author, publisher, retail_price, stock_qty) VALUES (?, ?, ?, ?, ?, ?)";
        String updateOrderSql = "UPDATE purchase_orders SET status = 'stocked' WHERE id = ?";
        try (Connection connection = DbUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                 PreparedStatement updateBookStatement = connection.prepareStatement(updateBookSql);
                 PreparedStatement insertBookStatement = connection.prepareStatement(insertBookSql);
                 PreparedStatement updateOrderStatement = connection.prepareStatement(updateOrderSql)) {
                selectStatement.setLong(1, purchaseOrderId);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("进货单不存在");
                    }
                    if (!"paid".equals(resultSet.getString("status"))) {
                        throw new SQLException("只有已付款进货单可以入库");
                    }
                    long bookId = resultSet.getLong("book_id");
                    boolean existingBook = !resultSet.wasNull();
                    int quantity = resultSet.getInt("quantity");
                    if (existingBook) {
                        updateBookStatement.setInt(1, quantity);
                        updateBookStatement.setBigDecimal(2, retailPrice);
                        updateBookStatement.setLong(3, bookId);
                        updateBookStatement.executeUpdate();
                    } else {
                        insertBookStatement.setString(1, resultSet.getString("isbn"));
                        insertBookStatement.setString(2, resultSet.getString("title"));
                        insertBookStatement.setString(3, resultSet.getString("author"));
                        insertBookStatement.setString(4, resultSet.getString("publisher"));
                        insertBookStatement.setBigDecimal(5, retailPrice);
                        insertBookStatement.setInt(6, quantity);
                        insertBookStatement.executeUpdate();
                    }
                    updateOrderStatement.setLong(1, purchaseOrderId);
                    updateOrderStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
