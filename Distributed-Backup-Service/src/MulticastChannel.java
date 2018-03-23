import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
     * @param addr The Inet Address to be used in the communication
     * @param port The Port to be used in the communication
     */
    protected MulticastChannel(String addr, int port) {
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
}