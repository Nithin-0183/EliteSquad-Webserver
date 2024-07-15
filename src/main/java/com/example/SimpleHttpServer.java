package main.java.com.example;

import com.ues.database.DatabaseConfig;
import com.ues.database.ResourceManager;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import javax.net.ssl.*;

public class SimpleHttpServer {
    private static final int HTTP_PORT = 8080;
    private static final int HTTPS_PORT = 8443;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static ServerSocket httpServerSocket;
    private static SSLServerSocket httpsServerSocket;
    private static Map<String, String> siteRoots = new HashMap<>();

    public static void main(String[] args) {
        try {
            // Initialize the database
            DatabaseConfig.initializeDatabase();

            // Load site root directories from database
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT domain, root FROM sites");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    siteRoots.put(rs.getString("domain"), rs.getString("root"));
                }
            }

            // Create HTTP server socket
            httpServerSocket = new ServerSocket(HTTP_PORT);
            System.out.println("HTTP Server is listening on port " + HTTP_PORT);

            // Create HTTPS server socket
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("keystore.jks"), "changeit".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "changeit".toCharArray());

            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            httpsServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(HTTPS_PORT);
            System.out.println("HTTPS Server is listening on port " + HTTPS_PORT);

            // Add a shutdown hook to close the server sockets gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (httpServerSocket != null && !httpServerSocket.isClosed()) {
                        httpServerSocket.close();
                    }
                    if (httpsServerSocket != null && !httpsServerSocket.isClosed()) {
                        httpsServerSocket.close();
                    }
                    threadPool.shutdown();
                    System.out.println("Server sockets closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            // Start accepting connections on both HTTP and HTTPS sockets
            new Thread(() -> acceptConnections(httpServerSocket)).start();
            acceptConnections(httpsServerSocket);

        } catch (SSLHandshakeException e) {
            System.err.println("SSL Handshake failed: " + e.getMessage());
            e.printStackTrace();
        } catch (SocketException e) {
            System.err.println("Socket exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void acceptConnections(ServerSocket serverSocket) {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Accepted connection from " + socket.getInetAddress());
                threadPool.execute(new ClientHandler(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getSiteRoot(String domain) {
        return siteRoots.getOrDefault(domain, "./www");
    }
}

class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

            String line = reader.readLine();
            System.out.println("Received: " + line);  // Logging received request

            if (line != null && !line.isEmpty()) {
                String[] requestLine = line.split(" ");
                String method = requestLine[0];
                String path = requestLine[1];
                String hostHeader = null;

                while (!(line = reader.readLine()).isEmpty()) {
                    if (line.startsWith("Host:")) {
                        hostHeader = line.split(" ")[1];
                        break;
                    }
                }

                String rootDir = SimpleHttpServer.getSiteRoot(hostHeader);

                if (path.endsWith("/")) {
                    // Check for index.html or index.php in the directory
                    File indexHtml = new File(rootDir + path + "index.html");
                    File indexPhp = new File(rootDir + path + "index.php");
                    if (indexHtml.exists()) {
                        path += "index.html";
                    } else if (indexPhp.exists()) {
                        path += "index.php";
                    }
                }

                if (path.endsWith(".php")) {
                    handlePhpRequest(rootDir + path, writer);
                } else {
                    switch (method) {
                        case "GET":
                            handleGet(rootDir + path, writer);
                            break;
                        case "POST":
                            handlePost(path, reader, writer);
                            break;
                        case "PUT":
                            handlePut(path, reader, writer);
                            break;
                        case "DELETE":
                            handleDelete(path, writer);
                            break;
                        default:
                            sendResponse(writer, 405, "Method Not Allowed", "The method " + method + " is not supported.");
                    }
                }
            }

            writer.flush();
        } catch (SSLHandshakeException e) {
            System.err.println("SSL Handshake failed: " + e.getMessage());
            e.printStackTrace();
        } catch (SocketException e) {
            System.err.println("Socket exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePhpRequest(String absolutePath, BufferedWriter writer) {
        try {
            System.out.println("Executing PHP file: " + absolutePath);  // Debugging output

            ProcessBuilder processBuilder = new ProcessBuilder("php-cgi", "-f", absolutePath);
            Process process = processBuilder.start();

            BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = processReader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            sendResponse(writer, 200, "OK", responseBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
            try {
                sendResponse(writer, 500, "Internal Server Error", "Failed to execute PHP file.");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleGet(String absolutePath, BufferedWriter writer) {
        try {
            File file = new File(absolutePath);
            if (file.exists() && !file.isDirectory()) {
                sendFile(writer, file);
            } else {
                sendResponse(writer, 404, "Not Found", "File not found: " + absolutePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendResponseSilently(writer, 500, "Internal Server Error", "Failed to handle GET request.");
        }
    }

    private void sendFile(BufferedWriter writer, File file) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = fileReader.readLine()) != null) {
                responseBody.append(line).append("\n");
            }
            sendResponse(writer, 200, "OK", responseBody.toString());
        }
    }

    private void handlePost(String path, BufferedReader reader, BufferedWriter writer) {
        try {
            // Handle POST request and insert data into the database
            Map<String, String> postData = new HashMap<>();
            String bodyLine;
            while ((bodyLine = reader.readLine()) != null && !bodyLine.isEmpty()) {
                String[] keyValue = bodyLine.split("=");
                if (keyValue.length == 2) {
                    postData.put(keyValue[0], keyValue[1]);
                }
            }
            ResourceManager.createMessage(postData).subscribe(success -> {
                if (success) {
                    sendResponseSilently(writer, 200, "OK", "Message created successfully.");
                } else {
                    sendResponseSilently(writer, 500, "Internal Server Error", "Failed to create message.");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            sendResponseSilently(writer, 500, "Internal Server Error", "Failed to handle POST request.");
        }
    }

    private void handlePut(String path, BufferedReader reader, BufferedWriter writer) {
        try {
            // Extract ID from path
            String id = path.substring(path.lastIndexOf("/") + 1);

            // Handle PUT request and update data in the database
            Map<String, String> putData = new HashMap<>();
            String bodyLine;
            while ((bodyLine = reader.readLine()) != null && !bodyLine.isEmpty()) {
                String[] keyValue = bodyLine.split("=");
                if (keyValue.length == 2) {
                    putData.put(keyValue[0], keyValue[1]);
                }
            }
            ResourceManager.updateMessage(id, putData).subscribe(success -> {
                if (success) {
                    sendResponseSilently(writer, 200, "OK", "Message updated successfully.");
                } else {
                    sendResponseSilently(writer, 500, "Internal Server Error", "Failed to update message.");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            sendResponseSilently(writer, 500, "Internal Server Error", "Failed to handle PUT request.");
        }
    }

    private void handleDelete(String path, BufferedWriter writer) {
        // Extract ID from path
        String id = path.substring(path.lastIndexOf("/") + 1);

        // Handle DELETE request and delete data from the database
        ResourceManager.deleteMessage(id).subscribe(success -> {
            if (success) {
                sendResponseSilently(writer, 200, "OK", "Message deleted successfully.");
            } else {
                sendResponseSilently(writer, 500, "Internal Server Error", "Failed to delete message.");
            }
        });
    }

    private void sendResponse(BufferedWriter writer, int statusCode, String statusText, String responseBody) throws IOException {
        writer.write("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("Content-Length: " + responseBody.length() + "\r\n");
        writer.write("\r\n");
        writer.write(responseBody);
    }

    private void sendResponseSilently(BufferedWriter writer, int statusCode, String statusText, String responseBody) {
        try {
            sendResponse(writer, statusCode, statusText, responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
