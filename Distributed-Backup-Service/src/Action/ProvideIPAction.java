package Action;

import Channel.ControlChannel;
import Messages.Message;
import Messages.SetTCPIP;
import Utils.Utils;

public class ProvideIPAction extends Action {
    
    /**
     * The channel used to communicate with other peers, regarding restore information
     */
    private ControlChannel controlChannel;

    /**
     * The sender peer ID
     */
    private int peerID;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;
    
    public ProvideIPAction(ControlChannel controlChannel, int peerID, Message message) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        this.protocolVersion = message.getProtocolVersion();
        this.fileID = message.getFileID();
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
