package me.jtech.repacked.client;

import me.jtech.repacked.PackUtils;
import me.jtech.repacked.Repacked;
import me.jtech.repacked.client.io.ProfileIO;
import me.jtech.repacked.client.io.RepackedClientConfig;
import me.jtech.repacked.client.profiles.Effector;
import me.jtech.repacked.client.screen.ProfileEffectorSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourcePackProfile;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
                client.worldRenderer.reload();
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

    public static String getSingleplayerWorldName() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isIntegratedServerRunning() && client.getServer() != null) {
            return client.getServer().getSaveProperties().getLevelName();
        }
        return "";
    }

}
