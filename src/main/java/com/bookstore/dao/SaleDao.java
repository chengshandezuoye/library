package com.bookstore.dao;

import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SaleDao {
    public void sell(long bookId, int quantity, long operatorId) throws SQLException {
        String selectBookSql = "SELECT retail_price, stock_qty FROM books WHERE id = ? FOR UPDATE";
        String updateBookSql = "UPDATE books SET stock_qty = stock_qty - ? WHERE id = ?";
        String insertSaleSql = "INSERT INTO sales_orders (book_id, sale_price, quantity, total_amount, operator_id) VALUES (?, ?, ?, ?, ?)";
        String insertFinanceSql = """
                INSERT INTO finance_records (type, amount, related_type, related_id, remark, operator_id)
                VALUES ('income', ?, 'sale', ?, '图书销售', ?)
                """;
        try (Connection connection = DbUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement selectStatement = connection.prepareStatement(selectBookSql);
                 PreparedStatement updateStatement = connection.prepareStatement(updateBookSql);
                 PreparedStatement saleStatement = connection.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement financeStatement = connection.prepareStatement(insertFinanceSql)) {
                selectStatement.setLong(1, bookId);
                BigDecimal price;
                int stock;
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("图书不存在");
                    }
                    price = resultSet.getBigDecimal("retail_price");
                    stock = resultSet.getInt("stock_qty");
                }
                if (stock < quantity) {
                    throw new SQLException("库存不足，当前库存：" + stock);
                }
                BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
                updateStatement.setInt(1, quantity);
                updateStatement.setLong(2, bookId);
                updateStatement.executeUpdate();
                saleStatement.setLong(1, bookId);
                saleStatement.setBigDecimal(2, price);
                saleStatement.setInt(3, quantity);
                saleStatement.setBigDecimal(4, total);
                saleStatement.setLong(5, operatorId);
                saleStatement.executeUpdate();
                long saleId;
                try (ResultSet keys = saleStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("创建销售单失败：未返回销售单ID");
                    }
                    saleId = keys.getLong(1);
                }
                financeStatement.setBigDecimal(1, total);
                financeStatement.setLong(2, saleId);
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
}
