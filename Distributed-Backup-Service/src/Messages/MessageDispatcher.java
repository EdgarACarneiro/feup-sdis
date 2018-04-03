package Messages;

import Action.*;

import Channel.ControlChannel;
import Channel.RestoreChannel;
import Channel.BackupChannel;
import Database.BackedUpFiles;
import Database.ChunksRecorder;
import Main.Peer;
import Utils.Utils;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageDispatcher implements Runnable {

    private ControlChannel controlChannel;

    private RestoreChannel restoreChannel;

    private BackupChannel backupChannel;

    private int peerID;

    private CopyOnWriteArrayList<ActionHasReply> subscribedActions;

    private Message message;

    private ChunksRecorder record;

    private BackedUpFiles peerStoredFiles;


    public MessageDispatcher(Peer peer, ChunksRecorder record, BackedUpFiles peerStoredFiles, CopyOnWriteArrayList<ActionHasReply> subscribedActions, Message message) {
        this.controlChannel = peer.getControlChannel();
        this.restoreChannel = peer.getRestoreChannel();
        this.backupChannel= peer.getBackupChannel();
        this.peerID = peer.getPeerID();
        this.subscribedActions = subscribedActions;
        this.message = message;
        this.record = record;
        this.peerStoredFiles = peerStoredFiles;
    }

    @Override
    public void run() {
        if (message == null || peerID == message.getSenderID())
            return;

        if (message instanceof PutchunkMsg) {
            (new StoreAction(controlChannel, record, peerStoredFiles, peerID, (PutchunkMsg) message)).run();

            // For reclaim actions
            for (ActionHasReply action : subscribedActions)
                action.parseResponse(message);
        }
        else if (message instanceof StoredMsg) {
            (new AckStoreAction(peerStoredFiles, record, (StoredMsg) message)).run();
        }
        else if (message instanceof GetchunkMsg) {
            (new RetrieveChunkAction(restoreChannel, record, peerID, (GetchunkMsg) message)).run();
        }
        else if (message instanceof ChunkMsg) {
            for (ActionHasReply action : subscribedActions)
                action.parseResponse(message);
        }
        else if (message instanceof DeleteMsg) {
            (new DeleteAction((DeleteMsg) message, record, peerID)).run();
        }
        else if (message instanceof RemovedMsg) {
            (new RemovedAction(record, backupChannel, peerID, (RemovedMsg) message)).run();
        }
        else if (message instanceof GetTCPIP) {
            (new SetTCPServer(record, controlChannel, peerID, (GetTCPIP) message)).run();
        }
        else if (message instanceof SetTCPIP) {
            (new SetTCPClient(peerStoredFiles, peerID, (SetTCPIP) message)).run();
        }
        else if (message instanceof CheckDeleteMsg && message.getProtocolVersion()==2) {
            (new DeleteAfterCheckAction(record, controlChannel, peerID, (CheckDeleteMsg) message)).run();
        } 
    }

    public static Message messageInterpreter(byte[] readMsg, int msgLength) {
        byte[] byteHeader = getMessageHeader(readMsg);
        if (byteHeader == null) {
            Utils.showWarning("Unable to parse message header. Discarding it.", MessageDispatcher.class);
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
                case "GETTCPIP":
                    return new GetTCPIP(header);
                case "SETTCPIP":
                    return new SetTCPIP(header); 
                case "CHECKDELETE":
                    return new CheckDeleteMsg(header); 

                default:
                    Utils.showWarning("Unrecognizable message type. Discarding it.", MessageDispatcher.class);
                    return null;
            }
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Unrecognizable message type. Discarding it.", MessageDispatcher.class);
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
