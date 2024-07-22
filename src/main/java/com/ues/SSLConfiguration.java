package com.ues;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLConfiguration {
    private static final String KEYSTORE_PATH = "C:\\Users\\unnam\\IdeaProjects\\EliteSquad-Webserver\\keystore.jks";
    private static final String KEYSTORE_PASSWORD = "Text@0987"; // Consider using environment variables or a secure config file

    public static SSLServerSocketFactory getServerSocketFactory() throws Exception {
        SSLContext sslContext = createSSLContext();
        return sslContext.getServerSocketFactory();
    }

    public static SSLEngine createSSLEngine() throws Exception {
        SSLContext sslContext = createSSLContext();
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(false);
        return engine;
    }

    private static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        return sslContext;
    }
}