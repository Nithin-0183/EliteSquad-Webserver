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

import com.ues.core.RequestHandler;
import com.ues.database.DatabaseConfig;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;

import reactor.core.publisher.Mono;

public class NioHttpServer {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        // Initialize the database
        DatabaseConfig.initializeDatabase();

        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO Server is listening on port " + PORT);

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
        int bytesRead = socketChannel.read(buffer);

        if (bytesRead == -1) {
            socketChannel.close();
        } else {
            buffer.flip();
            String request = new String(buffer.array(), 0, bytesRead);
            System.out.println("Request: " + request);

            HttpRequest httpRequest = new HttpRequest(request);
            HttpResponse response = new HttpResponse();

            RequestHandler handler = new RequestHandler();
            Mono<Void> result = handler.handleRequest(httpRequest, response);

            result.doOnTerminate(() -> {
                try {
                    byte[] responseBytes = response.getResponseBytes();
                    ByteBuffer responseBuffer = ByteBuffer.allocate(responseBytes.length);
                    responseBuffer.put(responseBytes);
                    responseBuffer.flip();
                    while (responseBuffer.hasRemaining()) {
                        socketChannel.write(responseBuffer);
                    }
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).subscribe();
        }
    }
}
