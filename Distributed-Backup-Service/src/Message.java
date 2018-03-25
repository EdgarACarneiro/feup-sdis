import Utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    /**
     * Regex useful for parsing Message Headers
     */
    private static final Pattern msgHeaderRegex = Pattern.compile(
            "\\s*?(\\w+?)\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([A-F0-9]){64})\\s+((\\d){1,6})\\s+(\\d)\\s*?$"
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


    public Message() {}

    public boolean parseHeader(String header) {

        Matcher headerMatch = msgHeaderRegex.matcher(header);

        if (! headerMatch.matches()) {
            Utils.showError("Failed to get a Regex match in message Header", this.getClass());
            return false;
        }

        //TODO MISSING THE FINAL HEADER FLAG <CRLF>

        String msgType = headerMatch.group(TYPE_GROUP);
        String version = headerMatch.group(VERSION_GROUP);
        int senderID = Integer.parseInt(headerMatch.group(SENDER_ID_GROUP));
        String fileID = headerMatch.group(FIELD_ID_GROUP);
        int chunkNum = Integer.parseInt(headerMatch.group(CHUNK_NUM_GROUP));
        int replicationDegree = Integer.parseInt(headerMatch.group(REP_DEGREE_GROUP));

        return true;
    }
}
