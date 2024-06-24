package com.ues.http;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets; // 추가된 import
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpResponseTest {

    @Test
    public void testSetStatus() {
        HttpResponse response = new HttpResponse();
        response.setStatus(200);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testSetBody() {
        HttpResponse response = new HttpResponse();
        response.setBody("Hello, World!");
        assertEquals("Hello, World!", response.getBody());
    }

    @Test
    public void testAddHeader() {
        HttpResponse response = new HttpResponse();
        response.addHeader("Content-Type", "text/plain");
        Map<String, String> headers = response.getHeaders();
        assertEquals(1, headers.size());
        assertEquals("text/plain", headers.get("Content-Type"));
    }

    @Test
    public void testGetResponseBytes() {
        HttpResponse response = new HttpResponse();
        response.setStatus(200);
        response.setBody("Hello, World!");
        response.addHeader("Content-Type", "text/plain");

        String expectedResponse = "HTTP/1.1 200 OK\r\n" +
                                  "Content-Type: text/plain\r\n" +
                                  "Content-Length: 13\r\n" +
                                  "\r\n" +
                                  "Hello, World!";
        byte[] expectedBytes = expectedResponse.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expectedBytes, response.getResponseBytes());
    }

    @Test
    public void testGetResponseBytesNoBody() {
        HttpResponse response = new HttpResponse();
        response.setStatus(204);
        response.addHeader("Content-Type", "text/plain");

        String expectedResponse = "HTTP/1.1 204 No Content\r\n" +
                                  "Content-Type: text/plain\r\n" +
                                  "Content-Length: 0\r\n" +
                                  "\r\n";
        byte[] expectedBytes = expectedResponse.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expectedBytes, response.getResponseBytes());
    }
}
