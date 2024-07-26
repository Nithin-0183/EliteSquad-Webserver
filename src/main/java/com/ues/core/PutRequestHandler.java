package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class PutRequestHandler {

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        Map<String, String> data = parseRequestBody(request.getBody());
        String condition = getConditionFromPath(path);

        return ResourceManager.updateData(getTableNameFromPath(path), data, condition)
                .flatMap(success -> {
                    if (success) {
                        response.setStatusCode(HttpStatus.OK.getCode());
                        response.setReasonPhrase(HttpStatus.OK.getReasonPhrase());
                    } else {
                        send500(response, "Failed to update data");
                    }
                    return Mono.empty();
                });
    }

    private Map<String, String> parseRequestBody(String body) {
        Map<String, String> data = new HashMap<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            data.put(keyValue[0], keyValue[1]);
        }
        return data;
    }

    private String getTableNameFromPath(String path) {
        return path.split("/")[1];
    }

    private String getConditionFromPath(String path) {
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : "1=1";
    }

    private void send500(HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        response.setReasonPhrase(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.setHeaders(Map.of("Content-Type", "text/html"));
        response.setBody(("<h1>500 Internal Server Error</h1><p>" + message + "</p>").getBytes());
    }
}
