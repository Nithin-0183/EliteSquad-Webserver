package com.ues.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpResponseUtil;
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
                HttpResponseUtil.send404(response, "Host not found: " + host, "text/html");
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
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return HttpResponseUtil.send404(response, "API data not found for path: " + path, "text/html");
                    } else {
                        return sendJsonResponse(response, list.get(0));
                    }
                })
                .onErrorResume(e -> HttpResponseUtil.send500(response, e.getMessage(), "text/html"));
    }    

    private Mono<Void> sendJsonResponse(HttpResponse response, Map<String, String> data) {
        return Mono.fromCallable(() -> {
            try {
                String json = new ObjectMapper().writeValueAsString(data);
                return HttpResponseUtil.send200(response, json, "application/json");
            } catch (IOException e) {
                return HttpResponseUtil.send500(response, e.getMessage(), "text/html");
            }
        }).flatMap(mono -> mono);
    }

    protected Mono<Void> sendResponse(File file, HttpResponse response) {
        return Mono.fromCallable(() -> {
            try {
                String contentType = Files.probeContentType(file.toPath());
                if (contentType == null) {
                    contentType = "text/plain"; // Fallback if content type is not detected
                }
                byte[] content = Files.readAllBytes(file.toPath());
                return HttpResponseUtil.send200(response, new String(content), contentType);
            } catch (IOException e) {
                return HttpResponseUtil.send500(response, e.getMessage(), "text/html");
            }
        }).flatMap(mono -> mono);
    }

    protected Mono<Void> executePhp(File file, HttpResponse response) {
        return Mono.fromCallable(() -> {
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
                return HttpResponseUtil.send200(response, new String(outputStream.toByteArray()), "text/html");
            } catch (IOException e) {
                return HttpResponseUtil.send500(response, e.getMessage(), "text/html");
            }
        }).flatMap(mono -> mono);
    }

    protected Flux<Void> sendMultipleResponses(File directory, HttpResponse response) {
        return Flux.defer(() -> {
            try (Stream<Path> paths = Files.list(directory.toPath())) {
                return Flux.fromStream(paths)
                        .flatMap(path -> Mono.fromCallable(() -> {
                            try {
                                String contentType = Files.probeContentType(path);
                                if (contentType == null) {
                                    contentType = "text/plain"; // Fallback if content type is not detected
                                }
                                byte[] content = Files.readAllBytes(path);
                                return HttpResponseUtil.send200(response, new String(content), contentType);
                            } catch (IOException e) {
                                return HttpResponseUtil.send500(response, e.getMessage(), "text/html");
                            }
                        }).flatMap(mono -> mono));
            } catch (IOException e) {
                return Flux.error(e);
            }
        });
    }
}
