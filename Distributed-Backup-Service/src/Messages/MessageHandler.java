package Messages;

import Action.ActionHasReply;
import Action.DeleteAction;
import Action.RetrieveChunkAction;
import Action.StoreAction;
import Channel.ControlChannel;
import Channel.RestoreChannel;
import Main.ChunksRecorder;
import Utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageHandler implements Runnable {

    private ControlChannel controlChannel;

    private RestoreChannel restoreChannel;

    private int peerID;

    private ArrayList<ActionHasReply> subscribedActions;

    private Message message;

    private ChunksRecorder record;


    public MessageHandler(ControlChannel controlChannel, RestoreChannel restoreChannel, ChunksRecorder record, int  peerID, ArrayList<ActionHasReply> subscribedActions, Message message) {
        this.controlChannel = controlChannel;
        this.restoreChannel = restoreChannel;
        this.peerID = peerID;
        this.subscribedActions = subscribedActions;
        this.message = message;
        this.record = record;
    }

    @Override
    public void run() {
        if (message == null || peerID == message.getSenderID())
            return;

        if (message instanceof PutchunkMsg) {
            (new StoreAction(controlChannel, record, peerID, (PutchunkMsg) message)).run();
        }
        else if (message instanceof  StoredMsg) {
            for (ActionHasReply action : subscribedActions)
                action.parseResponse(message);
        }
        else if (message instanceof GetchunkMsg) {
            (new RetrieveChunkAction(restoreChannel, record, peerID, (GetchunkMsg) message)).run();
        }
        else if (message instanceof ChunkMsg) {
            for (ActionHasReply action : subscribedActions)
                action.parseResponse(message);
        }
        else if (message instanceof DeleteMsg) {
            (new DeleteAction(message, peerID)).run();
        }
        else if (message instanceof RemovedMsg) {
            (new TriggerRemovedAction(controlChannel, record, peerID, (RemovedMsg) message)).run();
        }
    }

    public static Message messageInterpreter(byte[] readMsg, int msgLength) {
        byte[] byteHeader = getMessageHeader(readMsg);
        if (byteHeader == null) {
            Utils.showWarning("Unable to parse message header. Discarding it.", MessageHandler.class);
            return null;
        }

        String header = new String(byteHeader, 0, byteHeader.length);
        byte[] chunk = Arrays.copyOfRange(readMsg, byteHeader.length, msgLength);

        System.out.println(findType(header)); // TODO - Delete

        try {
            switch (findType(header)) {
                case "PUTCHUNK":
                    return new PutchunkMsg(header, chunk);
                case "STORED":
                    return new StoredMsg(header);
                case "GETCHUNK":
                    return new GetchunkMsg(header);
                case "CHUNK":
                    return new ChunkMsg(header, chunk);
                case "DELETE":
                    return new DeleteMsg(header);
                case "REMOVED":
                    return new RemovedMsg(header);
                default:
                    Utils.showWarning("Unrecognizable message type. Discarding it.", MessageHandler.class);
                    return null;
            }
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Unrecognizable message type. Discarding it.", MessageHandler.class);
            return null;
        }
    }

    private static byte[] getMessageHeader(byte[] readMsg) {
        for (int i = 0; i < readMsg.length; ++i) {
            if ((readMsg[i] == (byte) Message.ASCII_CR) &&
                (readMsg[i+1] == (byte) Message.ASCII_LF) &&
                (readMsg[i+2] == (byte) Message.ASCII_CR) &&
                (readMsg[i+3] == (byte) Message.ASCII_LF))
                return Arrays.copyOfRange(readMsg, 0, (i+4));
        }
        return null;
    }

    private static String findType(String header) {
        return header.substring(0, header.indexOf(" "));
    }
}
