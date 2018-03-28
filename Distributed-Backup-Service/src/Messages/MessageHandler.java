package Messages;

import Utils.Utils;

public class MessageHandler {

    /**
     * The maximum length of a possible message type
     */
    private final static int MAXIMUM_TYPE_SIZE = 8;

    public static Message messageHandler(String receivedMsg) {
        String trimmedMsg = receivedMsg.trim();
        String[] temp = trimmedMsg.substring(0, MAXIMUM_TYPE_SIZE).split(" ");

        switch(temp[0].trim()) {
            case "PUTCHUNK":
                return new PutchunkMsg(trimmedMsg);
            case "STORED":
                return new StoredMsg(trimmedMsg);
            case "GETCHUNK":
                return new GetchunkMsg(trimmedMsg);
            case "CHUNK":
                return new ChunkMsg(trimmedMsg);
            case "DELETE":
                return new DeleteMsg(trimmedMsg);
            case "REMOVED":
                return new RemovedMsg(trimmedMsg);
            default:
                Utils.showWarning("Unrecognizable message type. Discarding it.", MessageHandler.class);
                return null;
        }
    }
}
