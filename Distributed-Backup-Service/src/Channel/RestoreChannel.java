package Channel;

import Channel.MulticastChannel;

/**
 * Class to implement the RestoreAction channel, using a multicast channel
 */
public class RestoreChannel extends MulticastChannel {

    /**
     * RestoreAction channel constructor.
     * Defines multicast communication settings used.
     *
     * @param channelName The channel name used to build the RestoreAction channel
     */
    public RestoreChannel(String channelName) {
        super(channelName);
    }
}
