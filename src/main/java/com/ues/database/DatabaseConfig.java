package com.ues.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error loading database properties", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            properties.getProperty("db.url"),
            properties.getProperty("db.user"),
            properties.getProperty("db.password")
        );
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Read SQL file from resources directory
            InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream("init.sql");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
            reader.close();

            // Execute SQL file
            String[] commands = sql.toString().split(";");
            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command);
                }
            }

            System.out.println("Database and table initialized successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing database", e);
        }
    }
}