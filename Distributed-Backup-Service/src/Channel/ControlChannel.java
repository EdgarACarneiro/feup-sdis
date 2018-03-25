package Channel;

import Channel.MulticastChannel;

/**
 * Class to implement the Control channel, using a multicast channel
 */
public class ControlChannel extends MulticastChannel {

    /**
     * Control channel constructor.
     * Defines multicast communication settings used.
     *
     * @param channelName The channel name used to build the Control channel
     */
    public ControlChannel(String channelName) {
        super(channelName);
    }
}
