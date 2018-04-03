package Action;

import Channel.ControlChannel;
import Channel.RestoreChannel;
import Database.BackedUpFiles;
import Main.Peer;
import Messages.ChunkMsg;
import Messages.GetTCPIP;
import Messages.GetchunkMsg;
import Messages.Message;
import Utils.*;

import java.io.File;
import java.util.HashMap;

public class TriggerRestoreAction extends ActionHasReply {

    /**
     * directory were all the restore files will be
     */
    private static final String RESTORE_DIRECTORY = "Restored Files";

    /**
     * The channel used to communicate with other peers, regarding control messages
     */
    private ControlChannel controlChannel;

    /**
     * The channel used to communicate with other peers, regarding restore messages
     */
    private RestoreChannel restoreChannel;

    /**
     * The backed up files container associated to the peer triggering this action
     * It is important to store this, for later indicating if the file was successfully backed up
     */
    private BackedUpFiles backedUpFiles;

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
     * ArrayList containing the file correspondent chunks
     */
    private HashMap<Integer, byte[]> chunks = new HashMap<>();


    /**
     * Trigger Restore Action constructor
     *
     * @param peer The peer associated to the restore action
     * @param protocolVersion The protocol version being used
     * @param senderID The identifier of the sender peer
     * @param file The file to be restored
     */
    public TriggerRestoreAction(Peer peer, float protocolVersion, int senderID, String file) {
        this.controlChannel = peer.getControlChannel();
        this.restoreChannel = peer.getRestoreChannel();
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;

        this.fileID = FileManager.genFileID(file);
        backedUpFiles = peer.getBackedUpFiles();
        if (! backedUpFiles.hasFileBackedUp(fileID))
            throw new ExceptionInInitializerError();
    }

    @Override
    public void run() {
        if (protocolVersion == 1.0) {
            for (int i = 0; i < backedUpFiles.getNumChunks(fileID); ++i) {
                try {
                    controlChannel.sendMessage(
                        new GetchunkMsg(protocolVersion, senderID, fileID, i).genMsg()
                    );
                } catch (ExceptionInInitializerError e) {
                    Utils.showError("Failed to build message, stopping delete action", this.getClass());
                    return;
                }
            }
        } else if (protocolVersion == 2.0) {
            try {
                controlChannel.sendMessage(
                    new GetTCPIP(protocolVersion, senderID, fileID).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping restore action", this.getClass());
                return;
            }
        }
    }

    @Override
    public void parseResponse(Message msg) {
        if (! msg.getFileID().equals(fileID))
            return;

        ChunkMsg realMsg = (ChunkMsg) msg;
        chunks.put(realMsg.getChunkNum(), realMsg.getChunk());

        if (chunks.size() == backedUpFiles.getNumChunks(fileID)) {
            String restoreDir = FileManager.getFileDirectory(senderID, RESTORE_DIRECTORY);
            new File(restoreDir).mkdirs();

            if (FileManager.createFile(chunks, restoreDir, backedUpFiles.getFileName(fileID)) ) {
                Utils.showSuccess("Succesfully restored file: " + backedUpFiles.getFileName(fileID));
                restoreChannel.unsubscribeAction(this);
            } else
                Utils.showError("Failed to restore file, due to errors on file outputing.", this.getClass());
        }
    }
}
