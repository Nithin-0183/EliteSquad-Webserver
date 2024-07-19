package com.ues.core;

import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;

import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

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
                path = "/index.html";
            }
            System.out.println("rootDir="+rootDir+" path= "+path);
            File file = new File(rootDir, path);
            if (!file.exists()) {
                send404(response);
                sink.success();
            } else if (file.isDirectory()) {
                sendMultipleResponses(file, response)
                        .doOnTerminate(sink::success)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();
            } else {
                if (file.getName().endsWith(".php")) {
                    executePhp(file, response)
                            .doOnTerminate(sink::success)
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                } else {
                    sendResponse(file, response)
                            .doOnTerminate(sink::success)
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                }
            }
        });
    }

    private void send404(HttpResponse response) {
        response.setStatusCode(404);
        response.setReasonPhrase("Not Found");
        response.setHeaders(Map.of("Content-Type", "text/html"));
        response.setBody("<h1>404 Not Found</h1>".getBytes());
    }

    private Mono<Void> sendResponse(File file, HttpResponse response) {
        return Mono.fromRunnable(() -> {
            try {
                String contentType = Files.probeContentType(file.toPath());
                byte[] content = Files.readAllBytes(file.toPath());

                response.setStatusCode(200);
                response.setReasonPhrase("OK");
                response.setHeaders(Map.of("Content-Type", contentType));
                response.setBody(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Mono<Void> executePhp(File file, HttpResponse response) {
        return Mono.fromRunnable(() -> {
            try {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Flux<Void> sendMultipleResponses(File directory, HttpResponse response) {
        return Flux.defer(() -> {
            try (Stream<Path> paths = Files.list(directory.toPath())) {
                return Flux.fromStream(paths)
                        .flatMap(path -> Mono.fromRunnable(() -> {
                            try {
                                String contentType = Files.probeContentType(path);
                                byte[] content = Files.readAllBytes(path);

                                response.setStatusCode(200);
                                response.setReasonPhrase("OK");
                                response.setHeaders(Map.of("Content-Type", contentType));
                                response.setBody(content);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
            } catch (IOException e) {
                return Flux.error(e);
            }
        });
    }
}
