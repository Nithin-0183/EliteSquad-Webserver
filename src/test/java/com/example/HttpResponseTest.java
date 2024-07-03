package com.example;

import static org.mockito.Mockito.*;

import java.io.PrintWriter;

import com.ues.http.HttpResponse;
import org.junit.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public class HttpResponseTest {

    private PrintWriter mockPrintWriter;
    private HttpResponse httpResponse;

    @Before
    public void setUp() {
        mockPrintWriter = mock(PrintWriter.class);
        httpResponse = new HttpResponse(mockPrintWriter);
    }

    @Test
    public void testSendResponse200() {
        String message = "OK response";
        int statusCode = 200;

        httpResponse.sendResponse(statusCode, message);

        InOrder inOrder = inOrder(mockPrintWriter);
        inOrder.verify(mockPrintWriter).println("HTTP/1.1 200 OK");
        inOrder.verify(mockPrintWriter).println("Content-Type: text/plain");
        inOrder.verify(mockPrintWriter).println("Content-Length: " + message.length());
        inOrder.verify(mockPrintWriter).println();
        inOrder.verify(mockPrintWriter).println(message);
    }

    @Test
    public void testSendResponse400() {
        String message = "Bad Request response";
        int statusCode = 400;

        httpResponse.sendResponse(statusCode, message);

        InOrder inOrder = inOrder(mockPrintWriter);
        inOrder.verify(mockPrintWriter).println("HTTP/1.1 400 Bad Request");
        inOrder.verify(mockPrintWriter).println("Content-Type: text/plain");
        inOrder.verify(mockPrintWriter).println("Content-Length: " + message.length());
        inOrder.verify(mockPrintWriter).println();
        inOrder.verify(mockPrintWriter).println(message);
    }

    @Test
    public void testSendResponse404() {
        String message = "Not Found response";
        int statusCode = 404;

        httpResponse.sendResponse(statusCode, message);

        InOrder inOrder = inOrder(mockPrintWriter);
        inOrder.verify(mockPrintWriter).println("HTTP/1.1 404 Not Found");
        inOrder.verify(mockPrintWriter).println("Content-Type: text/plain");
        inOrder.verify(mockPrintWriter).println("Content-Length: " + message.length());
        inOrder.verify(mockPrintWriter).println();
        inOrder.verify(mockPrintWriter).println(message);
    }

    @Test
    public void testSendResponse500() {
        String message = "Internal Server Error response";
        int statusCode = 500;

        httpResponse.sendResponse(statusCode, message);

        InOrder inOrder = inOrder(mockPrintWriter);
        inOrder.verify(mockPrintWriter).println("HTTP/1.1 500 Internal Server Error");
        inOrder.verify(mockPrintWriter).println("Content-Type: text/plain");
        inOrder.verify(mockPrintWriter).println("Content-Length: " + message.length());
        inOrder.verify(mockPrintWriter).println();
        inOrder.verify(mockPrintWriter).println(message);
    }

    @Test
    public void testSendResponseUnknownStatus() {
        String message = "Unknown response";
        int statusCode = 999;

        httpResponse.sendResponse(statusCode, message);

        InOrder inOrder = inOrder(mockPrintWriter);
        inOrder.verify(mockPrintWriter).println("HTTP/1.1 999 Unknown");
        inOrder.verify(mockPrintWriter).println("Content-Type: text/plain");
        inOrder.verify(mockPrintWriter).println("Content-Length: " + message.length());
        inOrder.verify(mockPrintWriter).println();
        inOrder.verify(mockPrintWriter).println(message);
    }
}
