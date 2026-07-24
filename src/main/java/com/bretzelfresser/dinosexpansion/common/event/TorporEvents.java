package com.bretzelfresser.dinosexpansion.common.event;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModEnchantmentEffectComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = DinosExpansion.MODID)
public class TorporEvents {

    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof BaseDinoEntity dino) {
            if (!(dino.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            var damageSource = event.getSource();
            ItemStack weaponStack = getWeaponStack(damageSource);

            if (weaponStack.isEmpty()) {
                return;
            }

            final float[] torporToApply = {0.0F};

            // 3. Iterate over the weapon's enchantments and process our custom component
            EnchantmentHelper.runIterationOnItem(weaponStack, (enchantmentHolder, level) -> {
                Enchantment enchantment = enchantmentHolder.value();
                List<ConditionalEffect<EnchantmentValueEffect>> effects = enchantment.effects().get(ModEnchantmentEffectComponents.TORPOR_MODIFICATION.get());
                if (effects != null) {
                    // Build the required LootContext for evaluating conditions
                    LootParams lootParams = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.THIS_ENTITY, dino)
                            .withParameter(LootContextParams.ORIGIN, dino.position())
                            .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                            .withParameter(LootContextParams.ENCHANTMENT_LEVEL, level)
                            .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
                            .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity())
                            .create(LootContextParamSets.ENCHANTED_DAMAGE);

                    LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());

                    for (ConditionalEffect<EnchantmentValueEffect> conditionalEffect : effects) {
                        if (conditionalEffect.matches(lootContext)) {
                            // Process the base value to calculate torpor addition
                            torporToApply[0] = conditionalEffect.effect().process(level, dino.getRandom(), torporToApply[0]);
                        }
                    }
                }
            });

            if (torporToApply[0] > 0.0F) {
                dino.applyBufferedNarcotics(torporToApply[0]);
            }
        }
    }

    private static ItemStack getWeaponStack(DamageSource damageSource) {
        ItemStack weaponStack = ItemStack.EMPTY;

        // 1. If damage is from a projectile (bow, crossbow, etc.), retrieve the weapon that fired it
        if (damageSource.getDirectEntity() instanceof Projectile projectile) {
            weaponStack = projectile.getWeaponItem();
        }
        // 2. Otherwise, check if it's direct melee damage from a living entity
        else if (damageSource.getEntity() instanceof LivingEntity attacker) {
            weaponStack = attacker.getMainHandItem();
        }
        return weaponStack;
    }
}
