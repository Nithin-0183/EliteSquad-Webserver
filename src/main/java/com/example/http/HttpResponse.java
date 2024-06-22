package com.example.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private int statusCode;
    private String body;
    private Map<String, String> headers = new HashMap<>();

    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_ACCEPTED = 202;
    public static final int STATUS_NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int STATUS_NO_CONTENT = 204;
    public static final int STATUS_RESET_CONTENT = 205;
    public static final int STATUS_PARTIAL_CONTENT = 206;
    public static final int STATUS_MULTI_STATUS = 207;
    public static final int STATUS_ALREADY_REPORTED = 208;
    public static final int STATUS_IM_USED = 226;

    public static final int STATUS_MOVED_PERMANENTLY = 301;
    public static final int STATUS_FOUND = 302;
    public static final int STATUS_SEE_OTHER = 303;
    public static final int STATUS_NOT_MODIFIED = 304;
    public static final int STATUS_TEMPORARY_REDIRECT = 307;
    public static final int STATUS_PERMANENT_REDIRECT = 308;

    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_METHOD_NOT_ALLOWED = 405;
    public static final int STATUS_CONFLICT = 409;

    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_NOT_IMPLEMENTED = 501;
    public static final int STATUS_BAD_GATEWAY = 502;
    public static final int STATUS_SERVICE_UNAVAILABLE = 503;

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
        switch (statusCode) {
            case STATUS_OK:
                return "OK";
            case STATUS_CREATED:
                return "Created";
            case STATUS_ACCEPTED:
                return "Accepted";
            case STATUS_NON_AUTHORITATIVE_INFORMATION:
                return "Non-Authoritative Information";
            case STATUS_NO_CONTENT:
                return "No Content";
            case STATUS_RESET_CONTENT:
                return "Reset Content";
            case STATUS_PARTIAL_CONTENT:
                return "Partial Content";
            case STATUS_MULTI_STATUS:
                return "Multi-Status";
            case STATUS_ALREADY_REPORTED:
                return "Already Reported";
            case STATUS_IM_USED:
                return "IM Used";
            case STATUS_MOVED_PERMANENTLY:
                return "Moved Permanently";
            case STATUS_FOUND:
                return "Found";
            case STATUS_SEE_OTHER:
                return "See Other";
            case STATUS_NOT_MODIFIED:
                return "Not Modified";
            case STATUS_TEMPORARY_REDIRECT:
                return "Temporary Redirect";
            case STATUS_PERMANENT_REDIRECT:
                return "Permanent Redirect";
            case STATUS_BAD_REQUEST:
                return "Bad Request";
            case STATUS_UNAUTHORIZED:
                return "Unauthorized";
            case STATUS_FORBIDDEN:
                return "Forbidden";
            case STATUS_NOT_FOUND:
                return "Not Found";
            case STATUS_METHOD_NOT_ALLOWED:
                return "Method Not Allowed";
            case STATUS_CONFLICT:
                return "Conflict";
            case STATUS_INTERNAL_SERVER_ERROR:
                return "Internal Server Error";
            case STATUS_NOT_IMPLEMENTED:
                return "Not Implemented";
            case STATUS_BAD_GATEWAY:
                return "Bad Gateway";
            case STATUS_SERVICE_UNAVAILABLE:
                return "Service Unavailable";
            default:
                return "Unknown";
        }
    }
}
