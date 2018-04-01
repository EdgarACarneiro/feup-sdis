package Action;

import Messages.Message;
import Messages.SetTCPIP;

public class ProvideIPAction extends Action {
    
    /**
     * The sender peer ID
     */
    private int peerID;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    
    public ProvideIPAction(Message message, int peerID) {
        this.fileID = message.getFileID();
        this.peerID = peerID;
    }

    @Override
    public void run() {
        try {
            controlChannel.sendMessage(
                    new SetTCPIP(protocolVersion, peerID, fileID).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        }
    }
}
