package Action;

import Messages.CheckDeleteMsg;
import Messages.Message;
import Utils.FileManager;
import Utils.Utils;

import java.io.File;

import Channel.ControlChannel;

/**
 * Class implementing the Check Delete Action
 */
public class CheckDeleteAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding restore information
     */
    private ControlChannel controlChannel;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;

    /**
     * Check Delete Action constructor
     *
     * @param controlChannel The control channel used to communicate
     * @param protocolVersion The protocol version used
     * @param peerID The peer identifier
     */
    public CheckDeleteAction(ControlChannel controlChannel, float protocolVersion, int peerID) {
        this.controlChannel = controlChannel;
        this.protocolVersion = protocolVersion;
        this.peerID = peerID;
    }

    /**
     * Send the message to check if file has been deleted
     *
     * @param fileID Name of File to check
     */
    private void checkDelete(String fileID) {
        try {
            controlChannel.sendMessage(
                new CheckDeleteMsg(protocolVersion, peerID, fileID).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        }
    }

    @Override
    public void run() {

        File[] backupFiles = FileManager.getPeerBackups(peerID);
        if (backupFiles == null)
            Utils.showError("Failed to get Peer backup files", this.getClass());

        for (File backupFile : backupFiles) {
            Utils.log("NEW CHILD: " + backupFile.getName());
            checkDelete(backupFile.getName());
        }
    }
}
