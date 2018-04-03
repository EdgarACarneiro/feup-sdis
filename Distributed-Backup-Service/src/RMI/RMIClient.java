package RMI;

import Utils.Utils;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Class used to make requests to the Main.Peer using RMI
 */
public class RMIClient {

    /**
     * Stub to make requests and read answers from Main.Peer
     */
    private RMIInterface stub;

    /**
     * RMI.RMIClient Constructor
     *
     * @param accessPoint he access point identifier / name
     */
    public RMIClient(String accessPoint) {
        String[] acc = accessPoint.split("/");
        try {
            Registry registry = LocateRegistry.getRegistry(acc[0]);
            stub = (RMIInterface) registry.lookup(acc[1]);
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