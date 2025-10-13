package me.jtech.repacked.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jtech.repacked.PackUtils;
import me.jtech.repacked.Repacked;
import me.jtech.repacked.client.io.ClientConfig;
import me.jtech.repacked.client.io.ProfileIO;
import me.jtech.repacked.client.io.RepackedClientConfig;
import me.jtech.repacked.client.profiles.Effector;
import me.jtech.repacked.client.screen.ProfileEffectorSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RepackedClient implements ClientModInitializer {
    public static final String MOD_ID = Repacked.MOD_ID;
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final RepackedClientConfig CONFIG = RepackedClientConfig.createAndLoad();

    private static final String version = Repacked.version;

    public static boolean reloaded = false;

    private static KeyBinding reloadPackKeybind;
    private static KeyBinding openMenuKeybind;

    private static List<ResourcePackProfile> pushedPacks = new ArrayList<>();
    private static List<ResourcePackProfile> oldPacks = new ArrayList<>();
    private static boolean joinedProfiledWorld = false;
    public static boolean loading;

    public static void reloadWorld() {
        if (RepackedClient.CONFIG.reRenderWorld()) {
            RepackedClient.reloaded = true;
        } else {
            RepackedClient.loading = false;
        }
    }

    @Override
    public void onInitializeClient() {
        reloadPackKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.repacked.reload", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                "category.repacked.general" // The translation key of the keybinding's category.
        ));
        openMenuKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.repacked.open_menu", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_U, // The keycode of the key
                "category.repacked.general" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (reloadPackKeybind.wasPressed() && CONFIG.overridePackReload() && CONFIG.enabled()) {
                CompletableFuture.runAsync(PackUtils::reloadPack);
            }
            if (openMenuKeybind.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ProfileEffectorSelectionScreen());
            }
            if (reloaded) {
                reloaded = false;
                loading = false;
                sendBlockUpdateToLoadedChunks();
            }
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(MOD_ID, "before_chat"), (context, tickCounter) -> {
            if (loading && CONFIG.showReloadThrobber()) {
                int x = 10, y = 10; // Position on screen
                int width = 24, height = 24; // Size of the icon
                float angle = ((float) Util.getMeasuringTimeMs() / 8) % 360;

                context.getMatrixStack().pushMatrix();
                context.getMatrixStack().translate(x + (float) width /2, y + (float) height /2); // Move to the center of the icon
                context.getMatrixStack().rotate(angle); // Rotate around the center
                context.getMatrixStack().translate((float) -width /2, (float) -height /2);

                Identifier texture = Identifier.of(MOD_ID, "textures/ui/reload.png");
                context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0, 0, width, height, width, height);

                context.getMatrixStack().popMatrix();
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Map<Effector, List<ResourcePackProfile>> profiles = ProfileIO.loadProfiles();
            if (profiles != null && CONFIG.enabled()) {
                profiles.forEach((effector, packs) -> {
                    String name = effector.getName();
                    if (effector.isServer()) {
                        if (handler.getServerInfo().name.equals(name)) {
                            joinedProfiledWorld = true;
                            if (CONFIG.disablePacks()) {
                                oldPacks = client.getResourcePackManager().getEnabledProfiles().stream().toList();
                                oldPacks.forEach(pack -> {
                                    if (!packs.contains(pack)) {
                                        client.getResourcePackManager().disable(pack.getId());
                                    }
                                });
                            }
                            packs.forEach(pack -> {
                                if (!client.getResourcePackManager().getEnabledProfiles().contains(pack)) {
                                    client.getResourcePackManager().enable(pack.getId());
                                    pushedPacks.add(pack);
                                }
                            });
                        }
                    } else {
                        if (getSingleplayerWorldName().equalsIgnoreCase(name)) {
                            joinedProfiledWorld = true;
                            if (CONFIG.disablePacks()) {
                                oldPacks = client.getResourcePackManager().getEnabledProfiles().stream().toList();
                                oldPacks.forEach(pack -> {
                                    if (!packs.contains(pack)) {
                                        client.getResourcePackManager().disable(pack.getId());
                                    }
                                });
                            }
                            packs.forEach(pack -> {
                                if (!client.getResourcePackManager().getEnabledProfiles().contains(pack)) {
                                    client.getResourcePackManager().enable(pack.getId());
                                    pushedPacks.add(pack);
                                }
                            });
                        }
                    }
                });
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (joinedProfiledWorld && CONFIG.enabled()) {
                joinedProfiledWorld = false;
                for (ResourcePackProfile pack : pushedPacks) {
                    client.getResourcePackManager().disable(pack.getId());
                }
                pushedPacks.clear();
                if (CONFIG.disablePacks()) {
                    oldPacks.forEach(pack -> {
                        client.getResourcePackManager().enable(pack.getId());
                    });
                    oldPacks.clear();
                }
                PackUtils.reloadPack();
            }
        });

    }

    public static void sendBlockUpdateToLoadedChunks() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.clientWorld == null) return;

        int renderDistance = client.options.getViewDistance().getValue() * 2;
        ChunkPos chunkPos = client.player.getChunkPos();

        for (int i = 0; i < renderDistance; i++) {
            for (int j = 0; j < renderDistance; j++) {
                int chunkX = chunkPos.x + i - renderDistance / 2;
                int chunkZ = chunkPos.z + j - renderDistance / 2;
                int ySections = ChunkSectionPos.getSectionCoord(client.player.clientWorld.getHeight());
                for (int chunkY = 0; chunkY < ySections; chunkY++) {
                    client.worldRenderer.scheduleChunkRender(chunkX, chunkY, chunkZ);
                }
            }
        }
    }

    public static String getSingleplayerWorldName() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isIntegratedServerRunning() && client.getServer() != null) {
            return client.getServer().getSaveProperties().getLevelName();
        }
        return "";
    }
}
