package com.ues;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import com.ues.core.RequestHandler;

public class SecureServer implements Runnable{
    private static Map<String, String> domainToRootMap = new HashMap<>();
    public void run () {
        
        try {
            loadConfiguration();
            SSLServerSocketFactory sslServerSocketFactory = SSLConfiguration.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(8443);

            System.out.println("Secure NIO HTTPS Server is listening on port " + 8443);

            //ResourceManager resourceManager = new ResourceManager();
            RequestHandler handler = new RequestHandler(domainToRootMap);

            while (true) {
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                new Thread(new SecureRequestHandler(sslSocket, handler)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfiguration() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = SecureServer.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
            domainToRootMap.put(properties.getProperty("site1.domain"), properties.getProperty("site1.root"));
            domainToRootMap.put(properties.getProperty("site2.domain"), properties.getProperty("site2.root"));
        }
    }
}
