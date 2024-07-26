package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class PostRequestHandler {

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        Map<String, String> data = parseRequestBody(request.getBody());

        return ResourceManager.createData(getTableNameFromPath(path), data)
                .flatMap(success -> {
                    if (success) {
                        response.setStatusCode(HttpStatus.CREATED.getCode());
                        response.setReasonPhrase(HttpStatus.CREATED.getReasonPhrase());
                    } else {
                        send500(response, "Failed to create data");
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

    private void send500(HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        response.setReasonPhrase(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.setHeaders(Map.of("Content-Type", "text/html"));
        response.setBody(("<h1>500 Internal Server Error</h1><p>" + message + "</p>").getBytes());
    }
}
