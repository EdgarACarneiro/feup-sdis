package Action;

import Database.BackedUpFiles;
import Database.ChunksRecorder;
import Messages.StoredMsg;

public class AckStoreAction extends Action {

    private BackedUpFiles peerStoredChunks;

    private ChunksRecorder chunksRecord;

    private StoredMsg msg;

    public AckStoreAction(BackedUpFiles peerStoredChunks, ChunksRecorder chunksRecord, StoredMsg msg) {
        this.peerStoredChunks = peerStoredChunks;
        this.chunksRecord = chunksRecord;
        this.msg = msg;
    }

    @Override
    public void run() {
        peerStoredChunks.backedChunk(msg.getFileID(), msg.getChunkNum(), msg.getSenderID());
        chunksRecord.updateChunkRecord(msg.getFileID(), msg.getChunkNum(), msg.getSenderID());
    }
}
