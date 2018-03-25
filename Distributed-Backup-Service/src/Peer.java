import Channel.BackupChannel;
import Channel.ControlChannel;
import Channel.RestoreChannel;

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


    Peer(String protocolVersion, String serverID, String accessPoint, String channelMC, String channelMDB, String channelMDR) {

        this.protocolVersion = Float.parseFloat(protocolVersion);
        peerID = Integer.parseInt(serverID);
        this.acessPoint = accessPoint;

        controlChannel = new ControlChannel(channelMC);
        backupChannel = new BackupChannel(channelMDB);
        restoreChannel = new RestoreChannel(channelMDR);

        // TODO Contem tb a funcionalidade test app que interpreta logo o comando
        //TODO- The "name" of each multicast channel consists of the IP multicast address and port
        // todo - The "name" of the channels should be provided in the following order MC, MDB, MDR. These arguments must follow immediately the first three command line arguments, which are the protocol version, the server id and the service access point

        //MulticastClient tretas = new MulticastClient(controlChannel);

    }

    /**
     * Peer main function. Initiates a new Peer.
     */
    public static void main(String args[]){
        // TODO - Fazer parsing através do regex
        //new Peer(false, false, args[0].equals("1"), args[1]);
    }
}
