package ru.otus.october.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private int port;
    private Dispatcher dispatcher;

    private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);


    public HttpServer(int port) {
        this.port = port;
        this.dispatcher = new Dispatcher();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("Сервер запущен на порту: {}", port);
            while (true) {
                Socket socket = serverSocket.accept();

                ExecutorService executorService = Executors.newFixedThreadPool(10);
                executorService.execute(() -> {
                    try {
                        LOGGER.info("Thread: {} ", Thread.currentThread().getName());
                        fromRequestToDispatcher(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                executorService.shutdown();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fromRequestToDispatcher(Socket socket) throws IOException {
        byte[] buffer = new byte[8192];
        int n = socket.getInputStream().read(buffer);
        if (n == -1) {
            return;
        }
        String rawRequest = new String(buffer, 0, n);
        HttpRequest request = new HttpRequest(rawRequest);
        request.info(false);
        dispatcher.execute(request, socket.getOutputStream());
    }
}
