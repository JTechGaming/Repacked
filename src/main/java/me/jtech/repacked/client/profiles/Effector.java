package me.jtech.repacked.client.profiles;

import me.jtech.repacked.Repacked;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.resource.ResourcePackProfile;

import java.io.File;
import java.util.List;

public record Effector(File world, boolean isServer, ServerInfo server) {
    public Effector(File world) {
        this(world, false, null);
    }

    public Effector(ServerInfo server) {
        this(null, true, server);
    }

    public static Effector fromString(String effectorString) {
        Effector effector;
        if (effectorString.startsWith("true>")) {
            effector = new Effector(new ServerInfo(effectorString.split(">")[1], effectorString.split(">")[2], ServerInfo.ServerType.OTHER));
        } else {
            File file = FabricLoader.getInstance().getGameDir().resolve("saves").resolve(effectorString.split(">")[1]).toFile();
            if (!file.exists()) {
                Repacked.LOGGER.error("World does not exist: {}", file);
            }
            effector = new Effector(file);
        }
        return effector;
    }

    @Override
    public String toString() {
        return String.format("%s>%s", isServer, isServer ? server.name + ">" + server.address : world.getPath());
    }

    public <C> C getProfile(Class<C> clazz) {
        if (isServer && clazz.isInstance(server)) {
            return clazz.cast(server);
        } else if (!isServer && clazz.isInstance(world)) {
            return clazz.cast(world);
        } else {
            throw new ClassCastException("Cannot cast profile to " + clazz.getName());
        }
    }

    public String getName() {
        return isServer ? server.name : world.getName();
    }
}
