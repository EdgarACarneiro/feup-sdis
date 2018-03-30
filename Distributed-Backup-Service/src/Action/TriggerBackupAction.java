package Action;

import Channel.BackupChannel;
import Messages.Message;
import Messages.PutchunkMsg;
import Messages.StoredMsg;
import Utils.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TriggerBackupAction extends ActionHasReply {

    /**
     * Maximum number of cycles the Action will execute in order to make all the chunks
     * replication degree equivalent to the desired replication degree
     */
    private final static int MAXIMUM_NUM_CYCLES = 5;

    /**
     * The channel used to communicate with other peers, regarding backup files
     */
    private BackupChannel backupChannel;

    /**
     * The thread waiting time for checking chunks RD, in mili seconds
     */
    private int waitCheckTime = 1000;

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
     * An hashmap containing the chunks associated to each Peer, telling whether they were already received or not
     */
    private HashMap <Integer, ArrayList<Boolean> > peersChunks = new HashMap<>();

    /**
     * The desired replication degree of the file
     */
    private int repDegree;

    public TriggerBackupAction(BackupChannel backupChannel, float protocolVersion, int senderID, String file, String repDegree) {
        this.backupChannel = backupChannel;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.fileID = FileManager.genFileID(file);
        this.repDegree = Integer.parseInt(repDegree);
        chunks = FileManager.splitFile(file);

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
        checkLoop();
    }

    private void checkLoop() {
        Utils.showWarning("TRIES BACKUP: " + numTimeCycles, this.getClass());
        if (numTimeCycles >= MAXIMUM_NUM_CYCLES)
            return;

        try {
            Thread.sleep(waitCheckTime);
        } catch (java.lang.InterruptedException e) {
            Utils.showError("Unable to wait " + waitCheckTime + "mili seconds to proceed. Proceeding now.", this.getClass());
        }

        // Get chunks whose RD isn't superior to repDegree
        ArrayList<Integer> missingRDChunks = new ArrayList<>();
        for (int i = 0; i < chunksRD.size(); ++i) {
            if (chunksRD.get(i) < repDegree)
                missingRDChunks.add(i);
        }

        // If size is bigger than 0, all chunks have the desired repDegree
        if (missingRDChunks.size() == 0)
            return;

        for (int chunkIdx : missingRDChunks)
            requestBackUp(chunkIdx);

        numTimeCycles += 1;
        waitCheckTime *= 2;
        checkLoop();
    }

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
