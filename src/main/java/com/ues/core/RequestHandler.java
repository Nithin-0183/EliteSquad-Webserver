package com.ues.core;

import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.Map;

public class RequestHandler {

    private final Map<String, String> domainToRootMap;
    private final GetRequestHandler getRequestHandler;
    private final PostRequestHandler postRequestHandler;
    private final PutRequestHandler putRequestHandler;
    private final DeleteRequestHandler deleteRequestHandler;

    public RequestHandler(Map<String, String> domainToRootMap) {
        this.domainToRootMap = domainToRootMap;
        this.getRequestHandler = new GetRequestHandler(domainToRootMap);
        this.postRequestHandler = new PostRequestHandler();
        this.putRequestHandler = new PutRequestHandler();
        this.deleteRequestHandler = new DeleteRequestHandler();
    }

    public Mono<Void> handle(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();
        switch (method) {
            case "GET":
                return getRequestHandler.handle(request, response);
            case "POST":
                return postRequestHandler.handle(request, response);
            case "PUT":
                return putRequestHandler.handle(request, response);
            case "DELETE":
                return deleteRequestHandler.handle(request, response);
            default:
                send405(response);
                return Mono.empty();
        }
    }

    private void send405(HttpResponse response) {
        response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.getCode());
        response.setReasonPhrase(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
        response.setHeaders(Map.of("Content-Type", "application/json"));
        response.setBody("{\"error\":\"Method Not Allowed\"}".getBytes());
    }
}