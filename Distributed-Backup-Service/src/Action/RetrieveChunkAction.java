package Action;

import Channel.RestoreChannel;
import Messages.ChunkMsg;
import Messages.GetchunkMsg;
import Utils.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RetrieveChunkAction extends Action {

    /**
     * Maximum time waited to trigger the Retrieve Action, exclusively.
     */
    private final static int MAX_TIME_TO_SEND = 4001;

    /**
     * The getchunk message that triggered this action
     */
    private GetchunkMsg getchunkMsg;

    /**
     * The directory of this file, where the chunks are
     */
    private String fileDir;

    /**
     * The channel used to communicate with other peers, regarding restore information
     */
    private RestoreChannel restoreChannel;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;

    private byte[] chunk;


    public RetrieveChunkAction (RestoreChannel restoreChannel, int peerID, GetchunkMsg requestMsg) {
        this.restoreChannel = restoreChannel;
        this.peerID = peerID;
        getchunkMsg = requestMsg;

        getChunks();
    }

    private void getChunks() {
        File[] backupFiles = FileManager.getPeerBackups(peerID);
        if (backupFiles == null)
            Utils.showError("Failed to get Peer backup files", this.getClass());

        for (File backupFile : backupFiles) {
            if ( backupFile.isDirectory() && backupFile.getName().equals(getchunkMsg.getFileID()) ) {
                for (File chunk : backupFile.listFiles()) {
                    if ( chunk.getName().equals(Integer.toString(getchunkMsg.getChunkNum())) )
                    try {
                        this.chunk = Files.readAllBytes(chunk.toPath());
                    } catch (java.io.IOException e) {
                        Utils.showWarning("Failed to get chunk Bytes", this.getClass());
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        ScheduledThreadPoolExecutor scheduledThread = new ScheduledThreadPoolExecutor(1);
        scheduledThread.schedule(new Sender(), new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
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
