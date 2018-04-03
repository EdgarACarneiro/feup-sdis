package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface to implement RMI callable methods
 */
public interface RMIInterface extends Remote {

    void backupAction(ArrayList<String> args) throws RemoteException;
    void restoreAction(ArrayList<String> args) throws RemoteException;
    void deleteAction(ArrayList<String> args) throws RemoteException;
    void reclaimAction(ArrayList<String> args) throws RemoteException;
    String stateAction(ArrayList<String> args) throws RemoteException;
}