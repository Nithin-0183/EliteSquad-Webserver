package com.ues;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.ues.core.RequestHandler;
import com.ues.http.HttpRequest;
import com.ues.http.HttpResponse;
import reactor.core.publisher.Mono;

public class NioHttpServer implements Runnable {
    private static Map<String, String> domainToRootMap = new HashMap<>();

    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;

    @Override
    public void run() {
        try {
            loadConfiguration();

            AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            System.out.println("Server is listening on port " + PORT);

            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                    serverChannel.accept(null, this);
                    handleClient(clientChannel, threadPool);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    exc.printStackTrace();
                }
            });

            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfiguration() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = NioHttpServer.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                domainToRootMap.put(properties.getProperty("site1.domain"), properties.getProperty("site1.root"));
                domainToRootMap.put(properties.getProperty("site2.domain"), properties.getProperty("site2.root"));
                System.out.println(domainToRootMap.toString());
            } else {
                throw new FileNotFoundException("Property file 'application.properties' not found in the classpath");
            }
        }
    }

    private static void handleClient(AsynchronousSocketChannel clientChannel, ThreadPoolExecutor threadPool) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        clientChannel.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                if (result == -1) {
                    closeChannel(clientChannel);
                    return;
                }

                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String request = new String(bytes);
                System.out.println("Request: " + request);

                HttpRequest httpRequest = new HttpRequest(request);
                HttpResponse response = new HttpResponse();

                RequestHandler requestHandler = new RequestHandler(domainToRootMap);
                Mono<Void> resultMono = requestHandler.handle(httpRequest, response);

                resultMono.doOnTerminate(() -> {
                    ByteBuffer responseBuffer = ByteBuffer.wrap(response.getResponseBytes());
                    clientChannel.write(responseBuffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer result, Void attachment) {
                            closeChannel(clientChannel);
                        }

                        @Override
                        public void failed(Throwable exc, Void attachment) {
                            exc.printStackTrace();
                            closeChannel(clientChannel);
                        }
                    });
                }).subscribe();
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
                closeChannel(clientChannel);
            }
        });
    }

    private static void closeChannel(AsynchronousSocketChannel channel) {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}