package Main;

import Action.*;
import Channel.BackupChannel;
import Channel.ControlChannel;
import Channel.RestoreChannel;
import Database.BackedUpFiles;
import Database.ChunksRecorder;
import Utils.Utils;
import Utils.ProtocolVersions;
import ThreadPool.ThreadPool;

import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import Utils.FileManager;

/**
 * Class representing a Peer in the service
 */
public class Peer implements RMI.RMIInterface {

    /**
     * Number of seconds it takes to perform a save loop to the files
     */
    private static final Integer SAVE_LOOP_SECONDS = 3;

    /**
     * The channel used for communication regarding control
     */
    private ControlChannel controlChannel;

    /**
     * The channel used for communication regarding the backup action
     */
    private BackupChannel backupChannel;

    /**
     * The channel used for communication regarding the restore of files action
     */
    private RestoreChannel restoreChannel;

    /**
     * Directory containing the Main.Peer associated files
     */
    private String dirName;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The peer's ID
     */
    private int peerID;

    /**
     * Name of the access point to be accessed by the Main.TestApp using RMI
     */
    private String accessPoint;

    /**
     * The thread pool for running different action at the same time
     */
    private ThreadPool threadPool;

    /**
     * The hashMap used for keeping information about the files that were backed up
     */
    private BackedUpFiles backedUpFiles;

    /**
     * Class used to save records of what chunks are stored in this Peer disk
     */
    public ChunksRecorder chunksRecord;

    /**
     * Regex used to validate the program args for initiating a peer
     */
    private final static Pattern argsRegex = Pattern.compile("\\s*?(\\d+(\\.\\d*)?)\\s+?(\\d+)\\s+?(\\w+)\\s+?(((\\d+\\.?){1,4}):(\\d{4}))\\s+?(((\\d+\\.?){1,4}):(\\d{4}))\\s+?(((\\d+\\.?){1,4}):(\\d{4}))\\s*?");

    /**
     * Main.Peer constructor. Receives the necessary arguments to initiate a new peer
     *
     * @param protocolVersion The protocol version to be used
     * @param serverID The id of the peer
     * @param accessPoint The access point identifier / name
     * @param channelMC The control channel information in format address:port
     * @param channelMDB The backup channel information in format address:port
     * @param channelMDR The restore channel information in format address:port
     */
    private Peer(String protocolVersion, String serverID, String accessPoint, String channelMC, String channelMDB, String channelMDR) {

        this.protocolVersion = Float.parseFloat(protocolVersion);
        peerID = Integer.parseInt(serverID);
        this.accessPoint = accessPoint;

        dirName = FileManager.getPeerDirectory(peerID);
        new File(dirName).mkdir();

        controlChannel = new ControlChannel(channelMC, this);
        backupChannel = new BackupChannel(channelMDB, this);
        restoreChannel = new RestoreChannel(channelMDR, this);

        threadPool = new ThreadPool();
        threadPool.executeThread(controlChannel);
        threadPool.executeThread(backupChannel);
        threadPool.executeThread(restoreChannel);

        initializeRMI();
        initDatabase();

        if (this.protocolVersion == ProtocolVersions.ENHANCEMENTS_VERSION)
            threadPool.executeThread(new CheckDeleteAction(controlChannel, this.protocolVersion, peerID));
    }

    /**
     * Initialize the database using either serializable files or the default constructors.
     * A thread is also started, to save to the files every X to X seconds
     */
    private void initDatabase() {
        File backedUpFiles_File = new File(dirName + "/" + FileManager.BACKED_UP_FILES_SERIALIZABLE);
        File chunksRecord_File = new File(dirName + "/" + FileManager.CHUNKS_RECORDER_SERIALIZABLE);

        try {
            if (backedUpFiles_File.exists())
                backedUpFiles = (BackedUpFiles) (new ObjectInputStream(new FileInputStream(backedUpFiles_File))).readObject();
            else {
                backedUpFiles_File.createNewFile();
                backedUpFiles = new BackedUpFiles();
            }

            if (chunksRecord_File.exists()) {
                chunksRecord = (ChunksRecorder) (new ObjectInputStream(new FileInputStream(chunksRecord_File))).readObject();
            } else {
                chunksRecord_File.createNewFile();
                chunksRecord = new ChunksRecorder();
            }

            // Scheduling a save loop of to seconds
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    (new ObjectOutputStream(new FileOutputStream(chunksRecord_File))).writeObject(chunksRecord);
                    (new ObjectOutputStream(new FileOutputStream(backedUpFiles_File))).writeObject(backedUpFiles);

                } catch (IOException e1) {
                    Utils.showError("Failed to save the database to files", this.getClass());
                }
            }, SAVE_LOOP_SECONDS, SAVE_LOOP_SECONDS, TimeUnit.SECONDS);

        } catch (java.io.IOException | java.lang.ClassNotFoundException e) {
            Utils.showError("Failed to initialize database from files", this.getClass());
        }
    }

    /**
     * Main.Peer main function. Initiates a new Main.Peer.
     *
     * @param args List of arguments containing the user input
     */
    public static void main(String args[]){
        String argString = String.join(" ", args);

        if (argsRegex.matcher(argString).matches())
            new Peer(args[0], args[1], args[2], args[3],args[4], args[5]);
        else
            Utils.showError("Unacceptable arguments\n" +
                    "Usage: <protocol version> <server ID> <access point name> <MC address>:<MC port> <MDB address>:<MDB port> <MDR address>:<MDR port>", Peer.class);
    }

    /**
     * Initialize the RMI service from the Server part
     */
    private void initializeRMI() {
        try {
            RMI.RMIInterface stub = (RMI.RMIInterface) UnicastRemoteObject.exportObject(this, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            try {
                registry.bind(accessPoint, stub);
            } catch (java.rmi.AlreadyBoundException e) {
                registry.rebind(accessPoint, stub);
            }
        } catch (Exception e) {
            Utils.showError("Failed to initiate RMI", this.getClass());
        }
    }

    /**
     * Getter for the used back up channel
     *
     * @return the back up channel
     */
    public BackupChannel getBackupChannel() {
        return backupChannel;
    }

    /**
     * Getter for the used control channel
     *
     * @return the control channel
     */
    public ControlChannel getControlChannel() {
        return controlChannel;
    }

    /**
     * Getter for the used restore channel
     *
     * @return the restore channel
     */
    public RestoreChannel getRestoreChannel() {
        return restoreChannel;
    }

    /**
     * Getter for this peer identifier
     *
     * @return the peer identifier
     */
    public int getPeerID() {
        return peerID;
    }

    /**
     * Getter for the protocol version being used in this peer
     *
     * @return the protcol version in format 'X.Y'
     */
    public float getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Getter for the main thread pool being used in the application launched by this Peer
     *
     * @return the thread pool used
     */
    public ThreadPool getThreadPool() {
        return threadPool;
    }

    /**
     * Getter for information regarding the files backed up having this peer as initiator peer
     *
     * @return The information mentioned above
     */
    public BackedUpFiles getBackedUpFiles() {
        return backedUpFiles;
    }


    /* INTERFACE FUNCTIONS */

    @Override
    public void backupAction(ArrayList<String> args) {
        if (args.size() < 2)
            Utils.showError("Not enough arguments given for backup action", this.getClass());
        else if (args.size() > 2)
            Utils.showWarning("Too many arguments given for backup action", this.getClass());

        threadPool.executeThread(new TriggerBackupAction(this, protocolVersion, peerID, args.get(0), args.get(1)));
    }

    @Override
    public void restoreAction(ArrayList<String> args) {
        if (args.isEmpty())
            Utils.showError("Not enough arguments given for restore action", this.getClass());
        if (args.size() > 1)
            Utils.showWarning("Too many arguments given for restore action", this.getClass());

        try {
            TriggerRestoreAction action = new TriggerRestoreAction(this, protocolVersion, peerID, args.get(0));
            restoreChannel.subscribeAction(action);
            threadPool.executeThread(action);

        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Unable to restore the given file. " +
                    "You can only restore files that were previously backed up by this Peer.", this.getClass());
        }
    }

    @Override
    public void deleteAction(ArrayList<String> args) {
        if (args.isEmpty())
            Utils.showError("Not enough arguments given for delete action", this.getClass());
        if (args.size() > 1)
            Utils.showWarning("Too many arguments given for delete action", this.getClass());

        threadPool.executeThread(new TriggerDeleteAction(controlChannel, backedUpFiles, protocolVersion, peerID, args.get(0)));
    }

    @Override
    public void reclaimAction(ArrayList<String> args) {
        if (args.isEmpty())
            Utils.showError("Not enough arguments given for reclaim disk space action", this.getClass());
        if (args.size() > 1)
            Utils.showWarning("Too many arguments given for reclaim disk space action", this.getClass());

        threadPool.executeThread(new TriggerReclaimAction(controlChannel, chunksRecord, protocolVersion, peerID, args.get(0)));
    }

    @Override
    public String stateAction(ArrayList<String> args) {
        if (args.size() > 0)
            Utils.showWarning("Too many arguments given for state action", this.getClass());

        TriggerStateAction info =  new TriggerStateAction(chunksRecord, backedUpFiles);
        threadPool.executeThread(info);

        return info.getResult();
    }
}
