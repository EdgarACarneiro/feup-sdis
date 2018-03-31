package Action;

import Channel.RestoreChannel;
import Main.ChunksRecorder;
import Messages.ChunkMsg;
import Messages.GetchunkMsg;
import Utils.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RetrieveChunkAction extends Action {

    /**
     * Maximum time waited to trigger the Retrieve Action, exclusively.
     */
    private final static int MAX_TIME_TO_SEND = 4000;

    /**
     * The getchunk message that triggered this action
     */
    private GetchunkMsg getchunkMsg;

    /**
     * Boolean indicating if the chunk was indeed stored, and therefore can be retrieved
     */
    private boolean isStored;

    /**
     * The channel used to communicate with other peers, regarding restore information
     */
    private RestoreChannel restoreChannel;

    /**
     * Data Structure to get information from, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;

    /**
     * Chunks to be resent to the
     */
    private byte[] chunk;


    public RetrieveChunkAction (RestoreChannel restoreChannel, ChunksRecorder peerStoredChunks, int peerID, GetchunkMsg requestMsg) {
        this.restoreChannel = restoreChannel;
        this.peerID = peerID;
        getchunkMsg = requestMsg;
        this.peerStoredChunks = peerStoredChunks;

        isStored = getChunk();
    }

    private boolean getChunk() {
        String fileID = getchunkMsg.getFileID();
        int chunkNum = getchunkMsg.getChunkNum();

        if (! peerStoredChunks.hasChunk(fileID, chunkNum))
            return false;

        try {
            this.chunk = Files.readAllBytes(FileManager.getChunkFile(peerID, fileID, chunkNum));
            return true;

        } catch (java.io.IOException e) {
            Utils.showWarning("Failed to get chunk bytes", this.getClass());
        }

        return false;
    }

    @Override
    public void run() {
        if (isStored) {
            ScheduledThreadPoolExecutor scheduledThread = new ScheduledThreadPoolExecutor(1);
            scheduledThread.schedule(new Sender(), new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Class used to Send the Chunk correspondent message to the channel, using ScheduledThreadPoolExecutor
     */
    private class Sender implements Runnable {

        @Override
        public void run() {
            try {
                restoreChannel.sendMessage(
                    new ChunkMsg(getchunkMsg.getProtocolVersion(), peerID,
                            getchunkMsg.getFileID(), getchunkMsg.getChunkNum(), chunk).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping Store action", this.getClass());
            }
        }
    }

}
