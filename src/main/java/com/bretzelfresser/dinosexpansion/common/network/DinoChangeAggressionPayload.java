package com.bretzelfresser.dinosexpansion.common.network;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoAggressionMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DinoChangeAggressionPayload(int entityId, int modeOrdinal) implements CustomPacketPayload {
    public static final Type<DinoChangeAggressionPayload> TYPE = new Type<>(DinosExpansion.modLoc("dino_change_aggression"));

    public static final StreamCodec<FriendlyByteBuf, DinoChangeAggressionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.entityId());
                buf.writeInt(value.modeOrdinal());
            },
            buf -> new DinoChangeAggressionPayload(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DinoChangeAggressionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof BaseDinoEntity dino) {
                // Ensure the player is the owner or can access it
                if (dino.isTamed() && dino.canPlayerAccessContainer(player)) {
                    dino.setAggressionMode(DinoAggressionMode.byId(payload.modeOrdinal()));
                }
            }
        });
    }
}
