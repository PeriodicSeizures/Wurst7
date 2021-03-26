/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import net.minecraft.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.VelocityFromFluidListener.VelocityFromFluidEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, CommandOutput
{
	@Redirect(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
		opcode = Opcodes.INVOKEVIRTUAL,
		ordinal = 0),
		method = {"updateMovementInFluid(Lnet/minecraft/tag/Tag;D)Z"})
	private void setVelocityFromFluid(Entity entity, Vec3d velocity)
	{
		VelocityFromFluidEvent event = new VelocityFromFluidEvent();
		EventManager.fire(event);
		
		if(!event.isCancelled())
			entity.setVelocity(velocity);
	}


	/*
		CUSTOM:
	 */

	/*
		Only works in single player LAN
	 */
	//@Inject(at = @At("HEAD"),
	//		method = "dealDamage(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V")
	////private void onDamage(DamageSource source, float amount, CallbackInfo ci) {
	//public void onDealDamage(LivingEntity attacker, Entity target, CallbackInfo ci) {
	//	EventManager.fire(new DealDamageListener.DealDamageEvent(attacker, target));
	//}

	/*
		Only works in single player LAN
	 */
	//@Inject(at = @At(value = "HEAD"),
	////opcode = Opcodes.INVOKEVIRTUAL),
	//		method = "dealDamage(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V")
	//public void onDealDamage(LivingEntity attacker, Entity target, CallbackInfo ci) {
	//	EventManager.fire(new DealDamageListener.DealDamageEvent(attacker, target));
	//}


}
