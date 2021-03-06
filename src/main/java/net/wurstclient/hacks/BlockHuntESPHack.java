/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.events.UpdateListener;
import org.lwjgl.opengl.GL11;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SearchTags({"blockhunt esp"})
public final class BlockHuntESPHack extends Hack implements RenderListener
{
    private static final Box FAKE_BLOCK_BOX =
            new Box(-0.5, 0, -0.5, 0.5, 1, 0.5);

    //private HashMap<UUID, Double> tracking = new HashMap<>();
    //private HashSet<Integer> tracking = new HashSet<>();
    Set<Integer> tracking = Collections.synchronizedSet(new HashSet<>());

    private final double THRESHOLD = 0.21; //0.21585;

    public BlockHuntESPHack()
    {
        super("BlockHuntESP", "Allows you to see hiders in Mineplex Blockhunt.");
        setCategory(Category.RENDER);
    }

    @Override
    public void onEnable()
    {
        WURST.getHax().farmHuntESPHack.setEnabled(false);
        WURST.getHax().hideAndSeekESPHack.setEnabled(false);

        tracking.clear();
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    public void onDisable()
    {
        tracking.clear();
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onRender(float partialTicks)
    {

        ClientWorld world = MC.world;
        if (world == null) return;

        // GL settings
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glPushMatrix();
        RenderUtils.applyRenderOffset();

        // set color
        float alpha = 0.5F + 0.25F * MathHelper
                .sin(System.currentTimeMillis() % 1000 / 500F * (float)Math.PI);
        GL11.glColor4f(1, 0, 0, alpha);

        //float[] color = new float[] {0, 0, 0, alpha};





        for(Entity entity : world.getEntities()) {

            if(!(entity instanceof MobEntity))
                continue;


            if(MC.player.squaredDistanceTo(entity) < 0.25)
                continue;


            if (entity.isSwimming() || entity.isSubmergedInWater())
                continue;


            int id = entity.getEntityId();
            if (MC.world.getEntityById(id) instanceof PlayerEntity) {
                tracking.remove(id);
                continue;
            }

            Vec3d v = entity.getVelocity();
            if (entity.isInvisible())
            {
                tracking.add(id);
            }



            if (entity instanceof ChickenEntity && v.y < -.18)
            {
                tracking.add(id);
            }


            // anything faster than a walking animal is also sus

            double h = v.x*v.x + v.z*v.z;
            if (h >= (THRESHOLD)*(THRESHOLD))
            {
                tracking.add(id);
            }

            // for cases where velocity is hard to track (direction instead of vel)
            //Vec3d p = entity.getPos();


            // slimes
            if (entity instanceof SlimeEntity)
            {
                tracking.add(id);
            }

            if (!tracking.contains(id))
                continue;

            GL11.glPushMatrix();
            GL11.glTranslated(entity.getX(), entity.getY(), entity.getZ());

            RenderUtils.drawOutlinedBox(FAKE_BLOCK_BOX);
            RenderUtils.drawSolidBox(FAKE_BLOCK_BOX);

            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();

        // GL resets
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}
