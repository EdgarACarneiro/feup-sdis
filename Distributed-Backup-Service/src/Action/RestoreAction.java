package Action;

import Messages.GetchunkMsg;
import Channel.RestoreChannel;
import Messages.Message;
import Utils.*;

import java.util.ArrayList;

public class RestoreAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private RestoreChannel restoreChannel;

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

    public RestoreAction(RestoreChannel restoreChannel, float protocolVersion, int senderID, String file) {
        this.restoreChannel = restoreChannel;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.fileID = FileManager.genFileID(file);
        chunks = FileManager.splitFile(file);
    }

    public void run() {
        for (int i = 0; i < chunks.size(); ++i) {
            try {
                restoreChannel.sendMessage(
                    new GetchunkMsg(protocolVersion, senderID, fileID, i+1).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping delete action", this.getClass());
                return;
            }
        }
    }
}
