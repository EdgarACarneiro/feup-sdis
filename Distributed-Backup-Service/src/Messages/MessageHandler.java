package Messages;

import Action.ActionHasReply;
import Action.DeleteAction;
import Action.RetrieveChunkAction;
import Action.StoreAction;
import Channel.ControlChannel;
import Channel.RestoreChannel;
import Utils.Utils;

import java.util.ArrayList;

public class MessageHandler implements Runnable {

    /**
     * The maximum length of a possible message type
     */
    private final static int MAXIMUM_TYPE_SIZE = 8;

    private ControlChannel controlChannel;

    private RestoreChannel restoreChannel;

    private int peerID;

    private ArrayList<ActionHasReply> subscribedActions;

    private Message message;

    public MessageHandler(ControlChannel controlChannel, RestoreChannel restoreChannel, int peerID, ArrayList<ActionHasReply> subscribedActions, Message message) {
        this.controlChannel = controlChannel;
        this.restoreChannel = restoreChannel;
        this.peerID = peerID;
        this.subscribedActions = subscribedActions;
        this.message = message;
    }

    @Override
    public void run() {
        if (message == null || peerID == message.getSenderID())
            return;

        if (message instanceof PutchunkMsg) {
            (new StoreAction(controlChannel, peerID, (PutchunkMsg) message)).run();
        }
        else if (message instanceof  StoredMsg) {
            for (ActionHasReply action : subscribedActions)
                action.parseResponse(message);
        }
        else if (message instanceof GetchunkMsg) {
            (new RetrieveChunkAction(restoreChannel, peerID, (GetchunkMsg) message)).run();
        }
        else if (message instanceof ChunkMsg) {
            for (ActionHasReply action : subscribedActions)
                action.parseResponse(message);
        }
        else if (message instanceof DeleteMsg) {
            (new DeleteAction(message, peerID)).run();
        }
    }

    public static Message messageInterpreter(String receivedMsg) {
        String[] temp = receivedMsg.substring(0, MAXIMUM_TYPE_SIZE).split(" ");

        System.out.println(temp[0]); // TODO - Delete

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
