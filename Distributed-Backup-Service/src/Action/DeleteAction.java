package Action;

import Messages.Message;
import Utils.FileManager;

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
        File dir = new File(System.getProperty("user.dir"));
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().equals(FileManager.BASE_DIRECTORY_NAME + peerID)){
                    File newDir = new File(dir, child.getName());
                    System.out.println("DIR: " + newDir.getName());

                    for (File newchild : newDir.listFiles()) {
                        System.out.println("NEW CHILD: " + newchild.getName());

                        if (newchild.isDirectory()){
                            if (newchild.getName().equals(fileID)){
                                System.out.println("DELETING " + newchild.getName() + "...");

                                if (deleteFolder(newchild))
                                    System.out.println("DELETING " + newchild.getName() + "...");
                                else
                                    System.out.println("FAILED");
                            }
                        }
                    }
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
