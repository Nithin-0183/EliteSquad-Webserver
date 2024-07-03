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
        handleResponse("GET", request, response);
    }

    private void handlePost(HttpRequest request, HttpResponse response) {
        handleResponse("POST", request, response);
    }

    private void handlePut(HttpRequest request, HttpResponse response) {
        handleResponse("PUT", request, response);
    }

    private void handleDelete(HttpRequest request, HttpResponse response) {
        handleResponse("DELETE", request, response);
    }

    private void handleBadRequest(HttpResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.getCode());
        response.setBody("Bad Request");
        response.addHeader("Content-Type", "text/plain");
    }

    private void handleResponse(String method, HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        HttpStatus status = HttpStatusMapping.STATUS_CODES.getOrDefault(uri, HttpStatus.BAD_REQUEST);
        String responseBody = HttpStatusMapping.RESPONSE_BODIES.getOrDefault(uri, status.getReasonPhrase());

        responseBody = method + " " + responseBody;

        response.setStatus(status.getCode());
        response.setBody(responseBody);
        response.addHeader("Content-Type", "text/plain");
    }
}
