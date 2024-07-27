package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        Map<String, String> data = Map.of("message", "Hello, World!");
        try (MockedStatic<ResourceManager> mockedStatic = mockStatic(ResourceManager.class)) {
            mockedStatic.when(() -> ResourceManager.getData(anyString(), anyString()))
                    .thenReturn(Mono.just(List.of(data)));

            Mono<Void> result = getRequestHandler.handle(request, response);

            StepVerifier.create(result)
                    .verifyComplete();

            assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
            assertEquals(HttpStatus.OK.getReasonPhrase(), response.getReasonPhrase());
            assertEquals("{\"message\":\"Hello, World!\"}", new String(response.getBody()));
        }
    }

    @Test
    void handleApiRequest_dataNotExists() {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/api/test");

        try (MockedStatic<ResourceManager> mockedResourceManager = mockStatic(ResourceManager.class);
            MockedStatic<HttpResponseUtil> mockedUtil = mockStatic(HttpResponseUtil.class)) {

            mockedResourceManager.when(() -> ResourceManager.getData(anyString(), anyString()))
                                .thenReturn(Mono.just(List.of()));

            // Adjusted to match the method signature
            mockedUtil.when(() -> HttpResponseUtil.send404(any(HttpResponse.class), anyString(), anyString()))
                    .thenReturn(Mono.empty());

            Mono<Void> result = getRequestHandler.handleApiRequest("/api/test", response);

            StepVerifier.create(result)
                        .verifyComplete();

            // Adjusted to match the method signature
            mockedUtil.verify(() -> HttpResponseUtil.send404(response, "API data not found for path: /api/test", "text/html"));
        }
    }
    @Test
    void handleFileRequest_fileExists() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/index.html");

        Path filePath = tempDir.resolve("index.html");
        Files.createFile(filePath);

        doReturn(Mono.empty()).when(getRequestHandler).sendResponse(any(File.class), eq(response));

        Mono<Void> result = getRequestHandler.handle(request, response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(getRequestHandler).sendResponse(any(File.class), eq(response));
    }

    @Test
    void handleFileRequest_fileNotExists() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/nonexistent.html");

        doReturn(Mono.empty()).when(getRequestHandler).handleApiRequest(anyString(), eq(response));

        Mono<Void> result = getRequestHandler.handle(request, response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(getRequestHandler).handleApiRequest(anyString(), eq(response));
    }

    @Test
    void handlePhpFileRequest_phpFileExists() throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getPath()).thenReturn("/index.php");

        Path filePath = tempDir.resolve("index.php");
        Files.createFile(filePath);

        doReturn(Mono.empty()).when(getRequestHandler).executePhp(any(File.class), eq(response));

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

        Path dirPath = tempDir.resolve("dir");
        Files.createDirectories(dirPath);

        doReturn(Flux.empty()).when(getRequestHandler).sendMultipleResponses(any(File.class), eq(response));

        Mono<Void> result = getRequestHandler.handle(request, response);

        StepVerifier.create(result)
                .verifyComplete();

        verify(getRequestHandler).sendMultipleResponses(any(File.class), eq(response));
    }
}
