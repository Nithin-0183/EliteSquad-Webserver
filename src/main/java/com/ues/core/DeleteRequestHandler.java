package com.ues.core;

import java.util.Map;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
import reactor.core.publisher.Mono;

public class DeleteRequestHandler {

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        String condition = getConditionFromPath(path);

        return ResourceManager.deleteData(getTableNameFromPath(path), condition)
                .flatMap(success -> {
                    if (success) {
                        response.setStatusCode(HttpStatus.OK.getCode());
                        response.setReasonPhrase(HttpStatus.OK.getReasonPhrase());
                    } else {
                        send500(response, "Failed to delete data");
                    }
                    return Mono.<Void>empty();
                })
                .onErrorResume(e -> {
                    send500(response, e.getMessage());
                    return Mono.<Void>empty();
                });
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
