package com.bookstore.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DbUtil {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = DbUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot find db.properties in resources.");
            }
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DbUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.username"),
                PROPERTIES.getProperty("db.password")
        );
    }
}
