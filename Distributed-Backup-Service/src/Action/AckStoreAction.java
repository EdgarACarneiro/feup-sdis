package Action;

import Database.BackedUpFiles;
import Database.ChunksRecorder;
import Messages.StoredMsg;

/**
 * Action that acknowledges the receiving af a Stored Action
 */
public class AckStoreAction extends Action {

    /**
     * The database regarding chunks that were backed up from this peer
     */
    private BackedUpFiles peerStoredChunks;

    /**
     * The database regarding chunk that were stored in this peer
     */
    private ChunksRecorder chunksRecord;

    /**
     * The Stored message received
     */
    private StoredMsg msg;

    /**
     * Acknowledge Stored Action constructor
     *
     * @param peerStoredChunks The database regarding chunks that were backed up from this peer
     * @param chunksRecord The database regarding chunk that were stored in this peer
     * @param msg The Stored message received
     */
    public AckStoreAction(BackedUpFiles peerStoredChunks, ChunksRecorder chunksRecord, StoredMsg msg) {
        this.peerStoredChunks = peerStoredChunks;
        this.chunksRecord = chunksRecord;
        this.msg = msg;
    }

    @Override
    public void run() {
        peerStoredChunks.backedChunk(msg.getFileID(), msg.getChunkNum(), msg.getSenderID());
        chunksRecord.incChunkRecord(msg.getFileID(), msg.getChunkNum(), msg.getSenderID());
    }
}
