package net.wurstclient.hacks;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

import java.util.stream.Stream;

public final class BedwarsHack extends Hack implements UpdateListener {

    public BedwarsHack() {
        super("Bedwars Bot", "Hypixel Bedwars useful hacks");
    }

    @Override
    public void onUpdate() {
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

}
