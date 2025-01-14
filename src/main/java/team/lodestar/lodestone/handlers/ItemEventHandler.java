package team.lodestar.lodestone.handlers;

import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingHurtEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import team.lodestar.lodestone.helpers.ItemHelper;
import team.lodestar.lodestone.systems.item.IEventResponderItem;

/**
 * A handler for firing {@link IEventResponderItem} events
 */
public class ItemEventHandler {

    public static boolean respondToDeath(LivingEntity livingEntity, DamageSource damageSource, float damageAmount) {
        LivingEntity attacker = null;
        if (damageSource.getEntity() instanceof LivingEntity directAttacker) {
            attacker = directAttacker;
        }
        if (attacker == null) {
            attacker = livingEntity.getLastHurtByMob();
        }
        if (attacker != null) {
            LivingEntity finalAttacker = attacker;
            ItemHelper.getEventResponders(attacker).forEach(s -> ((IEventResponderItem) s.getItem()).killEvent(finalAttacker, livingEntity, s, damageSource, damageAmount));
        }

        return true;
    }


    public static void respondToHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemHelper.getEventResponders(attacker).forEach(s -> ((IEventResponderItem) s.getItem()).hurtEvent(event, attacker, target, s));
            ItemHelper.getEventResponders(target).forEach(s -> ((IEventResponderItem) s.getItem()).takeDamageEvent(event, attacker, target, s));
        }
    }


}