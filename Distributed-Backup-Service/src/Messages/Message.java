package Messages;

import Utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    /**
     * Regex useful for parsing Messages
     */
    protected Pattern msgRegex;
    /**
     * Regex catch group corresponding to the message type
     */
    private static final int TYPE_GROUP = 1;

    /**
     * Regex catch group corresponding to the message version
     */
    private static final int VERSION_GROUP = 2;

    /**
     * Regex catch group corresponding to the message sender ID
     */
    private static final int SENDER_ID_GROUP = 3;

    /**
     * Regex catch group corresponding to the message filed ID
     */
    private static final int FIELD_ID_GROUP = 4;

    /**
     * Regex catch group corresponding to the message correspondent chunk number
     */
    private static final int CHUNK_NUM_GROUP = 6;

    /**
     * Regex catch group corresponding to the message correspondent replication degree
     */
    private static final int REP_DEGREE_GROUP = 8;

    /**
     * The entry in the ASCII table for the carriage return char
     */
    protected static final int ASCII_CR = 13;

    /**
     * The entry in the ASCII table for the line feed char
     */
    protected static final int ASCII_LF = 10;

    /**
     * The Messages.Message protocol version
     */
    protected float protocolVersion;

    /**
     * The id of the peer who sent / is going to send the message
     */
    protected int senderID;

    /**
     * The file identifier of the file the chunk belongs to
     */
    protected String fileID;

    public Message() {}

    public Message (float protocolVersion, int senderID, String fileID) {
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;

        if (fileID.equals("")) {
            Utils.showError("Unacceptable file identifier", this.getClass());
            throw new ExceptionInInitializerError();
        }
        this.fileID = fileID;
    }

    /**
     * Generate the Messages.Message from private fields
     *
     * @return The String containing the message
     */
    public String genMsg() {
        return (msgType + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                repDegree + " " +
                (char) ASCII_CR + (char) ASCII_LF);
    }
}
