package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpResponseUtil;
import reactor.core.publisher.Mono;

public class DeleteRequestHandler {

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        String condition = getConditionFromPath(path);

        return ResourceManager.deleteData(getTableNameFromPath(path), condition)
                .flatMap(success -> {
                    if (success) {
                        return HttpResponseUtil.send200(response, "Data deleted successfully", "text/html");
                    } else {
                        return HttpResponseUtil.send500(response, "Failed to delete data", "text/html");
                    }
                })
                .onErrorResume(e -> HttpResponseUtil.send500(response, e.getMessage(), "text/html"));
    }

    private String getTableNameFromPath(String path) {
        return path.split("/")[1];
    }

    private String getConditionFromPath(String path) {
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : "1=1";
    }
}
