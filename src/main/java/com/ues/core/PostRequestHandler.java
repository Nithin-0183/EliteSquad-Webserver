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

        return ResourceManager.createData(getTableNameFromPath(path), data)
                .flatMap(success -> {
                    if (success) {
                        return HttpResponseUtil.send201(response, "Data created successfully", "text/html");
                    } else {
                        return HttpResponseUtil.send500(response, "Failed to create data", "text/html");
                    }
                })
                .onErrorResume(e -> HttpResponseUtil.send500(response, e.getMessage(), "text/html"));
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
}