package com.ues.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class GetRequestHandler {

    private final Map<String, String> domainToRootMap;

    public GetRequestHandler(Map<String, String> domainToRootMap) {
        this.domainToRootMap = domainToRootMap;
    }

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        return Mono.create(sink -> {
            String host = request.getHeader("Host").split(":")[0];
            String rootDir = domainToRootMap.get(host);
            if (rootDir == null) {
                send404(response);
                sink.success();
                return;
            }

            String path = request.getPath();
            if ("/".equals(path)) {
                path = "/index.php";
            }
            File file = new File(rootDir, path);

            if (!file.exists()) {
                handleApiRequest(path, response)
                        .then(Mono.fromRunnable(sink::success))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();
            } else if (file.isDirectory()) {
                sendMultipleResponses(file, response)
                        .then(Mono.fromRunnable(sink::success))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();
            } else {
                if (file.getName().endsWith(".php")) {
                    executePhp(file, response)
                            .then(Mono.fromRunnable(sink::success))
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                } else {
                    sendResponse(file, response)
                            .then(Mono.fromRunnable(sink::success))
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                }
            }
        });
    }

    protected Mono<Void> handleApiRequest(String path, HttpResponse response) {
        return ResourceManager.getData("api_responses", "path = '" + path + "'")
                .flatMapMany(Flux::fromIterable)
                .switchIfEmpty(Mono.defer(() -> {
                    send404(response);
                    return Mono.empty();
                }))
                .flatMap(data -> sendJsonResponse(response, data))
                .onErrorResume(e -> {
                    send500(response, e.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> sendJsonResponse(HttpResponse response, Map<String, String> data) {
        return Mono.fromRunnable(() -> {
            try {
                String json = new ObjectMapper().writeValueAsString(data);
                response.setStatusCode(HttpStatus.OK.getCode());
                response.setReasonPhrase(HttpStatus.OK.getReasonPhrase());
                response.setHeaders(Map.of("Content-Type", "application/json"));
                response.setBody(json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                send500(response, e.getMessage());
            }
        });
    }

    private void send404(HttpResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND.getCode());
        response.setReasonPhrase(HttpStatus.NOT_FOUND.getReasonPhrase());
        response.setHeaders(Map.of("Content-Type", "text/html"));
        response.setBody("<h1>404 Not Found.</h1>".getBytes());
    }

    private void send500(HttpResponse response, String message) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        response.setReasonPhrase(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.setHeaders(Map.of("Content-Type", "text/html"));
        response.setBody(("<h1>500 Internal Server Error</h1><p>" + message + "</p>").getBytes());
    }

    protected Mono<Void> sendResponse(File file, HttpResponse response) {
        return Mono.fromRunnable(() -> {
            try {
                String contentType = Files.probeContentType(file.toPath());
                byte[] content = Files.readAllBytes(file.toPath());

                response.setStatusCode(HttpStatus.OK.getCode());
                response.setReasonPhrase(HttpStatus.OK.getReasonPhrase());
                response.setHeaders(Map.of("Content-Type", contentType));
                response.setBody(content);
            } catch (IOException e) {
                e.printStackTrace();
                send500(response, e.getMessage());
            }
        });
    }

    protected Mono<Void> executePhp(File file, HttpResponse response) {
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

                String contentType = "text/html";
                response.setStatusCode(HttpStatus.OK.getCode());
                response.setReasonPhrase(HttpStatus.OK.getReasonPhrase());
                response.setHeaders(Map.of("Content-Type", contentType));
                response.setBody(outputStream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                send500(response, e.getMessage());
            }
        });
    }

    protected Flux<Void> sendMultipleResponses(File directory, HttpResponse response) {
        return Flux.defer(() -> {
            try (Stream<Path> paths = Files.list(directory.toPath())) {
                return Flux.fromStream(paths)
                        .flatMap(path -> Mono.fromRunnable(() -> {
                            try {
                                String contentType = Files.probeContentType(path);
                                byte[] content = Files.readAllBytes(path);

                                response.setStatusCode(HttpStatus.OK.getCode());
                                response.setReasonPhrase(HttpStatus.OK.getReasonPhrase());
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