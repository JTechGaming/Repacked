package me.jtech.repacked.client.mixin;

import me.jtech.repacked.PackUtils;
import me.jtech.repacked.client.RepackedClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "reloadResources()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
    public void reloadResources(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (RepackedClient.CONFIG.enabled() && RepackedClient.CONFIG.overridePackReload()) {
            CompletableFuture.runAsync(PackUtils::reloadPack);
            cir.cancel();
        }
    }
}
