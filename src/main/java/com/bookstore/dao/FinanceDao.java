package com.bookstore.dao;

import com.bookstore.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceDao {
    public List<Object[]> search(LocalDate startDate, LocalDate endDate, String type) throws SQLException {
        String sql = """
                SELECT fr.id, fr.type, fr.amount, fr.related_type, fr.related_id, fr.remark, u.username, fr.created_at
                FROM finance_records fr
                JOIN users u ON u.id = fr.operator_id
                WHERE fr.created_at >= ?
                  AND fr.created_at < DATE_ADD(?, INTERVAL 1 DAY)
                  AND (? = '' OR fr.type = ?)
                ORDER BY fr.created_at DESC
                """;
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));
            statement.setString(3, type);
            statement.setString(4, type);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new Object[]{
                            resultSet.getLong("id"),
                            resultSet.getString("type"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("related_type"),
                            resultSet.getLong("related_id"),
                            resultSet.getString("remark"),
                            resultSet.getString("username"),
                            resultSet.getTimestamp("created_at")
                    });
                }
            }
        }
        return rows;
    }
}
