package Action;

import Messages.Message;

/**
 * Abstract class representing classes that must read other Peer's reply
 */
public abstract class ActionHasReply extends Action {

    /**
     * Do something with the message to a action previously made by this class
     *
     * @param msg Msg to be interpreted
     */
    public abstract void parseResponse(Message msg);

}
