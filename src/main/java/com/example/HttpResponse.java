package com.example;

import java.io.PrintWriter;

public class HttpResponse {

    private PrintWriter out;

    public HttpResponse(PrintWriter out) {
        this.out = out;
    }

    public void sendResponse(int statusCode, String message) {
        out.println("HTTP/1.1 " + statusCode + " " + getReasonPhrase(statusCode));
        out.println("Content-Type: text/plain");
        out.println("Content-Length: " + message.length());
        out.println();
        out.println(message);
    }

    private String getReasonPhrase(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown";
        }
    }
}

