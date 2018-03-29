package Action;

import Channel.ControlChannel;
import Messages.PutchunkMsg;
import Messages.StoredMsg;

public class StoreAction extends Action {

    /**
     * The application protocol version
     */
    protected float protocolVersion;

    /**
     * The id of the peer that is going to send the message
     */
    protected int senderID;

    /**
     * The file identifier of the file the chunk belongs to
     */
    protected String fileID;

    /**
     * The channel used to communicate with other peers, regarding control information
     */
    private ControlChannel controlChannel;

    public StoreAction (ControlChannel controlChannel, int peerID, PutchunkMsg requestMsg) {
        this.controlChannel = controlChannel;
        this.protocolVersion = requestMsg.getProtocolVersion();
        this.senderID = requestMsg.getSenderID();
        this.fileID = requestMsg.getFileID();
    }

    public void run() {

        try {
            controlChannel.sendMessage(
                    new StoredMsg(protocolVersion, senderID, fileID, chunkNo).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showError("Failed to build message, stopping backup action", this.getClass());
            return;
        }
    }

}
