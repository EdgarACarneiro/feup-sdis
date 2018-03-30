package Action;

import Channel.ControlChannel;
import Messages.Message;
import Messages.RemovedMsg;
import Utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Action used to begin a back up. It also handles the other Peer's answers.
 */
public class TriggerReclaimAction extends Action {

    /**
     * Maximum number of cycles the Action will execute in order to make all the chunks
     * replication degree equivalent to the desired replication degree
     */
    private final static int MAXIMUM_NUM_CYCLES = 5;

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private ControlChannel controlChannel;

    /**
     * Thread Pool useful for running scheduled check loops
     */
    private ScheduledThreadPoolExecutor sleepThreadPool;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The thread waiting time for checking chunks RD, in mili seconds
     */
    private int waitCheckTime = 1000;

    /**
     * The sender peer ID
     */
    private int senderID;

    /**
     * Max File ocupation size
     */
    private long maxKBytes;

    /**
     * The file identifier for the file to be backed up
     */
    private long shrinkSize;

    /**
     * Trigger Reclaim Action Constructor
     * 
     * @param controlChannel The channel used to communicate the desired course of action
     * @param protocolVersion The protocol version used
     * @param senderID The identifier of the sender peer
     * @param maxKBytes Max KBytes to be used
     */
    public TriggerReclaimAction(ControlChannel controlChannel, float protocolVersion, int senderID, String maxKBytes) {
        this.controlChannel = controlChannel;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        if ( Long.parseLong(maxKBytes) < getFreeSpace())
            this.maxKBytes = Long.parseLong(maxKBytes);
        else {
            Utils.showError("Not enough space!", TriggerReclaimAction.class);
        }

        sleepThreadPool = new ScheduledThreadPoolExecutor(MAXIMUM_NUM_CYCLES);
    }

    /**
     * Send the information about the removal of X given Chunks
     *
     * @param chunkNum Number of the chunk to be backed up
     */
    private void requestRemoved(int chunkNum, String fileID) {
        try {
            controlChannel.sendMessage(
                new RemovedMsg(protocolVersion, senderID, fileID, chunkNum).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        }
    }

    @Override
    public void run() {
        System.out.println("Reclaim redifined max disk usage to " + this.maxKBytes + " and occupied space was " + getCurrentUsage());
        
		if (getCurrentUsage() > this.maxKBytes) {
            shrinkSize = getCurrentUsage() - this.maxKBytes;
			System.out.println("Storage space will be shrunk down by " + shrinkSize);
        }
        
        File dir = new File(System.getProperty("user.dir"));
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().equals(FileManager.BASE_DIRECTORY_NAME + senderID)){
                    File newDir = new File(dir, child.getName());

                    File newchild = newDir.listFiles()[0];
                    
                    if (newchild.isDirectory()){
                        shrinkSize -= findSize(newchild)/1000;
                        Utils.log("DELETING " + newchild.getName() + "...");

                        if (deleteFolder(newchild)){
                            Utils.log("DELETED " + newchild.getName() + "!");
                        }
                        else
                            Utils.showError("FAILED TO DELETE " + newchild.getName(), DeleteAction.class);
                    }
                }
            }
        }

        sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Class used to implement the Check loop for the action.
     * A class was used instead of a method, in order to implement it with ScheduledThreadPoolExecutor
     */
    private class Repeater implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(waitCheckTime);
            } catch (java.lang.InterruptedException e) {
                Utils.showError("Unable to wait " + waitCheckTime + "mili seconds to proceed. Proceeding now.", this.getClass());
            }

            sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Get Current Usage
     */
    private long getCurrentUsage() {
        File dir = new File(System.getProperty("user.dir"));
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().equals(FileManager.BASE_DIRECTORY_NAME + senderID)){
                    File newDir = new File(dir, child.getName());
                    return findSize(newDir);
                }
            }
        }
        return -1;
    }

    /**
     * Get Free Space
     */
    private long getFreeSpace() {
        File dir = new File(System.getProperty("user.dir"));
        
        return dir.getFreeSpace();
    }

    /**
	 * Deletes a Folder with files in it
	 *
	 * @param folder folder to be deleted
	 * @return Boolean with success or not 
	 */
    public boolean deleteFolder(File folder){
        File[] directoryListing = folder.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                child.delete();
                requestRemoved(Integer.parseInt(child.getName()), folder.getName());
            }
        }
        return folder.delete();
    }

    /**
	 * Get directory size
	 *
	 * @param folder folder to be deleted
	 * @return Boolean with success or not 
	 */
    public long findSize(File file) { 
        long totalSize = 0;
        ArrayList<String> directory = new ArrayList<String>();
        
        if(file.isDirectory()) { 
           directory.add(file.getAbsolutePath());
           while (directory.size() > 0) {
              String folderPath = directory.get(0);
              directory.remove(0);
              File folder = new File(folderPath);
              File[] filesInFolder = folder.listFiles();
              int noOfFiles = filesInFolder.length;
              
              for(int i = 0 ; i < noOfFiles ; i++) { 
                 File f = filesInFolder[i];
                 if(f.isDirectory()) { 
                    directory.add(f.getAbsolutePath());
                 } else { 
                    totalSize+=f.length();
                 } 
              } 
           } 
        } else { 
           totalSize = file.length();
        } 
        System.out.print("DELETED " + totalSize + " BYTES");
        return totalSize;
     }
}