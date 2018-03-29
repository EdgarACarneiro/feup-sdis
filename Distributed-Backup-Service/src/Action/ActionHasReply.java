package Action;

import Messages.Message;

/**
 * Abstract class representing classes that must read other Peer's reply
 */
public abstract class ActionHasReply extends Action {

    public abstract void checkResponse(Message msg);

}
