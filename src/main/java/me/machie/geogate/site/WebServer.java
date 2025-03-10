package me.machie.geogate.site;

import me.machie.geogate.Geogate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class WebServer {
    private static Server server;

    static {
        // Jetty logs a bunch of annoying info when setting up a server
        Configurator.setLevel("org.eclipse.jetty", Level.WARN);
        Configurator.setLevel("org.eclipse.jetty.server", Level.WARN);
        Configurator.setLevel("org.eclipse.jetty.util", Level.WARN);
    }

    /**
     * Initializes the IP verification website hosted by the mod and the websocket that allows communication between
     * the two. If a keystore is provided the website will be upgraded from HTTP to HTTPS and redirect all requests
     * to the secure site.
     *
     * @return <code>false</code> if an exception occurred while initializing the server/socket.
     */
    public static boolean start() {
        createServer();

        try {
            server.start();
            return true;
        } catch (Exception e) {
            stop();
            Geogate.LOG.error("Encountered an error while starting the web server:", e);
            return false;
        }
    }

    /**
     * Stops the web server if it's running.
     */
    public static void stop() {
        if (server == null || !server.isRunning()) return;

        try {
            server.stop();
        } catch (Exception e) {
            Geogate.LOG.error("Failed to shut down web server:", e);
        }
    }


    // TODO make the actual webserver lole
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
