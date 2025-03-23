package me.jtech.repacked.client.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.container.ScrollContainer;
import me.jtech.repacked.PackUtils;
import me.jtech.repacked.client.RepackedClient;
import me.jtech.repacked.client.profiles.Effector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileEffectorSelectionScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        if (!RepackedClient.CONFIG.enabled()) {
            rootComponent.child(Components.label(Text.literal("Repacked is disabled in the config")))
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .verticalAlignment(VerticalAlignment.CENTER);
            return;
        }

        // Create a horizontal container to hold the two scroll lists
        FlowLayout horizontalContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        horizontalContainer.gap(20); // Add some gap between the two lists
        horizontalContainer.surface(Surface.DARK_PANEL)
                .padding(Insets.of(20, 20, 50, 50))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        // Create a vertical container for singleplayer worlds
        FlowLayout singleplayerContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        singleplayerContainer.surface(Surface.DARK_PANEL)
                .padding(Insets.of(10))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.CENTER);
        singleplayerContainer.gap(20);

        singleplayerContainer.child(Components.label(Text.literal("Singleplayer Worlds")))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(10));

        // List all singleplayer worlds
        File[] worldFiles = PackUtils.getWorldsFolder().toFile().listFiles();
        if (worldFiles != null) {
            Arrays.stream(worldFiles).forEach(world -> {
                if (world.isDirectory()) {
                    singleplayerContainer.child(
                                    Components.button(Text.literal(world.getName()), button -> { openProfileScreen(new Effector(world)); }))
                            .padding(Insets.of(5));
                }
            });
        }

        // Create a vertical container for multiplayer servers
        FlowLayout multiplayerContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        multiplayerContainer.surface(Surface.DARK_PANEL)
                .padding(Insets.of(10))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.CENTER);
        multiplayerContainer.gap(20);

        multiplayerContainer.child(Components.label(Text.literal("Multiplayer Servers")))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(10));

        // List all the servers in the server list
        ServerList serverList = new ServerList(client);
        serverList.loadFile();
        for (int i = 0; i < serverList.size(); i++) {
            ServerInfo serverInfo = serverList.get(i);
            int finalI = i;
            multiplayerContainer.child(
                            Components.button(Text.literal(serverInfo.name), button -> { openProfileScreen(new Effector(serverList.get(finalI))); }))
                    .padding(Insets.of(5));
        }

        // Create scroll containers for singleplayer and multiplayer lists
        ScrollContainer<FlowLayout> singleplayerScrollContainer = Containers.verticalScroll(Sizing.fixed(200), Sizing.fixed(300), singleplayerContainer);
        ScrollContainer<FlowLayout> multiplayerScrollContainer = Containers.verticalScroll(Sizing.fixed(200), Sizing.fixed(300), multiplayerContainer);

        // Add the scroll containers to the horizontal container
        horizontalContainer.child(singleplayerScrollContainer);
        horizontalContainer.child(multiplayerScrollContainer);

        // Add the horizontal container to the root component
        rootComponent.child(horizontalContainer);
    }

    private void openProfileScreen(Effector effector) {
        MinecraftClient.getInstance().setScreen(new ProfileEditorScreen(Text.literal("test"), effector));
    }
}