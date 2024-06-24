package com.ues.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpRequestTest {

    @Test
    void httpRequest_shouldParseRequestCorrectly() {
        String rawRequest = "GET /test HTTP/1.1\r\nHost: localhost\r\n\r\n";

        HttpRequest request = new HttpRequest(rawRequest);

        assertEquals(HttpRequest.Method.GET, request.getMethod());
        assertEquals("/test", request.getUri());
        assertEquals("HTTP/1.1", request.getVersion());
        assertTrue(request.getHeaders().containsKey("Host"));
    }

    @Test
    void httpRequest_shouldParseBodyCorrectly() {
        String rawRequest = "POST /submit HTTP/1.1\r\nHost: localhost\r\nContent-Length: 27\r\n\r\nuser=testUser&message=testMessage";

        HttpRequest request = new HttpRequest(rawRequest);

        assertEquals(HttpRequest.Method.POST, request.getMethod());
        assertEquals("/submit", request.getUri());
        assertEquals("HTTP/1.1", request.getVersion());
        assertEquals("user=testUser&message=testMessage", request.getBody());
    }
}
