package me.ricky.geogate;

import me.ricky.geogate.config.ConfigManager;
import me.ricky.geogate.config.GeogateConfig;
import me.ricky.geogate.site.WebServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geogate implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("geogate");
    public static final GeogateConfig CONFIG = ConfigManager.load();

    public static boolean loadedSafely = false;

    @Override
    public void onInitialize() {




        if (!WebServer.start()) {
            System.exit(1);
            return;
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {

        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            WebServer.stop();
            ConfigManager.save(CONFIG);
        });

        ServerLoginConnectionEvents.INIT.register((handler, server) -> {

        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

        });


    }
}
