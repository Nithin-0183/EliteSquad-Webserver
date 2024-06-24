package com.ues.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private int statusCode;
    private String body;
    private Map<String, String> headers = new HashMap<>();

    public HttpResponse() {
    }

    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public byte[] getResponseBytes() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(getReasonPhrase(statusCode)).append("\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        response.append("Content-Length: ").append(body.length()).append("\r\n");
        response.append("\r\n");
        response.append(body);
        return response.toString().getBytes();
    }

    private String getReasonPhrase(int statusCode) {
        HttpStatus status = HttpStatus.fromCode(statusCode);
        return status.getReasonPhrase();
    }
}
