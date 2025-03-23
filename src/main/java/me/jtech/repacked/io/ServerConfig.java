package me.jtech.repacked.io;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.SectionHeader;
import me.jtech.repacked.Repacked;
import me.jtech.repacked.client.RepackedClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.awt.*;
import java.util.List;

@Environment(EnvType.SERVER)
@Modmenu(modId = Repacked.MOD_ID)
@Config(name = "repacked-server", wrapperName = "RepackedServerConfig")
public class ServerConfig {
    public int anIntOption = 16;
    public boolean aBooleanToggle = false;
}
