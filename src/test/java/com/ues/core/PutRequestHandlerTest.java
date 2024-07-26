package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class PutRequestHandlerTest {

    private PutRequestHandler putRequestHandler;

    @BeforeEach
    void setUp() {
        putRequestHandler = new PutRequestHandler();
    }

    @Test
    void handle_updateDataSuccess() {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getPath()).thenReturn("/table/1");
        when(request.getBody()).thenReturn("key=value");

        try (MockedStatic<ResourceManager> mockedStatic = mockStatic(ResourceManager.class)) {
            mockedStatic.when(() -> ResourceManager.updateData(anyString(), anyMap(), anyString()))
                    .thenReturn(Mono.just(true));

            Mono<Void> result = putRequestHandler.handle(request, response);

            StepVerifier.create(result)
                    .verifyComplete();

            assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
            assertEquals(HttpStatus.OK.getReasonPhrase(), response.getReasonPhrase());
        }
    }

    @Test
    void handle_updateDataFailure() {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getPath()).thenReturn("/table/1");
        when(request.getBody()).thenReturn("key=value");

        try (MockedStatic<ResourceManager> mockedStatic = mockStatic(ResourceManager.class)) {
            mockedStatic.when(() -> ResourceManager.updateData(anyString(), anyMap(), anyString()))
                    .thenReturn(Mono.just(false));

            Mono<Void> result = putRequestHandler.handle(request, response);

            StepVerifier.create(result)
                    .verifyComplete();

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), response.getStatusCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.getReasonPhrase());
            assertEquals("<h1>500 Internal Server Error</h1><p>Failed to update data</p>", new String(response.getBody()));
        }
    }
}
