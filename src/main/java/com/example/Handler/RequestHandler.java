package com.example.Handler;

import com.example.http.HttpRequest;
import com.example.http.HttpResponse;
import com.example.http.HttpStatus;
import com.example.http.HttpStatusMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RequestHandler {

    private static final String WEB_ROOT = "./WEB_ROOT";

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
        //handleResponse("GET", request, response);
        String uri = request.getUri();
        File file = new File(WEB_ROOT, uri);

        if (file.exists() && !file.isDirectory()) {
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String contentType = getContentType(file.getName());
                response.setStatus(HttpStatus.OK.getCode());
                response.addHeader("Content-Type", contentType);
                response.addHeader("Content-Length", String.valueOf(fileContent.length));
                response.setBody(new String(fileContent, "UTF-8"));
            } catch (IOException e) {
                handleInternalServerError(response);
            }
        } else {
            handleNotFound(response);
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "text/plain";  // Default Content Type
        }
    }

    private void handleNotFound(HttpResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.getCode());
        response.setBody("404 Not Found");
        response.addHeader("Content-Type", "text/plain");
    }

    private void handleInternalServerError(HttpResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        response.setBody("500 Internal Server Error");
        response.addHeader("Content-Type", "text/plain");
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
