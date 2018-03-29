package Messages;

import Utils.Utils;

import java.util.regex.Pattern;

public class Message {

    /**
     * Regex useful for parsing Messages
     */
    protected Pattern msgRegex;

    /**
     * Regex catch group corresponding to the message version
     */
    protected static final int VERSION_GROUP = 1;

    /**
     * Regex catch group corresponding to the message sender ID
     */
    protected static final int SENDER_ID_GROUP = 2;

    /**
     * Regex catch group corresponding to the message filed ID
     */
    protected static final int FIELD_ID_GROUP = 3;

    /**
     * Regex catch group corresponding to the message correspondent chunk number
     */
    protected static final int CHUNK_NUM_GROUP = 5;

    /**
     * Regex catch group corresponding to the message correspondent replication degree
     */
    protected static final int REP_DEGREE_GROUP = 7;

    /**
     * The entry in the ASCII table for the carriage return char
     */
    protected static final int ASCII_CR = 13;

    /**
     * The entry in the ASCII table for the line feed char
     */
    protected static final int ASCII_LF = 10;

    /**
     * The application protocol version
     */
    protected float protocolVersion;

    /**
     * The id of the peer that sent / is going to send the message
     */
    protected int senderID;

    /**
     * The file identifier of the file the chunk belongs to
     */
    protected String fileID;


    protected Message(String Regex) {
        msgRegex = Pattern.compile(Regex, Pattern.UNIX_LINES);
    }

    protected Message (float protocolVersion, int senderID, String fileID) {
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;

        if (fileID.equals("")) {
            Utils.showError("Unacceptable file identifier", this.getClass());
            throw new ExceptionInInitializerError();
        }
        this.fileID = fileID;
    }

    public float getProtocolVersion() {
        return protocolVersion;
    }

    public int getSenderID() {
        return senderID;
    }

    public String getFileID() {
        return fileID;
    }
}
