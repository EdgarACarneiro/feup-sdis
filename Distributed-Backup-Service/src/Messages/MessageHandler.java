package Messages;

import Action.ActionHasReply;
import Action.StoreAction;
import Channel.ControlChannel;
import ThreadPool.ThreadPool;
import Utils.Utils;

import java.util.ArrayList;

public class MessageHandler implements Runnable {

    /**
     * The maximum length of a possible message type
     */
    private final static int MAXIMUM_TYPE_SIZE = 8;

    private ControlChannel controlChannel;

    private int peerID;

    private ArrayList<ActionHasReply> subscribedActions;

    private Message message;

    public MessageHandler(ControlChannel controlChannel, int peerID, ArrayList<ActionHasReply> subscribedActions, Message message) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        this.subscribedActions = subscribedActions;
        this.message = message;
    }

    public void run() {
        if (message == null || peerID == message.getSenderID())
            return;

        if (message instanceof PutchunkMsg) {
            (new StoreAction(controlChannel, peerID, (PutchunkMsg) message)).run();
        } else if (message instanceof  StoredMsg) {
            for (ActionHasReply action : subscribedActions) {
                action.checkResponse(message);
            }
        }
    }

    public static Message messageInterpreter(String receivedMsg) {
        String[] temp = receivedMsg.substring(0, MAXIMUM_TYPE_SIZE).split(" ");

        System.out.println(temp[0]);

        try {
            switch (temp[0].trim()) {
                case "PUTCHUNK":
                    return new PutchunkMsg(receivedMsg);
                case "STORED":
                    return new StoredMsg(receivedMsg);
                case "GETCHUNK":
                    return new GetchunkMsg(receivedMsg);
                case "CHUNK":
                    return new ChunkMsg(receivedMsg);
                case "DELETE":
                    return new DeleteMsg(receivedMsg);
                case "REMOVED":
                    return new RemovedMsg(receivedMsg);
                default:
                    Utils.showWarning("Unrecognizable message type. Discarding it.", MessageHandler.class);
                    return null;
            }
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Unrecognizable message type. Discarding it.", MessageHandler.class);
            return null;
        }
    }
}
