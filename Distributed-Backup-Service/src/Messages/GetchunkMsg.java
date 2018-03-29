package Messages;

public class GetchunkMsg extends CommonMsg implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'getchunk' message
     */
    private final static String REGEX_STRING =
            "\\s*?GETCHUNK\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+?\\r\\n\\r\\n";

    public GetchunkMsg(String receivedMsg) {
        super(receivedMsg, REGEX_STRING);
    }

    public GetchunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum) {
        super(protocolVersion, senderID, fileID, chunkNum);
    }

    @Override
    public byte[] genMsg() {
        return ("GETCHUNK" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }
}
