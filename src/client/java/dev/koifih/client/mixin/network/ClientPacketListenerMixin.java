package dev.koifih.client.mixin.network;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.TotemPopEvent;
<<<<<<< HEAD
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
=======
import dev.koifih.client.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.Relative;
import org.spongepowered.asm.mixin.Mixin;
<<<<<<< HEAD
=======
import org.spongepowered.asm.mixin.Unique;
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void adin$syncMove(ClientboundPlayerPositionPacket packet, CallbackInfo info) {
<<<<<<< HEAD
        if (!packet.relatives().contains(Relative.Y_ROT) || !packet.relatives().contains(Relative.X_ROT)) {
            AdinClient.ROTATIONS.syncFromPlayer();
        }
=======
        if (!packet.relatives().contains(Relative.Y_ROT) || !packet.relatives().contains(Relative.X_ROT)) adin$sync();
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    }

    @Inject(method = "handleRotatePlayer", at = @At("TAIL"))
    private void adin$syncRotate(ClientboundPlayerRotationPacket packet, CallbackInfo info) {
<<<<<<< HEAD
        AdinClient.ROTATIONS.syncFromPlayer();
=======
        adin$sync();
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    }

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void adin$totemPop(ClientboundEntityEventPacket packet, CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();
        if (packet.getEventId() != EntityEvent.PROTECTED_FROM_DEATH || client.level == null || !client.isSameThread()) return;
        if (packet.getEntity(client.level) == client.player) AdinClient.EVENTS.post(new TotemPopEvent(client));
    }
<<<<<<< HEAD
=======

    @Unique
    private static void adin$sync() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) AdinClient.ROTATIONS.sync(Rotation.of(player));
    }
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
}
