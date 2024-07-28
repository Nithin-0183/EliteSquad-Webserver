package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpResponseUtil;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class PostRequestHandler {

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        Map<String, String> data = parseRequestBody(request.getBody());
        String contentType = determineContentType(request);

        return ResourceManager.createData(getTableNameFromPath(path), data)
                .flatMap(success -> {
                    if (success) {
                        return HttpResponseUtil.send201(response, "Data created successfully", contentType);
                    } else {
                        return HttpResponseUtil.send500(response, "Failed to create data", contentType);
                    }
                })
                .onErrorResume(e -> HttpResponseUtil.send500(response, e.getMessage(), contentType));
    }

    private Map<String, String> parseRequestBody(String body) {
        Map<String, String> data = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    data.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return data;
    }

    private String getTableNameFromPath(String path) {
        return path.split("/")[1];
    }

    private String determineContentType(HttpRequest request) {
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null) {
            if (acceptHeader.contains("application/json")) {
                return "application/json";
            }
            if (acceptHeader.contains("text/html")) {
                return "text/html";
            }
        }
        return "text/plain";
    }
}
