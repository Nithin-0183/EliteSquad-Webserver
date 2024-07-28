package com.ues;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainServerTest {

    private Thread httpServerThread;
    private Thread secureServerThread;

    @BeforeEach
    void setUp() {
        // Start the servers in separate threads
        httpServerThread = new Thread(() -> {
            MainServer.main(new String[]{});
        });

        secureServerThread = new Thread(() -> {
            MainServer.main(new String[]{});
        });

        httpServerThread.start();
        secureServerThread.start();
    }

    @Test
    void testServerThreadsStarted() throws InterruptedException {
        // Allow some time for servers to start
        httpServerThread.join(5000);
        secureServerThread.join(5000);

        // Check if the threads are still alive
        assertTrue(httpServerThread.isAlive(), "HTTP Server thread should be alive");
        assertTrue(secureServerThread.isAlive(), "Secure Server thread should be alive");
    }

    @AfterEach
    void tearDown() {
        // Interrupt the threads to stop the servers (if they support interruption)
        httpServerThread.interrupt();
        secureServerThread.interrupt();
    }
}
