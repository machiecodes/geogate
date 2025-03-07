package me.ricky.geogate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geogate implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("geogate");

    @Override
    public void onInitialize() {
        ServerLoginConnectionEvents.INIT.register((handler, server) -> {
            handler.disconnect(Text.of("Bruh"));
            // Check for vpn and deny them even connecting if so
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Once they're actually logged in freeze them in place until they verify
        });
    }
}
