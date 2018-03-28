package Action;

import Channel.BackupChannel;
import Messages.PutchunkMsg;
import Utils.*;

import java.util.ArrayList;

public class BackupAction extends Action {

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private BackupChannel backupChannel;

    /**
     * The type for generating the messages associated to the backup action
     */
    private final static String BACKUP_TYPE = "PUTCHUNK";

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

    /**
     * The desired replication degree of the file
     */
    private int repDegree;

    public BackupAction(BackupChannel backupChannel, float protocolVersion, int senderID, String file, String repDegree) {
        this.backupChannel = backupChannel;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.fileID = FileManager.genFileID(file);
        this.repDegree = Integer.parseInt(repDegree);
        chunks = FileManager.splitFile(file);
    }

    public void run() {
        for (int i = 0; i < chunks.size(); ++i) {
            try {
                backupChannel.sendMessage(
                        new PutchunkMsg(protocolVersion, senderID, fileID, i+1, repDegree).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showError("Failed to build message, stopping backup action", this.getClass());
                return;
            }
        }
    }
}
