/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MushroomStewItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RotationUtils;

import java.lang.reflect.Field;

@SearchTags({"no fall"})
public final class NoFallHack extends Hack implements UpdateListener
{
	private final EnumSetting<FallMode> mode = new EnumSetting<>("Mode",
			"\u00a7lPacket\u00a7r mode will attempt to persuade \n"
					+ "the server that you shouldn't take fall damage.\n"
					+ "\u00a7lWater Bucket\u00a7r mode will MLG\n"
					+ "water bucket to prevent fall damage.",
			FallMode.values(), FallMode.PACKET);

	private int oldSlot = -1;
	private boolean placed = false;

	public NoFallHack()
	{
		super("NoFall", "Protects you from fall damage.");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
		//javax.crypto.spec.SecretKeySpec
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		if(player.fallDistance <= (player.isFallFlying() ? 1 : 2)) {
			placed = true;
			return;
		}
		
		if(player.isFallFlying() && player.isSneaking()
			&& !isFallingFastEnoughToCauseDamage(player))
			return;

		placed = false;

		switch (mode.getSelected()) {
			case PACKET:
				player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
				break;
			case BUCKET:
				// scan the area below the player in a 2x2 (if near edge of block
				// center of block is at .5, .5

				// try to place water below the player

				//RotationUtils.g



				// make player hold water bucket


				// place block
				double dy = player.getY();
				for (; dy > player.getY()-4; dy--) {
					BlockPos pos = new BlockPos(player.getX(), dy, player.getZ());
					if (!MC.world.getBlockState(pos).isAir()) {

						PlayerMoveC2SPacket.LookOnly packet =
								new PlayerMoveC2SPacket.LookOnly((float)(Math.PI/2),
										player.pitch, MC.player.isOnGround());

						MC.player.networkHandler.sendPacket(packet);


						// equip waterbucket
						int waterBucketInHotbar = findWaterBucket(0, 9);
						if (waterBucketInHotbar != -1) {


							if(oldSlot == -1)
								oldSlot = MC.player.inventory.selectedSlot;

							// set slot
							MC.player.inventory.selectedSlot = waterBucketInHotbar;

							IMC.getInteractionManager().rightClickBlock(pos,
									Direction.UP, new Vec3d(0, -1, 0));
							MC.player.swingHand(Hand.MAIN_HAND);
							IMC.setItemUseCooldown(4);

							//BucketItem bucketItem = (BucketItem)(MC.player.inventory.getStack(waterBucketInHotbar).getItem());

							//bucketItem.placeFluid(player, player.world, pos, null);

							MC.player.inventory.selectedSlot = oldSlot;

							placed = true;

						}


						//IMC.getInteractionManager().rightClickBlock(pos,
						//		Direction.UP, RotationUtils.getClientLookVec());
						//MC.player.swingHand(Hand.MAIN_HAND);
						//IMC.setItemUseCooldown(4);

						break;
					}
				}
				break;
		}
	}
	
	private boolean isFallingFastEnoughToCauseDamage(ClientPlayerEntity player)
	{
		return player.getVelocity().y < -0.5;
	}

	private int findWaterBucket(int startSlot, int endSlot)
	{

		for(int i = startSlot; i < endSlot; i++)
		{
			ItemStack stack = MC.player.inventory.getStack(i);

			if(stack != null && stack.getItem() instanceof BucketItem) {
				BucketItem bucketItem = (BucketItem) stack.getItem();

				try {
					Field f = BucketItem.class.getDeclaredField("fluid");
					f.setAccessible(true);

					Fluid fluid = (Fluid)f.get(bucketItem);

					if (fluid.matchesType(Fluids.WATER))
						return i;

				} catch (Exception e) {

				}
			}
		}

		return -1;
	}

	private enum FallMode
	{
		PACKET("Packet"),
		BUCKET("Water Bucket");

		private final String name;

		private FallMode(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

}
