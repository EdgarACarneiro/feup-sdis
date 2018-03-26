import Channel.MulticastChannel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class MulticastClient {

    /**
     * The multicast socket used
     */
    private MulticastSocket socket;

    /**
     * The multicast channel being used for communication
     */
    private MulticastChannel channel;

    public MulticastClient(MulticastChannel channel) {
        this.channel = channel;

        try {
            socket = new MulticastSocket(channel.getPort());

            //Joint the Multicast group
            socket.joinGroup(channel.getInetAddr());
        }
        catch (java.io.IOException e) {
            Utils.Utils.showError("Failed to join multicast channel", this.getClass());
        }
    }

    public void receiveLoop() {
        byte[] buf = new byte[256];

        // Create a new Multicast socket (that will allow other sockets/programs to join it as well.
        try {
            while (true) {
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                socket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);
                System.out.println("Received msg: " + msg);
            }
        } catch (IOException ex) {
            Utils.Utils.showError("Failed to receive messages using multicast channel", this.getClass());
        }
    }

    public void sendMessage(String msg) {
        try {
            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                    msg.getBytes().length, channel.getInetAddr(), channel.getPort());
            socket.send(msgPacket);

            System.out.println("Sent packet with msg: " + msg);
            Thread.sleep(500);

        } catch (IOException | java.lang.InterruptedException ex) {
            Utils.Utils.showError("Failed to send message through multicast channel", this.getClass());
        }
    }
}
