package Action;

import Database.BackedUpFiles;
import Messages.StoredMsg;

public class AckStoreAction extends Action {

    private BackedUpFiles peerStoredChunks;

    private StoredMsg msg;

    public AckStoreAction(BackedUpFiles peerStoredChunks, StoredMsg msg) {
        this.peerStoredChunks = peerStoredChunks;
        this.msg = msg;
    }

    @Override
    public void run() {
        peerStoredChunks.backedChunk(msg.getFileID(), msg.getChunkNum(), msg.getSenderID());
    }
}
