package Channel;

import Messages.Message;
import Messages.MessageHandler;
import Utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Generic class representing a multicast network address, used for communication between peers
 */
public abstract class MulticastChannel implements Runnable{

    /**
     * The maximum size of a chunk ( Header + Body)
     */
    private static final int CHUNK_MAXIMUM_SIZE = 65000;

    /**
     * The identifier of the Peer associated to the channel
     */
    private int peerID;

    /**
     * Inet Address used in the communication
     */
    private InetAddress inetAddr;

    /**
     * Port used in the communication
     */
    private int port;

    /**
     * The multicast socket used
     */
    private MulticastSocket socket;

    /**
     * Multicast Network unique Constructor.
     *
     * @param channelName The Communication address and Port, using a string
     * @param peerID The peerID of the Peer associated to the channel
     */
    public MulticastChannel(String channelName, int peerID) {

        String addr = extractAddr(channelName);
        int port = extractPort(channelName);

        try {
            inetAddr = InetAddress.getByName(addr);
            this.port = port;
        } catch (java.net.UnknownHostException e) {
            Utils.showError("Unable to recognize host given to initialize network", this.getClass());
        }

        try {
            socket = new MulticastSocket(port);

            //Joint the Multicast group
            socket.joinGroup(inetAddr);
        }
        catch (java.io.IOException e) {
            Utils.showError("Failed to join multicast channel", this.getClass());
        }
    }

    /**
     * Getter for multicast network inet adress
     *
     * @return multicast network inet adress
     */
    public InetAddress getInetAddr() {
        return inetAddr;
    }

    /**
     * Getter for multicast network port
     *
     * @return multicast network port
     */
    public int getPort() {
        return port;
    }

    /**
     * Extract the adress for a channel, from a given String
     *
     * @param channelName The String to be parsed
     * @return The resultant adress
     */
    private String extractAddr(String channelName) {
        String[] nameParts = channelName.split(":");
        return nameParts[0];
    }

    /**
     * Extract the port for a channel, from a given String
     *
     * @param channelName The String to be parsed
     * @return The resultant port
     */
    private int extractPort(String channelName) {
        String[] nameParts = channelName.split(":");
        return Integer.parseInt(nameParts[1]);
    }

    @Override
    public void run() {
        byte[] buf = new byte[CHUNK_MAXIMUM_SIZE];

        // Create a new Multicast socket (that will allow other sockets/programs to join it as well.
        try {
            while (true) {
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                socket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);
                System.out.println("Received msg: " + msg);
                Message result = MessageHandler.messageHandler(msg);
                // TODO - Do sth with the resultant msg or mby let the msg itself trigger the action
            }
        } catch (IOException ex) {
            Utils.showError("Failed to receive messages using multicast channel", this.getClass());
        }
    }

    public void sendMessage(String msg) {
        try {
            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                    msg.getBytes().length, inetAddr, port);
            socket.send(msgPacket);

            System.out.println("Sent packet with msg: " + msg);
            Thread.sleep(500);

        } catch (IOException | java.lang.InterruptedException ex) {
            Utils.showError("Failed to send message through multicast channel", this.getClass());
        }
    }
}