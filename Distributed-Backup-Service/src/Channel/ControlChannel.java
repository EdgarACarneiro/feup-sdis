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
     * @param peer The peerID of the Main.Peer associated to the channel
     */
    public ControlChannel(String channelName, Main.Peer peer) {
        super(channelName, peer);
    }
}
