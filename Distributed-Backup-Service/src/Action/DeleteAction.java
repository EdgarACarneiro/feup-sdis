package Action;

import Messages.Message;
import Utils.FileManager;
import Utils.Utils;

import java.io.File;

public class DeleteAction extends Action {

    private String fileID;

    private int peerID;

    public DeleteAction(Message message, int peerID) {
        fileID = message.getFileID();
        this.peerID = peerID;
    }

    @Override
    public void run() {
        File[] backupFiles = FileManager.getPeerBackups(peerID);
        if (backupFiles == null)
            Utils.showError("Failed to get Peer backup files", this.getClass());

        for (File backupFile : backupFiles) {
            System.out.println("NEW CHILD: " + backupFile.getName());

            if (backupFile.isDirectory() && backupFile.getName().equals(fileID)) {
                System.out.println("DELETING " + backupFile.getName() + "...");

                if (Utils.deleteFolder(backupFile))
                    System.out.println("SUCCESSFULLY DELETED " + backupFile.getName() + "!");
                else
                    System.out.println("FAILED");
            }
        }
    }
}
