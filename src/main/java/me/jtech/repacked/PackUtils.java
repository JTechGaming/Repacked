package me.jtech.repacked;

import me.jtech.repacked.client.RepackedClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.resource.*;
import net.minecraft.util.Util;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PackUtils {
    private static List<ResourcePackProfile> resourcePacks;

    static {
        resourcePacks = MinecraftClient.getInstance().getResourcePackManager().getProfiles().stream().toList();
    }

    public static boolean hasPack() {
        return resourcePacks.size() > 1;
    }

    public static void reloadPack() {
        MinecraftClient client = MinecraftClient.getInstance();
        Repacked.LOGGER.info("Reloading pack");
        client.getResourcePackManager().scanPacks();
        List<ResourcePack> list = client.resourcePackManager.createResourcePacks();
        client.resourceReloadLogger.reload(ResourceReloadLogger.ReloadReason.UNKNOWN, list);
        client.resourceManager.reload(Util.getMainWorkerExecutor(), client, MinecraftClient.COMPLETED_UNIT_FUTURE, list).whenComplete().thenRun(() -> RepackedClient.reloaded = RepackedClient.CONFIG.reRenderWorld());
        client.resourceReloadLogger.finish();
        client.serverResourcePackLoader.onReloadSuccess();
    }

    public static Path getPackFolder(ResourcePack pack) {
        for (ResourcePackProfile resourcePack : resourcePacks) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (resourcePack.getId().equals(pack.getId())) {
                System.out.println(resourcePack.getId());
                return FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve(resourcePack.getDisplayName().getString());
            }
        }
        return null;
    }

    public static List<ResourcePackProfile> refresh() {
        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
        resourcePackManager.scanPacks();
        Path resourcePacksPath = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
        resourcePacks = resourcePackManager.getProfiles().stream()
                .filter(pack -> resourcePacksPath.resolve(pack.getDisplayName().getString().replaceAll("\"", "")).toFile().exists() || pack.getDisplayName().getString().equalsIgnoreCase("Default"))
                .toList();
        return resourcePacks;
    }

    private static String legalizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static ResourcePackProfile getPack(String packName) {
        for (ResourcePackProfile resourcePack : refresh()) {
            if (resourcePack.getDisplayName().getString().equals(packName.replaceAll("\"", ""))) {
                return resourcePack;
            }
        }
        return null;
    }

    public static Path getPacksFolder() {
        return FabricLoader.getInstance().getGameDir()
                .resolve("resourcepacks");
    }

    public static Path getWorldsFolder() {
        return FabricLoader.getInstance().getGameDir()
                .resolve("saves");
    }

    private static Path getPackFolderPath(ResourcePackProfile packProfile) {
        return FabricLoader.getInstance().getGameDir()
                .resolve("resourcepacks")
                .resolve(packProfile.getDisplayName().getString());
    }

    public static void checkPackType(ResourcePackProfile packProfile) {
        File resourcePackFolder = new File("resourcepacks/" + packProfile.getDisplayName().getString());
//        if (FileUtils.isZipFile(resourcePackFolder)) {
//            ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
//            resourcePackManager.disable(packProfile.getId());
//            File tempDir = new File(resourcePackFolder.getParent(), resourcePackFolder.getName().replace(".zip", ""));
//            if (!tempDir.exists()) {
//                tempDir.mkdirs();
//            }
//            try {
//                FileUtils.unzipPack(resourcePackFolder, tempDir);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    public static void loadPack(ResourcePackProfile currentPack) {
        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
        resourcePackManager.enable(currentPack.getId());
    }

    public static void unloadPack(ResourcePackProfile currentPack) {
        // unloads the pack from the game so that the packs files can be modified
        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
        resourcePackManager.disable(currentPack.getId());
    }

    public static int getCurrentPackFormat() {
        return SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES);
    }
}
