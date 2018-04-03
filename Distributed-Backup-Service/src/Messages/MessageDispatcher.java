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

/**
 * Class implementing the message dispatcher. This class is responsible for handling the messages and dispatch them to new actions
 */
public class MessageDispatcher implements Runnable {

    /**
     * The control channel used in the communication
     */
    private ControlChannel controlChannel;

    /**
     * The restore channel used in the communication
     */
    private RestoreChannel restoreChannel;

    /**
     * The back up used in the communication
     */
    private BackupChannel backupChannel;

    /**
     * The peer identifier associated to this dispatcher
     */
    private int peerID;

    /**
     * The protocol version used by the peer
     */
    private float protocolVersion;

    /**
     * A list of actions that may be subscribed (meaning that want to be noticed) whenever a certain message arrives
     */
    private CopyOnWriteArrayList<ActionHasReply> subscribedActions;

    /**
     * The message received
     */
    private Message message;

    /**
     * The ChunksRecord Database that will be updated by the messages
     */
    private ChunksRecorder record;

    /**
     * The BackedUpFiles Database that will be updated by the messages
     */
    private BackedUpFiles peerStoredFiles;

    /**
     * Message Dispatcher constructor
     *
     * @param peer The peer that created the dispatcher
     * @param record The ChunksRecord Database that will be updated by the messages
     * @param peerStoredFiles The BackedUpFiles Database that will be updated by the messages
     * @param subscribedActions The list of actions that may be subscribed
     * @param message The received message
     */
    public MessageDispatcher(Peer peer, ChunksRecorder record, BackedUpFiles peerStoredFiles, CopyOnWriteArrayList<ActionHasReply> subscribedActions, Message message) {
        this.controlChannel = peer.getControlChannel();
        this.restoreChannel = peer.getRestoreChannel();
        this.backupChannel= peer.getBackupChannel();
        this.peerID = peer.getPeerID();
        this.protocolVersion = peer.getProtocolVersion();
        this.subscribedActions = subscribedActions;
        this.message = message;
        this.record = record;
        this.peerStoredFiles = peerStoredFiles;
    }

    /**
     * Will trigger new actions with the received message
     */
    @Override
    public void run() {
        if (message == null || peerID == message.getSenderID())
            return;

        if (message instanceof PutchunkMsg) {
            if (protocolVersion == 2.0)
                (new StoreEnhAction(controlChannel, record, peerStoredFiles, peerID, (PutchunkMsg) message)).run();
            else
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

    /**
     * The message interpreter, that will find what is the kind of message of the message received
     *
     * @param readMsg The generic message received
     * @param msgLength The message length
     * @return The typo of message received, already changed to the correct type
     */
    public static Message messageInterpreter(byte[] readMsg, int msgLength) {
        byte[] byteHeader = getMessageHeader(readMsg);
        if (byteHeader == null) {
            Utils.showWarning("Unable to parse message header. Discarding it.", MessageDispatcher.class);
            return null;
        }

        String header = new String(byteHeader, 0, byteHeader.length);
        byte[] chunk = Arrays.copyOfRange(readMsg, byteHeader.length, msgLength);

        Utils.log(findType(header)); // TODO - Delete

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

    /**
     * Getter of the message header, from a given complete message
     *
     * @param readMsg The complete message
     * @return The byte[] correspondent to the message header
     */
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

    /**
     * Gets the String correspondent to the type of the message
     *
     * @param header The message header to be analysed
     * @return The type discovered
     */
    private static String findType(String header) {
        return header.substring(0, header.indexOf(" "));
    }
}
