package Action;

import Messages.CheckDeleteMsg;
import Messages.DeleteMsg;
import Messages.Message;
import Utils.FileManager;
import Utils.Utils;

import java.io.File;

import Channel.ControlChannel;
import Database.ChunksRecorder;

public class DeleteAfterCheckAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding restore information
     */
    private ControlChannel controlChannel;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * Data Structure to get update after eliminating chunks, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    public DeleteAfterCheckAction(ChunksRecorder peerStoredChunks, ControlChannel controlChannel, int peerID, CheckDeleteMsg message) {
        this.peerStoredChunks = peerStoredChunks;
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        this.fileID = message.getFileID();
        this.protocolVersion = message.getProtocolVersion();
    }

    /**
     * Send the message to check if file has been deleted
     *
     * @param fileID Name of File to check
     */
    private void sendDelete() {
        try {
            controlChannel.sendMessage(
                new DeleteMsg(protocolVersion, peerID, fileID).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showError("Failed to build message, stopping delete action", this.getClass());
        }
    }

    @Override
    public void run() {
        if (peerStoredChunks.wasDeleted(fileID))
            sendDelete();
    }
}
