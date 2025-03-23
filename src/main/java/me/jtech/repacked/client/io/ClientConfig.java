package me.jtech.repacked.client.io;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.SectionHeader;
import me.jtech.repacked.client.RepackedClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
@Modmenu(modId = RepackedClient.MOD_ID)
@Config(name = "repacked-client", wrapperName = "RepackedClientConfig")
public class ClientConfig {
    @SectionHeader("general")
    public boolean enabled = true;
    @SectionHeader("reload")
    public boolean overridePackReload = true;
    public boolean reRenderWorld = true;
    @SectionHeader("pack-profile")
    public boolean disablePacks = true;
}
