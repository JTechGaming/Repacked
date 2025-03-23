package me.jtech.repacked.client.io;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.jtech.repacked.PackUtils;
import me.jtech.repacked.client.RepackedClient;
import me.jtech.repacked.client.profiles.Effector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourcePackProfile;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileIO {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(File.class, new FileTypeAdapter())
            .registerTypeAdapter(Effector.class, new EffectorTypeAdapter())
            .create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(RepackedClient.MOD_ID).resolve("profiles.json");

    public static void saveProfile(Effector effector, List<String> data) {
        Map<Effector, List<String>> map = new HashMap<>();
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<Effector, List<String>>>() {
                }.getType();
                map = GSON.fromJson(reader, type); // Load the existing map
            } catch (IOException e) {
                RepackedClient.LOGGER.error("Error reading profile storage: {}", String.valueOf(e));
            }
        } else {
            createFile();
        }
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(effector, data);
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(map, writer);
        } catch (IOException e) {
            RepackedClient.LOGGER.error("Error saving profile storage: {}", String.valueOf(e));
        }
    }

    public static Map<Effector, List<ResourcePackProfile>> loadProfiles() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<Effector, List<String>>>() {}.getType();
                Map<Effector, List<String>> data = GSON.fromJson(reader, type);
                if (data == null || data.isEmpty()) {
                    return null;
                }
                return data.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                                .map(PackUtils::getPack)
                                .collect(Collectors.toList())));
            } catch (IOException e) {
                RepackedClient.LOGGER.error("Error loading profile storage: {}", e);
            }
        } else {
            createFile();
        }
        return null;
    }

    public static void removeProfile(Effector effector) {
//        Map<Effector, List<ResourcePackProfile>> map = loadProfiles();
//        if (map == null) {
//            return;
//        }
//        map.remove(effector);
//        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
//            GSON.toJson(map, writer);
//        } catch (IOException e) {
//            RepackedClient.LOGGER.error("Error saving profile storage: {}", String.valueOf(e));
//        }
    }

    private static void createFile() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.createFile(CONFIG_FILE);
        } catch (IOException e) {
            RepackedClient.LOGGER.error("Error creating profile storage: {}", String.valueOf(e));
        }
    }

    public static class FileTypeAdapter extends TypeAdapter<File> {
        @Override
        public void write(JsonWriter out, File value) throws IOException {
            out.value(value.getPath());
        }

        @Override
        public File read(JsonReader in) throws IOException {
            return new File(in.nextString());
        }
    }

    public static class EffectorTypeAdapter extends TypeAdapter<Effector> {
        @Override
        public void write(JsonWriter out, Effector effector) throws IOException {
            out.value(effector.toString());
        }

        @Override
        public Effector read(JsonReader in) throws IOException {
            String effectorString = in.nextString();
            // Parse the effectorString to create an Effector object
            // Assuming Effector has a static method fromString to parse the string
            return Effector.fromString(effectorString);
        }
    }
}
