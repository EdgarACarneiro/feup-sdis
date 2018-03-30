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
        if (backupFiles == null) {
            Utils.showError("Failed to get Peer backup files", this.getClass());
        }

        for (File newChild : backupFiles) {
            System.out.println("NEW CHILD: " + newChild.getName());

            if (newChild.isDirectory()){
                if (newChild.getName().equals(fileID)){
                    System.out.println("DELETING " + newChild.getName() + "...");

                    if (deleteFolder(newChild))
                        System.out.println("DELETING " + newChild.getName() + "...");
                    else
                        System.out.println("FAILED");
                }
            }
        }
    }

    private boolean deleteFolder(File folder){
        File[] directoryListing = folder.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                child.delete();
            }
        }
        return folder.delete();
    }
}
