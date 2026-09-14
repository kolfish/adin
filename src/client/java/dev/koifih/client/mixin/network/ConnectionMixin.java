package dev.koifih.client.mixin.network;

import dev.koifih.client.backtrack.Backtracker;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static void adin$intercept(Packet<?> packet, PacketListener listener, CallbackInfo info) {
        if (Backtracker.intercept(packet, listener)) info.cancel();
    }
}
