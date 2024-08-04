package com.ues.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ues.NioHttpServer;

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
        } catch (Exception ex) {
            System.err.println("Error loading database properties: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error loading database properties", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );
    }

    public static void initializeDatabase() {
        int retryCount = 5;
        while (retryCount > 0) {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream("init.sql");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                if (inputStream == null) {
                    throw new RuntimeException("Unable to find init.sql");
                }

                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");
                }

                String[] commands = sql.toString().split(";");
                for (String command : commands) {
                    if (!command.trim().isEmpty()) {
                        stmt.execute(command);
                    }
                }

                System.out.println("Database and table initialized successfully.");
                break;

            } catch (SQLException e) {
                System.err.println("Failed to connect to database. Retrying in 5 seconds...");
                retryCount--;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                System.err.println("Error initializing database: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error initializing database", e);
            }
        }

        if (retryCount == 0) {
            throw new RuntimeException("Failed to connect to database after multiple attempts.");
        }
    }

    public static Map<String, String> loadConfigurationFromDatabase() throws IOException{
        Map<String, String> domainToRootMap = new HashMap<>();
        Properties properties = new Properties();
        try {
            InputStream input = NioHttpServer.class.getClassLoader().getResourceAsStream("application.properties");
            properties.load(input);
            String select_query = "select domain, root from sites";
            Connection connection = DriverManager.getConnection(properties.getProperty("db.url")+"/"+properties.getProperty("db.database"), properties.getProperty("db.username"), properties.getProperty("db.password"));
            PreparedStatement preparedStatement = connection.prepareStatement(select_query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String domain = resultSet.getString("domain");
                String root = resultSet.getString("root");
                domainToRootMap.put(domain, root);
            }
            System.out.println("DatabaseConfig << Read from Database: "+domainToRootMap.toString());
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return domainToRootMap;
    }
}
