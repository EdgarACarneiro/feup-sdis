package Action;

import Channel.BackupChannel;
import Main.ChunksRecorder;
import Main.Peer;
import Messages.Message;
import Messages.PutchunkMsg;
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
public class TriggerRemovedAction extends Action {

    /**
     * The starting  waiting time for checking chunks RD, in mili seconds
     */
    private final static int STARTING_WAIT_TIME = 400;

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private BackupChannel backupChannel;

    /**
     * Thread Pool useful for running scheduled check loops
     */
    private ScheduledThreadPoolExecutor sleepThreadPool;

    /**
     * The thread waiting time for checking chunks RD, in mili seconds
     */
    private int waitCheckTime = STARTING_WAIT_TIME;

    /**
     * The peer associated to this action
     * It is important to store the peer, for later indicating to the peer if the file was successfully backed up
     */
    private RemovedMsg message;

    /**
     * Data Structure to get information from, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The sender peer ID
     */
    private int peerID;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The desired replication degree of the file
     */
    private int repDegree;

    /**
     * Trigger Remove Action Constructor
     *
     * @param backupChannel The channel associated to this action
     * @param record The protocol version used
     * @param peerID The identifier of the sender peer
     * @param removedMsg The chunk number that was deleted
     */
    public TriggerRemovedAction(BackupChannel backupChannel, ChunksRecorder record, int peerID, RemovedMsg removedMsg) {
        this.backupChannel = backupChannel;
        this.peerStoredChunks = record;
        this.peerID = peerID;
        this.message = removedMsg;
        this.protocolVersion = removedMsg.getProtocolVersion();
        this.fileID = removedMsg.getFileID();
        this.chunkNum = removedMsg.getChunkNum();
    }

    /**
     * Send the request to backup the given file chunk
     *
     * @param chunkNum Number of the chunk to be backed up
     */
    private void requestBackUp() {
        try {
            backupChannel.sendMessage(
                    new PutchunkMsg(protocolVersion, peerID, fileID, chunkNum, repDegree, new byte[0]).genMsg()//Get chunk?
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        }
    }

    /**
     * Class used to implement the Check loop for the action.
     * A class was used instead of a method, in order to implement it with ScheduledThreadPoolExecutor
     */
    private class Repeater implements Runnable {

        @Override
        public void run() { 
            sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public void run() {
        File[] files = FileManager.getPeerBackups(peerID);

        for (File file : files) {                         
            if (file.isDirectory() && file.getName().equals(fileID)){
                System.out.println("Hmm, you deleted a file from something I have...");

                File fileChunks = new File(file.getPath(), file.getName());
                File[] chunkList = fileChunks.listFiles();

                if (chunkList != null) {
                    for (File chunk : chunkList) {
                        if(Integer.valueOf(chunk.getName()).equals(chunkNum)){
                            System.out.println("IT WAS " + chunkNum);
                            if(true){ //Check if RD is over the minimum
                                sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
                                requestBackUp();
                            }
                        }
                    }
                }
            }
        } 
    }
}