package Action;

import Channel.BackupChannel;
import Main.BackedUpFiles;
import Main.Peer;
import Messages.Message;
import Messages.PutchunkMsg;
import Messages.StoredMsg;
import Utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Action used to begin a back up. It also handles the other Peer's answers.
 */
public class TriggerBackupAction extends ActionHasReply {

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
     * ArrayList containing the replication degree of each chunk
     */
    private ArrayList<Integer> chunksRD = new ArrayList<>();

    /**
     * An hashMap containing the chunks associated to each Peer, telling whether they were already received or not
     */
    private HashMap <Integer, ArrayList<Boolean> > peersChunks = new HashMap<>();

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


        sleepThreadPool = new ScheduledThreadPoolExecutor(MAXIMUM_NUM_CYCLES);
        initRDCounter();
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

            // Get chunks whose RD isn't superior to repDegree
            ArrayList<Integer> missingRDChunks = new ArrayList<>();
            for (int i = 0; i < chunksRD.size(); ++i) {
                if (chunksRD.get(i) < repDegree)
                    missingRDChunks.add(i);
            }

            // If size is bigger than 0, all chunks have the desired repDegree
            if (missingRDChunks.size() == 0) {
                backedUpFiles.backedFile(fileID, fileName, repDegree, chunksRD);
                return;
            }


            for (int chunkIdx : missingRDChunks)
                requestBackUp(chunkIdx);

            numTimeCycles += 1;
            waitCheckTime *= 2;
            sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void parseResponse(Message msg) {
        if (! msg.getFileID().equals(fileID))
            return;

        StoredMsg realMsg = (StoredMsg) msg;
        int chunkNum = realMsg.getChunkNum();

        if (peersChunks.containsKey(realMsg.getSenderID())) {

            ArrayList<Boolean> peerChunks = peersChunks.get(realMsg.getSenderID());
            if (peerChunks.get(chunkNum))
                return;
            else peerChunks.set(chunkNum, true);

        } else {
            peersChunks.put(realMsg.getSenderID(), initCheckArray(new ArrayList<>(), chunkNum) );
        }

        chunksRD.set(chunkNum, chunksRD.get(chunkNum) + 1);
    }

    /**
     * Initialize the counter for the replication degree for each chunk
     */
    private void initRDCounter() {
        for (int i = 0; i < chunks.size(); ++i)
            chunksRD.add(0);
    }

    /**
     * Initialize the chunks array while also marking the given chunk as received
     *
     * @param array The received chunks array to be initialized
     * @param chunkNum The chunk that was already received
     */
    private ArrayList<Boolean> initCheckArray(ArrayList<Boolean> array, int chunkNum) {
        for (int i = 0; i < chunks.size(); ++i)
            array.add(i == chunkNum);
        return array;
    }
}
