package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

import static Utils.Utils.byteArrayConcat;

public class SetTCPIP extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'chunk' message
     */
    private final static String REGEX_STRING =
            "\\s*?SETTCPIP\\s+?(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+?\\r\\n\\r\\n";

    /**
     * The machine's IP address
     */
    private String ipAddress;

    public SetTCPIP(String header) {
        super(REGEX_STRING);
        /* Matcher protocolMatch = msgRegex.matcher(header);

        if (!protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        } */

        //System.out.println(ipAddress);
    }

    public SetTCPIP(float protocolVersion, int senderID, String fileID, String ipAddress) {
        super(protocolVersion, senderID, fileID);
        this.ipAddress = ipAddress;
    }

    @Override
    public byte[] genMsg() {
        return ("SETTCPIP" + " " +
                ipAddress + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }

    public String getIP() {
        return ipAddress;
    }
}
