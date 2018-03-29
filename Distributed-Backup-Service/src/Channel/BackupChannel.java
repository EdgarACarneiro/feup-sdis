package Channel;

/**
 * Class to implement the Backup channel, using a multicast channel
 */
public class BackupChannel extends MulticastChannel {

    /**
     * Backup channel constructor.
     * Defines multicast communication settings used.
     *
     * @param channelName The channel name used to build the Back up channel
     * @param peer The peerID of the Main.Peer associated to the channel
     */
    public BackupChannel(String channelName, Main.Peer peer) {
        super(channelName, peer);
    }
}
