package Channel;

/**
 * Class to implement the TriggerRestoreAction channel, using a multicast channel
 */
public class RestoreChannel extends MulticastChannel {

    /**
     * TriggerRestoreAction channel constructor.
     * Defines multicast communication settings used.
     *
     * @param channelName The channel name used to build the TriggerRestoreAction channel
     * @param peer The peerID of the Main.Peer associated to the channel
     */
    public RestoreChannel(String channelName, Main.Peer peer) {
        super(channelName, peer);
    }
}
