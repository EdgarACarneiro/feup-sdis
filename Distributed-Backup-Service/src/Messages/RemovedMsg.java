package Messages;

public class RemovedMsg extends CommonMsg implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'remove' message
     */
    private final static String REGEX_STRING =
            "\\s*?REMOVED\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+?\\r\\n\\r\\n";

    public RemovedMsg(String receivedMsg) {
        super(receivedMsg, REGEX_STRING);
    }

    public RemovedMsg(float protocolVersion, int senderID, String fileID, int chunkNum) {
        super(protocolVersion, senderID, fileID, chunkNum);
    }

    @Override
    public byte[] genMsg() {
        return ("REMOVED" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }
}
