package Action;

import Main.ChunksRecorder;
import Messages.Message;
import Utils.FileManager;
import Utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class DeleteAction extends Action {

    private String fileID;

    private int peerID;

    /**
     * Data Structure to get update after eliminating chunks, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    public DeleteAction(Message message, ChunksRecorder peerStoredChunks, int peerID) {
        fileID = message.getFileID();
        this.peerID = peerID;
    }

    @Override
    public void run() {
        ArrayList<Integer> chunks = peerStoredChunks.getChunks(fileID);

        if (chunks == null)
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
