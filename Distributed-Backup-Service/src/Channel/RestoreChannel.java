package Channel;

import Channel.MulticastChannel;

/**
 * Class to implement the Restore channel, using a multicast channel
 */
public class RestoreChannel extends MulticastChannel {

    /**
     * Restore channel constructor.
     * Defines multicast communication settings used.
     *
     * @param channelName The channel name used to build the Restore channel
     */
    public RestoreChannel(String channelName) {
        super(channelName);
    }
}
