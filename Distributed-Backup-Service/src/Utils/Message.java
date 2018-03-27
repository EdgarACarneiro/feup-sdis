package Utils;

import Utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    /**
     * Regex useful for parsing Utils.Message Headers
     */
    private static final Pattern msgHeaderRegex = Pattern.compile(
            "\\s*?(\\w+?)\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+(\\d)\\s*?$"
    );

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
    private static final int ASCII_CR = 13;

    /**
     * The entry in the ASCII table for the line feed char
     */
    private static final int ASCII_LF = 10;

    /**
     * The message type
     */
    private String msgType;

    /**
     * The Utils.Message protocol version
     */
    private float protocolVersion;

    /**
     * The id of the peer who sent / is going to send the message
     */
    private int senderID;

    /**
     * The file identifier of the file the chunk belongs to
     */
    private String fileID;

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The desired replication degree for the given chunk
     */
    private int repDegree;

    public Message(String msgType, float protocolVersion, int senderID, String fileID, int chunkNum, int repDegree) {
        this.msgType = msgType;
        this.protocolVersion = protocolVersion;
        this.senderID = senderID;
        if (fileID.equals("")) {
            Utils.showError("Unacceptable file identifier", this.getClass());
            throw new ExceptionInInitializerError();
        }
        this.fileID = fileID;
        this.chunkNum = chunkNum;
        this.repDegree = repDegree;
    }

    public Message(String header) {
        Matcher headerMatch = msgHeaderRegex.matcher(header);

        if (! headerMatch.matches()) {
            Utils.showError("Failed to get a Regex match in message Header", this.getClass());
            throw new ExceptionInInitializerError();
        }

        msgType = headerMatch.group(TYPE_GROUP);
        protocolVersion = Float.parseFloat(headerMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(headerMatch.group(SENDER_ID_GROUP));
        fileID = headerMatch.group(FIELD_ID_GROUP);
        chunkNum = Integer.parseInt(headerMatch.group(CHUNK_NUM_GROUP));
        repDegree = Integer.parseInt(headerMatch.group(REP_DEGREE_GROUP));

        //TODO MISSING THE FINAL HEADER FLAG <CRLF>
    }

    /**
     * Generate the Utils.Message from private fields
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
