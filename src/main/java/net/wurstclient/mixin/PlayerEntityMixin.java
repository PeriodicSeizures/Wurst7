package net.wurstclient.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.wurstclient.event.EventManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {


	/*
		CUSTOM:
	 */

    //@Inject(at = @At("RETURN"), method = "damage")
    //private void onDamage(DamageSource source, float amount, CallbackInfoReturnable info) {
    //    System.out.println("The player received damage!");
    //    EventManager.fire(new DamageListener.DamageEvent(source, amount));
    //}

    /*
        Only works in single player LAN
    */
    //@Inject(at = @At("HEAD"),
    //		method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V")
    //private void onDamage(DamageSource source, float amount, CallbackInfo ci) {
    //	EventManager.fire(new DamageListener.DamageEvent(source, amount));
    //}

}
