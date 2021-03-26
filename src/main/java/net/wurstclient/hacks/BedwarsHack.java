package net.wurstclient.hacks;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.CombatEventS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.wurstclient.WurstClient;
import net.wurstclient.events.*;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyBinding;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;

public final class BedwarsHack extends Hack implements UpdateListener, PacketInputListener {

    private final HashSet<String> items = new HashSet<>(Arrays.asList("minecraft:iron_ingot",
            "minecraft:gold_ingot", "minecraft:diamond", "minecraft:emerald"));

    private SliderSetting heightSetting = new SliderSetting("\"Kill\" height",
            "Set the height to throw items out when you will die", 63, 0, 255, 1, SliderSetting.ValueDisplay.INTEGER);

    private SliderSetting verticalVelocitySetting = new SliderSetting("\"Kill\" velocity",
            "Set the velocity to throw items out when you will die", .5, 0, 1, .1, SliderSetting.ValueDisplay.DECIMAL);

    private long damageTime;
    private boolean doThrowItems;
    private int throwTimer;
    private int slot;

    public BedwarsHack() {
        super("Bedwars Bot", "Hypixel Bedwars useful hacks");
        addSetting(heightSetting);
        addSetting(verticalVelocitySetting);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketInputListener.class, this);
        this.reset();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketInputListener.class, this);
    }

    @Override
    public void onReceivedPacket(PacketInputEvent event) {
        if (event.getPacket() instanceof CombatEventS2CPacket) {
            CombatEventS2CPacket packet = (CombatEventS2CPacket)event.getPacket();
            if (packet.type == CombatEventS2CPacket.Type.ENTER_COMBAT)
                damageTime = System.currentTimeMillis();
        }
    }

    private void tryBridge() {
        KeyBinding sneakKey = WurstClient.MC.options.keySneak;

        if(!MC.player.isOnGround() || MC.options.keyJump.isPressed() ||
                ((IKeyBinding)sneakKey).isActallyPressed())
            return;

        Box box = MC.player.getBoundingBox();
        //Box adjustedBox = box.offset(0, -0.5, 0).expand(-0.25, 0, -0.25); // was -.001
        Box adjustedBox = box.offset(0, -0.5, 0).stretch(0, -2.5, 0).expand(-0.25, 0, -0.25);

        Stream<VoxelShape> blockCollisions =
                MC.world.getBlockCollisions(MC.player, adjustedBox);

        if(blockCollisions.findAny().isPresent()) {
            sneakKey.setPressed(((IKeyBinding)sneakKey).isActallyPressed());
            return;
        }

        sneakKey.setPressed(true);
    }

    private void tryToThrowItems() {
        throwTimer--;
        if (throwTimer > 0)
            return;

        throwTimer = 2;

        // Skip wrong slots
        while (slot < 45) {
            int adjustedSlot = slot;
            if (adjustedSlot >= 36)
                adjustedSlot -= 36;

            ItemStack stack = MC.player.inventory.getStack(adjustedSlot);
            Item item = stack.getItem();
            if (!stack.isEmpty()) {
                String itemName = Registry.ITEM.getId(item).toString();
                if (items.contains(itemName)) {
                    //slot++;
                    break;
                }
            }
            slot++;
        }

        if (slot == 45) {
            reset();
            return;
        }

        IMC.getInteractionManager().windowClick_THROW(slot);
        slot++;
    }

    @Override
    public void onUpdate() {

        tryBridge();

        if (doThrowItems) {
            tryToThrowItems();
            return;
        }



        /*
            All the below pertains to testing whether player is knocked off into the void by another player
         */
        if (System.currentTimeMillis() - damageTime > 10000) {
            return;
        }

        Vec3d pos = MC.player.getPos(); //.add(MC.player.getVelocity());

        if (MC.player.getVelocity().y > -verticalVelocitySetting.getValue())
            return;

        int h = (int)pos.y;

        for (; h > 0; h--) {
            if (h < heightSetting.getValueI()) break;


            if (!MC.world.isAir(new BlockPos(pos.x, (double)h, pos.z))) {
                // then
                break;
            }
        }

        // determine if the height is "death"
        if (h < heightSetting.getValueI())
            doThrowItems = true;





        if (true)
            return;

        // if there is void beneath player, set blocks
        Vec3d eyesPos = RotationUtils.getEyesPos();
        double rangeSq = Math.pow(5, 2);

        //if (MC.world.getBlo)

        Box box = MC.player.getBoundingBox();
        Box bottomArea = box.expand(1, 0, 1);
        bottomArea = box.stretch(0, -5, 0);

        Stream<VoxelShape> blockCollisions =
                MC.world.getBlockCollisions(MC.player, bottomArea);

        if(blockCollisions.findAny().isPresent())
            return;

        // else, set blocks?
        //tryToPlace()

    }


    private boolean tryToPlace(BlockPos pos, Vec3d eyesPos, double rangeSq)
    {
        Vec3d posVec = Vec3d.ofCenter(pos);
        double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);

        for(Direction side : Direction.values())
        {
            BlockPos neighbor = pos.offset(side);

            // check if neighbor can be right clicked
            if(!BlockUtils.canBeClicked(neighbor)
                    || BlockUtils.getState(neighbor).getMaterial().isReplaceable())
                continue;

            Vec3d dirVec = Vec3d.of(side.getVector());
            Vec3d hitVec = posVec.add(dirVec.multiply(0.5));

            // check if hitVec is within range
            if(eyesPos.squaredDistanceTo(hitVec) > rangeSq)
                continue;

            // check if side is visible (facing away from player)
            if(distanceSqPosVec > eyesPos.squaredDistanceTo(posVec.add(dirVec)))
                continue;

            // check line of sight
            if(MC.world
                    .raycast(new RaycastContext(eyesPos, hitVec,
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE, MC.player))
                    .getType() != HitResult.Type.MISS)
                continue;

            // face block
            RotationUtils.Rotation rotation = RotationUtils.getNeededRotations(hitVec);
            PlayerMoveC2SPacket.LookOnly packet =
                    new PlayerMoveC2SPacket.LookOnly(rotation.getYaw(),
                            rotation.getPitch(), MC.player.isOnGround());
            MC.player.networkHandler.sendPacket(packet);

            // place block
            IMC.getInteractionManager().rightClickBlock(neighbor,
                    side.getOpposite(), hitVec);
            MC.player.swingHand(Hand.MAIN_HAND);
            IMC.setItemUseCooldown(4);
            return true;
        }

        return false;
    }

    private void reset() {
        damageTime = 0;

        doThrowItems = false;
        throwTimer = 2;
        slot = 9;
    }

}
