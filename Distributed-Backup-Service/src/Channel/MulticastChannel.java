package Channel;

import Utils.Utils;
import java.net.InetAddress;

/**
 * Generic class representing a multicast network address, used for communication between peers
 */
public class MulticastChannel {

    /**
     * Inet Address used in the communication
     */
    private InetAddress inetAddr;

    /**
     * Port used in the communication
     */
    private int port;

    /**
     * Multicast Network unique Constructor.
     *
     * @param channelName The Communication address and Port, using a string
     */
    protected MulticastChannel(String channelName) {

        String addr = extractAddr(channelName);
        int port = extractPort(channelName);

        System.out.println(addr);
        System.out.println(port);

        try {
            inetAddr = InetAddress.getByName(addr);
            this.port = port;
        } catch (java.net.UnknownHostException e) {
            Utils.showError("Unable to recognize host given to initialize network", this.getClass());
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
}