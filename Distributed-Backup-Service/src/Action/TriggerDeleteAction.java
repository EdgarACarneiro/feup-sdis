package Action;

import Channel.ControlChannel;
import Messages.DeleteMsg;
import Utils.*;

public class TriggerDeleteAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private ControlChannel controlChannel;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The sender peer ID
     */
    private int senderID;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * Trigger Delete Action constructor
     *
     * @param controlChannel The control channel used for communication
     * @param protocolVersion The protocol version being used
     * @param senderID The sender peer identifier
     * @param file The file to be deleted
     */
    public TriggerDeleteAction(ControlChannel controlChannel, float protocolVersion, int senderID, String file) {
        this.controlChannel = controlChannel;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.fileID = FileManager.genFileID(file);
    }

    @Override
    public void run() {
        try {
            controlChannel.sendMessage(
                new DeleteMsg(protocolVersion, senderID, fileID).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showError("Failed to build message, stopping delete action", this.getClass());
        }
    }
}