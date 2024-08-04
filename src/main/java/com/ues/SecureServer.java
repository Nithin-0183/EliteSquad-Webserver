package com.ues;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.ues.core.RequestHandler;
import com.ues.database.DatabaseConfig;

public class SecureServer implements Runnable {

    private static Map<String, String> domainToRootMap = new HashMap<>();

    @Override
    public void run() {
        try {
            loadConfigurationFromDatabase();

            SSLServerSocketFactory sslServerSocketFactory = SSLConfiguration.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(8443);

            System.out.println("Secure NIO HTTPS Server is listening on port " + 8443);

            RequestHandler handler = new RequestHandler(domainToRootMap);

            while (true) {
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                new Thread(new SecureRequestHandler(sslSocket, handler)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurationFromDatabase() {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT domain, root FROM sites")) {

            while (resultSet.next()) {
                String domain = resultSet.getString("domain");
                String root = resultSet.getString("root");
                domainToRootMap.put(domain, root);
                System.out.println("Loaded domain: " + domain + ", root: " + root);
            }
            System.out.println("Domain to Root Map: " + domainToRootMap);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading configuration from database: " + e.getMessage());
            throw new RuntimeException("Error loading configuration from database", e);
        }
    }
}