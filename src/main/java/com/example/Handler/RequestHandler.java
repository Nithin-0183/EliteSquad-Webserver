package com.example.Handler;

import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.http.HttpStatus;
import com.example.http.HttpStatusMapping;

public class RequestHandler {

    public void handleRequest(HttpRequest request, HttpResponse response) {
        String method = request.getMethod().toString();
        switch (method) {
            case "GET":
                handleGet(request, response);
                break;
            case "POST":
                handlePost(request, response);
                break;
            case "PUT":
                handlePut(request, response);
                break;
            case "DELETE":
                handleDelete(request, response);
                break;
            default:
                handleBadRequest(response);
                break;
        }
    }

    private void handleGet(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        HttpStatus status = HttpStatusMapping.STATUS_CODES.getOrDefault(uri, HttpStatus.BAD_REQUEST);
        String responseBody = HttpStatusMapping.RESPONSE_BODIES.getOrDefault(uri, status.getReasonPhrase());

        responseBody = "GET " + responseBody;

        response.setStatus(status.getCode());
        response.setBody(responseBody);
        response.addHeader("Content-Type", "text/plain");
    }

    private void handlePost(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        HttpStatus status = HttpStatusMapping.STATUS_CODES.getOrDefault(uri, HttpStatus.BAD_REQUEST);
        String responseBody = HttpStatusMapping.RESPONSE_BODIES.getOrDefault(uri, status.getReasonPhrase());

        responseBody = "POST " + responseBody;

        response.setStatus(status.getCode());
        response.setBody(responseBody);
        response.addHeader("Content-Type", "text/plain");
    }

    private void handlePut(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        HttpStatus status = HttpStatusMapping.STATUS_CODES.getOrDefault(uri, HttpStatus.BAD_REQUEST);
        String responseBody = HttpStatusMapping.RESPONSE_BODIES.getOrDefault(uri, status.getReasonPhrase());

        responseBody = "PUT " + responseBody;

        response.setStatus(status.getCode());
        response.setBody(responseBody);
        response.addHeader("Content-Type", "text/plain");
    }

    private void handleDelete(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        HttpStatus status = HttpStatusMapping.STATUS_CODES.getOrDefault(uri, HttpStatus.BAD_REQUEST);
        String responseBody = HttpStatusMapping.RESPONSE_BODIES.getOrDefault(uri, status.getReasonPhrase());

        responseBody = "DELETE " + responseBody;

        response.setStatus(status.getCode());
        response.setBody(responseBody);
        response.addHeader("Content-Type", "text/plain");
    }

    private void handleBadRequest(HttpResponse response) {
        String responseBody = "Bad Request";

        response.setStatus(HttpStatus.BAD_REQUEST.getCode());
        response.setBody(responseBody);
        response.addHeader("Content-Type", "text/plain");
    }
}
