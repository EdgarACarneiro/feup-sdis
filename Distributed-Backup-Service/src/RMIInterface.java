import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface to implement RMI callable methods
 */
public interface RMIInterface extends Remote {

    String backupAction(ArrayList<String> args) throws RemoteException;
    String restoreAction(ArrayList<String> args) throws RemoteException;
    String deleteAction(ArrayList<String> args) throws RemoteException;
    String reclaimAction(ArrayList<String> args) throws RemoteException;
    String stateAction(ArrayList<String> args) throws RemoteException;
}