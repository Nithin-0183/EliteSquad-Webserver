package com.ues;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.nio.charset.StandardCharsets;

import com.ues.core.RequestHandler;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;

public class NioHttpServer {

    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int DEFAULT_HTTPS_PORT = 8443;

    public static void main(String[] args) {
        int httpPort = DEFAULT_HTTP_PORT;
        int httpsPort = DEFAULT_HTTPS_PORT;

        try {
            Selector selector = Selector.open();

            // HTTP Server
            ServerSocketChannel httpServerSocketChannel = ServerSocketChannel.open();
            httpServerSocketChannel.bind(new InetSocketAddress(httpPort));
            httpServerSocketChannel.configureBlocking(false);
            httpServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO HTTP Server is listening on port " + httpPort);

            // HTTPS Server
            ServerSocketChannel httpsServerSocketChannel = ServerSocketChannel.open();
            httpsServerSocketChannel.bind(new InetSocketAddress(httpsPort));
            httpsServerSocketChannel.configureBlocking(false);
            httpsServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO HTTPS Server is listening on port " + httpsPort);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }

                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ);
        System.out.println("Accepted connection from " + socketChannel);
    }

    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuilder requestBuilder = new StringBuilder();

        int bytesRead;
        while ((bytesRead = socketChannel.read(buffer)) > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            requestBuilder.append(new String(bytes, StandardCharsets.UTF_8));
            buffer.clear();
        }

        if (bytesRead == -1) {
            socketChannel.close();
        } else {
            String request = requestBuilder.toString();
            System.out.println("Request: " + request);

            HttpRequest httpRequest = new HttpRequest(request);
            HttpResponse response = new HttpResponse();

            RequestHandler handler = new RequestHandler();
            handler.handleRequest(httpRequest, response);

            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getResponseBytes());
            while (responseBuffer.hasRemaining()) {
                socketChannel.write(responseBuffer);
            }
            socketChannel.close();
        }
    }

}
