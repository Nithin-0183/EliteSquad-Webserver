package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GetRequestHandlerTest {

    private GetRequestHandler getRequestHandler;
    private Map<String, String> domainToRootMap;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        domainToRootMap = new HashMap<>();
        domainToRootMap.put("example.com", tempDir.toString());
        getRequestHandler = spy(new GetRequestHandler(domainToRootMap));
    }

    @Test
    void handleApiRequest_dataExists() {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/api/test");
        when(request.getHeader("Accept")).thenReturn("application/json");

        Map<String, String> data = Map.of("message", "Hello, World!");
        try (MockedStatic<ResourceManager> mockedStatic = mockStatic(ResourceManager.class);
             MockedStatic<HttpResponseUtil> mockedUtil = mockStatic(HttpResponseUtil.class)) {

            mockedStatic.when(() -> ResourceManager.getData(anyString(), anyString()))
                    .thenReturn(Mono.just(List.of(data)));

            mockedUtil.when(() -> HttpResponseUtil.send200(any(HttpResponse.class), anyString(), anyString()))
                    .thenReturn(Mono.empty());

            Mono<Void> result = getRequestHandler.handleApiRequest("/api/test", request, response);

            StepVerifier.create(result)
                    .verifyComplete();

            mockedUtil.verify(() -> HttpResponseUtil.send200(response, "{\"message\":\"Hello, World!\"}", "application/json"));
        }
    }

    @Test
    void handleApiRequest_dataNotExists() {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/api/test");
        when(request.getHeader("Accept")).thenReturn("application/json");

        try (MockedStatic<ResourceManager> mockedResourceManager = mockStatic(ResourceManager.class);
             MockedStatic<HttpResponseUtil> mockedUtil = mockStatic(HttpResponseUtil.class)) {

            mockedResourceManager.when(() -> ResourceManager.getData(anyString(), anyString()))
                    .thenReturn(Mono.just(List.of()));

            mockedUtil.when(() -> HttpResponseUtil.send404(any(HttpResponse.class), anyString(), anyString()))
                    .thenReturn(Mono.empty());

            Mono<Void> result = getRequestHandler.handleApiRequest("/api/test", request, response);

            StepVerifier.create(result)
                    .verifyComplete();

            mockedUtil.verify(() -> HttpResponseUtil.send404(response, "API data not found for path: /api/test", "application/json"));
        }
    }

    @Test
    void handleFileRequest_fileExists() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/index.html");
        when(request.getHeader("Accept")).thenReturn("text/html");

        Path filePath = tempDir.resolve("index.html");
        Files.createFile(filePath);

        doReturn(Mono.empty()).when(getRequestHandler).sendResponse(any(File.class), any(HttpResponse.class), any(HttpRequest.class));

        Mono<Void> result = getRequestHandler.handle(request, response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(getRequestHandler).sendResponse(any(File.class), eq(response), eq(request));
    }

    @Test
    void handleFileRequest_fileNotExists() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/nonexistent.html");
        when(request.getHeader("Accept")).thenReturn("text/html");

        doReturn(Mono.empty()).when(getRequestHandler).handleApiRequest(anyString(), any(HttpRequest.class), any(HttpResponse.class));

        Mono<Void> result = getRequestHandler.handle(request, response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(getRequestHandler).handleApiRequest(eq("/nonexistent.html"), eq(request), eq(response));
    }

    @Test
    void handlePhpFileRequest_phpFileExists() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/index.php");
        when(request.getHeader("Accept")).thenReturn("text/html");

        Path filePath = tempDir.resolve("index.php");
        Files.createFile(filePath);

        doReturn(Mono.empty()).when(getRequestHandler).executePhp(any(File.class), any(HttpResponse.class));

        Mono<Void> result = getRequestHandler.handle(request, response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(getRequestHandler).executePhp(any(File.class), eq(response));
    }

    @Test
    void handleDirectoryRequest_directoryExists() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/dir/");
        when(request.getHeader("Accept")).thenReturn("text/html");

        Path dirPath = tempDir.resolve("dir");
        Files.createDirectories(dirPath);

        doReturn(Flux.empty()).when(getRequestHandler).sendMultipleResponses(any(File.class), any(HttpResponse.class));

        Mono<Void> result = getRequestHandler.handle(request, response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(getRequestHandler).sendMultipleResponses(any(File.class), eq(response));
    }
}
