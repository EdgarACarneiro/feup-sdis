package Action;

import Channel.ControlChannel;
import Messages.PutchunkMsg;
import Messages.StoredMsg;
import Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import static Utils.FileManager.geFileDirectory;

public class StoreAction extends Action {

    /**
     * Maximum time waited to trigger the Store Action, exclusively.
     */
    private final static int MAX_TIME_TO_SEND = 4001;

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
     * The identifier of the Peer associated to this action
     */
    private int peerID;


    public StoreAction (ControlChannel controlChannel, int peerID, PutchunkMsg requestMsg) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        putchunkMsg = requestMsg;

        this.fileDir = geFileDirectory(peerID, putchunkMsg.getFileID());
        new File(fileDir).mkdirs();

        storeChunk();
    }

    private void storeChunk() {
        try {
            FileOutputStream out = new FileOutputStream (fileDir + "/" + putchunkMsg.getChunkNum());
            out.write(putchunkMsg.getChunk(), 0, putchunkMsg.getChunk().length);
        } catch (java.io.IOException e) {
            Utils.showError("Failed to save chunk in disk", this.getClass());
        }
    }

    public void run() {
        try {
            Thread.sleep(new Random().nextInt(MAX_TIME_TO_SEND));
            controlChannel.sendMessage(
                    new StoredMsg(putchunkMsg.getProtocolVersion(), peerID,
                            putchunkMsg.getFileID(), putchunkMsg.getChunkNum()).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showError("Failed to build message, stopping Store action", this.getClass());
        } catch (java.lang.InterruptedException e) {
            Utils.showError("Unable to wait before proceeding.", this.getClass());
        }
    }

}
