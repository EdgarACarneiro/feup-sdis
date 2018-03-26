import Utils.Utils;

import java.util.ArrayList;

/**
 * Class used to test the program
 */
public class TestApp {

    /**
     * Action to be performed by the Peer
     */
    private String action;

    /**
     * List of arguments necessary to perform the requested action
     */
    private ArrayList<String> actionArgs = new ArrayList<String>();

    /**
     * Object to make requests to Peer using RMI;
     */
    private RMIInterface rmi;

    /**
     * TestApp constructor.
     *
     * @param args List of arguments necessary to perform a new test
     */
    private TestApp(String args[]) {
        rmi = (new RMIClient(args[0])).getStub();
        action = args[1];

        for(int i = 2; i < args.length; ++i) {
            actionArgs.add(args[i]);
        }

        triggerAction();
    }

    /**
     * TestApp main function.
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
                    rmi.backupAction();
                    break;
                case "RESTORE":
                    rmi.restoreAction();
                    break;
                case "DELETE":
                    rmi.deleteAction();
                    break;
                case "RECLAIM":
                    rmi.reclaimAction();
                    break;
                case "STATE":
                    rmi.stateAction();
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
