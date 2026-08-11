package com.bretzelfresser.dinosexpansion.common.network;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoOrderMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DinoChangeOrderPayload(int entityId, int orderOrdinal) implements CustomPacketPayload {
    public static final Type<DinoChangeOrderPayload> TYPE = new Type<>(DinosExpansion.modLoc("dino_change_order"));

    public static final StreamCodec<FriendlyByteBuf, DinoChangeOrderPayload> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.entityId());
                buf.writeInt(value.orderOrdinal());
            },
            buf -> new DinoChangeOrderPayload(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DinoChangeOrderPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof BaseDinoEntity dino) {
                // Ensure the player is the owner or can access it
                if (dino.isTamed() && dino.canPlayerAccessContainer(player)) {
                    dino.setOrderMode(DinoOrderMode.byId(payload.orderOrdinal()));
                }
            }
        });
    }
}
