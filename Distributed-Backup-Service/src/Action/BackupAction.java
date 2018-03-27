package Action;

import Utils.FileManager;

public class BackupAction extends Action {

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
     * The desired replication degree of the file
     */
    private int repDegree;

    public BackupAction(float protocolVersion, int senderID, String file, String repDegree) {
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;

        this.fileID = FileManager.genFileID(file);
        this.repDegree = Integer.parseInt(repDegree);
    }

    public void run() {

    }
}
