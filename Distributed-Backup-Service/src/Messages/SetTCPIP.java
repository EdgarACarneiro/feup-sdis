package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

import static Utils.Utils.byteArrayConcat;

public class SetTCPIP extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'chunk' message
     */
    private final static String REGEX_STRING =
            "\\s*?SETTCPIP\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+?([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\s+?((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4})).*?\\r\\n\\r\\n";

    /**
     * The machine's IP address
     */
    private String ipAddress;

    /**
     * The machine's port
     */
    private int port;

    public SetTCPIP(String header) {
        super(REGEX_STRING);
        Utils.log(header);
        Matcher protocolMatch = msgRegex.matcher(header);

        if (!protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
        ipAddress = protocolMatch.group(5) + "." + protocolMatch.group(6) + "." +protocolMatch.group(7) + "." +protocolMatch.group(8);
        port = Integer.parseInt(protocolMatch.group(9));
        Utils.log(ipAddress + "\n" + port);
    }

    public SetTCPIP(float protocolVersion, int senderID, String fileID, String ipAddress, int port) {
        super(protocolVersion, senderID, fileID);
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public byte[] genMsg() {
        return ("SETTCPIP" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                ipAddress + " " +
                port + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }

    public String getIP() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
