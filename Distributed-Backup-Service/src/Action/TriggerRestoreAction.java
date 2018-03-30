package Action;

import Channel.ControlChannel;
import Messages.ChunkMsg;
import Messages.GetchunkMsg;
import Messages.Message;
import Utils.*;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class TriggerRestoreAction extends ActionHasReply {

    /**
     * The channel used to communicate with other peers, regarding control messages
     */
    private ControlChannel controlChannel;

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
    private ArrayList<byte[]> chunks = new ArrayList<>();

    public TriggerRestoreAction(ControlChannel controlChannel, float protocolVersion, int senderID, String file) {
        this.controlChannel = controlChannel;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.fileID = FileManager.genFileID(file);
        chunks = FileManager.splitFile(file); // TODO - mudar isto para ele passar a ir buscar antes quais os chunks que tem guardado de um file, que estara na PEer
    }

    @Override
    public void run() {
        for (int i = 0; i < chunks.size(); ++i) {
            try {
                controlChannel.sendMessage(
                    new GetchunkMsg(protocolVersion, senderID, fileID, i+1).genMsg()
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
        try {
            FileOutputStream out = new FileOutputStream (FileManager.getFileDirectory(senderID, fileID) + "/" + realMsg.getChunkNum());
            out.write(realMsg.getChunk(), 0, realMsg.getChunk().length);
        } catch (java.io.IOException e) {
            Utils.showError("Failed to save chunk in disk", this.getClass());
        }
    }
}
