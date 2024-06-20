package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestHandler implements Runnable {

    private Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("Request: " + requestLine);

            // Parse the request
            HttpRequest request = new HttpRequest(in, requestLine);
            HttpResponse response = new HttpResponse(out);

            // Handle the request
            switch (request.getMethod()) {
                case GET:
                    handleGet(request, response);
                    break;
                case POST:
                    handlePost(request, response);
                    break;
                case PUT:
                    handlePut(request, response);
                    break;
                case DELETE:
                    handleDelete(request, response);
                    break;
                default:
                    response.sendResponse(400, "Bad Request");
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGet(HttpRequest request, HttpResponse response) {

        response.sendResponse(200, "GET request handled successfully.");
    }

    private void handlePost(HttpRequest request, HttpResponse response) {

        response.sendResponse(200, "POST request handled successfully.");
    }

    private void handlePut(HttpRequest request, HttpResponse response) {

        response.sendResponse(200, "PUT request handled successfully.");
    }

    private void handleDelete(HttpRequest request, HttpResponse response) {

        response.sendResponse(200, "DELETE request handled successfully.");
    }
}

