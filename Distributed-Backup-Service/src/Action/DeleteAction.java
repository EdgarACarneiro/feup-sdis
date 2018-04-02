package Action;

import Database.ChunksRecorder;
import Messages.DeleteMsg;
import Messages.Message;
import Utils.FileManager;
import Utils.Utils;
import sun.security.ssl.ProtocolVersion;

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

        if (chunks == null && protocolVersion==1)
            return;

        File[] backupFiles = FileManager.getPeerBackups(peerID);
        if (backupFiles == null)
            Utils.showError("Failed to get Peer backup files", this.getClass());

        for (File backupFile : backupFiles) {
            System.out.println("NEW CHILD: " + backupFile.getName());

            if (backupFile.isDirectory() && backupFile.getName().equals(fileID)) {
                System.out.println("DELETING " + backupFile.getName() + "...");

                if (Utils.deleteFolder(backupFile)) {
                    System.out.println("SUCCESSFULLY DELETED " + backupFile.getName() + "!");
                    peerStoredChunks.removeFile(fileID);
                }
                else
                    System.out.println("FAILED");
            }
        }
    }
}
