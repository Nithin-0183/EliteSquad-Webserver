package com.ues;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import com.ues.core.RequestHandler;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;

import reactor.core.publisher.Mono;

public class SecureRequestHandler implements Runnable {
    private final SSLSocket sslSocket;
    private final RequestHandler handler;

    public SecureRequestHandler(SSLSocket sslSocket, RequestHandler handler) {
        this.sslSocket = sslSocket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
             OutputStream out = sslSocket.getOutputStream()) {

            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }

            HttpRequest httpRequest = new HttpRequest(requestBuilder.toString());
            HttpResponse response = new HttpResponse();

            Mono<Void> result = handler.handle(httpRequest, response);

            result.doOnSuccess(v -> {
                try {
                    byte[] responseBytes = response.getResponseBytes();
                    out.write(responseBytes);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).doFinally(signalType -> {
                try {
                    sslSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).block(); // Block until the request is fully handled

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
