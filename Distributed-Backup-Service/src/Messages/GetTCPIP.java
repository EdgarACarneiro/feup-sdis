package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

import static Utils.Utils.byteArrayConcat;

public class GetTCPIP extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'chunk' message
     */
    private final static String REGEX_STRING =
                    "\\s*?GETTCPIP\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+?\\r\\n\\r\\n";

    public GetTCPIP(String header) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(header);

        if (!protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
    }

    public GetTCPIP(float protocolVersion, int senderID, String fileID) {
        super(protocolVersion, senderID, fileID);
    }

    @Override
    public byte[] genMsg() {
        return ("GETTCPIP" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }
}
