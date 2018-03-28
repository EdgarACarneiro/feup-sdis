package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

public class PutchunkMsg extends Message {

    /**
     * Regex used to parse a String containing a putchunk message
     */
    private final static String REGEX_STRING =
            "\\s*?PUTCHUNK\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+(\\d)\\s+?\\r\\n\\r\\n(.*)";

    /**
     * Regex catch group corresponding to the message body
     */
    private static final int BODY_GROUP = 8;

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The message associated chunk
     */
    private String chunk;

    /**
     * The desired replication degree for the given chunk
     */
    private int repDegree;

    public PutchunkMsg(String receivedMsg) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(receivedMsg);

        if (! protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
        chunkNum = Integer.parseInt(protocolMatch.group(CHUNK_NUM_GROUP));
        repDegree = Integer.parseInt(protocolMatch.group(REP_DEGREE_GROUP));
        chunk = protocolMatch.group(BODY_GROUP);
    }

    public PutchunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum, int repDegree) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
        this.repDegree = repDegree;
    }

    @Override
    public String genMsg() {
        return ("PUTCHUNK" +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                repDegree + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF) +
                chunk;
    }
}
