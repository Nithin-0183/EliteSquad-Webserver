package com.ues.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String method;
    private String path;
    private String version;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public HttpRequest(String request) {
        String[] lines = request.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        this.method = requestLine[0];
        this.path = requestLine[1];
        this.version = requestLine[2];

        boolean isBody = false;
        StringBuilder bodyBuilder = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            if (isBody) {
                bodyBuilder.append(lines[i]).append("\r\n");
            } else {
                String[] header = lines[i].split(": ");
                if (header.length == 2) {
                    headers.put(header[0], header[1]);
                } else if (lines[i].isEmpty()) {
                    isBody = true;
                }
            }
        }
        this.body = bodyBuilder.toString().trim();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody() {
        return body;
    }
}
