package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

public class ChunkMsg extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'chunk' message
     */
    private final static String REGEX_STRING =
            "\\s*?CHUNK\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+?\\r\\n\\r\\n(.*)";

    /**
     * Regex catch group corresponding to the message body
     */
    private static final int BODY_GROUP = 7;

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The message associated chunk
     */
    private String chunk;


    public ChunkMsg(String receivedMsg) {
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
        chunk = protocolMatch.group(BODY_GROUP);
    }

    public ChunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum, int repDegree) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
    }

    @Override
    public String genMsg() {
        return ("CHUNK" +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF)+
                chunk;
    }
}
