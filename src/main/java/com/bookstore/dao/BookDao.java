package com.bookstore.dao;

import com.bookstore.model.Book;
import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    public List<Book> search(String keyword) throws SQLException {
        String sql = """
                SELECT id, isbn, title, author, publisher, retail_price, stock_qty
                FROM books
                WHERE ? = ''
                   OR CAST(id AS CHAR) = ?
                   OR isbn LIKE ?
                   OR title LIKE ?
                   OR author LIKE ?
                   OR publisher LIKE ?
                ORDER BY id
                """;
        List<Book> books = new ArrayList<>();
        String like = "%" + keyword + "%";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, like);
            statement.setString(4, like);
            statement.setString(5, like);
            statement.setString(6, like);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapBook(resultSet));
                }
            }
        }
        return books;
    }

    public Book findById(long id) throws SQLException {
        String sql = "SELECT id, isbn, title, author, publisher, retail_price, stock_qty FROM books WHERE id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapBook(resultSet) : null;
            }
        }
    }

    public Long findIdByIsbn(String isbn) throws SQLException {
        String sql = "SELECT id FROM books WHERE isbn = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, isbn);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong("id") : null;
            }
        }
    }

    public long create(String isbn, String title, String author, String publisher, BigDecimal retailPrice, int stockQty) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, publisher, retail_price, stock_qty) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, isbn);
            statement.setString(2, title);
            statement.setString(3, author);
            statement.setString(4, publisher);
            statement.setBigDecimal(5, retailPrice);
            statement.setInt(6, stockQty);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("创建图书失败：未返回图书ID");
            }
        }
    }

    public void update(long id, String isbn, String title, String author, String publisher, BigDecimal retailPrice, int stockQty) throws SQLException {
        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, publisher = ?, retail_price = ?, stock_qty = ? WHERE id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, isbn);
            statement.setString(2, title);
            statement.setString(3, author);
            statement.setString(4, publisher);
            statement.setBigDecimal(5, retailPrice);
            statement.setInt(6, stockQty);
            statement.setLong(7, id);
            statement.executeUpdate();
        }
    }

    private Book mapBook(ResultSet resultSet) throws SQLException {
        Book book = new Book();
        book.setId(resultSet.getLong("id"));
        book.setIsbn(resultSet.getString("isbn"));
        book.setTitle(resultSet.getString("title"));
        book.setAuthor(resultSet.getString("author"));
        book.setPublisher(resultSet.getString("publisher"));
        book.setRetailPrice(resultSet.getBigDecimal("retail_price"));
        book.setStockQty(resultSet.getInt("stock_qty"));
        return book;
    }
}
