package Action;

import Channel.ControlChannel;
import Main.BackedupFile;
import Main.Peer;
import Messages.ChunkMsg;
import Messages.GetchunkMsg;
import Messages.Message;
import Utils.*;

import java.io.File;
import java.util.HashMap;

public class TriggerRestoreAction extends ActionHasReply {

    /**
     * directory were all the restore files will be
     */
    private static final String RESTORE_DIRECTORY = "Restored Files";

    /**
     * The channel used to communicate with other peers, regarding control messages
     */
    private ControlChannel controlChannel;

    /**
     * Important information regarding the file that is going to be restored
     */
    private BackedupFile fileToBeRestored;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The sender peer ID
     */
    private int senderID;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * ArrayList containing the file correspondent chunks
     */
    private HashMap<Integer, byte[]> chunks = new HashMap<>();


    public TriggerRestoreAction(Peer peer, float protocolVersion, int senderID, String file) {
        this.controlChannel = peer.getControlChannel();
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;

        this.fileID = FileManager.genFileID(file);
        fileToBeRestored = peer.getBackedUpFile(fileID);
        if (fileToBeRestored == null)
            throw new ExceptionInInitializerError();
    }

    @Override
    public void run() {
        for (int i = 0; i < fileToBeRestored.getNumChunks(); ++i) {
            try {
                controlChannel.sendMessage(
                    new GetchunkMsg(protocolVersion, senderID, fileID, i).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping delete action", this.getClass());
                return;
            }
        }
    }

    @Override
    public void parseResponse(Message msg) {
        if (! msg.getFileID().equals(fileID))
            return;

        ChunkMsg realMsg = (ChunkMsg) msg;
        chunks.put(realMsg.getChunkNum(), realMsg.getChunk());

        if (chunks.size() == fileToBeRestored.getNumChunks()) {
            String restoreDir = FileManager.getFileDirectory(senderID, RESTORE_DIRECTORY);
            new File(restoreDir).mkdirs();

            if (FileManager.createFile(chunks, restoreDir, fileToBeRestored.getFileName()) ) {
                System.out.print("Succesfully restored file: " + fileToBeRestored.getFileName());
                return;
            } else
                Utils.showError("Failed to restore file, due to errors on file outputing.", this.getClass());
        }
    }
}
