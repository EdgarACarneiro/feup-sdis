package Action;

import Channel.ControlChannel;
import Database.BackedUpFiles;
import Database.ChunksRecorder;
import Messages.PutchunkMsg;
import Messages.StoredMsg;
import Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static Utils.FileManager.getFileDirectory;

public class StoreEnhAction extends Action {

    /**
     * Maximum time waited to trigger the Store Action, exclusively.
     * It has time to received the normal protocol first.
     */
    private final static int MAX_TIME_TO_SEND = 8000;

    /**
     * The putchunk message that triggered this action
     */
    private PutchunkMsg putchunkMsg;

    /**
     * The directory of this file, where the chunks are
     */
    private String fileDir;

    /**
     * The channel used to communicate with other peers, regarding control information
     */
    private ControlChannel controlChannel;

    /**
     * Data Structure to be updated by this action, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    /**
     * Data structure that has information regarding the Peer running this action, own backed up files
     */
    private BackedUpFiles ownBackedFiles;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;


    public StoreEnhAction (ControlChannel controlChannel, ChunksRecorder peerStoredChunks, BackedUpFiles ownBackedFiles, int peerID, PutchunkMsg requestMsg) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        putchunkMsg = requestMsg;
        this.peerStoredChunks = peerStoredChunks;
        this.ownBackedFiles = ownBackedFiles;

        this.fileDir = getFileDirectory(peerID, putchunkMsg.getFileID());

        peerStoredChunks.initChunkRecord(putchunkMsg.getFileID(), putchunkMsg.getChunkNum(), putchunkMsg.getChunk().length, putchunkMsg.getRepDegree());
    }

    private void storeChunk() {
        try {
            String fileID = putchunkMsg.getFileID();
            int chunkNum = putchunkMsg.getChunkNum();

            if (peerStoredChunks.incChunkRecord(fileID, chunkNum, putchunkMsg.getSenderID())) {
                new File(fileDir).mkdirs();
                FileOutputStream out = new FileOutputStream (fileDir + "/" + chunkNum);
                out.write(putchunkMsg.getChunk());
                out.close();
            }

        } catch (java.io.IOException e) {
            Utils.showError("Failed to save chunk in disk", this.getClass());
        }
    }

    @Override
    public void run() {
        if (ownBackedFiles.hasFileBackedUp(putchunkMsg.getFileID()))
            return;

        ScheduledThreadPoolExecutor scheduledThread = new ScheduledThreadPoolExecutor(1);
        scheduledThread.schedule(() -> {
            if (peerStoredChunks.getChunkRD(putchunkMsg.getFileID(), putchunkMsg.getChunkNum()) < putchunkMsg.getRepDegree()) {
                try {
                    controlChannel.sendMessage(
                            new StoredMsg(putchunkMsg.getProtocolVersion(), peerID,
                                    putchunkMsg.getFileID(), putchunkMsg.getChunkNum()).genMsg()
                    );
                    storeChunk();

                } catch (ExceptionInInitializerError e) {
                    Utils.showError("Failed to build message, stopping Store action", this.getClass());
                }
            } else {
                peerStoredChunks.removeChunk(putchunkMsg.getFileID(), putchunkMsg.getChunkNum());
            }
        }, new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
    }
}
