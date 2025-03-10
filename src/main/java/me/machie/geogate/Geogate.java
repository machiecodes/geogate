package me.machie.geogate;

import me.machie.geogate.config.ConfigManager;
import me.machie.geogate.config.GeogateConfig;
import me.machie.geogate.site.WebServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geogate implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("geogate");
    public static GeogateConfig CONFIG;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        ServerLoginConnectionEvents.INIT.register(this::onAttemptedLogin);
        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
    }

    private void onServerStarting(MinecraftServer server) {
        CONFIG = ConfigManager.load();

        if (CONFIG == null) {
            server.stop(false);
            return;
            // TODO prevent saving null config
        }

        WebServer.start();
    }

    private void onServerStopping(MinecraftServer server) {
        WebServer.stop();
        ConfigManager.save(CONFIG);
    }

    private void onAttemptedLogin(ServerLoginNetworkHandler handler, MinecraftServer server) {
        // TODO vpn check
    }

    private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        // TODO send message and freeze in place
    }
}
