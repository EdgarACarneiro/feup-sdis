import Utils.Utils;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Class used to make requests to the Peer using RMI
 */
public class RMIClient {

    /**
     * Stub to make requests and read answers from Peer
     */
    private RMIInterface stub;

    /**
     * RMIClient Constructor
     *
     * @param accessPoint he access point identifier / name
     */
    public RMIClient(String accessPoint) {
        try {
            Registry registry = LocateRegistry.getRegistry();
            stub = (RMIInterface) registry.lookup(accessPoint);
        } catch(Exception e) {
            Utils.showError("Failed to initiate RMI", this.getClass());
        }
    }

    /**
     * Getter for RMI stub
     *
     * @return RMI stub
     */
    public RMIInterface getStub() {
        return stub;
    }
}