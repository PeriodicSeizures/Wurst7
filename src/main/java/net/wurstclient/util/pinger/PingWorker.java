package net.wurstclient.util.pinger;

public class PingWorker implements Runnable {
    private PingerCallable callable;
    private int ping;
    private boolean run = true;

    public PingWorker(String rawAddress) {
      this.callable = new PingerCallable(rawAddress);
    }

    public int getPing() {
        return this.ping;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    @Override
    public void run() {
        while(this.run) {
            try {
                //if(this.currentConnection != null) {
                //    this.callable = new PingerCallable(this.currentConnection);
                //    this.currentConnection = null;
                //}

                if(this.callable != null) {
                    this.ping = Integer.parseInt(
                            String.valueOf(this.callable.getPing()));
                    // 15.009459 = 15ms
                } else {
                    this.ping = 0;
                }

                Thread.sleep(5000L);
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }

    }



}