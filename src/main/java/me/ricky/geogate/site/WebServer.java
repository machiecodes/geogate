package me.ricky.geogate.site;

import me.ricky.geogate.Geogate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.*;

public class WebServer {
    private static Server server;

    static {
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
     * @author MachieCodes
     */
    public static boolean start() {
        createServer();

        try {
            server.start();

            Geogate.LOG.info("Web server started successfully!");
            return true;
        } catch (Exception e) {
            stop();

            Geogate.LOG.error("Exception while starting web server!", e);
            return false;
        }
    }

    public static void stop() {
        if (server == null || !server.isRunning()) {
            Geogate.LOG.warn("Attempted to stop null/unstarted web server!");
            return;
        }

        try {
            server.stop();

            Geogate.LOG.info("Stopped the web server successfully!");
        } catch (Exception e) {
            Geogate.LOG.error("Failed to stop web server!", e);
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
