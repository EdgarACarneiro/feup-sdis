package Action;

import Channel.ControlChannel;
import Main.Peer;
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
            long currentUsage = getCurrentUsage();
            System.out.println("Reclaim redifined max disk usage to " + this.maxKBytes + " KBytes and occupied space was " + currentUsage + " KBytes.");
            
            if (currentUsage > this.maxKBytes) {
                shrinkSize = currentUsage - this.maxKBytes;
                System.out.println("Storage space will be shrunk down by at least " + shrinkSize);

                File dir = new File(System.getProperty("user.dir"));
                File[] directoryListing = dir.listFiles();
        
                if (directoryListing != null) {
                    for (File child : directoryListing) {
                        if (child.getName().equals(FileManager.BASE_DIRECTORY_NAME + senderID)){

                            File newDir = new File(dir, child.getName());
                            File[] files = newDir.listFiles();

                            for (File newchild : files) {                         
                                if (newchild.isDirectory() && shrinkSize > 0){
                                    shrinkSize -= Utils.findSize(newchild);
                                    Utils.log("DELETING " + newchild.getName() + "...");
            
                                    if (Utils.deleteFolder(newchild)){
                                        Utils.log("DELETED " + newchild.getName() + "!");
                                    }
                                    else
                                        Utils.showError("FAILED TO DELETE " + newchild.getName(), DeleteAction.class);
                                }
                            } 
                        }
                    }
                }
            } else {
                System.out.println("There was no need to shrink down disk usage!");
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
                    return Utils.findSize(newDir);
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
}