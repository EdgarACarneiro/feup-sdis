package Action;

import Channel.ControlChannel;
import Database.ChunksRecorder;
import Messages.RemovedMsg;
import Utils.*;

import java.io.File;

/**
 * Action used to begin a back up. It also handles the other Peer's answers.
 */
public class TriggerReclaimAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private ControlChannel controlChannel;

    /**
     * The record about the files / chunks stored
     */
    private ChunksRecorder record;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The sender peer ID
     */
    private int senderID;

    /**
     * Max File occupation size
     */
    private long maxKBytes;

    /**
     * The size that stills needed to be shrink
     */
    private long shrinkSize;

    /**
     * Trigger Reclaim Action Constructor
     * 
     * @param controlChannel The channel used to communicate the desired course of action
     * @param record The peer's record fo stored files / chunks
     * @param protocolVersion The protocol version used
     * @param senderID The identifier of the sender peer
     * @param maxKBytes Max KBytes to be used
     */
    public TriggerReclaimAction(ControlChannel controlChannel, ChunksRecorder record, float protocolVersion, int senderID, String maxKBytes) {
        this.controlChannel = controlChannel;
        this.record = record;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        if ( Long.parseLong(maxKBytes) < getFreeSpace())
            this.maxKBytes = Long.parseLong(maxKBytes);
        else {
            Utils.showError("Not enough space!", TriggerReclaimAction.class);
        }
    }

    /**
     * Send the information about the removal of X given Chunks
     *
     * @param chunkNum Number of the chunk to be backed up
     */
    private void requestRemoved(int chunkNum, String fileID) {
        try {
            controlChannel.sendMessage(
                new RemovedMsg(protocolVersion, senderID, fileID, chunkNum).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        }
    }

    @Override
    public void run() {
        record.updateMaxSpace(maxKBytes);
        Utils.log("Reclaim redefined max disk usage to " + this.maxKBytes + " KBytes and occupied space was " + record.getUsedDiskSpace() + " KBytes.");

        if (record.getUsedDiskSpace() > this.maxKBytes) {
            shrinkSize = record.getUsedDiskSpace() - this.maxKBytes;
            Utils.log("Storage space will be shrunk down by at least " + shrinkSize);

            File[] files = FileManager.getPeerBackups(senderID);

            for (File file : files) {
                if (file.isDirectory() && shrinkSize > 0) {
                    Utils.log("DELETING CHUNKS FROM " + file.getName() + "...");
                    Utils.log("DELETING CHUNKS FROM " + file.getName() + "...");

                    File fileChunks = new File(file.getParentFile(), file.getName());
                    File[] chunkList = fileChunks.listFiles();

                    if (chunkList != null) {
                        for (File chunk : chunkList) {
                            if (shrinkSize <= 0)
                                return;

                            Utils.log("DELETED CHUNK " + chunk.getName() + " FROM " + file.getName());
                            Utils.log("DELETED CHUNK " + chunk.getName() + " FROM " + file.getName());

                            shrinkSize -= record.getChunkSize(file.getName(), Integer.parseInt(chunk.getName()) );
                            record.removeChunk(file.getName(), Integer.parseInt(chunk.getName()));
                            chunk.delete();

                            requestRemoved(Integer.parseInt(chunk.getName()), file.getName());
                        }
                    }
                }
            }
        } else {
            Utils.log("There was no need to shrink down disk usage!");
        }
    }

    /**
     * Get Free Space
     */
    private long getFreeSpace() {
        File dir = new File(System.getProperty("user.dir"));
        
        return dir.getFreeSpace();
    }
}