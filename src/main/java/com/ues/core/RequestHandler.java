package com.ues.core;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;

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
        String uri = request.getUri();
        if (uri.equals("/")) {
            uri = "/index.html"; // Ensure this is your landing page.
        } else if (uri.equals("/messages")) {
            handleGetMessages(response);
            return;
        }
        File file = new File(WEB_ROOT, uri);
    
        if (file.exists() && !file.isDirectory()) {
            serveFile(file, response);
        } else {
            handleNotFound(response);
        }
    }
    
    private void handleGetMessages(HttpResponse response) {
        List<Map<String, String>> messages = ResourceManager.getMessages();
        StringBuilder json = new StringBuilder("[");
        for (Map<String, String> message : messages) {
            json.append("{")
                .append("\"id\":").append(message.get("id")).append(",")
                .append("\"user\":\"").append(message.get("user")).append("\",")
                .append("\"message\":\"").append(message.get("message")).append("\",")
                .append("\"timestamp\":\"").append(message.get("timestamp")).append("\"")
                .append("},");
        }
        if (json.length() > 1) {
            json.setLength(json.length() - 1); // Remove trailing comma
        }
        json.append("]");
        
        response.setStatus(HttpStatus.OK.getCode());
        response.addHeader("Content-Type", "application/json");
        response.setBody(json.toString());
    }
    
    private void serveFile(File file, HttpResponse response) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String contentType = getContentType(file.getName());
            response.setStatus(HttpStatus.OK.getCode());
            response.addHeader("Content-Type", contentType);
            response.addHeader("Content-Length", String.valueOf(fileContent.length));
            response.setBody(new String(fileContent, StandardCharsets.UTF_8));
        } catch (IOException e) {
            handleInternalServerError(response);
        }
    } 
    
    private void handlePost(HttpRequest request, HttpResponse response) {
        if (request.getUri().equals("/messages")) {
            Map<String, String> formData = parseRequestBody(request.getBody());
            if (isValidPostData(formData)) {
                boolean isCreated = ResourceManager.createMessage(formData);
                if (isCreated) {
                    response.setStatus(HttpStatus.CREATED.getCode());
                    response.setBody("Message created successfully.");
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                    response.setBody("Failed to create message.");
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
        if (request.getUri().startsWith("/messages/")) {
            String id = request.getUri().substring("/messages/".length());
            Map<String, String> formData = parseRequestBody(request.getBody());
            if (isValidPutData(formData)) {
                boolean isUpdated = ResourceManager.updateMessage(id, formData);
                if (isUpdated) {
                    response.setStatus(HttpStatus.OK.getCode());
                    response.setBody("Message updated successfully.");
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                    response.setBody("Failed to update message.");
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

    private void handleDelete(HttpRequest request, HttpResponse response) {
        if (request.getUri().startsWith("/messages/")) {
            String id = request.getUri().substring("/messages/".length());
            boolean isDeleted = ResourceManager.deleteMessage(id);
            if (isDeleted) {
                response.setStatus(HttpStatus.NO_CONTENT.getCode());
                response.setBody(""); // Empty body for No Content status
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                response.setBody("Failed to delete message.");
                response.addHeader("Content-Type", "text/plain");
            }
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.getCode());
            response.setBody("Endpoint not found.");
            response.addHeader("Content-Type", "text/plain");
        }
    }

    private void handleBadRequest(HttpResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.getCode());
        response.setBody("Bad Request");
        response.addHeader("Content-Type", "text/plain");
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
                    e.printStackTrace();
                }
            }
        }
        return postData;
    }

    private boolean isValidPostData(Map<String, String> data) {
        return data.containsKey("user") && !data.get("user").isBlank()
            && data.containsKey("message") && !data.get("message").isBlank();
    }

    private boolean isValidPutData(Map<String, String> data) {
        return data.containsKey("message") && !data.get("message").isBlank();
    }
}
