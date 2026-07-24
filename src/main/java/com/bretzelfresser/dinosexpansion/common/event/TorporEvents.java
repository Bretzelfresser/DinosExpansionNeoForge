package com.bretzelfresser.dinosexpansion.common.event;


import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.BaseDinoEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = DinosExpansion.MODID)
public class TorporEvents {


    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof BaseDinoEntity dino){
            var damageSource = event.getSource();
            var damageContainer = event.getContainer();
            var damage = event.getOriginalDamage();

        }

    }
}
