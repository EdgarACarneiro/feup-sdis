package Channel;

/**
 * Class to implement the RestoreAction channel, using a multicast channel
 */
public class RestoreChannel extends MulticastChannel {

    /**
     * RestoreAction channel constructor.
     * Defines multicast communication settings used.
     *
     * @param channelName The channel name used to build the RestoreAction channel
     * @param peer The peerID of the Main.Peer associated to the channel
     */
    public RestoreChannel(String channelName, Main.Peer peer) {
        super(channelName, peer);
    }
}
