import RMI.RMIClient;
import RMI.RMIInterface;

/**
 * Actions available to a client.
 * Implements a User Interface for the program.
 */
public class Client {

    /**
     * Object to make requests to Main.Peer using RMI;
     */
    private RMIInterface rmi;

    /**
     * Client Constructor.
     *
     * @param accessPoint The name of the Main.Peer to establish connection with
     */
    private Client(String accessPoint) {
        rmi = (new RMIClient(accessPoint)).getStub();
    }

    /**
     * Client interface main function. Used for making requests to the service.
     */
    public static void main(String args[]){

        new Client(args[0]);
    }

    // TODO Contém uma mini interface grafica

}