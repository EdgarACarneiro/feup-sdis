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
     */
    public BackupChannel(String channelName) {
        super(channelName);
    }
}
