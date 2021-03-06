package net.wurstclient.util.pinger;

import net.minecraft.network.ServerAddress;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class PingerCallable {

    private SocketAddress address;

    PingerCallable(String address) {
        ServerAddress serveraddress = ServerAddress.parse(address);
        this.address = new InetSocketAddress(serveraddress.getAddress(), serveraddress.getPort());
    }

    long getPing() {
        Socket socket = new Socket();

        try {
            long i = System.currentTimeMillis();
            socket.connect(this.address, 1000);
            socket.close();
            return System.currentTimeMillis() - i;
        } catch (IOException e) {
            if(!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ignored) {

                }
            }

            return -1L;
        }
    }

}
