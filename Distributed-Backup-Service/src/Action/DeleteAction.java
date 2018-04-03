package Action;

import Database.ChunksRecorder;
import Messages.DeleteMsg;
import Utils.FileManager;
import Utils.Utils;
import Utils.ProtocolVersions;

import java.io.File;
import java.util.ArrayList;

public class DeleteAction extends Action {

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;


    /**
     * Data Structure to get update after eliminating chunks, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    public DeleteAction(DeleteMsg message, ChunksRecorder peerStoredChunks, int peerID) {
        fileID = message.getFileID();
        this.peerID = peerID;
        this.peerStoredChunks = peerStoredChunks;
        this.protocolVersion = message.getProtocolVersion();
    }

    @Override
    public void run() {
        ArrayList<Integer> chunks = peerStoredChunks.getChunksList(fileID);

        if (chunks == null && protocolVersion == ProtocolVersions.VANILLA_VERSION)
            return;

        File[] backupFiles = FileManager.getPeerBackups(peerID);
        if (backupFiles == null)
            Utils.showError("Failed to get Peer backup files", this.getClass());

        for (File backupFile : backupFiles) {

            if (backupFile.isDirectory() && backupFile.getName().equals(fileID)) {

                if (Utils.deleteFolder(backupFile)) {
                    Utils.log("SUCCESSFULLY DELETED " + backupFile.getName() + "!");
                    Utils.showSuccess("Successfully deleted File!");
                    peerStoredChunks.removeFile(fileID);
                }
                else
                    Utils.log("FAILED TO DELETE "+ backupFile.getName() + "!");
            }
        }
    }
}
