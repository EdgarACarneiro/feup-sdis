package Channel;

/**
 * Class to implement the Control channel, using a multicast channel
 */
public class ControlChannel extends MulticastChannel {

    /**
     * Control channel constructor.
     * Defines multicast communication settings used.
     *
     * @param channelName The channel name used to build the Control channel
     * @param peerID The peerID of the Peer associated to the channel
     */
    public ControlChannel(String channelName, int peerID) {
        super(channelName, peerID);
    }
}
