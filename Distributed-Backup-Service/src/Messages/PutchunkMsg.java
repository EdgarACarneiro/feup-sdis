package Messages;

import Utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PutchunkMsg extends Message {

    /**
     * Regex used to parse a String containing a message
     */
    private final static String REGEX_STRING = "\\s*?(\\w+?)\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+(\\d)\\s*?$";

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The desired replication degree for the given chunk
     */
    private int repDegree;

    public PutchunkMsg(String receivedMsg) {
        msgRegex = Pattern.compile(REGEX_STRING);
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

        //TODO MISSING THE FINAL HEADER FLAG <CRLF>
    }

    public PutchunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum, int repDegree) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
        this.repDegree = repDegree;
    }
}
