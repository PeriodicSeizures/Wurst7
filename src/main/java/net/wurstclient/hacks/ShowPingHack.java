package net.wurstclient.hacks;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.wurstclient.WurstClient;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.other_features.HackListOtf;
import net.wurstclient.util.LastServerRememberer;
import net.wurstclient.util.pinger.PingWorker;
import net.wurstclient.util.pinger.PingerCallable;
import org.apache.http.util.TextUtils;

public class ShowPingHack extends Hack implements GUIRenderListener {

    private int ping;
    private Thread workerThread;
    private PingWorker worker;
    private PingerCallable pinger;

    public ShowPingHack() {
        super("ShowPing", "Displays the server ping in the \n"
                + "upper right corner of the screen.");
    }

    private boolean failed = false;

    @Override
    public void onEnable() {
        failed = false;
        ServerInfo serverInfo = LastServerRememberer.getLastServer();
        if (serverInfo == null) {
            failed = true;
            this.setEnabled(false);
            return;
        }
        this.worker = new PingWorker(serverInfo.address);
        this.workerThread = new Thread(this.worker);
        this.worker.setRun(true);
        this.workerThread.start();

        EVENTS.add(GUIRenderListener.class, this);
    }

    @Override
    public void onDisable() {
        if (!failed) {
            worker.setRun(false);
            EVENTS.remove(GUIRenderListener.class, this);
        }
    }

    @Override
    public void onRenderGUI(MatrixStack matrixStack, float partialTicks) {


        // draw the thing in text on top right
        int ping = this.worker.getPing();

        drawString(matrixStack, Integer.toString(ping) + "ms");

    }

    private void drawString(MatrixStack matrixStack, String s)
    {
        TextRenderer tr = MC.textRenderer;
        int posX;

        int screenWidth = MC.getWindow().getScaledWidth();
        int stringWidth = tr.getWidth(s);

        posX = screenWidth - stringWidth - 2;

        tr.draw(matrixStack, s, posX + 1, 3, 0xff0000FF);
        //tr.draw(matrixStack, s, posX, 3, 0xff000000);

        //posY += 9;
    }



}
