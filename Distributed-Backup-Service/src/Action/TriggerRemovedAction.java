package Action;

import Channel.ControlChannel;
import Main.Peer;
import Messages.Message;
import Messages.PutchunkMsg;
import Messages.RemovedMsg;
import Utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Action used to begin a back up. It also handles the other Peer's answers.
 */
public class TriggerRemovedAction extends ActionHasReply {

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
    private ControlChannel controlChannel;

    /**
     * Thread Pool useful for running scheduled check loops
     */
    private ScheduledThreadPoolExecutor sleepThreadPool;

    /**
     * The thread waiting time for checking chunks RD, in mili seconds
     */
    private int waitCheckTime = STARTING_WAIT_TIME;

    /**
     * The peer associated to this action
     * It is important to store the peer, for later indicating to the peer if the file was successfully backed up
     */
    private Peer peer;

    /**
     * The resultant backedupFile of a well succeeded back up implementation
     */
    private BackedupFile backedUpFile;

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
     * Trigger Backup Action Constructor
     *
     * @param peer The peer associated to this action
     * @param protocolVersion The protocol version used
     * @param senderID The identifier of the sender peer
     * @param file The File to be backed up
     * @param repDegree The desired replication degree of the file
     */
    public TriggerRemovedAction(Peer peer, float protocolVersion, int senderID, String file, String repDegree) {
        this.peer = peer;
        this.controlChannel = peer.getControlChannel();
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        this.fileID = FileManager.genFileID(file);
        this.repDegree = Integer.parseInt(repDegree);
        chunks = FileManager.splitFile(file);

        backedUpFile = new BackedupFile(chunks.size(), FileManager.getFileName(file));

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

    /**
     * Class used to implement the Check loop for the action.
     * A class was used instead of a method, in order to implement it with ScheduledThreadPoolExecutor
     */
    private class Repeater implements Runnable {

        @Override
        public void run() {
            
            sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public void run() {
        //IF something
        for (int i = 0; i < chunks.size(); ++i)
            requestBackUp(i);

        sleepThreadPool.schedule(new Repeater(), waitCheckTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void parseResponse(Message msg) {
        if (! msg.getFileID().equals(fileID))
            return;

            RemovedMsg realMsg = (RemovedMsg) msg;
        int chunkNum = realMsg.getChunkNum();
    }
}