package Action;

import Channel.BackupChannel;
import Database.BackedUpFiles;
import Main.Peer;
import Messages.Message;
import Messages.PutchunkMsg;
import Utils.*;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Action used to begin a back up. It also handles the other Peer's answers.
 */
public class TriggerBackupAction extends Action {

    /**
     * Maximum number of cycles the Action will execute in order to make all the chunks
     * replication degree equivalent to the desired replication degree
     */
    private final static int MAXIMUM_NUM_CYCLES = 5;

    /**
     * The starting  waiting time for checking chunks RD, in mili seconds
     */
    private final static int STARTING_WAIT_TIME = 1000;

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private BackupChannel backupChannel;

    /**
     * Thread Pool useful for running scheduled check loops
     */
    private ScheduledThreadPoolExecutor sleepThreadPool;

    /**
     * The thread waiting time for checking chunks RD, in mili seconds
     */
    private int waitCheckTime = STARTING_WAIT_TIME;

    /**
     * The backed up files container associated to the peer triggering this action
     * It is important to store this, for later indicating if the file was successfully backed up
     */
    private BackedUpFiles backedUpFiles;

    /**
     * The number of round trips already completed.
     */
    private int numTimeCycles = 0;

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

    /**
     * The name of the file being backed up
     */
    private String fileName;

    /**
     * Trigger Backup Action Constructor
     *
     * @param peer The peer associated to this action
     * @param protocolVersion The protocol version used
     * @param senderID The identifier of the sender peer
     * @param file The File to be backed up
     * @param repDegree The desired replication degree of the file
     */
    public TriggerBackupAction(Peer peer, float protocolVersion, int senderID, String file, String repDegree) {
        this.backedUpFiles = peer.getBackedUpFiles();
        this.backupChannel = peer.getBackupChannel();
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;

        fileName = FileManager.getFileName(file);
        this.fileID = FileManager.genFileID(file);
        this.repDegree = Integer.parseInt(repDegree);
        chunks = FileManager.splitFile(file);

        backedUpFiles.backedFile(fileID, fileName, this.repDegree, chunks.size());

        sleepThreadPool = new ScheduledThreadPoolExecutor(MAXIMUM_NUM_CYCLES);
    }

    /**
     * Send the request to backup the given file chunk
     *
     * @param chunkNum Number of the chunk to be backed up
     */
    private void requestBackUp(int chunkNum) {
        try {
            backupChannel.sendMessage(
                    new PutchunkMsg(protocolVersion, senderID, fileID, chunkNum, repDegree, chunks.get(chunkNum)).genMsg()
            );
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < chunks.size(); ++i)
            requestBackUp(i);

        numTimeCycles += 1;
        sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Class used to implement the Check loop for the action.
     * A class was used instead of a method, in order to implement it with ScheduledThreadPoolExecutor
     */
    private class Repeater implements Runnable {

        @Override
        public void run() {
            Utils.showWarning("TRIES BACKUP: " + numTimeCycles, this.getClass());
            if (numTimeCycles >= MAXIMUM_NUM_CYCLES)
                return;

            ArrayList<Integer> missingChunks = backedUpFiles.checkAllRD(fileID);
            if (missingChunks.size() == 0)
                return;

            for (int chunkIdx : missingChunks)
                requestBackUp(chunkIdx);

            numTimeCycles += 1;
            waitCheckTime *= 2;
            sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
        }
    }
}
