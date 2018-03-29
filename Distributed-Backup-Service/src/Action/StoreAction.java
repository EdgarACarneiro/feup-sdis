package Action;

import Channel.ControlChannel;
import Messages.PutchunkMsg;
import Messages.StoredMsg;
import Utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static Utils.FileManager.geFileDirectory;

public class StoreAction extends Action {

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


    public StoreAction (ControlChannel controlChannel, int peerID, PutchunkMsg requestMsg) {
        this.controlChannel = controlChannel;
        putchunkMsg = requestMsg;

        this.fileDir = geFileDirectory(peerID, putchunkMsg.getFileID());
        new File(fileDir).mkdirs();

        storeChunk();
    }

    private void storeChunk() {
        try {
            FileOutputStream out = new FileOutputStream (fileDir + putchunkMsg.getChunkNum());
            out.write(putchunkMsg.getChunk(), 0, putchunkMsg.getChunk().length);
        } catch (java.io.IOException e) {
            Utils.showError("Failed to save chunk in disk", this.getClass());
        }
    }

    public void run() {
        try {
            controlChannel.sendMessage(
                    new StoredMsg(putchunkMsg.getProtocolVersion(), putchunkMsg.getSenderID(),
                            putchunkMsg.getFileID(), putchunkMsg.getChunkNum()).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showError("Failed to build message, stopping backup action", this.getClass());
        }
    }

}
