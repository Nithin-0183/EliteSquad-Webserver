package com.ues.core;

import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class RequestHandler {

    private final Map<String, String> domainToRootMap;

    public RequestHandler(Map<String, String> domainToRootMap) {
        this.domainToRootMap = domainToRootMap;
    }

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        return Mono.create(sink -> {
            String host = request.getHeader("Host");
            String rootDir = domainToRootMap.get(host);
            if (rootDir == null) {
                send404(response);
                sink.success();
                return;
            }

            String path = request.getPath();
            if ("/".equals(path)) {
                path = "/index.php"; // Default to index.php for the root path
            }

            File file = new File(rootDir, path);
            if (!file.exists() || file.isDirectory()) {
                send404(response);
            } else {
                try {
                    if (file.getName().endsWith(".php")) {
                        executePhp(file, response);
                    } else {
                        sendResponse(file, response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sink.success();
        });
    }

    private void send404(HttpResponse response) {
        response.setStatusCode(404);
        response.setReasonPhrase("Not Found");
        response.setHeaders(Map.of("Content-Type", "text/html"));
        response.setBody("<h1>404 Not Found</h1>".getBytes());
    }

    private void sendResponse(File file, HttpResponse response) throws IOException {
        String contentType = Files.probeContentType(Paths.get(file.getPath()));
        byte[] content = Files.readAllBytes(file.toPath());

        response.setStatusCode(200);
        response.setReasonPhrase("OK");
        response.setHeaders(Map.of("Content-Type", contentType));
        response.setBody(content);
    }

    private void executePhp(File file, HttpResponse response) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("php-cgi", file.getPath());
        Process process = pb.start();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = process.getInputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }

        String contentType = "text/html"; // Default to text/html for PHP output
        response.setStatusCode(200);
        response.setReasonPhrase("OK");
        response.setHeaders(Map.of("Content-Type", contentType));
        response.setBody(outputStream.toByteArray());
    }
}
