/**
 * Class to implement the Restore channel, using a multicast channel
 */
public class RestoreChannel extends MulticastChannel {

    /**
     * Restore channel constructor.
     * Defines multicast communication settings used.
     */
    public RestoreChannel() {
        super("224.0.0.5", 8888);
    }
}
