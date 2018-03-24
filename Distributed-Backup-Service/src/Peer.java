public class Peer {

    private ControlChannel controlChannel;

    private BackupChannel backupChannel;

    private RestoreChannel restoreChannel;

    Peer(String protocolVersion, String serverID, String accessPoint, String channelMC, String channelMDB, String channelMDR) {
        controlChannel = new ControlChannel();
        backupChannel = new BackupChannel();
        restoreChannel = new RestoreChannel();

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
        new Peer(false, false, args[0].equals("1"), args[1]);
    }
}
