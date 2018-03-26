import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {

    String backupAction() throws RemoteException;
    String restoreAction() throws RemoteException;
    String deleteAction() throws RemoteException;
    String reclaimAction() throws RemoteException;
    String stateAction() throws RemoteException;
}