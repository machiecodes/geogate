package me.ricky.geogate;

import me.ricky.geogate.site.WebServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geogate implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("geogate");


    @Override
    public void onInitialize() {
        if (!WebServer.start()) {
            LOG.error("Failed to initialize the webserver, geogate will not work.");
        }

        ServerLoginConnectionEvents.INIT.register((handler, server) -> {
            // Check for a VPN with ipinfo when a player connects
            // Maybe add the IP to a list of IPs confirmed in the city?
            // Don't know if that's feasible yet but we will find out
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Once they're actually logged in freeze them in place until they verify
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Need to wait until the server is actually started to be able to grab the domain
        });
    }
}
