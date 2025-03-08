package me.ricky.geogate.site;

import me.ricky.geogate.Geogate;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.*;

public class WebServer {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Server server;

    /**
     * Initializes the IP verification website hosted by the mod and the websocket that allows communication between
     * the two. If a keystore is provided the website will be upgraded from HTTP to HTTPS and redirect all requests
     * to the secure site.
     *
     * @return <code>false</code> if an exception occurred while initializing the server/socket.
     * @author MachieCodes
     */
    public static boolean start() {
        createServer();

        Runtime.getRuntime().addShutdownHook(new Thread(WebServer::stop));

        Future<Exception> serverFuture = executor.submit(() -> {
            try {
                server.start();
                return null;
            } catch (Exception e) {
                return e;
            }
        });

        try {
            Exception e = serverFuture.get(5, TimeUnit.SECONDS);
            if (e != null) throw e;
            Geogate.LOG.info("Web server started successfully!");

            return true;
        } catch (Exception e) {
            Geogate.LOG.error("Exception while starting web server!", e);
            stop();

            return false;
        }
    }

    public static void stop() {
        Geogate.LOG.info("Testing");

        if (server == null) {
            Geogate.LOG.warn("Attempted to stop null/unstarted web server!");
            return;
        }

        try {
            Geogate.LOG.info("Stopping web server");
            server.stop();
            Geogate.LOG.info("Web server stopped, joining");
            server.join();
            Geogate.LOG.info("Web server joined");
        } catch (Exception e) {
            Geogate.LOG.error("Failed to stop web server!", e);
        }

        try {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Geogate.LOG.error("Failed to stop executor!", e);
        }
    }

    private static void createServer() {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("webserver-pool");
        threadPool.setMaxThreads(64);
        threadPool.setMinThreads(1);

        server = new Server(threadPool);

        Connector connector = new ServerConnector(server);
        server.addConnector(connector);

        server.setHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response, Callback callback) {
                callback.succeeded();
                return true;
            }
        });
    }
}
