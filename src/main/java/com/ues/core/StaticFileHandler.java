package com.ues.core;

import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StaticFileHandler extends RequestHandler {

    private String basePath;

    public StaticFileHandler(String basePath, Map<String, String> config) {
        super(config); // Pass the config to the superclass constructor
        this.basePath = basePath;
    }

    @Override
    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        try {
            String path = request.getPath().equals("/") ? basePath + "/templates/index.html" : basePath + request.getPath();
            System.out.println("Path: " + path);
            if (Files.exists(Paths.get(path))) {
                byte[] content = Files.readAllBytes(Paths.get(path));
                response.setStatusCode(200);
                response.setReasonPhrase("OK");
                response.setBody(content);
                response.getHeaders().put("Content-Type", getMimeType(path));
            } else {
                response.setStatusCode(404);
                response.setReasonPhrase("Not Found");
                response.setBody("File not found".getBytes());
            }
            return Mono.empty();
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    private String getMimeType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        return "text/plain";
    }
}
