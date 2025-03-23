package me.jtech.repacked.client.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.container.ScrollContainer;
import me.jtech.repacked.PackUtils;
import me.jtech.repacked.client.io.ProfileIO;
import me.jtech.repacked.client.profiles.Effector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class ProfileEditorScreen extends BaseOwoScreen<FlowLayout> {
    private final Effector effector;

    private final List<File> initialSelected = new ArrayList<>();
    private final List<File> files = new ArrayList<>(List.of(Objects.requireNonNull(PackUtils.getPacksFolder().toFile().listFiles())));

    private FlowLayout availablePacksContainer;
    private FlowLayout selectedPacksContainer;

    public ProfileEditorScreen(Text title, Effector effector) {
        super(title);
        this.effector = effector;

        // Load the selected packs for the profile
        Map<Effector, List<ResourcePackProfile>> profiles = ProfileIO.loadProfiles();
        if (profiles == null) return;
        if (profiles.containsKey(effector)) {
            List<ResourcePackProfile> selectedPacks = profiles.get(effector);
            selectedPacks.forEach(pack -> {
                if (pack == null) return;
                File packFile = files.stream().filter(file -> file.getName().equals(pack.getDisplayName().getString())).findFirst().orElse(null);
                if (packFile == null) return;
                if (packFile.exists()) {
                    initialSelected.add(packFile);
                }
            });
        }
    }

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

        // Create a horizontal container to hold the resource pack lists
        FlowLayout horizontalContainer = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        horizontalContainer.gap(20); // Add some gap between the two lists
        horizontalContainer.surface(Surface.DARK_PANEL)
                .padding(Insets.of(20, 20, 50, 50))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        // Create a vertical container for available resource packs
        availablePacksContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        availablePacksContainer.surface(Surface.DARK_PANEL)
                .padding(Insets.of(10))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.CENTER);
        availablePacksContainer.gap(20);

        availablePacksContainer.child(Components.label(Text.literal("Available Resource Packs")))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(10));

        // Create a vertical container for selected resource packs
        selectedPacksContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        selectedPacksContainer.surface(Surface.DARK_PANEL)
                .padding(Insets.of(10))
                .verticalAlignment(VerticalAlignment.TOP)
                .horizontalAlignment(HorizontalAlignment.CENTER);
        selectedPacksContainer.gap(20);

        selectedPacksContainer.child(Components.label(Text.literal("Selected Resource Packs")))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(10));

        // List all the resource packs in the player's resource pack folder
        files.forEach(pack -> {
            if (!pack.getName().endsWith(".zip") && !pack.isDirectory()) return;
            if (initialSelected.contains(pack)) return;
            Component child = Components.button(Text.literal(pack.getName()), button -> {
                // Handle pack deselection
                selectedPacksContainer.child(createSelectedPackComponent(pack));
                availablePacksContainer.removeChild(button);
            });
            availablePacksContainer.child(child)
                    .padding(Insets.of(5));
        });

        // List all the selected resource packs
        initialSelected.forEach(pack -> {
            Component child = Components.button(Text.literal(pack.getName()), button -> {
                // Handle pack deselection
                availablePacksContainer.child(createAvailablePackComponent(pack));
                selectedPacksContainer.removeChild(button);
            });
            selectedPacksContainer.child(child)
                    .padding(Insets.of(5));
        });

        // Create scroll containers for available and selected packs
        ScrollContainer<FlowLayout> availablePacksScrollContainer = Containers.verticalScroll(Sizing.content(), Sizing.fixed(300), availablePacksContainer);
        ScrollContainer<FlowLayout> selectedPacksScrollContainer = Containers.verticalScroll(Sizing.content(), Sizing.fixed(300), selectedPacksContainer);

        // Add the scroll containers to the horizontal container
        horizontalContainer.child(availablePacksScrollContainer);
        horizontalContainer.child(selectedPacksScrollContainer);

        // Add the horizontal container to the root component
        rootComponent.child(horizontalContainer);

        // Add save, delete, and close buttons at the bottom
        FlowLayout buttonContainer = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        buttonContainer.surface(Surface.DARK_PANEL);
        buttonContainer.gap(40)
            .horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER)
            .padding(Insets.of(5, 5, 5, 5));
        buttonContainer.child(Components.button(Text.literal("Save"), button -> saveProfile()));
        buttonContainer.child(Components.button(Text.literal("Delete"), button -> deleteProfile()));
        buttonContainer.child(Components.button(Text.literal("Close"), button -> closeScreen()));

        rootComponent.child(buttonContainer.margins(Insets.of(0, 5, 0, 0)));
    }

    private Component createSelectedPackComponent(File pack) {
        return Components.button(Text.literal(pack.getName()), button -> {
            // Handle pack deselection
            availablePacksContainer.child(createAvailablePackComponent(pack));
            selectedPacksContainer.removeChild(button);
        });
    }

    private Component createAvailablePackComponent(File pack) {
        return Components.button(Text.literal(pack.getName()), button -> {
            // Handle pack selection
            selectedPacksContainer.child(createSelectedPackComponent(pack));
            availablePacksContainer.removeChild(button);
        });
    }

    private void saveProfile() {
        // Implement save profile logic
        List<String> selectedPacks = new ArrayList<>();
        selectedPacksContainer.children().forEach(component -> {
            if (component instanceof ButtonComponent button) {
                selectedPacks.add(button.getMessage().getString());
            }
        });
        ProfileIO.saveProfile(effector, selectedPacks);
    }

    private void deleteProfile() {
        // Implement delete profile logic
        ProfileIO.removeProfile(effector);
    }

    private void closeScreen() {
        MinecraftClient.getInstance().setScreen(new ProfileEffectorSelectionScreen());
    }
}