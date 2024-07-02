package com.ues.core;

import com.ues.database.ResourceManager;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import com.ues.http.HttpStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RequestHandlerTest {

    private MockedStatic<ResourceManager> mockedStaticResourceManager;

    @BeforeEach
    void setUp() {
        mockedStaticResourceManager = Mockito.mockStatic(ResourceManager.class);
    }

    @AfterEach
    void tearDown() {
        mockedStaticResourceManager.close();
    }

    @Test
    void handleGet_shouldReturnIndexHtml() {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        when(request.getMethod()).thenReturn(HttpRequest.Method.GET);
        when(request.getUri()).thenReturn("/");

        StepVerifier.create(RequestHandler.handleRequest(request, response))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
        assertTrue(response.getBody().contains("<html>")); // Index HTML content should be present
    }

    @Test
    void handlePost_shouldCreateMessage() {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        Map<String, String> formData = new HashMap<>();
        formData.put("user", "testUser");
        formData.put("message", "testMessage");

        when(request.getMethod()).thenReturn(HttpRequest.Method.POST);
        when(request.getUri()).thenReturn("/messages");
        when(request.getBody()).thenReturn("user=testUser&message=testMessage");
        mockedStaticResourceManager.when(() -> ResourceManager.createMessage(any())).thenReturn(Mono.just(true));

        StepVerifier.create(RequestHandler.handleRequest(request, response))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.CREATED.getCode(), response.getStatusCode());
        assertEquals("Message created successfully.", response.getBody());
    }

    @Test
    void handlePut_shouldUpdateMessage() {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();
        Map<String, String> formData = new HashMap<>();
        formData.put("message", "updatedMessage");

        when(request.getMethod()).thenReturn(HttpRequest.Method.PUT);
        when(request.getUri()).thenReturn("/messages/1");
        when(request.getBody()).thenReturn("message=updatedMessage");
        mockedStaticResourceManager.when(() -> ResourceManager.updateMessage(eq("1"), any())).thenReturn(Mono.just(true));

        StepVerifier.create(RequestHandler.handleRequest(request, response))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
        assertEquals("Message updated successfully.", response.getBody());
    }

    @Test
    void handleDelete_shouldDeleteMessage() {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();

        when(request.getMethod()).thenReturn(HttpRequest.Method.DELETE);
        when(request.getUri()).thenReturn("/messages/1");
        mockedStaticResourceManager.when(() -> ResourceManager.deleteMessage("1")).thenReturn(Mono.just(true));

        StepVerifier.create(RequestHandler.handleRequest(request, response))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.NO_CONTENT.getCode(), response.getStatusCode());
        assertEquals("", response.getBody());
    }

    @Test
    void handleGetMessages_shouldReturnMessages() {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpResponse response = new HttpResponse();

        when(request.getMethod()).thenReturn(HttpRequest.Method.GET);
        when(request.getUri()).thenReturn("/messages");
        mockedStaticResourceManager.when(ResourceManager::getMessages).thenReturn(Mono.just(List.of(
                Map.of("id", "1", "user", "testUser", "message", "testMessage", "timestamp", "2024-06-22T12:00:00")
        )));

        StepVerifier.create(RequestHandler.handleRequest(request, response))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
        assertTrue(response.getBody().contains("\"id\":1"));
        assertTrue(response.getBody().contains("\"user\":\"testUser\""));
        assertTrue(response.getBody().contains("\"message\":\"testMessage\""));
        assertTrue(response.getBody().contains("\"timestamp\":\"2024-06-22T12:00:00\""));
    }
}
