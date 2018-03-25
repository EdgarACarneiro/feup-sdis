import java.util.ArrayList;

/**
 * Class used to test the program
 */
public class TestApp {

    /**
     * Acces Point used to communicate with the Peer
     */
    private String accessPoint;

    /**
     * Action to be performed by the Peer
     */
    private String action;

    /**
     * List of arguments necessary to perform the requested action
     */
    private ArrayList<String> actionArgs = new ArrayList<String>();

    /**
     * TestApp constructor.
     *
     * @param args List of arguments necessary to perform a new test
     */
    private TestApp(String args[]) {
        accessPoint = args[0];
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

        switch(action) {
            case "BACKUP":
                System.out.println("Backup test");
                break;
            case "RESTORE":
                break;
            case "DELETE":
                break;
            case "RECLAIM":
                break;
            case "STATE":
                break;
            default:
                Utils.Utils.showError("Unknown test action requested\n" +
                        "May be 1 of 5: BACKUP, RESTORE DELETE RECLAIM OR STATE",this);

        }
    }

}
