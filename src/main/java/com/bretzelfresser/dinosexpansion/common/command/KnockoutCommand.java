package com.bretzelfresser.dinosexpansion.common.command;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class KnockoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("knockout")
                .requires(source -> source.hasPermission(2))
                .executes(context -> execute(context.getSource()))
        );
    }

    private static int execute(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        double reach = 50.0D;
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 endPoint = eyePosition.add(lookVector.x * reach, lookVector.y * reach, lookVector.z * reach);
        AABB searchArea = player.getBoundingBox().expandTowards(lookVector.scale(reach)).inflate(1.0D, 1.0D, 1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                eyePosition,
                endPoint,
                searchArea,
                entity -> entity instanceof BaseDinoEntity
        );

        if (hitResult != null && hitResult.getEntity() instanceof BaseDinoEntity dino) {
            dino.setUnconsciousFrom(player);
            dino.setTorpor((float) dino.getAttributeValue(ModAttributes.MAX_TORPOR));
            source.sendSuccess(() -> Component.literal("Successfully knocked out " + dino.getName().getString()), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("No dinosaur in sight!"));
            return 0;
        }
    }
}
