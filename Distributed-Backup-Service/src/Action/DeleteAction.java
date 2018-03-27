package Action;

import Channel.ControlChannel;
import Utils.*;

import java.util.ArrayList;

public class DeleteAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private ControlChannel controlChannel;

    /**
     * The type for generating the messages associated to the backup action
     */
    private final static String MSG_TYPE = "DELETE";

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

    public DeleteAction(ControlChannel controlChannel, float protocolVersion, int senderID, String file) {
        this.controlChannel = controlChannel;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.fileID = FileManager.genFileID(file);
        chunks = FileManager.splitFile(file);
    }

    public void run() {
        for (int i = 0; i < chunks.size(); ++i) {
            try {
                controlChannel.sendMessage(
                    new Message(MSG_TYPE, protocolVersion, senderID, fileID).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping delete action", this.getClass());
                return;
            }
        }
    }
}