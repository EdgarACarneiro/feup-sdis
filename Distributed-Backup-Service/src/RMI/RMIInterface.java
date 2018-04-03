package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface to implement RMI callable methods
 */
public interface RMIInterface extends Remote {

    /**
     * Trigger a back up action in the peer side
     *
     * @param args The args necessary to trigger a backup action
     * @throws RemoteException
     */
    void backupAction(ArrayList<String> args) throws RemoteException;

    /**
     * Trigger a restore action in the peer side
     *
     * @param args The args necessary to trigger a restore action
     * @throws RemoteException
     */
    void restoreAction(ArrayList<String> args) throws RemoteException;

    /**
     * Trigger a delete action in the peer side
     *
     * @param args The args necessary to trigger delete action
     * @throws RemoteException
     */
    void deleteAction(ArrayList<String> args) throws RemoteException;

    /**
     * Trigger a reclaim action in the peer side
     *
     * @param args The args necessary to trigger a reclaim action
     * @throws RemoteException
     */
    void reclaimAction(ArrayList<String> args) throws RemoteException;

    /**
     * Trigger a state action in the peer side
     *
     * @param args The args necessary to trigger a state action
     * @throws RemoteException
     */
    String stateAction(ArrayList<String> args) throws RemoteException;
}