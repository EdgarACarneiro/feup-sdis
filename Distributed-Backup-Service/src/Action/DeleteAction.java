package Action;

import Messages.Message;

import java.io.File;

public class DeleteAction extends Action {

    private String fileID;

    public DeleteAction(Message message) {
        fileID = message.getFileID();
    }

    @Override
    public void run() {
        File dir = new File(System.getProperty("user.dir"));
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().substring(0, Math.min(6, child.getName().length())).equals("backup")){
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
