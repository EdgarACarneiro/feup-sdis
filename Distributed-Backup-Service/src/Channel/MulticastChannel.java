package Channel;

import Action.ActionHasReply;
import Main.Peer;
import Messages.MessageHandler;
import Utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

/**
 * Generic class representing a multicast network address, used for communication between peers
 */
public abstract class MulticastChannel implements Runnable{

    /**
     * The maximum size of a chunk ( Header + Body) : 64K (1024B)
     */
    private static final int CHUNK_MAXIMUM_SIZE = 65535;

    /**
     * ArrayList containing the subscribed Actions to the channel
     */
    private ArrayList<ActionHasReply> subscribedActions = new ArrayList<>();

    /**
     * The associated Peer to this multicast channel
     */
    private Peer peer;

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
     * @param peer The peerID of the Main.Peer associated to the channel
     */
    public MulticastChannel(String channelName, Main.Peer peer) {
        this.peer= peer;

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
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                socket.receive(msgPacket);

                String msg = new String(buf, 0, msgPacket.getLength());
                peer.getThreadPool().executeThread(
                        new MessageHandler(peer.getControlChannel(), peer.getPeerID(),
                                subscribedActions, MessageHandler.messageInterpreter(msg))
                );
            }
        } catch (IOException ex) {
            Utils.showError("Failed to receive messages using multicast channel", this.getClass());
        }
    }

    public void sendMessage(byte[] msg) {
        try {
            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg,
                    msg.length, inetAddr, port);
            socket.send(msgPacket);

            System.out.println("Sent packet with msg: " + new String(msg, 0, 8));

        } catch (IOException  ex) {
            Utils.showError("Failed to send message through multicast channel", this.getClass());
            ex.printStackTrace();
        }
    }

    public void subscribeAction(ActionHasReply action) {
        subscribedActions.add(action);
    }
}