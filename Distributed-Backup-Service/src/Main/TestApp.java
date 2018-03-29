package Main;

import Utils.Utils;

import java.util.ArrayList;

/**
 * Class used to test the program
 */
public class TestApp {

    /**
     * Action to be performed by the Main.Peer
     */
    private String action;

    /**
     * List of arguments necessary to perform the requested action
     */
    private ArrayList<String> actionArgs = new ArrayList<>();

    /**
     * Object to make requests to Main.Peer using RMI;
     */
    private RMI.RMIInterface rmi;

    /**
     * Main.TestApp constructor.
     *
     * @param args List of arguments necessary to perform a new test
     */
    private TestApp(String args[]) {
        rmi = (new RMI.RMIClient(args[0])).getStub();
        action = args[1];

        for(int i = 2; i < args.length; ++i) {
            actionArgs.add(args[i]);
        }

        triggerAction();
    }

    /**
     * Main.TestApp main function.
     *
     * @param args List of arguments containing the user input
     */
    public static void main(String args[]){
        new TestApp(args);
    }

    /**
     * Trigger the action correspondent to the request made
     */
    private void triggerAction() {

        try {
            switch (action) {
                case "BACKUP":
                   rmi.backupAction(actionArgs);
                    break;
                case "RESTORE":
                    rmi.restoreAction(actionArgs);
                    break;
                case "DELETE":
                    rmi.deleteAction(actionArgs);
                    break;
                case "RECLAIM":
                    rmi.reclaimAction(actionArgs);
                    break;
                case "STATE":
                    rmi.stateAction(actionArgs);
                    break;
                default:
                    Utils.showError("Unknown test action requested\n" +
                            "May be 1 of 5: BACKUP, RESTORE DELETE RECLAIM OR STATE", this.getClass());
            }
        } catch (java.rmi.RemoteException e) {
            Utils.showError("Unable to make request using RMI", this.getClass());
        }
    }

}
