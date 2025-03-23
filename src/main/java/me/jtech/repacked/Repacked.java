package me.jtech.repacked;

import me.jtech.repacked.io.RepackedServerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Repacked implements ModInitializer {
    public static final String MOD_ID = "repacked";
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static RepackedServerConfig CONFIG;

    public static final String version = "1.0.0";

    @Override
    public void onInitialize() {
        // If executed on the server, load the config
        if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER)) {
            CONFIG = RepackedServerConfig.createAndLoad();
        }
    }
}
