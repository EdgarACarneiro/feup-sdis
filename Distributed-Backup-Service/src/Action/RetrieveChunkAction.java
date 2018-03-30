package Action;

import Channel.ControlChannel;
import Channel.RestoreChannel;
import Messages.GetchunkMsg;
import Messages.StoredMsg;
import Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static Utils.FileManager.geFileDirectory;

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


    public RetrieveChunkAction (RestoreChannel restoreChannel, int peerID, GetchunkMsg requestMsg) {
        this.restoreChannel = restoreChannel;
        this.peerID = peerID;
        getchunkMsg = requestMsg;

        this.fileDir = geFileDirectory(peerID, getchunkMsg.getFileID());
        new File(fileDir).mkdirs();

        storeChunk();
    }

    private void storeChunk() {
        try {
            FileOutputStream out = new FileOutputStream (fileDir + "/" + getchunkMsg.getChunkNum());
            out.write(getchunkMsg.getChunk(), 0, getchunkMsg.getChunk().length);
        } catch (java.io.IOException e) {
            Utils.showError("Failed to save chunk in disk", this.getClass());
        }
    }

    @Override
    public void run() {
        ScheduledThreadPoolExecutor scheduledThread = new ScheduledThreadPoolExecutor(1);
        scheduledThread.schedule(new Sender(), new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
    }

    /**
     * Class used to Send the Action correspondent message to the channel, using ScheduledThreadPoolExecutor
     */
    private class Sender implements Runnable {

        @Override
        public void run() {
            try {
                restoreChannel.sendMessage(
                        new StoredMsg(getchunkMsg.getProtocolVersion(), peerID,
                                getchunkMsg.getFileID(), getchunkMsg.getChunkNum()).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping Store action", this.getClass());
            }
        }
    }

}
