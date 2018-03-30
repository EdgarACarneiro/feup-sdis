package Action;

import Messages.RemovedMsg;
import Channel.ControlChannel;
import Messages.Message;
import Messages.RemovedMsg;
import Utils.*;

import java.util.ArrayList;

public class TriggerReclaimAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding backup files
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

    public TriggerReclaimAction(ControlChannel controlChannel, float protocolVersion, int senderID, String file) {
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
                    new RemovedMsg(protocolVersion, senderID, fileID, i+1).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping delete action", this.getClass());
                return;
            }
        }
    }
}