/**
 * Actions available to a client.
 * Implements a User Interface for the program.
 */
public class Client {

    /**
     * Object to make requests to Peer using RMI;
     */
    private RMIInterface rmi;

    /**
     * Client Constructor.
     *
     * @param accessPoint The name of the Peer to establish connection with
     */
    private Client(String accessPoint) {
        rmi = (new RMIClient(accessPoint)).getStub();
    }

    /**
     * Client interface main function. Used for making requests to the service.
     */
    public static void main(String args[]){

        new Client(args[0]);

        //FileSplitter.splitFile("C:\\Users\\ASUS\\Pictures\\Shade.jpg");
        //(new Messages.Message()).parseHeader("PUTCHUNK 1.1 32452 4CA00E99D225CwAFFD7AE27B5CF63EAC44FECB2D1360293A3011E50 23 1");
    }

    // TODO Contém uma mini interface grafica

}