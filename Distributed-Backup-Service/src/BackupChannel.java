/**
 * Class to implement the Backup channel, using a multicast channel
 */
public class BackupChannel extends MulticastChannel {

    /**
     * Backup channel constructor.
     * Defines multicast communication settings used.
     */
    public BackupChannel() {
        super("224.0.0.4", 8888);
    }
}
