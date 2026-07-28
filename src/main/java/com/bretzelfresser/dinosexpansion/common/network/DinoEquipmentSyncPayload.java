package com.bretzelfresser.dinosexpansion.common.network;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record DinoEquipmentSyncPayload(int entityId, int slotOrdinal, ItemStack itemStack) implements CustomPacketPayload {
    public static final Type<DinoEquipmentSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_equipment_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DinoEquipmentSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.entityId());
                buf.writeInt(value.slotOrdinal());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, value.itemStack());
            },
            buf -> new DinoEquipmentSyncPayload(
                    buf.readInt(),
                    buf.readInt(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DinoEquipmentSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity instanceof BaseDinoEntity dino) {
                Optional<DinoEquipment> eqOpt = DinoEquipment.optionalById(payload.slotOrdinal());
                if (eqOpt.isPresent()) {
                    dino.getEquipmentInventory().setEquipment(eqOpt.get(), payload.itemStack());
                }
            }
        });
    }
}
