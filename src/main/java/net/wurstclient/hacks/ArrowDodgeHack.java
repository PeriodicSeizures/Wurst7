package net.wurstclient.hacks;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.wurstclient.FriendsList;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ArrowDodgeHack extends Hack implements RenderListener {

    public ArrowDodgeHack() {
        super("ArrowDodge", "Dodge arrows shot towards you.\n" +
                "Useful for one-in-a-quiver games.");
    }

    @Override
    protected void onEnable() {
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(RenderListener.class, this);
    }

    private static final double GRAVITY = 0.05;
    //private ArrayList<ArrayList<Vec3d>> paths = new ArrayList<>();

    @Override
    public void onRender(float partialTicks) {
        // look for arrows
        ClientWorld world = MC.world;

        if (world == null || MC.player == null) return;

        for(Entity entity : world.getEntities()) {

            if (!(entity instanceof ArrowEntity))
                continue;

            ArrowEntity arrow = (ArrowEntity) entity;
            Entity owner = arrow.getOwner();

            if (arrow.isOnGround())
                continue;

            if (owner == MC.player)
                continue;

            if (owner != null && WURST.getFriends().contains(owner.getEntityName()))
                continue;

            if (arrow.squaredDistanceTo(MC.player) < 7*7)
                continue;



            // cast pos vel from arrow

            Vec3d finalPos = arrow.getPos();
            Vec3d changingVel = arrow.getVelocity();

            for (int i=0; i<1000; i++) {


                float f2 = 0.99F; // velocity multiplier per tick
                if (world.isWater(new BlockPos(finalPos)))
                    f2 = 0.6f; // water drag

                changingVel = changingVel.multiply((double)f2);
                changingVel = changingVel.subtract(0, changingVel.y - 0.05, 0);

                Box box = arrow.getBoundingBox(); //MC.player.getBoundingBox();
                List<Entity> entityList = MC.world.getOtherEntities(arrow, box);

                for (Entity entity1 : entityList) {
                    // if player is within box cast of arrow, then should try to dodge
                    if (entity1 == MC.player) {
                        /*
                            NEO STUFF HERE
                         */
                        // dodge by moving along an orthogonal vector to the arrow velocity
                        Vec3d n = arrow.getVelocity().multiply(1, 0, 1).normalize();
                        Vec3d finalOrthoPos = MC.player.getPos().add(n.y, 0, -n.x);

                        //MC.options.keyForward.setPressed(true);
                        //PlayerMoveC2SPacket packet =
                        //MC.player.setPos(finalOrthoPos.x, finalOrthoPos. y, finalOrthoPos.z);
                        System.out.println("Dodging!");
                        return;

                    }
                }


                if (owner != null && arrow.squaredDistanceTo(owner) > 3*3) {
                    RaycastContext context = new RaycastContext(finalPos, finalPos.add(changingVel),
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE, MC.player);

                    finalPos = finalPos.add(arrow.getVelocity());

                    if (MC.world.raycast(context).getType() != HitResult.Type.MISS)
                        break;
                }

                finalPos = finalPos.add(changingVel);

            }





        }
    }
}
