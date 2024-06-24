package com.ues.core;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
import com.ues.http.HttpStatusMapping;

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
            if (uri.equals("/")) {
                uri = "/index.html"; // Ensure this is your landing page.
            }
            File file = new File(WEB_ROOT, uri);
        
            if (file.exists() && !file.isDirectory()) {
                serveFile(file, response);
            } else {
                handleNotFound(response);
            }
    }
    
    private void serveFile(File file, HttpResponse response) {
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
    } 
    

    
    private void handlePost(HttpRequest request, HttpResponse response) {
        //handleResponse("POST", request, response);
        if (request.getUri().equals("/submit")) {
        Map<String, String> formData = parseRequestBody(request.getBody());

            if (isValidPostData(formData)) {
                boolean isCreated = ResourceManager.createResource(formData);
                if (isCreated) {
                    response.setStatus(HttpStatus.CREATED.getCode());
                    response.setBody("Resource created successfully.");
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                    response.setBody("Failed to create resource.");
                }
            } else {
                handleBadRequest(response);
            }
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.getCode());
            response.setBody("Endpoint not found.");
        }
        response.addHeader("Content-Type", "text/plain");
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


    private Map<String, String> parseRequestBody(String body) {
        Map<String, String> postData = new HashMap<>();
        if (body != null && !body.isBlank()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.toString());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.toString());
                    postData.put(key, value);
                } catch (Exception e) {
                    // Log error or handle exception as needed
                    e.printStackTrace();
                }
            }
        }
        return postData;
    }

    private boolean isValidPostData(Map<String, String> data) {

        return data.containsKey("name") && !data.get("name").isBlank()
            && data.containsKey("value") && !data.get("value").isBlank();
    }


}
