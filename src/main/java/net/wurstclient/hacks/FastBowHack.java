package net.wurstclient.hacks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

public class FastBowHack extends Hack implements UpdateListener {

    public FastBowHack() {
        super("FastBow", "Spams your bow really quickly");
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        //MinecraftClient.getInstance().player.clic
        if (MC.options.keyUse.isPressed()) {
            ClientPlayerEntity player = MC.player;
            if (player.isOnGround() || player.isCreative()) {
                if (player.getHealth() > 0.0F) {
                    ItemStack stack = player.inventory.getMainHandStack();
                    if (stack != null && stack.getItem() instanceof BowItem) {
                        //WPlayerController.processRightClick();
                        //MC.player

                        for(int i = 0; i < 20; ++i) {
                            //WConnection.sendPacket(new CPacketPlayer(false));
                            //player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket());
                            player.networkHandler.sendPacket(new PlayerMoveC2SPacket(false));
                        }

                        player.stopUsingItem(); //onStoppedUsingItem(WMinecraft.getPlayer());
                    }
                }
            }
        }
    }
}
