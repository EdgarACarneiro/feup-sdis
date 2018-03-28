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
     * @param peerID The peerID of the Peer associated to the channel
     */
    public RestoreChannel(String channelName, int peerID) {
        super(channelName, peerID);
    }
}
