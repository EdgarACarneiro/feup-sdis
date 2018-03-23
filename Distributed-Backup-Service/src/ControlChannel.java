/**
 * Class to implement the Control channel, using a multicast channel
 */
public class ControlChannel extends MulticastChannel {

    /**
     * Control channel constructor.
     * Defines multicast communication settings used.
     */
    public ControlChannel() {
        super("224.0.0.3", 8888);
    }
}
