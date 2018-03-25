import Channel.BackupChannel;
import Channel.ControlChannel;
import Channel.RestoreChannel;
import Utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing a Peer in the service
 */
public class Peer {

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
     * Protocol Version in the communication
     */
    private float protocolVersion;

    /**
     * The peer's ID
     */
    private int peerID;

    /**
     * Name of the access point to be accessed by the Client or TestApp using RMI
     */
    private String acessPoint;

    /**
     * Regex used to validate the program args for initiating a peer
     */
    private final static Pattern argsRegex = Pattern.compile("\\s*?(\\d+(\\.\\d*)?)\\s+?(\\d+)\\s+?(\\w+)\\s+?(((\\d+\\.?){1,4}):(\\d{4}))\\s+?(((\\d+\\.?){1,4}):(\\d{4}))\\s+?(((\\d+\\.?){1,4}):(\\d{4}))\\s*?");


    Peer(String protocolVersion, String serverID, String accessPoint, String channelMC, String channelMDB, String channelMDR) {
        this.protocolVersion = Float.parseFloat(protocolVersion);
        peerID = Integer.parseInt(serverID);
        this.acessPoint = accessPoint;

        controlChannel = new ControlChannel(channelMC);
        backupChannel = new BackupChannel(channelMDB);
        restoreChannel = new RestoreChannel(channelMDR);

       // TODO - lançar aqui o listener thread

    }

    /**
     * Peer main function. Initiates a new Peer.
     */
    public static void main(String args[]){

        String argString = String.join(" ", args);

        if (argsRegex.matcher(argString).matches())
            new Peer(args[0], args[1], args[2], args[3],args[4], args[5]);
        else
            Utils.showError("Unacceptable arguments\n" +
                    "Usage: <protocol version> <server ID> <access point name> <MC address>:<MC port> <MDB address>:<MDB port> <MDR address>:<MDR port>", Peer.class);

    }
}
