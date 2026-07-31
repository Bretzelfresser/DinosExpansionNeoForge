package com.bretzelfresser.dinosexpansion.common.network;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayerTriggerAttackPayload(int entityId) implements CustomPacketPayload {
    public static final Type<PlayerTriggerAttackPayload> TYPE = new Type<>(DinosExpansion.modLoc("player_trigger_attack"));

    public static final StreamCodec<FriendlyByteBuf, PlayerTriggerAttackPayload> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeInt(value.entityId()),
            buf -> new PlayerTriggerAttackPayload(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerTriggerAttackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof BaseDinoEntity dino) {
                if (player.getVehicle() == dino) {
                    dino.playerTriggerAttack();
                }
            }
        });
    }
}
