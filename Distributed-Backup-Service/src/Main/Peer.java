package Main;

import Action.*;
import Channel.BackupChannel;
import Channel.ControlChannel;
import Channel.RestoreChannel;
import Utils.Utils;
import ThreadPool.ThreadPool;

import java.io.File;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static Utils.FileManager.getPeerDirectory;

/**
 * Class representing a Main.Peer in the service
 */
public class Peer implements RMI.RMIInterface {

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
     * Name of the access point to be accessed by the Client or Main.TestApp using RMI
     */
    private String accessPoint;

    /**
     * The thread pool for running different action at the same time
     */
    private ThreadPool threadPool;

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

        dirName = getPeerDirectory(peerID);
        new File(dirName).mkdir();

        controlChannel = new ControlChannel(channelMC, this);
        backupChannel = new BackupChannel(channelMDB, this);
        restoreChannel = new RestoreChannel(channelMDR, this);

        threadPool = new ThreadPool();
        threadPool.executeThread(controlChannel);
        threadPool.executeThread(backupChannel);
        threadPool.executeThread(restoreChannel);

        initializeRMI();
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

    public BackupChannel getBackupChannel() {
        return backupChannel;
    }

    public ControlChannel getControlChannel() {
        return controlChannel;
    }

    public RestoreChannel getRestoreChannel() {
        return restoreChannel;
    }

    public int getPeerID() {
        return peerID;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    /* INTERFACE FUNCTIONS */
    // TODO - below functions

    public void backupAction(ArrayList<String> args) {
        if (args.size() < 2)
            Utils.showError("Not enough arguments given for backup action", this.getClass());
        else if (args.size() > 2)
            Utils.showWarning("Too many arguments given for backup action", this.getClass());

        TriggerBackupAction action = new TriggerBackupAction(backupChannel, protocolVersion, peerID, args.get(0), args.get(1));
        controlChannel.subscribeAction(action);
        threadPool.executeThread(action);
    }

    public void restoreAction(ArrayList<String> args) {
        if (args.isEmpty())
            Utils.showError("Not enough arguments given for restore action", this.getClass());
        if (args.size() > 1)
            Utils.showWarning("Too many arguments given for restore action", this.getClass());

        threadPool.executeThread(new TriggerRestoreAction(restoreChannel, protocolVersion, peerID, args.get(0)));
    }

    public void deleteAction(ArrayList<String> args) {
        if (args.isEmpty())
            Utils.showError("Not enough arguments given for delete action", this.getClass());
        if (args.size() > 1)
            Utils.showWarning("Too many arguments given for delete action", this.getClass());

        threadPool.executeThread(new TriggerDeleteAction(controlChannel, protocolVersion, peerID, args.get(0)));
    }

    public void reclaimAction(ArrayList<String> args) {
        if (args.isEmpty())
            Utils.showError("Not enough arguments given for reclaim disk space action", this.getClass());
        if (args.size() > 1)
            Utils.showWarning("Too many arguments given for reclaim disk space action", this.getClass());

        threadPool.executeThread(new TriggerReclaimAction(controlChannel, protocolVersion, peerID, args.get(0)));
    }

    public void stateAction(ArrayList<String> args) {
        if (args.size() > 0)
            Utils.showWarning("Too many arguments given for state action", this.getClass());

        threadPool.executeThread(new StateAction(controlChannel, protocolVersion, peerID, args.get(0)));
    }
}
