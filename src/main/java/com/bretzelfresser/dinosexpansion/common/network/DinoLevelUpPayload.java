package com.bretzelfresser.dinosexpansion.common.network;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoStat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DinoLevelUpPayload(int entityId, int statOrdinal) implements CustomPacketPayload {
    public static final Type<DinoLevelUpPayload> TYPE = new Type<>(DinosExpansion.modLoc("dino_level_up"));

    public static final StreamCodec<FriendlyByteBuf, DinoLevelUpPayload> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.entityId());
                buf.writeInt(value.statOrdinal());
            },
            buf -> new DinoLevelUpPayload(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DinoLevelUpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof BaseDinoEntity dino) {
                if (dino.isTamed() && dino.canPlayerAccess(player) && dino.getAvailablePoints() > 0) {
                    if (payload.statOrdinal() >= 0 && payload.statOrdinal() < DinoStat.values().length) {
                        DinoStat stat = DinoStat.values()[payload.statOrdinal()];
                        dino.upgradeStat(stat);
                    }
                }
            }
        });
    }
}
