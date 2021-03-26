package net.wurstclient.hacks;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SearchTags({"farmhunt esp", "farm hunt esp"})
public class HideAndSeekESPHack extends Hack implements RenderListener {

    private static final Box FAKE_ANIMAL_BOX =
            new Box(-0.5, 0, -0.5, 0.5, 1, 0.5);

    private Set<Integer> tracking = Collections.synchronizedSet(new HashSet<>());

    public HideAndSeekESPHack() {
        super("HideAndSeekESP", "Allows you to see hiders in TheHive Hide and Seek");
    }

    @Override
    public void onEnable() {

        WURST.getHax().farmHuntESPHack.setEnabled(false);
        WURST.getHax().blockhuntESPHack.setEnabled(false);
        tracking.clear();

        EVENTS.add(RenderListener.class, this);
    }

    @Override
    public void onDisable() {
        tracking.clear();
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onRender(float partialTicks) {
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

        for (Entity e : MC.world.getEntities()) {

            //if (Math.abs(e.pitch) > 0.001)
//                tracking.add(e.getEntityId());

            if (e instanceof FallingBlockEntity)
                tracking.add(e.getEntityId());

            if (!tracking.contains(e.getEntityId()))
                continue;

            //if (e instanceof Falling)


            if (!(e instanceof FallingBlockEntity))
                continue;

            GL11.glPushMatrix();
            GL11.glTranslated(e.getX(), e.getY(), e.getZ());

            RenderUtils.drawOutlinedBox(FAKE_ANIMAL_BOX);
            RenderUtils.drawSolidBox(FAKE_ANIMAL_BOX);

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
