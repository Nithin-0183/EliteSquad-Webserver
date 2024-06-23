package com.example.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private Method method;
    private String uri;
    private String version;
    private String body;
    private Map<String, String> headers = new HashMap<>();

    public HttpRequest(String request) {
        String[] lines = request.split("\r\n");
        parseRequestLine(lines[0]);

        int i = 1;
        while (i < lines.length && !lines[i].isEmpty()) {
            String[] header = lines[i].split(": ");
            if (header.length == 2) {
                headers.put(header[0], header[1]);
            }
            i++;
        }

        StringBuilder bodyBuilder = new StringBuilder();
        for (int j = i + 1; j < lines.length; j++) {
            bodyBuilder.append(lines[j]);
        }
        this.body = bodyBuilder.toString();
    }

    private void parseRequestLine(String requestLine) {
        String[] parts = requestLine.split(" ");
        if (parts.length >= 3) {
            method = Method.valueOf(parts[0]);
            uri = parts[1];
            version = parts[2];
        } else {
            throw new IllegalArgumentException("Invalid HTTP request line: " + requestLine);
        }
    }

    public Method getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public enum Method {
        GET, POST, PUT, DELETE
    }
}
