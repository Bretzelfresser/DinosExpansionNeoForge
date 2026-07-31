package com.bretzelfresser.dinosexpansion.common.network;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public record OpenDinoInventoryPayload(int entityId) implements CustomPacketPayload {
    public static final Type<OpenDinoInventoryPayload> TYPE = new Type<>(DinosExpansion.modLoc("open_dino_inventory"));

    public static final StreamCodec<FriendlyByteBuf, OpenDinoInventoryPayload> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeInt(value.entityId()),
            buf -> new OpenDinoInventoryPayload(buf.readInt())
    );

    private static final Map<ServerPlayer, Long> LAST_OPEN_TIMES = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenDinoInventoryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                long currentTime = serverPlayer.level().getGameTime();
                long lastOpenTime = LAST_OPEN_TIMES.getOrDefault(serverPlayer, 0L);
                if (currentTime - lastOpenTime < 20L) {
                    return;
                }
                LAST_OPEN_TIMES.put(serverPlayer, currentTime);
            }

            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof BaseDinoEntity dino) {
                if (dino.canPlayerAccessContainer(player) && (player.getVehicle() == dino || player.distanceToSqr(dino) < 64.0D)) {
                    dino.openDinoInventory(player);
                }
            }
        });
    }
}
