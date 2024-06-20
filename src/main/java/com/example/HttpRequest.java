package com.example;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpRequest {

    private Method method;
    private String uri;
    private String version;
    private String body;

    public HttpRequest(BufferedReader reader, String requestLine) throws IOException {
        parseRequestLine(requestLine);
        StringBuilder bodyBuilder = new StringBuilder();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            // Skip headers for simplicity
        }
        while (reader.ready()) {
            bodyBuilder.append((char) reader.read());
        }
        this.body = bodyBuilder.toString();
    }

    private void parseRequestLine(String requestLine) {
        String[] parts = requestLine.split(" ");
        method = Method.valueOf(parts[0]);
        uri = parts[1];
        version = parts[2];
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

    public enum Method {
        GET, POST, PUT, DELETE, UNRECOGNIZED
    }
}

