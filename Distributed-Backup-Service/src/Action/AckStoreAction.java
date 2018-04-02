package Action;

import Database.BackedUpFiles;
import Messages.StoredMsg;

public class AckStoreAction extends Action {

    private BackedUpFiles peerStoredChunks;

    private StoredMsg msg;

    public AckStoreAction(BackedUpFiles peerStoredChunks, StoredMsg msg) {
        this.peerStoredChunks = peerStoredChunks;
        this.msg = msg;
        System.out.println(new String(msg.genMsg()));
    }

    @Override
    public void run() {
        peerStoredChunks.backedChunk(msg.getFileID(), msg.getChunkNum(), msg.getSenderID());
    }
}
