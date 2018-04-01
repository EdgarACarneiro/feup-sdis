package Action;

import Channel.ControlChannel;
import Main.ChunksRecorder;
import Messages.PutchunkMsg;
import Messages.StoredMsg;
import Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static Utils.FileManager.getFileDirectory;

public class StoreAction extends Action {

    /**
     * Maximum time waited to trigger the Store Action, exclusively.
     */
    private final static int MAX_TIME_TO_SEND = 4000;

    /**
     * The putchunk message that triggered this action
     */
    private PutchunkMsg putchunkMsg;

    /**
     * The directory of this file, where the chunks are
     */
    private String fileDir;

    /**
     * Boolean indicating if the chunk was successfully stored
     */
    private boolean wasStored;

    /**
     * The channel used to communicate with other peers, regarding control information
     */
    private ControlChannel controlChannel;

    /**
     * Data Structure to be updated by this action, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;


    public StoreAction (ControlChannel controlChannel, ChunksRecorder peerStoredChunks, int peerID, PutchunkMsg requestMsg) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        putchunkMsg = requestMsg;
        this.peerStoredChunks = peerStoredChunks;

        this.fileDir = getFileDirectory(peerID, putchunkMsg.getFileID());
        new File(fileDir).mkdirs();

        this.wasStored = storeChunk();
    }

    private boolean storeChunk() {
        try {
            String fileID = putchunkMsg.getFileID();
            int chunkNum = putchunkMsg.getChunkNum();

            FileOutputStream out = new FileOutputStream (fileDir + "/" + chunkNum);
            out.write(putchunkMsg.getChunk());
            out.close();

            if (! peerStoredChunks.hasChunk(fileID, chunkNum) ) {
                peerStoredChunks.updateChunks(fileID, chunkNum);
                return true;
            } else
                return false;

        } catch (java.io.IOException e) {
            Utils.showError("Failed to save chunk in disk", this.getClass());
            return false;
        }
    }

    @Override
    public void run() {
        if (wasStored) {
            ScheduledThreadPoolExecutor scheduledThread = new ScheduledThreadPoolExecutor(1);
            scheduledThread.schedule(new Sender(), new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Class used to Send the Action correspondent message to the channel, using ScheduledThreadPoolExecutor
     */
    private class Sender implements Runnable {

        @Override
        public void run() {
            try {
                controlChannel.sendMessage(
                        new StoredMsg(putchunkMsg.getProtocolVersion(), peerID,
                                putchunkMsg.getFileID(), putchunkMsg.getChunkNum()).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping Store action", this.getClass());
            }
        }
    }

}
