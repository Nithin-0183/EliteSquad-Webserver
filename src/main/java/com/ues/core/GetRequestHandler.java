package com.ues.core;

import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpResponseUtil;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

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
                HttpResponseUtil.send404(response, "Host not found: " + host, determineContentType(request))
                    .then(Mono.fromRunnable(sink::success))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
                return;
            }

            String path = request.getPath();
            if ("/".equals(path)) {
                path = "/index.php";
            }
            File file = new File(rootDir, path);
            
            if (!file.exists()) {
                HttpResponseUtil.send404(response, "File not found: " + path, determineContentType(request))
                    .then(Mono.fromRunnable(sink::success))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
                return;
            }
            if (file.getName().endsWith(".php")) {
                executePhp(file, response)
                    .then(Mono.fromRunnable(sink::success))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            } else {
                sendResponse(file, response, request)
                    .then(Mono.fromRunnable(sink::success))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            }
        });
    }

    protected Mono<Void> sendResponse(File file, HttpResponse response, HttpRequest request) {
        return Mono.fromCallable(() -> {
            try {
                String contentType = Files.probeContentType(file.toPath());
                if (contentType == null) {
                    contentType = "text/plain";
                }
                byte[] content = Files.readAllBytes(file.toPath());
                return HttpResponseUtil.send200(response, new String(content), contentType);
            } catch (IOException e) {
                return HttpResponseUtil.send500(response, e.getMessage(), "text/plain");
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

    private String determineContentType(HttpRequest request) {
        String acceptHeader = request != null ? request.getHeader("Accept") : "";
        if (acceptHeader.contains("application/json")) {
            return "application/json";
        }
        if (acceptHeader.contains("text/html")) {
            return "text/html";
        }
        return "text/plain";
    }
}