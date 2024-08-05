package com.ues;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.util.HashMap;
import java.util.Map;

import com.ues.core.RequestHandler;
import com.ues.database.DatabaseConfig;

public class SecureServer implements Runnable {

    private static Map<String, String> domainToRootMap = new HashMap<>();
    private static final int PORT = 8443;

    @Override
    public void run() {
        try {
            domainToRootMap = DatabaseConfig.loadConfigurationFromDatabase();

            SSLServerSocketFactory sslServerSocketFactory = SSLConfiguration.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);

            System.out.println("Secure NIO HTTPS Server is listening on port " + PORT);

            RequestHandler handler = new RequestHandler(domainToRootMap);

            while (true) {
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                new Thread(new SecureRequestHandler(sslSocket, handler)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}