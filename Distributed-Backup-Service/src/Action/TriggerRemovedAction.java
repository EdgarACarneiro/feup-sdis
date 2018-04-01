package Action;

import Channel.BackupChannel;
import Main.ChunksRecorder;
import Main.Peer;
import Messages.Message;
import Messages.PutchunkMsg;
import Messages.RemovedMsg;
import Utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Action used to begin a back up. It also handles the other Peer's answers.
 */
public class TriggerRemovedAction extends ActionHasReply {

    /**
     * Maximum time waited to trigger the Retrieve Action, exclusively.
     */
    private final static int MAX_TIME_TO_SEND = 4000;

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private BackupChannel backupChannel;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The sender peer ID
     */
    private int peerID;

     /**
     * The Peer
     */
    private Peer peer;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The chunk number
     */
    private File removedChunk;

    /**
     * The desired replication degree of the file
     */
    private int repDegree;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture test;

    /**
     * Trigger Remove Action Constructor
     *
     * @param backupChannel The channel associated to this action
     * @param record The protocol version used
     * @param peerID The identifier of the sender peer
     * @param removedMsg The chunk number that was deleted
     */
    public TriggerRemovedAction(Peer peer,BackupChannel backupChannel, ChunksRecorder record, int peerID, RemovedMsg removedMsg) {
        this.peer = peer;
        this.backupChannel = backupChannel;
        this.peerID = peerID;
        this.protocolVersion = removedMsg.getProtocolVersion();
        this.fileID = removedMsg.getFileID();
        this.chunkNum = removedMsg.getChunkNum();
    }

    /**
     * Send the request to backup the given file chunk
     *
     * @param chunkNum Number of the chunk to be backed up
     */
    private void requestBackUp(File chunk) {
        try {
            backupChannel.sendMessage(
                    new PutchunkMsg(protocolVersion, peerID, fileID, chunkNum, repDegree, Files.readAllBytes(chunk.toPath())).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        } catch (IOException e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        }
    }

    /**
     * Class used to implement the Check loop for the action.
     * A class was used instead of a method, in order to implement it with ScheduledThreadPoolExecutor
     */
    private class Sender implements Runnable {

        @Override
        public void run() { 
            peer.getBackedUpFiles().incRepDegree(fileID, chunkNum);
            requestBackUp(removedChunk);
        }

    }


    @Override
    public void run() {
        peer.getBackedUpFiles().decRepDegree(fileID, chunkNum);

        File[] files = FileManager.getPeerBackups(peerID);

        for (File file : files) {                         
            if (file.isDirectory() && file.getName().equals(fileID)){
                System.out.println("Hmm, you deleted a file from something I have...");
                
                File fileChunks = new File(file.getParentFile(), file.getName());
                File[] chunkList = fileChunks.listFiles();
                System.out.println("fileChunks " + fileChunks.getName());
                
                if (chunkList != null) {
                    for (File chunk : chunkList) {
                        if(Integer.valueOf(chunk.getName()).equals(chunkNum)){
                            this.removedChunk = chunk;
                            System.out.println("IT WAS " + chunkNum);
                            if(true){ //Check if RD is over the minimum
                                    test = scheduler.schedule(new Sender(), new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
                            }
                        }
                    }
                }
            }
        } 
    }

    @Override
    public void parseResponse(Message msg) {
        if (! (msg instanceof PutchunkMsg))
            return;

        PutchunkMsg realMsg = (PutchunkMsg) msg;
        if ((realMsg.getFileID().equals(fileID)) &&
            (realMsg.getChunkNum() == chunkNum)) {
            test.cancel(true);
        }
    }
}