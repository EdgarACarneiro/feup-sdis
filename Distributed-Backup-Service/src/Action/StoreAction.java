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


    /**
     * Store Action default constructor
     *
     * @param controlChannel The control channel used in communication
     * @param peerStoredChunks The database regarding chunk that were stored in this peer
     * @param ownBackedFiles The database regarding chunks that were backed up from this peer
     * @param peerID The peer identifier
     * @param requestMsg The message containing the request
     */
    public StoreAction (ControlChannel controlChannel, ChunksRecorder peerStoredChunks, BackedUpFiles ownBackedFiles, int peerID, PutchunkMsg requestMsg) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        putchunkMsg = requestMsg;
        this.peerStoredChunks = peerStoredChunks;

        this.fileDir = getFileDirectory(peerID, putchunkMsg.getFileID());

        this.wasStored = storeChunk(ownBackedFiles);
    }

    /**
     * Store a chunk in the database and in the disk
     *
     * @param ownBackedFiles The database regarding chunks that were backed up from this peer
     * @return true if the file was successfully stored
     */
    private boolean storeChunk(BackedUpFiles ownBackedFiles) {
        try {
            String fileID = putchunkMsg.getFileID();
            int chunkNum = putchunkMsg.getChunkNum();

            if (ownBackedFiles.hasFileBackedUp(fileID))
                return false;

            if (peerStoredChunks.addChunkRecord(fileID, chunkNum, putchunkMsg.getChunk().length, putchunkMsg.getRepDegree())) {
                new File(fileDir).mkdirs();
                FileOutputStream out = new FileOutputStream (fileDir + "/" + chunkNum);
                out.write(putchunkMsg.getChunk());
                out.close();
            }
            return true;

        } catch (java.io.IOException e) {
            Utils.showError("Failed to save chunk in disk", this.getClass());
        }
        return false;
    }

    @Override
    public void run() {
        if (wasStored) {
            ScheduledThreadPoolExecutor scheduledThread = new ScheduledThreadPoolExecutor(1);
            scheduledThread.schedule(() ->{
                try {
                    controlChannel.sendMessage(
                            new StoredMsg(putchunkMsg.getProtocolVersion(), peerID,
                                    putchunkMsg.getFileID(), putchunkMsg.getChunkNum()).genMsg()
                    );
                } catch (ExceptionInInitializerError e) {
                    Utils.showError("Failed to build message, stopping Store action", this.getClass());
                }
            }, new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
        }
    }
}
