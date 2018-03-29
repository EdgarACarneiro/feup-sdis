package Messages;

import Action.StoreAction;
import Channel.ControlChannel;
import ThreadPool.ThreadPool;
import Utils.Utils;

public class MessageHandler {

    /**
     * The maximum length of a possible message type
     */
    private final static int MAXIMUM_TYPE_SIZE = 8;

    public static Message messageInterpreter(String receivedMsg) {
        String[] temp = receivedMsg.substring(0, MAXIMUM_TYPE_SIZE).split(" ");

        System.out.println(receivedMsg);

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

    public static void messageHandler(ControlChannel controlChannel, ThreadPool threadPool, int peerID, Message message) {
        if (message == null || peerID == message.getSenderID())
            return;

        if (message instanceof PutchunkMsg) {
            threadPool.executeThread(
                    new StoreAction(controlChannel, peerID, (PutchunkMsg) message)
            );
        }
    }
}
